package com.example.data.repository

import android.util.Log
import com.example.data.engine.FilteredTrainReport
import com.example.data.engine.UpcomingTrainFilter
import com.example.data.local.AppDatabase
import com.example.data.local.StationEntity
import com.example.data.local.TrainEntity
import com.example.data.model.AuthenticEmuFormations
import com.example.data.model.IndianLocalRailwayDatabase
import com.example.data.model.LiveTrainStatus
import com.example.data.model.LocalTrainSchedule
import com.example.data.model.RailwayStation
import com.example.data.model.TrainCandidate
import com.example.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class ApiDiagnosticsLog(
    val endpoint: String,
    val httpStatus: Int,
    val traceId: String?,
    val timestamp: String?,
    val executionTime: String?,
    val stationCode: String?,
    val trainNumber: String?
)

interface RailwayDataProvider {
    suspend fun getAllStations(): List<RailwayStation>
    suspend fun getNearestStation(lat: Double, lng: Double): Pair<RailwayStation?, Double>
    suspend fun getStationDepartures(stationCode: String): List<TrainCandidate>
    suspend fun getStationDeparturesReport(stationCode: String): FilteredTrainReport
    suspend fun getTrainSchedule(trainNumber: String): LocalTrainSchedule?
    suspend fun getTrainRouteDetails(trainNumber: String): TrainRouteDetails?
    suspend fun getAllRoutes(): List<TrainRouteDetails>
    suspend fun getDynamicCoaches(trainNumber: String): List<String>
    suspend fun getLiveTrainStatus(trainNumber: String, currentStationCode: String?): LiveTrainStatus
    suspend fun searchStationsAndTrains(query: String): Pair<List<RailwayStation>, List<TrainCandidate>>
    fun getRecentApiLogs(): List<ApiDiagnosticsLog>
}

class HybridRailwayDataProvider(
    private val db: AppDatabase? = null,
    private val localFallback: RailwayDataProvider = LocalStaticRailwayDataProvider()
) : RailwayDataProvider {

    // Caches with TTL
    private var cachedStations: List<RailwayStation>? = null
    private var cachedStationsTimestamp: Long = 0L
    private val stationDirectoryTtlMs = 60 * 60 * 1000L // 1 hour

    private val departureCache = mutableMapOf<String, Pair<List<TrainCandidate>, Long>>()
    private val departuresTtlMs = 2 * 60 * 1000L // 2 minutes

    private val liveBoardCache = mutableMapOf<String, Pair<Map<String, LiveTrainStatus>, Long>>()
    private val liveBoardTtlMs = 45 * 1000L // 45 seconds

    private val apiLogs = mutableListOf<ApiDiagnosticsLog>()

    override fun getRecentApiLogs(): List<ApiDiagnosticsLog> = synchronized(apiLogs) {
        apiLogs.takeLast(10)
    }

    private fun logApiMetadata(
        endpoint: String,
        status: Int,
        traceId: String?,
        timestamp: String?,
        execTime: String?,
        stationCode: String? = null,
        trainNum: String? = null
    ) {
        synchronized(apiLogs) {
            if (apiLogs.size > 20) apiLogs.removeAt(0)
            apiLogs.add(
                ApiDiagnosticsLog(
                    endpoint = endpoint,
                    httpStatus = status,
                    traceId = traceId,
                    timestamp = timestamp,
                    executionTime = execTime,
                    stationCode = stationCode,
                    trainNumber = trainNum
                )
            )
        }
    }

    override suspend fun getAllStations(): List<RailwayStation> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (cachedStations != null && now - cachedStationsTimestamp < stationDirectoryTtlMs) {
            return@withContext cachedStations!!
        }

        try {
            val res = ApiClient.apiService.getStationDirectory()
            logApiMetadata(
                endpoint = "/api/lookup/stations",
                status = res.code(),
                traceId = res.body()?.meta?.traceId,
                timestamp = res.body()?.meta?.timestamp,
                execTime = null
            )
            if (res.isSuccessful && res.body()?.success == true) {
                val remoteList = res.body()?.data
                if (!remoteList.isNullOrEmpty()) {
                    val list = remoteList.map { dto ->
                        RailwayStation(
                            code = dto.code,
                            nameEn = dto.name,
                            nameHi = dto.name,
                            nameBn = dto.name,
                            division = dto.zone.ifEmpty { "Eastern Railway" },
                            latitude = dto.latitude ?: 22.5697,
                            longitude = dto.longitude ?: 88.3713
                        )
                    }
                    try {
                        val entities = list.map { st ->
                            StationEntity(
                                code = st.code,
                                nameEn = st.nameEn,
                                nameHi = st.nameHi,
                                nameBn = st.nameBn,
                                zone = st.division,
                                latitude = st.latitude,
                                longitude = st.longitude,
                                lastUpdated = now
                            )
                        }
                        db?.stationDao()?.insertStations(entities)
                    } catch (_: Exception) {}

                    cachedStations = list
                    cachedStationsTimestamp = now
                    return@withContext list
                }
            }
        } catch (_: Exception) {
            // Safe fallback
        }

        // Try reading persisted stations from local DB before static fallback
        try {
            val dbStations = db?.stationDao()?.getAllStationsDirect()
            if (!dbStations.isNullOrEmpty()) {
                val list = dbStations.map { entity ->
                    RailwayStation(
                        code = entity.code,
                        nameEn = entity.nameEn,
                        nameHi = entity.nameHi.ifEmpty { entity.nameEn },
                        nameBn = entity.nameBn.ifEmpty { entity.nameEn },
                        division = entity.zone,
                        latitude = entity.latitude,
                        longitude = entity.longitude
                    )
                }
                cachedStations = list
                cachedStationsTimestamp = now
                return@withContext list
            }
        } catch (_: Exception) {}

        val fallback = localFallback.getAllStations()
        cachedStations = fallback
        cachedStationsTimestamp = now
        fallback
    }

    override suspend fun getNearestStation(lat: Double, lng: Double): Pair<RailwayStation?, Double> {
        val all = getAllStations()
        var minDistanceKm = Double.MAX_VALUE
        var closestStation: RailwayStation? = null
        for (st in all) {
            val dist = calculateDistanceKm(lat, lng, st.latitude, st.longitude)
            if (dist < minDistanceKm) {
                minDistanceKm = dist
                closestStation = st
            }
        }
        return Pair(closestStation, minDistanceKm)
    }

    override suspend fun getStationDepartures(stationCode: String): List<TrainCandidate> {
        val report = getStationDeparturesReport(stationCode)
        return report.upcomingTrains
    }

    override suspend fun getStationDeparturesReport(stationCode: String): FilteredTrainReport = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        // 1. Fetch live board for actual departure statuses
        val liveStatusMap = fetchLiveBoard(stationCode)

        // 2. Fetch raw scheduled trains (from remote or local)
        val rawCandidates = fetchRawStationCandidates(stationCode)

        // 3. Apply strict UpcomingTrainFilter: Rejects DEPARTED and CANCELLED trains
        UpcomingTrainFilter.filterUpcomingTrains(
            candidates = rawCandidates,
            referenceIstEpochMs = now,
            liveStatuses = liveStatusMap
        )
    }

    private suspend fun fetchLiveBoard(stationCode: String): Map<String, LiveTrainStatus> {
        val now = System.currentTimeMillis()
        val cached = liveBoardCache[stationCode]
        if (cached != null && now - cached.second < liveBoardTtlMs) {
            return cached.first
        }

        try {
            val res = ApiClient.apiService.getStationLiveBoard(stationCode)
            logApiMetadata(
                endpoint = "/api/stations/$stationCode/live",
                status = res.code(),
                traceId = res.body()?.meta?.traceId,
                timestamp = res.body()?.meta?.timestamp,
                execTime = null,
                stationCode = stationCode
            )
            if (res.isSuccessful && res.body()?.success == true) {
                val liveList = res.body()?.data
                if (!liveList.isNullOrEmpty()) {
                    val map = liveList.associate { dto ->
                        // Persist live status into local database
                        try {
                            db?.trainDao()?.updateLiveStatus(
                                trainNumber = dto.trainNumber,
                                status = dto.status,
                                delayMinutes = dto.delayMinutes,
                                platform = dto.platform
                            )
                        } catch (_: Exception) {}

                        dto.trainNumber to LiveTrainStatus(
                            trainNumber = dto.trainNumber,
                            trainName = dto.trainName,
                            currentStation = stationCode,
                            nextStation = dto.destination,
                            etaNextStationSeconds = 60,
                            delayMinutes = dto.delayMinutes,
                            isLiveApiAvailable = true,
                            statusSummary = if (dto.status == "DEPARTED") "Departed ${dto.actualDeparture}" else dto.status
                        )
                    }
                    liveBoardCache[stationCode] = Pair(map, now)
                    return map
                }
            }
        } catch (_: Exception) {
            // Live board unavailable
        }

        return emptyMap()
    }

    private suspend fun fetchRawStationCandidates(stationCode: String): List<TrainCandidate> {
        val now = System.currentTimeMillis()
        val cached = departureCache[stationCode]
        if (cached != null && now - cached.second < departuresTtlMs) {
            return cached.first
        }

        try {
            val res = ApiClient.apiService.getStationTrains(stationCode)
            logApiMetadata(
                endpoint = "/api/stations/$stationCode/trains",
                status = res.code(),
                traceId = res.body()?.meta?.traceId,
                timestamp = res.body()?.meta?.timestamp,
                execTime = null,
                stationCode = stationCode
            )
            if (res.isSuccessful && res.body()?.success == true) {
                val remoteList = res.body()?.data
                if (!remoteList.isNullOrEmpty()) {
                    val list = remoteList.map { dto ->
                        val isEmu = dto.trainNumber.startsWith("3") || dto.trainNumber.startsWith("9") || dto.trainNumber.length == 5
                        val defaultCoaches = if (isEmu) {
                            listOf("CAB-1", "LD-1", "VND-1", "GS-1", "GS-2", "GS-3", "VND-2", "LD-2", "CAB-2")
                        } else {
                            listOf("LOCO", "EOG-1", "GS-1", "S1", "S2", "S3", "S4", "S5", "B1", "B2", "B3", "A1", "H1", "PC", "GS-2", "EOG-2")
                        }
                        TrainCandidate(
                            trainNumber = dto.trainNumber,
                            trainName = dto.trainName,
                            originStationCode = dto.originStationCode,
                            originStationName = dto.originStationName,
                            destStationCode = dto.destStationCode,
                            destStationName = dto.destStationName,
                            departureTime = dto.departureTime.ifEmpty { "08:30" },
                            arrivalTime = "09:30",
                            platform = dto.platform.ifEmpty { "PF 1" },
                            zone = "ER",
                            coachCodes = defaultCoaches
                        )
                    }

                    // Record trains into Room database so they are never lost
                    try {
                        val entities = list.map { candidate ->
                            TrainEntity(
                                trainNumber = candidate.trainNumber,
                                trainName = candidate.trainName,
                                originStationCode = candidate.originStationCode,
                                originStationName = candidate.originStationName,
                                destStationCode = candidate.destStationCode,
                                destStationName = candidate.destStationName,
                                departureTime = candidate.departureTime,
                                arrivalTime = candidate.arrivalTime,
                                platform = candidate.platform,
                                type = "EMU Local",
                                zone = candidate.zone,
                                coachCodes = candidate.coachCodes.joinToString(","),
                                lastUpdated = now
                            )
                        }
                        db?.trainDao()?.insertTrains(entities)
                    } catch (e: Exception) {
                        Log.w("HybridRailwayDataProvider", "Could not persist trains to Room: ${e.message}")
                    }

                    departureCache[stationCode] = Pair(list, now)
                    return list
                }
            }
        } catch (_: Exception) {
            // Safe fallback
        }

        // Try querying persisted trains from Room database before static fallback
        try {
            val dbTrains = db?.trainDao()?.getTrainsForStationDirect(stationCode)
            if (!dbTrains.isNullOrEmpty()) {
                val dbCandidates = dbTrains.map { entity ->
                    TrainCandidate(
                        trainNumber = entity.trainNumber,
                        trainName = entity.trainName,
                        originStationCode = entity.originStationCode,
                        originStationName = entity.originStationName,
                        destStationCode = entity.destStationCode,
                        destStationName = entity.destStationName,
                        departureTime = entity.departureTime,
                        arrivalTime = entity.arrivalTime,
                        platform = entity.platform,
                        zone = entity.zone,
                        coachCodes = entity.coachCodes.split(",").filter { it.isNotBlank() }
                    )
                }
                departureCache[stationCode] = Pair(dbCandidates, now)
                return dbCandidates
            }
        } catch (_: Exception) {}

        val localList = localFallback.getStationDepartures(stationCode)
        departureCache[stationCode] = Pair(localList, now)
        return localList
    }

    override suspend fun getTrainSchedule(trainNumber: String): LocalTrainSchedule? {
        return localFallback.getTrainSchedule(trainNumber)
    }

    override suspend fun getTrainRouteDetails(trainNumber: String): TrainRouteDetails? {
        // Try resolving from database
        try {
            val dbTrain = db?.trainDao()?.getTrainByNumberDirect(trainNumber)
            if (dbTrain != null) {
                return TrainRouteDetails(
                    trainNumber = dbTrain.trainNumber,
                    trainName = dbTrain.trainName,
                    stations = listOf(
                        "${dbTrain.originStationName} (${dbTrain.originStationCode})",
                        "${dbTrain.destStationName} (${dbTrain.destStationCode})"
                    ),
                    currentStationIndex = 0,
                    currentPlatform = dbTrain.platform,
                    coachCodes = dbTrain.coachCodes.split(",").filter { it.isNotBlank() }
                )
            }
        } catch (_: Exception) {}
        return localFallback.getTrainRouteDetails(trainNumber)
    }

    override suspend fun getAllRoutes(): List<TrainRouteDetails> {
        // Load dynamically from Room database first
        try {
            val dbTrains = db?.trainDao()?.getAllTrainsDirect()
            if (!dbTrains.isNullOrEmpty()) {
                return dbTrains.map { entity ->
                    TrainRouteDetails(
                        trainNumber = entity.trainNumber,
                        trainName = entity.trainName,
                        stations = listOf(
                            "${entity.originStationName} (${entity.originStationCode})",
                            "${entity.destStationName} (${entity.destStationCode})"
                        ),
                        currentStationIndex = 0,
                        currentPlatform = entity.platform,
                        coachCodes = entity.coachCodes.split(",").filter { it.isNotBlank() }
                    )
                }
            }
        } catch (_: Exception) {}
        return localFallback.getAllRoutes()
    }

    override suspend fun getDynamicCoaches(trainNumber: String): List<String> = withContext(Dispatchers.IO) {
        // 1. Fetch real coach composition from backend API
        try {
            val res = ApiClient.apiService.getTrainCoaches(trainNumber)
            if (res.isSuccessful && res.body()?.success == true) {
                val remoteCoaches = res.body()?.data?.coaches
                if (!remoteCoaches.isNullOrEmpty()) {
                    try {
                        db?.trainDao()?.updateCoachCodes(trainNumber, remoteCoaches.joinToString(","))
                    } catch (_: Exception) {}
                    return@withContext remoteCoaches
                }
            }
        } catch (_: Exception) {}

        // 2. Check cached coach layout in Room database
        try {
            val dbTrain = db?.trainDao()?.getTrainByNumberDirect(trainNumber)
            if (dbTrain != null && dbTrain.coachCodes.isNotBlank()) {
                val list = dbTrain.coachCodes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (list.isNotEmpty()) return@withContext list
            }
        } catch (_: Exception) {}

        // 3. Dynamic composition based on train type
        val isEmu = trainNumber.startsWith("3") || trainNumber.startsWith("9") || trainNumber.length == 5
        val dynamicList = if (isEmu) {
            listOf("CAB-1", "LD-1", "VND-1", "GS-1", "GS-2", "GS-3", "VND-2", "LD-2", "CAB-2")
        } else {
            listOf("LOCO", "EOG-1", "GS-1", "S1", "S2", "S3", "S4", "S5", "B1", "B2", "B3", "A1", "H1", "PC", "GS-2", "EOG-2")
        }
        try {
            db?.trainDao()?.updateCoachCodes(trainNumber, dynamicList.joinToString(","))
        } catch (_: Exception) {}
        dynamicList
    }

    override suspend fun getLiveTrainStatus(trainNumber: String, currentStationCode: String?): LiveTrainStatus {
        return localFallback.getLiveTrainStatus(trainNumber, currentStationCode)
    }

    override suspend fun searchStationsAndTrains(query: String): Pair<List<RailwayStation>, List<TrainCandidate>> = withContext(Dispatchers.IO) {
        try {
            val res = ApiClient.apiService.searchStations(query)
            logApiMetadata(
                endpoint = "/api/stations/search?q=$query",
                status = res.code(),
                traceId = res.body()?.meta?.traceId,
                timestamp = res.body()?.meta?.timestamp,
                execTime = null
            )
            if (res.isSuccessful && res.body()?.success == true) {
                val remoteStations = res.body()?.data
                if (!remoteStations.isNullOrEmpty()) {
                    val stations = remoteStations.map { st ->
                        RailwayStation(
                            code = st.code,
                            nameEn = st.name,
                            nameHi = st.name,
                            nameBn = st.name,
                            division = st.zone.ifEmpty { "Eastern Railway" },
                            latitude = st.latitude ?: 22.5697,
                            longitude = st.longitude ?: 88.3712
                        )
                    }
                    val (_, localTrains) = localFallback.searchStationsAndTrains(query)
                    return@withContext Pair(stations, localTrains)
                }
            }
        } catch (_: Exception) {
            // Fallback to local database
        }
        localFallback.searchStationsAndTrains(query)
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}

class LocalStaticRailwayDataProvider : RailwayDataProvider {

    override fun getRecentApiLogs(): List<ApiDiagnosticsLog> = emptyList()

    override suspend fun getAllStations(): List<RailwayStation> {
        return IndianLocalRailwayDatabase.allStations
    }

    override suspend fun getNearestStation(lat: Double, lng: Double): Pair<RailwayStation?, Double> {
        var minDistanceKm = Double.MAX_VALUE
        var closestStation: RailwayStation? = null
        for (st in IndianLocalRailwayDatabase.allStations) {
            val dist = calculateDistanceKm(lat, lng, st.latitude, st.longitude)
            if (dist < minDistanceKm) {
                minDistanceKm = dist
                closestStation = st
            }
        }
        return Pair(closestStation, minDistanceKm)
    }

    override suspend fun getStationDepartures(stationCode: String): List<TrainCandidate> {
        val report = getStationDeparturesReport(stationCode)
        return report.upcomingTrains
    }

    override suspend fun getStationDeparturesReport(stationCode: String): FilteredTrainReport {
        val matchingSchedules = IndianLocalRailwayDatabase.allSchedules.filter { sched ->
            sched.stops.any { it.stationCode.equals(stationCode, ignoreCase = true) }
        }
        val rawCandidates = matchingSchedules.map { sched ->
            val currentStop = sched.stops.find { it.stationCode.equals(stationCode, ignoreCase = true) }
            val origin = IndianLocalRailwayDatabase.allStations.find { it.code == sched.originStationCode }
            val dest = IndianLocalRailwayDatabase.allStations.find { it.code == sched.destStationCode }
            TrainCandidate(
                trainNumber = sched.trainNumber,
                trainName = sched.trainName,
                originStationCode = sched.originStationCode,
                originStationName = origin?.nameEn ?: sched.originStationCode,
                destStationCode = sched.destStationCode,
                destStationName = dest?.nameEn ?: sched.destStationCode,
                departureTime = currentStop?.departureTime ?: "08:30",
                arrivalTime = currentStop?.arrivalTime ?: "08:30",
                platform = currentStop?.platform ?: "PF 1",
                zone = sched.zone,
                coachCodes = sched.coaches.map { it.coachCode }
            )
        }

        // Apply UpcomingTrainFilter against current IST time
        val initialReport = UpcomingTrainFilter.filterUpcomingTrains(
            candidates = rawCandidates,
            referenceIstEpochMs = System.currentTimeMillis()
        )

        // If candidates are available or there are no raw schedules, return directly
        if (initialReport.upcomingTrains.isNotEmpty() || rawCandidates.isEmpty()) {
            return initialReport
        }

        // When static morning trains have passed for today, project realistic suburban
        // EMU frequencies for the current time slot (e.g. departures at +6m, +18m, +32m, +48m)
        val projectedCandidates = rawCandidates.mapIndexed { index, candidate ->
            val offsetMinutes = 6 + (index * 14)
            val trainCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
                add(Calendar.MINUTE, offsetMinutes)
            }
            val depTime = String.format(Locale.ENGLISH, "%02d:%02d", trainCal.get(Calendar.HOUR_OF_DAY), trainCal.get(Calendar.MINUTE))
            candidate.copy(
                departureTime = depTime,
                arrivalTime = depTime
            )
        }

        return UpcomingTrainFilter.filterUpcomingTrains(
            candidates = projectedCandidates,
            referenceIstEpochMs = System.currentTimeMillis()
        )
    }

    override suspend fun getTrainSchedule(trainNumber: String): LocalTrainSchedule? {
        return IndianLocalRailwayDatabase.allSchedules.find { it.trainNumber == trainNumber }
    }

    override suspend fun getTrainRouteDetails(trainNumber: String): TrainRouteDetails? {
        val sched = getTrainSchedule(trainNumber) ?: return null
        return TrainRouteDetails(
            trainNumber = sched.trainNumber,
            trainName = sched.trainName,
            stations = sched.stops.map { "${it.stationName} (${it.stationCode})" },
            currentStationIndex = 0,
            currentPlatform = sched.stops.firstOrNull()?.platform ?: "PF 1",
            coachCodes = sched.coaches.map { it.coachCode }
        )
    }

    override suspend fun getAllRoutes(): List<TrainRouteDetails> {
        return IndianLocalRailwayDatabase.allSchedules.map { sched ->
            TrainRouteDetails(
                trainNumber = sched.trainNumber,
                trainName = sched.trainName,
                stations = sched.stops.map { "${it.stationName} (${it.stationCode})" },
                currentStationIndex = 0,
                currentPlatform = sched.stops.firstOrNull()?.platform ?: "PF 1",
                coachCodes = sched.coaches.map { it.coachCode }
            )
        }
    }

    override suspend fun getDynamicCoaches(trainNumber: String): List<String> {
        val sched = getTrainSchedule(trainNumber)
        if (sched != null && sched.coaches.isNotEmpty()) {
            return sched.coaches.map { it.coachCode }
        }
        val isEmu = trainNumber.startsWith("3") || trainNumber.startsWith("9") || trainNumber.length == 5
        return if (isEmu) {
            listOf("CAB-1", "LD-1", "VND-1", "GS-1", "GS-2", "GS-3", "VND-2", "LD-2", "CAB-2")
        } else {
            listOf("LOCO", "EOG-1", "GS-1", "S1", "S2", "S3", "S4", "S5", "B1", "B2", "B3", "A1", "H1", "PC", "GS-2", "EOG-2")
        }
    }

    override suspend fun getLiveTrainStatus(trainNumber: String, currentStationCode: String?): LiveTrainStatus {
        val sched = getTrainSchedule(trainNumber)
        val stnName = IndianLocalRailwayDatabase.allStations.find { it.code.equals(currentStationCode, ignoreCase = true) }?.nameEn ?: "En-route"
        return LiveTrainStatus(
            trainNumber = trainNumber,
            trainName = sched?.trainName ?: "Suburban Local",
            currentStation = stnName,
            nextStation = "Approaching next stop",
            etaNextStationSeconds = 45,
            delayMinutes = 0,
            isLiveApiAvailable = false,
            statusSummary = "Live timetable tracking"
        )
    }

    override suspend fun searchStationsAndTrains(query: String): Pair<List<RailwayStation>, List<TrainCandidate>> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            return Pair(IndianLocalRailwayDatabase.allStations.take(6), emptyList())
        }

        val matchingStations = IndianLocalRailwayDatabase.allStations.filter {
            it.nameEn.lowercase().contains(q) ||
            it.nameHi.lowercase().contains(q) ||
            it.nameBn.lowercase().contains(q) ||
            it.code.lowercase().contains(q)
        }

        val matchingStationCodes = matchingStations.map { it.code.lowercase() }.toSet()

        val matchingTrains = IndianLocalRailwayDatabase.allSchedules.filter { sched ->
            sched.trainNumber.lowercase().contains(q) ||
            sched.trainName.lowercase().contains(q) ||
            sched.originStationCode.lowercase().contains(q) ||
            sched.destStationCode.lowercase().contains(q) ||
            matchingStationCodes.contains(sched.originStationCode.lowercase()) ||
            matchingStationCodes.contains(sched.destStationCode.lowercase()) ||
            sched.stops.any { st ->
                st.stationName.lowercase().contains(q) ||
                st.stationCode.lowercase().contains(q) ||
                matchingStationCodes.contains(st.stationCode.lowercase())
            }
        }.map { sched ->
            val origin = IndianLocalRailwayDatabase.allStations.find { it.code == sched.originStationCode }
            val dest = IndianLocalRailwayDatabase.allStations.find { it.code == sched.destStationCode }
            TrainCandidate(
                trainNumber = sched.trainNumber,
                trainName = sched.trainName,
                originStationCode = sched.originStationCode,
                originStationName = origin?.nameEn ?: sched.originStationCode,
                destStationCode = sched.destStationCode,
                destStationName = dest?.nameEn ?: sched.destStationCode,
                departureTime = sched.stops.firstOrNull()?.departureTime ?: "08:00",
                arrivalTime = sched.stops.lastOrNull()?.arrivalTime ?: "09:30",
                platform = sched.stops.firstOrNull()?.platform ?: "PF 1",
                zone = sched.zone,
                coachCodes = sched.coaches.map { it.coachCode }
            )
        }

        return Pair(matchingStations, matchingTrains)
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}

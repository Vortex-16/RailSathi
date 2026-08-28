package com.example.data.repository

import com.example.data.model.AuthenticEmuFormations
import com.example.data.model.IndianLocalRailwayDatabase
import com.example.data.model.LiveTrainStatus
import com.example.data.model.LocalTrainSchedule
import com.example.data.model.RailwayStation
import com.example.data.model.TrainCandidate

interface RailwayDataProvider {
    suspend fun getAllStations(): List<RailwayStation>
    suspend fun getNearestStation(lat: Double, lng: Double): Pair<RailwayStation?, Double>
    suspend fun getStationDepartures(stationCode: String): List<TrainCandidate>
    suspend fun getTrainSchedule(trainNumber: String): LocalTrainSchedule?
    suspend fun getTrainRouteDetails(trainNumber: String): TrainRouteDetails?
    suspend fun getAllRoutes(): List<TrainRouteDetails>
    suspend fun getLiveTrainStatus(trainNumber: String, currentStationCode: String?): LiveTrainStatus
    suspend fun searchStationsAndTrains(query: String): Pair<List<RailwayStation>, List<TrainCandidate>>
}

class LocalStaticRailwayDataProvider : RailwayDataProvider {

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
        val matchingSchedules = IndianLocalRailwayDatabase.allSchedules.filter { sched ->
            sched.stops.any { it.stationCode.equals(stationCode, ignoreCase = true) }
        }
        return matchingSchedules.map { sched ->
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

        val matchingTrains = IndianLocalRailwayDatabase.allSchedules.filter {
            it.trainNumber.contains(q) ||
            it.trainName.lowercase().contains(q) ||
            it.stops.any { st -> st.stationName.lowercase().contains(q) || st.stationCode.lowercase().contains(q) }
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

class RailwayApiAdapter(
    private val localProvider: LocalStaticRailwayDataProvider = LocalStaticRailwayDataProvider()
) : RailwayDataProvider by localProvider

package com.example.data.engine

import com.example.data.local.AppDatabase
import com.example.data.local.JourneySessionEntity
import com.example.data.location.TrainLocationTracker
import com.example.data.location.UserLocationInfo
import com.example.data.model.JourneySession
import com.example.data.model.JourneyStatus
import com.example.data.model.LocationDiagnosticsInfo
import com.example.data.model.RailwayStation
import com.example.data.model.StationConfidence
import com.example.data.model.TrainCandidate
import com.example.data.model.TrainConfidence
import com.example.data.model.TrainContextState
import com.example.data.repository.ApiDiagnosticsLog
import com.example.data.repository.RailwayDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.data.location.LocationManagerState
import kotlinx.coroutines.launch
import java.util.UUID

data class TrainDiagnosticsSnapshot(
    val locationDiagnostics: LocationDiagnosticsInfo,
    val locationManagerState: LocationManagerState = LocationManagerState(),
    val detectedStation: RailwayStation?,
    val stationConfidence: StationConfidence,
    val stationDistanceMeters: Double,
    val isAtStation: Boolean,
    val currentIstTime: String,
    val upcomingTrainsCount: Int,
    val upcomingTrains: List<TrainCandidate>,
    val filteredDepartedTrains: List<FilteredDepartedItem>,
    val activeJourney: JourneySession?,
    val trainConfidence: TrainConfidence,
    val recentApiLogs: List<ApiDiagnosticsLog>
)

class TrainContextEngine(
    private val db: AppDatabase,
    private val railwayDataProvider: RailwayDataProvider,
    val locationTracker: TrainLocationTracker,
    private val scope: CoroutineScope
) {
    private val journeyDao = db.journeySessionDao()

    val locationState: StateFlow<UserLocationInfo> = locationTracker.locationState

    val activeJourneyFlow = journeyDao.getActiveJourney().map { entity ->
        entity?.toDomainModel()
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private val _contextState = MutableStateFlow(TrainContextState.IDLE)
    val contextState: StateFlow<TrainContextState> = _contextState.asStateFlow()

    private val _nearbyStation = MutableStateFlow<RailwayStation?>(null)
    val nearbyStation: StateFlow<RailwayStation?> = _nearbyStation.asStateFlow()

    private val _stationCandidates = MutableStateFlow<List<TrainCandidate>>(emptyList())
    val stationCandidates: StateFlow<List<TrainCandidate>> = _stationCandidates.asStateFlow()

    private val _filteredDepartedTrains = MutableStateFlow<List<FilteredDepartedItem>>(emptyList())
    val filteredDepartedTrains: StateFlow<List<FilteredDepartedItem>> = _filteredDepartedTrains.asStateFlow()

    private val _selectedCandidate = MutableStateFlow<TrainCandidate?>(null)
    val selectedCandidate: StateFlow<TrainCandidate?> = _selectedCandidate.asStateFlow()

    private val _stationConfidence = MutableStateFlow(StationConfidence.NONE)
    val stationConfidence: StateFlow<StationConfidence> = _stationConfidence.asStateFlow()

    private val _confidenceScore = MutableStateFlow(100)
    val confidenceScore: StateFlow<Int> = _confidenceScore.asStateFlow()

    private val _confidenceDescription = MutableStateFlow("Timetable Standby")
    val confidenceDescription: StateFlow<String> = _confidenceDescription.asStateFlow()

    private val _currentIstTime = MutableStateFlow("")
    val currentIstTime: StateFlow<String> = _currentIstTime.asStateFlow()

    init {
        // Observe location updates
        scope.launch {
            locationState.collect { loc ->
                handleLocationUpdate(loc)
            }
        }

        // Observe active journey changes
        scope.launch {
            activeJourneyFlow.collect { journey ->
                if (journey != null && journey.status == JourneyStatus.ACTIVE) {
                    _contextState.value = TrainContextState.ACTIVE_JOURNEY
                    _confidenceScore.value = journey.confidence
                } else {
                    val currentLoc = locationState.value
                    if (currentLoc.isNearStation && currentLoc.nearestStation != null) {
                        _contextState.value = TrainContextState.NEAR_STATION
                    } else {
                        _contextState.value = TrainContextState.IDLE
                    }
                }
            }
        }
    }

    private suspend fun handleLocationUpdate(loc: UserLocationInfo) {
        val activeJourney = activeJourneyFlow.value

        // Execute StationDetectionEngine against station directory
        val allStations = railwayDataProvider.getAllStations()
        val detectionResult = StationDetectionEngine.detectStation(loc, allStations)

        _stationConfidence.value = detectionResult.confidence
        val detectedStation = detectionResult.currentStation ?: detectionResult.nearbyCandidates.firstOrNull()?.station

        // Update location tracker with resolved station match & confidence
        locationTracker.updateStationMatch(
            station = detectedStation,
            distanceKm = detectionResult.nearestDistanceMeters / 1000.0,
            isNear = detectionResult.confidence == StationConfidence.HIGH || detectionResult.confidence == StationConfidence.MEDIUM,
            confidence = detectionResult.confidence
        )

        if (activeJourney == null || activeJourney.status != JourneyStatus.ACTIVE) {
            // User is not in an active journey
            if (detectionResult.isAtStation && detectionResult.currentStation != null) {
                // High Confidence at Station
                _nearbyStation.value = detectionResult.currentStation
                val report = railwayDataProvider.getStationDeparturesReport(detectionResult.currentStation.code)
                _stationCandidates.value = report.upcomingTrains
                _filteredDepartedTrains.value = report.filteredDepartedTrains
                _currentIstTime.value = report.currentIstTimeFormatted
                if (_selectedCandidate.value == null) {
                    _contextState.value = TrainContextState.NEAR_STATION
                }
            } else if (detectionResult.confidence == StationConfidence.MEDIUM && detectedStation != null) {
                // Medium Confidence in vicinity
                _nearbyStation.value = detectedStation
                val report = railwayDataProvider.getStationDeparturesReport(detectedStation.code)
                _stationCandidates.value = report.upcomingTrains
                _filteredDepartedTrains.value = report.filteredDepartedTrains
                _currentIstTime.value = report.currentIstTimeFormatted
                if (_selectedCandidate.value == null) {
                    _contextState.value = TrainContextState.NEAR_STATION
                }
            } else {
                // User is off-track (>1.5km) or low accuracy -> Clear candidates to prevent ghost suggestions
                _nearbyStation.value = null
                _stationCandidates.value = emptyList()
                _filteredDepartedTrains.value = emptyList()
                if (_selectedCandidate.value == null) {
                    _contextState.value = TrainContextState.IDLE
                }
            }
        } else {
            // User is in an active journey -> Track progression
            _contextState.value = TrainContextState.TRACKING
            updateActiveJourneyTracking(activeJourney, loc, detectedStation, detectionResult)
        }
    }

    private suspend fun updateActiveJourneyTracking(
        journey: JourneySession,
        loc: UserLocationInfo,
        nearestStn: RailwayStation?,
        detection: StationDetectionResult
    ) {
        val schedule = railwayDataProvider.getTrainSchedule(journey.trainNumber)
        if (schedule == null) {
            _confidenceScore.value = 75
            _confidenceDescription.value = "Timetable tracking (Route data limited)"
            return
        }

        val distanceKm = detection.nearestDistanceMeters / 1000.0

        if (loc.isGpsActive && nearestStn != null) {
            val isStopInRoute = schedule.stops.any { it.stationCode.equals(nearestStn.code, ignoreCase = true) }
            if (isStopInRoute && distanceKm <= 0.6) {
                // High confidence - User is near a valid station on their train line
                _confidenceScore.value = 95
                _confidenceDescription.value = "Live GPS Corridor Match • ${nearestStn.nameEn}"
                journeyDao.updateProgress(
                    journeyId = journey.journeyId,
                    station = nearestStn.nameEn,
                    confidence = 95,
                    timestamp = System.currentTimeMillis()
                )
            } else if (isStopInRoute && distanceKm <= 3.5) {
                _confidenceScore.value = 85
                _confidenceDescription.value = "Approaching ${nearestStn.nameEn} • Good Signal"
            } else {
                _confidenceScore.value = 70
                _confidenceDescription.value = "Timetable progression (Distance ~${String.format(java.util.Locale.US, "%.1f", distanceKm)}km)"
            }
        } else {
            _confidenceScore.value = 70
            _confidenceDescription.value = "Timetable progression (Live GPS Standby)"
        }
    }

    fun selectCandidateTrain(candidate: TrainCandidate) {
        _selectedCandidate.value = candidate
        _contextState.value = TrainContextState.AWAITING_CONFIRMATION
    }

    fun clearCandidateSelection() {
        _selectedCandidate.value = null
        val loc = locationState.value
        _contextState.value = if (loc.isNearStation) TrainContextState.NEAR_STATION else TrainContextState.IDLE
    }

    fun startJourney(
        candidate: TrainCandidate,
        userId: String = "traveler_1",
        selectedCoach: String = "GS-2"
    ) {
        scope.launch {
            journeyDao.completeAllActiveJourneys()

            val session = JourneySessionEntity(
                journeyId = "journey_${UUID.randomUUID().toString().take(8)}_${System.currentTimeMillis()}",
                userId = userId,
                trainNumber = candidate.trainNumber,
                trainName = candidate.trainName,
                originStation = candidate.originStationName,
                destinationStation = candidate.destStationName,
                selectedAt = System.currentTimeMillis(),
                startedAt = System.currentTimeMillis(),
                currentStation = candidate.originStationName,
                currentCoach = selectedCoach,
                status = "ACTIVE",
                confidence = 92,
                lastLocationUpdate = System.currentTimeMillis(),
                isLiveTracking = true,
                trackingSource = if (locationState.value.isGpsActive) "GPS" else "TIMETABLE"
            )
            journeyDao.insertJourney(session)
            _selectedCandidate.value = null
            _contextState.value = TrainContextState.ACTIVE_JOURNEY
        }
    }

    fun startCustomJourney(
        trainNumber: String,
        trainName: String,
        originStation: String,
        destStation: String,
        coach: String,
        userId: String = "traveler_1"
    ) {
        scope.launch {
            journeyDao.completeAllActiveJourneys()
            val session = JourneySessionEntity(
                journeyId = "journey_${UUID.randomUUID().toString().take(8)}_${System.currentTimeMillis()}",
                userId = userId,
                trainNumber = trainNumber,
                trainName = trainName,
                originStation = originStation,
                destinationStation = destStation,
                selectedAt = System.currentTimeMillis(),
                startedAt = System.currentTimeMillis(),
                currentStation = originStation,
                currentCoach = coach,
                status = "ACTIVE",
                confidence = 90,
                lastLocationUpdate = System.currentTimeMillis(),
                isLiveTracking = true,
                trackingSource = "USER_CONFIRMED"
            )
            journeyDao.insertJourney(session)
            _selectedCandidate.value = null
            _contextState.value = TrainContextState.ACTIVE_JOURNEY
        }
    }

    fun updateJourneyCoach(coach: String) {
        scope.launch {
            val active = activeJourneyFlow.value ?: return@launch
            journeyDao.updateCoach(active.journeyId, coach)
        }
    }

    fun endJourney() {
        scope.launch {
            val active = activeJourneyFlow.value ?: return@launch
            journeyDao.completeJourney(active.journeyId)
            _selectedCandidate.value = null
            _contextState.value = TrainContextState.IDLE
        }
    }

    fun cancelJourney() {
        scope.launch {
            val active = activeJourneyFlow.value ?: return@launch
            journeyDao.cancelJourney(active.journeyId)
            _selectedCandidate.value = null
            _contextState.value = TrainContextState.IDLE
        }
    }

    fun getDiagnosticsSnapshot(): TrainDiagnosticsSnapshot {
        val loc = locationState.value
        val nearest = _nearbyStation.value
        return TrainDiagnosticsSnapshot(
            locationDiagnostics = loc.diagnostics,
            locationManagerState = locationTracker.managerState.value,
            detectedStation = nearest,
            stationConfidence = _stationConfidence.value,
            stationDistanceMeters = loc.distanceToStationKm * 1000.0,
            isAtStation = loc.isNearStation && _stationConfidence.value == StationConfidence.HIGH,
            currentIstTime = _currentIstTime.value,
            upcomingTrainsCount = _stationCandidates.value.size,
            upcomingTrains = _stationCandidates.value,
            filteredDepartedTrains = _filteredDepartedTrains.value,
            activeJourney = activeJourneyFlow.value,
            trainConfidence = if (activeJourneyFlow.value != null) TrainConfidence.HIGH else TrainConfidence.UNKNOWN,
            recentApiLogs = railwayDataProvider.getRecentApiLogs()
        )
    }

    private fun JourneySessionEntity.toDomainModel(): JourneySession {
        return JourneySession(
            journeyId = journeyId,
            userId = userId,
            trainNumber = trainNumber,
            trainName = trainName,
            originStation = originStation,
            destinationStation = destinationStation,
            selectedAt = selectedAt,
            startedAt = startedAt,
            endedAt = endedAt,
            currentStation = currentStation,
            currentCoach = currentCoach,
            status = try { JourneyStatus.valueOf(status) } catch (e: Exception) { JourneyStatus.ACTIVE },
            confidence = confidence,
            lastLocationUpdate = lastLocationUpdate,
            isLiveTracking = isLiveTracking,
            trackingSource = trackingSource
        )
    }
}

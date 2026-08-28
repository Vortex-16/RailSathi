package com.example.data.model

enum class JourneyStatus {
    IDLE,
    PLANNED,
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED
}

enum class TrainContextState {
    IDLE,
    NEAR_STATION,
    TRAIN_CANDIDATES,
    AWAITING_CONFIRMATION,
    ACTIVE_JOURNEY,
    TRACKING,
    JOURNEY_COMPLETED
}

data class TrainCandidate(
    val trainNumber: String,
    val trainName: String,
    val originStationCode: String,
    val originStationName: String,
    val destStationCode: String,
    val destStationName: String,
    val departureTime: String,
    val arrivalTime: String,
    val platform: String,
    val zone: String,
    val runningDays: String = "Daily",
    val coachCodes: List<String> = listOf("CAB-1", "LD-1", "VND-1", "GS-1", "GS-2", "GS-3", "VND-2", "LD-2", "CAB-2")
)

data class JourneySession(
    val journeyId: String,
    val userId: String,
    val trainNumber: String,
    val trainName: String,
    val originStation: String,
    val destinationStation: String,
    val selectedAt: Long,
    val startedAt: Long,
    val endedAt: Long? = null,
    val currentStation: String,
    val currentCoach: String,
    val status: JourneyStatus = JourneyStatus.ACTIVE,
    val confidence: Int = 100,
    val lastLocationUpdate: Long = System.currentTimeMillis(),
    val isLiveTracking: Boolean = true,
    val trackingSource: String = "TIMETABLE_GPS"
)

data class LiveTrainStatus(
    val trainNumber: String,
    val trainName: String,
    val currentStation: String,
    val nextStation: String,
    val etaNextStationSeconds: Int,
    val delayMinutes: Int = 0,
    val isLiveApiAvailable: Boolean = false,
    val statusSummary: String = "On-time timetable estimate"
)

data class RegularCommuteSchedule(
    val originStationCode: String = "BP",
    val originStationName: String = "Barrackpore",
    val destStationCode: String = "SDAH",
    val destStationName: String = "Sealdah",
    val usualTrainNumber: String = "31821",
    val usualTrainName: String = "Sealdah - Ranaghat Local",
    val usualDepartureTime: String = "08:42",
    val usualCoach: String = "GS-2"
)

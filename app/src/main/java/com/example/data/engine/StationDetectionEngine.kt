package com.example.data.engine

import com.example.data.location.UserLocationInfo
import com.example.data.model.RailwayStation
import com.example.data.model.StationConfidence
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class StationCandidateMatch(
    val station: RailwayStation,
    val distanceMeters: Double,
    val confidence: StationConfidence,
    val isCurrentStationCandidate: Boolean
)

data class StationDetectionResult(
    val currentStation: RailwayStation?,
    val nearbyCandidates: List<StationCandidateMatch>,
    val confidence: StationConfidence,
    val nearestDistanceMeters: Double,
    val isAtStation: Boolean,
    val diagnosticReason: String
)

object StationDetectionEngine {

    // Configurable thresholds for railway station identification
    const val HIGH_CONFIDENCE_GEOFENCE_METERS = 350.0
    const val MEDIUM_CONFIDENCE_GEOFENCE_METERS = 750.0
    const val NEARBY_SEARCH_RADIUS_METERS = 1500.0
    const val MAX_ACCEPTABLE_ACCURACY_METERS = 250.0
    const val HIGH_PRECISION_ACCURACY_METERS = 60.0

    /**
     * Evaluates the fresh device location against the station directory.
     * Uses distance, location accuracy, and quality gates to determine station confidence.
     */
    fun detectStation(
        location: UserLocationInfo,
        allStations: List<RailwayStation>
    ): StationDetectionResult {
        val lat = location.latitude
        val lng = location.longitude

        // Quality Gate 1: Check location validity
        if (lat == null || lng == null || lat == 0.0 || lng == 0.0) {
            return StationDetectionResult(
                currentStation = null,
                nearbyCandidates = emptyList(),
                confidence = StationConfidence.NONE,
                nearestDistanceMeters = Double.MAX_VALUE,
                isAtStation = false,
                diagnosticReason = "No valid GPS coordinates available"
            )
        }

        // Quality Gate 2: Location Age (Reject Stale Location)
        if (location.locationAgeSeconds > 60L && !location.isSimulationMode) {
            return StationDetectionResult(
                currentStation = null,
                nearbyCandidates = emptyList(),
                confidence = StationConfidence.UNKNOWN,
                nearestDistanceMeters = Double.MAX_VALUE,
                isAtStation = false,
                diagnosticReason = "Location data is stale (${location.locationAgeSeconds}s old)"
            )
        }

        // Quality Gate 3: Accuracy Gate
        val accuracy = location.accuracyMeters.toDouble()
        if (accuracy > MAX_ACCEPTABLE_ACCURACY_METERS && !location.isSimulationMode) {
            return StationDetectionResult(
                currentStation = null,
                nearbyCandidates = emptyList(),
                confidence = StationConfidence.UNKNOWN,
                nearestDistanceMeters = Double.MAX_VALUE,
                isAtStation = false,
                diagnosticReason = "Location accuracy too low (±${accuracy.toInt()}m) to confirm station"
            )
        }

        // Calculate distances to all stations
        val matches = mutableListOf<StationCandidateMatch>()
        for (st in allStations) {
            val distMeters = calculateDistanceMeters(lat, lng, st.latitude, st.longitude)
            if (distMeters <= NEARBY_SEARCH_RADIUS_METERS) {
                val conf = determineConfidence(distMeters, accuracy, location.isSimulationMode)
                matches.add(
                    StationCandidateMatch(
                        station = st,
                        distanceMeters = distMeters,
                        confidence = conf,
                        isCurrentStationCandidate = conf == StationConfidence.HIGH
                    )
                )
            }
        }

        // Sort by distance ascending
        val sortedMatches = matches.sortedBy { it.distanceMeters }

        if (sortedMatches.isEmpty()) {
            // User is at home or off-track (> 1.5km from any railway station)
            return StationDetectionResult(
                currentStation = null,
                nearbyCandidates = emptyList(),
                confidence = StationConfidence.NONE,
                nearestDistanceMeters = Double.MAX_VALUE,
                isAtStation = false,
                diagnosticReason = "Off-track (>1.5km from railway network)"
            )
        }

        val closest = sortedMatches.first()
        val nearestDistance = closest.distanceMeters

        // Check for ambiguous multiple close stations (e.g. within 150m of each other)
        val closeCompetitors = sortedMatches.filter {
            it.distanceMeters <= HIGH_CONFIDENCE_GEOFENCE_METERS &&
            it.distanceMeters - nearestDistance < 120.0
        }

        return if (closeCompetitors.size > 1 && !location.isSimulationMode) {
            // Multiple stations close by -> Show nearby candidates for manual user confirmation
            StationDetectionResult(
                currentStation = null,
                nearbyCandidates = sortedMatches,
                confidence = StationConfidence.MEDIUM,
                nearestDistanceMeters = nearestDistance,
                isAtStation = false,
                diagnosticReason = "Multiple stations nearby (${closeCompetitors.map { it.station.nameEn }.joinToString(", ")}) - confirmation recommended"
            )
        } else if (closest.confidence == StationConfidence.HIGH) {
            // Confident current station
            StationDetectionResult(
                currentStation = closest.station,
                nearbyCandidates = sortedMatches,
                confidence = StationConfidence.HIGH,
                nearestDistanceMeters = nearestDistance,
                isAtStation = true,
                diagnosticReason = "At ${closest.station.nameEn} (~${nearestDistance.toInt()}m, ±${accuracy.toInt()}m)"
            )
        } else {
            // Nearby but not inside platform/geofence
            StationDetectionResult(
                currentStation = null,
                nearbyCandidates = sortedMatches,
                confidence = closest.confidence,
                nearestDistanceMeters = nearestDistance,
                isAtStation = false,
                diagnosticReason = "Near ${closest.station.nameEn} (~${(nearestDistance / 1000.0).format(1)}km, ±${accuracy.toInt()}m)"
            )
        }
    }

    private fun determineConfidence(distMeters: Double, accuracyMeters: Double, isSimulation: Boolean): StationConfidence {
        if (isSimulation) return StationConfidence.HIGH

        return when {
            distMeters <= HIGH_CONFIDENCE_GEOFENCE_METERS && accuracyMeters <= HIGH_PRECISION_ACCURACY_METERS -> StationConfidence.HIGH
            distMeters <= HIGH_CONFIDENCE_GEOFENCE_METERS && accuracyMeters <= 120.0 -> StationConfidence.HIGH
            distMeters <= MEDIUM_CONFIDENCE_GEOFENCE_METERS -> StationConfidence.MEDIUM
            distMeters <= NEARBY_SEARCH_RADIUS_METERS -> StationConfidence.LOW
            else -> StationConfidence.NONE
        }
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun Double.format(digits: Int) = String.format(java.util.Locale.US, "%.${digits}f", this)
}

package com.example.data.location

import android.content.Context
import com.example.data.model.LocationDiagnosticsInfo
import com.example.data.model.RailwayStation
import com.example.data.model.StationConfidence
import kotlinx.coroutines.flow.StateFlow

class TrainLocationTracker(context: Context) {

    val freshLocationProvider = FreshLocationProvider(context)
    val locationState: StateFlow<UserLocationInfo> = freshLocationProvider.locationState

    fun updatePermissionStatus(granted: Boolean) {
        freshLocationProvider.updatePermissionStatus(fineGranted = granted, coarseGranted = granted)
    }

    fun updatePermissionStatus(fineGranted: Boolean, coarseGranted: Boolean) {
        freshLocationProvider.updatePermissionStatus(fineGranted = fineGranted, coarseGranted = coarseGranted)
    }

    fun startTracking() {
        freshLocationProvider.startTracking()
    }

    fun stopTracking() {
        freshLocationProvider.stopTracking()
    }

    fun setManualSimulationLocation(stationCode: String) {
        freshLocationProvider.setManualSimulationLocation(stationCode)
    }

    fun setManualSimulationLocation(station: RailwayStation) {
        freshLocationProvider.setManualSimulationLocation(station)
    }

    fun updateStationMatch(
        station: RailwayStation?,
        distanceKm: Double,
        isNear: Boolean,
        confidence: StationConfidence
    ) {
        freshLocationProvider.updateStationMatch(station, distanceKm, isNear, confidence)
    }
}

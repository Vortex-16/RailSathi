package com.example.data.location

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.example.data.model.RailwayStation
import com.example.data.model.StationConfidence
import kotlinx.coroutines.flow.StateFlow

class TrainLocationTracker(
    context: Context,
    val freshLocationProvider: FreshLocationProvider = FreshLocationProvider(context)
) {
    val locationStateManager = LocationStateManager(
        locationProvider = freshLocationProvider,
        initialForeground = true,
        initialTravelStatus = UserTravelStatus.STATIONARY,
        initialPermission = false,
        initialServicesEnabled = true
    )

    val locationState: StateFlow<UserLocationInfo> = freshLocationProvider.locationState
    val managerState: StateFlow<LocationManagerState> = locationStateManager.managerState
    val userTravelStatus: StateFlow<UserTravelStatus> = locationStateManager.userTravelStatus
    val isForeground: StateFlow<Boolean> = locationStateManager.isForeground

    fun attachLifecycle(lifecycleOwner: LifecycleOwner) {
        locationStateManager.attachLifecycle(lifecycleOwner)
    }

    fun updatePermissionStatus(granted: Boolean) {
        locationStateManager.updatePermissionStatus(fineGranted = granted, coarseGranted = granted)
    }

    fun updatePermissionStatus(fineGranted: Boolean, coarseGranted: Boolean) {
        locationStateManager.updatePermissionStatus(fineGranted = fineGranted, coarseGranted = coarseGranted)
    }

    fun setAppForeground(isForeground: Boolean) {
        locationStateManager.setAppForeground(isForeground)
    }

    fun setUserTravelStatus(status: UserTravelStatus) {
        locationStateManager.setUserTravelStatus(status)
    }

    fun toggleActiveTravel() {
        locationStateManager.toggleUserTravelStatus()
    }

    fun toggleLocationServices(enabled: Boolean? = null) {
        locationStateManager.toggleLocationServices(enabled)
    }

    fun startTracking() {
        locationStateManager.startTracking()
    }

    fun stopTracking() {
        locationStateManager.stopTracking()
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

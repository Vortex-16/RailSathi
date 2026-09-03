package com.example.data.location

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Defines a decoupled contract for the location hardware provider so that
 * LocationStateManager can orchestrate GPS updates without tight coupling
 * and can be tested cleanly in JVM unit tests.
 */
interface LocationProviderDelegate {
    fun startTracking()
    fun stopTracking(reason: String = "GPS Paused")
    fun updatePermissionStatus(fineGranted: Boolean, coarseGranted: Boolean)
    fun updateStatusMessage(message: String)
}

/**
 * User Travel Status indicating whether the user is actively traveling
 * (e.g. onboard a train, commuting, or en route) or stationary
 * (waiting, at home, at office, or not in active transit).
 */
enum class UserTravelStatus {
    STATIONARY,
    ACTIVE_TRAVEL
}

/**
 * Detailed operational state of the location service as governed by
 * the state-based policy engine.
 */
enum class LocationServiceState {
    ACTIVE,                     // GPS actively running (Foreground + Active Travel + Permission)
    PAUSED_BACKGROUND,          // Restricted: App is currently in the background
    PAUSED_INACTIVE_TRAVEL,     // Restricted: User status is stationary / not actively traveling
    PAUSED_NO_PERMISSION,       // Restricted: Location permission has not been granted
    PAUSED_MANUAL               // Restricted: Location services manually disabled
}

/**
 * Unified immutable snapshot of the LocationStateManager state.
 */
data class LocationManagerState(
    val isAppInForeground: Boolean = true,
    val userTravelStatus: UserTravelStatus = UserTravelStatus.STATIONARY,
    val hasPermission: Boolean = false,
    val isServicesEnabled: Boolean = true,
    val isTrackingGps: Boolean = false,
    val serviceState: LocationServiceState = LocationServiceState.PAUSED_INACTIVE_TRAVEL,
    val statusMessage: String = "GPS paused: User status is stationary"
)

/**
 * LocationStateManager
 *
 * Implements strict GPS restriction policy:
 * GPS hardware and fused updates are ONLY permitted when:
 * 1. The app is in the foreground (`isAppInForeground == true`), AND
 * 2. The user status is set to active travel (`userTravelStatus == UserTravelStatus.ACTIVE_TRAVEL`), AND
 * 3. Location permissions are granted (`hasPermission == true`), AND
 * 4. Master location services toggle is enabled (`isServicesEnabled == true`).
 *
 * It implements a state-based trigger that automatically toggles location services
 * on or off whenever any state changes.
 */
class LocationStateManager(
    private val locationProvider: LocationProviderDelegate,
    initialForeground: Boolean = true,
    initialTravelStatus: UserTravelStatus = UserTravelStatus.STATIONARY,
    initialPermission: Boolean = false,
    initialServicesEnabled: Boolean = true
) : DefaultLifecycleObserver {

    private val _isForeground = MutableStateFlow(initialForeground)
    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    private val _userTravelStatus = MutableStateFlow(initialTravelStatus)
    val userTravelStatus: StateFlow<UserTravelStatus> = _userTravelStatus.asStateFlow()

    private val _hasPermission = MutableStateFlow(initialPermission)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _isServicesEnabled = MutableStateFlow(initialServicesEnabled)
    val isServicesEnabled: StateFlow<Boolean> = _isServicesEnabled.asStateFlow()

    private val _serviceState = MutableStateFlow(LocationServiceState.PAUSED_INACTIVE_TRAVEL)
    val serviceState: StateFlow<LocationServiceState> = _serviceState.asStateFlow()

    private val _isTrackingGps = MutableStateFlow(false)
    val isTrackingGps: StateFlow<Boolean> = _isTrackingGps.asStateFlow()

    private val _statusMessage = MutableStateFlow("GPS paused: User status is stationary")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _managerState = MutableStateFlow(
        LocationManagerState(
            isAppInForeground = initialForeground,
            userTravelStatus = initialTravelStatus,
            hasPermission = initialPermission,
            isServicesEnabled = initialServicesEnabled,
            isTrackingGps = false,
            serviceState = LocationServiceState.PAUSED_INACTIVE_TRAVEL,
            statusMessage = "GPS paused: User status is stationary"
        )
    )
    val managerState: StateFlow<LocationManagerState> = _managerState.asStateFlow()

    init {
        evaluateTrigger()
    }

    /**
     * Connects an Android LifecycleOwner (Activity/Fragment) to automatically track
     * foreground and background transitions.
     */
    fun attachLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        setAppForeground(true)
    }

    override fun onPause(owner: LifecycleOwner) {
        setAppForeground(false)
    }

    override fun onStart(owner: LifecycleOwner) {
        setAppForeground(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        setAppForeground(false)
    }

    /**
     * Updates app foreground state.
     * When the app moves to background, GPS updates are immediately paused.
     * When the app moves to foreground, if in active travel, GPS updates resume.
     */
    fun setAppForeground(foreground: Boolean) {
        if (_isForeground.value != foreground) {
            _isForeground.value = foreground
            evaluateTrigger()
        }
    }

    /**
     * Sets user travel status.
     * ACTIVE_TRAVEL allows GPS updates (if in foreground and permitted).
     * STATIONARY restricts GPS updates to conserve power.
     */
    fun setUserTravelStatus(status: UserTravelStatus) {
        if (_userTravelStatus.value != status) {
            _userTravelStatus.value = status
            evaluateTrigger()
        }
    }

    /**
     * Toggles travel status between ACTIVE_TRAVEL and STATIONARY.
     */
    fun toggleUserTravelStatus() {
        val next = if (_userTravelStatus.value == UserTravelStatus.ACTIVE_TRAVEL) {
            UserTravelStatus.STATIONARY
        } else {
            UserTravelStatus.ACTIVE_TRAVEL
        }
        setUserTravelStatus(next)
    }

    /**
     * Updates runtime location permissions.
     */
    fun updatePermissionStatus(fineGranted: Boolean, coarseGranted: Boolean) {
        val granted = fineGranted || coarseGranted
        locationProvider.updatePermissionStatus(fineGranted, coarseGranted)
        if (_hasPermission.value != granted) {
            _hasPermission.value = granted
            evaluateTrigger()
        }
    }

    fun updatePermissionStatus(granted: Boolean) {
        updatePermissionStatus(fineGranted = granted, coarseGranted = granted)
    }

    /**
     * Master toggle for location services.
     */
    fun toggleLocationServices(enabled: Boolean? = null) {
        val next = enabled ?: !_isServicesEnabled.value
        if (_isServicesEnabled.value != next) {
            _isServicesEnabled.value = next
            evaluateTrigger()
        }
    }

    /**
     * Explicit trigger to request tracking. Sets travel status to ACTIVE_TRAVEL
     * and evaluates state conditions.
     */
    fun startTracking() {
        setUserTravelStatus(UserTravelStatus.ACTIVE_TRAVEL)
    }

    /**
     * Explicit trigger to stop tracking. Sets travel status to STATIONARY
     * and evaluates state conditions.
     */
    fun stopTracking() {
        setUserTravelStatus(UserTravelStatus.STATIONARY)
    }

    /**
     * The State-Based Trigger.
     * Evaluates: (isForeground && userTravelStatus == ACTIVE_TRAVEL && hasPermission && isServicesEnabled)
     * Automatically activates or pauses GPS hardware updates on the underlying provider.
     */
    @Synchronized
    fun evaluateTrigger() {
        val foreground = _isForeground.value
        val travelStatus = _userTravelStatus.value
        val permission = _hasPermission.value
        val servicesEnabled = _isServicesEnabled.value

        val shouldTrack = foreground &&
                (travelStatus == UserTravelStatus.ACTIVE_TRAVEL) &&
                permission &&
                servicesEnabled

        if (shouldTrack) {
            if (!_isTrackingGps.value) {
                _isTrackingGps.value = true
                locationProvider.startTracking()
            }
            val activeMsg = "GPS Active • Live Active Travel"
            _serviceState.value = LocationServiceState.ACTIVE
            _statusMessage.value = activeMsg
            locationProvider.updateStatusMessage(activeMsg)
        } else {
            val (newState, newMsg) = when {
                !permission -> LocationServiceState.PAUSED_NO_PERMISSION to "Location permission required"
                !servicesEnabled -> LocationServiceState.PAUSED_MANUAL to "Location services manually disabled"
                !foreground -> LocationServiceState.PAUSED_BACKGROUND to "GPS paused: App in background"
                travelStatus != UserTravelStatus.ACTIVE_TRAVEL -> LocationServiceState.PAUSED_INACTIVE_TRAVEL to "GPS paused: User status is stationary"
                else -> LocationServiceState.PAUSED_MANUAL to "GPS paused"
            }

            if (_isTrackingGps.value) {
                _isTrackingGps.value = false
                locationProvider.stopTracking(newMsg)
            } else {
                locationProvider.updateStatusMessage(newMsg)
            }

            _serviceState.value = newState
            _statusMessage.value = newMsg
        }

        _managerState.value = LocationManagerState(
            isAppInForeground = foreground,
            userTravelStatus = travelStatus,
            hasPermission = permission,
            isServicesEnabled = servicesEnabled,
            isTrackingGps = _isTrackingGps.value,
            serviceState = _serviceState.value,
            statusMessage = _statusMessage.value
        )
    }
}

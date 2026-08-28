package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import com.example.data.model.IndianLocalRailwayDatabase
import com.example.data.model.LocationDiagnosticsInfo
import com.example.data.model.RailwayStation
import com.example.data.model.StationConfidence
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class UserLocationInfo(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float = 0f,
    val speedMps: Float = 0f,
    val bearingDegrees: Float = 0f,
    val altitudeMeters: Double = 0.0,
    val locationTimestamp: Long = 0L,
    val locationAgeSeconds: Long = 0L,
    val provider: String = "none",
    val isGpsActive: Boolean = false,
    val hasPermission: Boolean = false,
    val isFinePermission: Boolean = false,
    val isMock: Boolean = false,
    val nearestStation: RailwayStation? = null,
    val distanceToStationKm: Double = 0.0,
    val isNearStation: Boolean = false,
    val stationConfidence: StationConfidence = StationConfidence.NONE,
    val statusMessage: String = "Standby",
    val currentPlatform: String = "PF 1",
    val isSimulationMode: Boolean = false,
    val diagnostics: LocationDiagnosticsInfo = LocationDiagnosticsInfo()
)

class FreshLocationProvider(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val fusedClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val _locationState = MutableStateFlow(
        UserLocationInfo(
            statusMessage = "Location standby"
        )
    )
    val locationState: StateFlow<UserLocationInfo> = _locationState.asStateFlow()

    private var isTracking = false
    private var isUsingFused = false

    // Quality gate thresholds
    companion object {
        const val MAX_LOCATION_AGE_SECONDS = 60L
        const val MAX_ACCEPTABLE_LOCATION_ACCURACY_METERS = 250f
        const val HIGH_ACCURACY_THRESHOLD_METERS = 75f
    }

    private val fusedLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            processFreshLocation(location, "fused")
        }
    }

    private val fallbackLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            processFreshLocation(location, location.provider ?: "location_manager")
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun updatePermissionStatus(fineGranted: Boolean, coarseGranted: Boolean) {
        val hasPerm = fineGranted || coarseGranted
        _locationState.value = _locationState.value.copy(
            hasPermission = hasPerm,
            isFinePermission = fineGranted,
            statusMessage = if (hasPerm) "Permission active" else "Location permission not granted"
        )
        if (hasPerm && !isTracking) {
            startTracking()
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (isTracking) return
        isTracking = true

        try {
            // 1. Try Google Play Services Fused Location Provider with Priority.PRIORITY_HIGH_ACCURACY
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 7000L)
                .setMinUpdateIntervalMillis(3500L)
                .setMinUpdateDistanceMeters(10f)
                .setWaitForAccurateLocation(false)
                .build()

            fusedClient.requestLocationUpdates(locationRequest, fusedLocationCallback, context.mainLooper)
                .addOnSuccessListener {
                    isUsingFused = true
                }
                .addOnFailureListener {
                    // Fallback to Android LocationManager
                    startLocationManagerFallback()
                }

            // Also check for fresh current location via modern CurrentLocation API
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        processFreshLocation(loc, "fused_current")
                    }
                }

        } catch (e: SecurityException) {
            _locationState.value = _locationState.value.copy(
                hasPermission = false,
                statusMessage = "Location permission required"
            )
        } catch (e: Exception) {
            startLocationManagerFallback()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationManagerFallback() {
        if (locationManager == null) return
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    7000L,
                    10f,
                    fallbackLocationListener
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    7000L,
                    15f,
                    fallbackLocationListener
                )
            }
        } catch (_: Exception) {
        }
    }

    fun stopTracking() {
        isTracking = false
        try {
            fusedClient.removeLocationUpdates(fusedLocationCallback)
        } catch (_: Exception) {}
        try {
            locationManager?.removeUpdates(fallbackLocationListener)
        } catch (_: Exception) {}
    }

    fun processFreshLocation(location: Location, providerTag: String) {
        val now = System.currentTimeMillis()
        val locTime = location.time
        val ageSeconds = if (locTime > 0) (now - locTime) / 1000L else 0L

        // Quality Gate: Check for stale location
        val isStale = ageSeconds > MAX_LOCATION_AGE_SECONDS
        val isAccuracyAcceptable = location.hasAccuracy() && location.accuracy <= MAX_ACCEPTABLE_LOCATION_ACCURACY_METERS

        val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }

        val gpsAvailable = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        val netAvailable = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        val diag = LocationDiagnosticsInfo(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else 0f,
            altitudeMeters = if (location.hasAltitude()) location.altitude else 0.0,
            speedMps = if (location.hasSpeed()) location.speed else 0f,
            bearingDegrees = if (location.hasBearing()) location.bearing else 0f,
            timestampEpochMs = locTime,
            ageSeconds = ageSeconds,
            provider = providerTag,
            isGpsEnabled = gpsAvailable,
            isNetworkEnabled = netAvailable,
            permissionType = if (_locationState.value.isFinePermission) "FINE (Precise)" else "COARSE (Approximate)",
            isMockLocation = isMock,
            qualityGatePass = !isStale && isAccuracyAcceptable
        )

        val statusMsg = when {
            isStale -> "Location stale (${ageSeconds}s old)"
            !isAccuracyAcceptable -> "Low accuracy (±${location.accuracy.toInt()}m)"
            else -> "Fresh GPS signal (±${location.accuracy.toInt()}m)"
        }

        _locationState.value = _locationState.value.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else 0f,
            speedMps = if (location.hasSpeed()) location.speed else 0f,
            bearingDegrees = if (location.hasBearing()) location.bearing else 0f,
            altitudeMeters = if (location.hasAltitude()) location.altitude else 0.0,
            locationTimestamp = locTime,
            locationAgeSeconds = ageSeconds,
            provider = providerTag,
            isGpsActive = !isStale && isAccuracyAcceptable,
            isMock = isMock,
            statusMessage = statusMsg,
            isSimulationMode = false,
            diagnostics = diag
        )
    }

    fun setManualSimulationLocation(stationCode: String) {
        val found = IndianLocalRailwayDatabase.allStations.find { it.code.equals(stationCode, ignoreCase = true) }
        if (found != null) {
            setManualSimulationLocation(found)
        }
    }

    fun setManualSimulationLocation(station: RailwayStation) {
        val now = System.currentTimeMillis()
        val diag = LocationDiagnosticsInfo(
            latitude = station.latitude,
            longitude = station.longitude,
            accuracyMeters = 8f,
            altitudeMeters = 15.0,
            speedMps = 0f,
            bearingDegrees = 0f,
            timestampEpochMs = now,
            ageSeconds = 0L,
            provider = "simulation",
            isGpsEnabled = true,
            isNetworkEnabled = true,
            permissionType = "SIMULATED",
            isMockLocation = true,
            qualityGatePass = true
        )

        _locationState.value = UserLocationInfo(
            latitude = station.latitude,
            longitude = station.longitude,
            accuracyMeters = 8f,
            speedMps = 0f,
            bearingDegrees = 0f,
            altitudeMeters = 15.0,
            locationTimestamp = now,
            locationAgeSeconds = 0L,
            provider = "simulation",
            isGpsActive = true,
            hasPermission = true,
            isFinePermission = true,
            isMock = true,
            nearestStation = station,
            distanceToStationKm = 0.05,
            isNearStation = true,
            stationConfidence = StationConfidence.HIGH,
            statusMessage = "Simulated at ${station.nameEn} (${station.code})",
            currentPlatform = station.platforms.firstOrNull() ?: "PF 1",
            isSimulationMode = true,
            diagnostics = diag
        )
    }

    fun updateStationMatch(
        station: RailwayStation?,
        distanceKm: Double,
        isNear: Boolean,
        confidence: StationConfidence
    ) {
        _locationState.value = _locationState.value.copy(
            nearestStation = station,
            distanceToStationKm = distanceKm,
            isNearStation = isNear,
            stationConfidence = confidence,
            currentPlatform = station?.platforms?.firstOrNull() ?: "PF 1"
        )
    }
}

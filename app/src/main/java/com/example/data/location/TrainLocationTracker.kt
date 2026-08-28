package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.example.data.model.IndianLocalRailwayDatabase
import com.example.data.model.RailwayStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class UserLocationInfo(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float = 0f,
    val speedMps: Float = 0f,
    val isGpsActive: Boolean = false,
    val hasPermission: Boolean = false,
    val nearestStation: RailwayStation? = null,
    val distanceToStationKm: Double = 0.0,
    val isNearStation: Boolean = false,
    val statusMessage: String = "Ready",
    val currentPlatform: String = "PF 1",
    val isSimulationMode: Boolean = false
)

class TrainLocationTracker(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _locationState = MutableStateFlow(
        UserLocationInfo(
            statusMessage = "Location standby"
        )
    )
    val locationState: StateFlow<UserLocationInfo> = _locationState.asStateFlow()

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateFromLocation(
                lat = location.latitude,
                lng = location.longitude,
                accuracy = location.accuracy,
                speed = location.speed,
                hasPerm = true
            )
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun updatePermissionStatus(granted: Boolean) {
        if (!granted) {
            _locationState.value = _locationState.value.copy(
                hasPermission = false,
                statusMessage = "Location permission not granted."
            )
            return
        }

        _locationState.value = _locationState.value.copy(hasPermission = true)
        startTracking()
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (locationManager == null) return
        try {
            val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = lastGps ?: lastNetwork

            if (best != null) {
                updateFromLocation(
                    lat = best.latitude,
                    lng = best.longitude,
                    accuracy = best.accuracy,
                    speed = best.speed,
                    hasPerm = true
                )
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    8000L,
                    15f,
                    locationListener
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    8000L,
                    15f,
                    locationListener
                )
            }
        } catch (e: SecurityException) {
            _locationState.value = _locationState.value.copy(
                hasPermission = false,
                statusMessage = "Location permission required for station detection."
            )
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun stopTracking() {
        try {
            locationManager?.removeUpdates(locationListener)
        } catch (e: Exception) {
            // Safe ignore
        }
    }

    fun updateFromLocation(lat: Double, lng: Double, accuracy: Float = 0f, speed: Float = 0f, hasPerm: Boolean = true) {
        val stations = IndianLocalRailwayDatabase.allStations
        var minDistanceKm = Double.MAX_VALUE
        var closestStation: RailwayStation? = null

        for (st in stations) {
            val dist = calculateDistanceKm(lat, lng, st.latitude, st.longitude)
            if (dist < minDistanceKm) {
                minDistanceKm = dist
                closestStation = st
            }
        }

        // Near station threshold: 800 meters
        val isNear = minDistanceKm <= 0.8

        val message = if (isNear && closestStation != null) {
            "Near ${closestStation.nameEn} (~${(minDistanceKm * 1000).toInt()}m)"
        } else if (closestStation != null) {
            "Off-track (~${String.format(java.util.Locale.US, "%.1f", minDistanceKm)} km from ${closestStation.nameEn})"
        } else {
            "Off-track / Standby"
        }

        _locationState.value = UserLocationInfo(
            latitude = lat,
            longitude = lng,
            accuracyMeters = accuracy,
            speedMps = speed,
            isGpsActive = true,
            hasPermission = hasPerm,
            nearestStation = closestStation,
            distanceToStationKm = minDistanceKm,
            isNearStation = isNear,
            statusMessage = message,
            currentPlatform = closestStation?.platforms?.firstOrNull() ?: "PF 1",
            isSimulationMode = false
        )
    }

    fun setManualSimulationLocation(stationCode: String) {
        val station = IndianLocalRailwayDatabase.allStations.find { it.code.equals(stationCode, ignoreCase = true) }
            ?: IndianLocalRailwayDatabase.allStations[0]

        _locationState.value = UserLocationInfo(
            latitude = station.latitude,
            longitude = station.longitude,
            accuracyMeters = 5f,
            speedMps = 0f,
            isGpsActive = true,
            hasPermission = true,
            nearestStation = station,
            distanceToStationKm = 0.05,
            isNearStation = true,
            statusMessage = "Simulated at ${station.nameEn} (${station.code})",
            currentPlatform = station.platforms.firstOrNull() ?: "PF 1",
            isSimulationMode = true
        )
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

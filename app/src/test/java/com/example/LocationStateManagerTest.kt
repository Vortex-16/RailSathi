package com.example

import com.example.data.location.LocationProviderDelegate
import com.example.data.location.LocationServiceState
import com.example.data.location.LocationStateManager
import com.example.data.location.UserTravelStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeLocationProviderDelegate : LocationProviderDelegate {
    var isTracking = false
    var lastStopReason: String? = null
    var fineGranted = false
    var coarseGranted = false
    var lastStatusMessage: String? = null

    override fun startTracking() {
        isTracking = true
    }

    override fun stopTracking(reason: String) {
        isTracking = false
        lastStopReason = reason
    }

    override fun updatePermissionStatus(fineGranted: Boolean, coarseGranted: Boolean) {
        this.fineGranted = fineGranted
        this.coarseGranted = coarseGranted
    }

    override fun updateStatusMessage(message: String) {
        lastStatusMessage = message
    }
}

class LocationStateManagerTest {

    private lateinit var fakeProvider: FakeLocationProviderDelegate
    private lateinit var manager: LocationStateManager

    @Before
    fun setUp() {
        fakeProvider = FakeLocationProviderDelegate()
        manager = LocationStateManager(
            locationProvider = fakeProvider,
            initialForeground = true,
            initialTravelStatus = UserTravelStatus.STATIONARY,
            initialPermission = false,
            initialServicesEnabled = true
        )
    }

    @Test
    fun `initial state is stationary and GPS is paused`() {
        assertFalse("GPS should not be tracking initially without permission and active travel", manager.isTrackingGps.value)
        assertEquals(LocationServiceState.PAUSED_NO_PERMISSION, manager.serviceState.value)
    }

    @Test
    fun `granting permission while stationary still keeps GPS restricted`() {
        manager.updatePermissionStatus(granted = true)

        assertFalse("GPS must remain restricted when user is stationary", manager.isTrackingGps.value)
        assertFalse(fakeProvider.isTracking)
        assertEquals(LocationServiceState.PAUSED_INACTIVE_TRAVEL, manager.serviceState.value)
        assertTrue(manager.statusMessage.value.contains("stationary", ignoreCase = true))
    }

    @Test
    fun `activating travel in foreground with permission triggers GPS tracking on`() {
        manager.updatePermissionStatus(granted = true)
        manager.setUserTravelStatus(UserTravelStatus.ACTIVE_TRAVEL)

        assertTrue("GPS must be tracking when in foreground and active travel", manager.isTrackingGps.value)
        assertTrue("Provider startTracking must be called", fakeProvider.isTracking)
        assertEquals(LocationServiceState.ACTIVE, manager.serviceState.value)
    }

    @Test
    fun `moving app to background restricts GPS updates immediately`() {
        // First activate tracking
        manager.updatePermissionStatus(granted = true)
        manager.setUserTravelStatus(UserTravelStatus.ACTIVE_TRAVEL)
        assertTrue(manager.isTrackingGps.value)

        // Minimize app to background
        manager.setAppForeground(false)

        assertFalse("GPS updates must be paused when app is in background", manager.isTrackingGps.value)
        assertFalse("Provider stopTracking must be called", fakeProvider.isTracking)
        assertEquals(LocationServiceState.PAUSED_BACKGROUND, manager.serviceState.value)
        assertTrue(manager.statusMessage.value.contains("background", ignoreCase = true))
    }

    @Test
    fun `returning app to foreground resumes GPS updates if active travel is set`() {
        manager.updatePermissionStatus(granted = true)
        manager.setUserTravelStatus(UserTravelStatus.ACTIVE_TRAVEL)
        manager.setAppForeground(false)
        assertFalse(manager.isTrackingGps.value)

        // Bring app back to foreground
        manager.setAppForeground(true)

        assertTrue("GPS updates must resume when app returns to foreground", manager.isTrackingGps.value)
        assertTrue(fakeProvider.isTracking)
        assertEquals(LocationServiceState.ACTIVE, manager.serviceState.value)
    }

    @Test
    fun `setting travel status to stationary stops GPS updates`() {
        manager.updatePermissionStatus(granted = true)
        manager.setUserTravelStatus(UserTravelStatus.ACTIVE_TRAVEL)
        assertTrue(manager.isTrackingGps.value)

        // User finishes travel
        manager.setUserTravelStatus(UserTravelStatus.STATIONARY)

        assertFalse("GPS updates must stop when user is stationary", manager.isTrackingGps.value)
        assertFalse(fakeProvider.isTracking)
        assertEquals(LocationServiceState.PAUSED_INACTIVE_TRAVEL, manager.serviceState.value)
    }

    @Test
    fun `toggling location services switch turns GPS on and off`() {
        manager.updatePermissionStatus(granted = true)
        manager.setUserTravelStatus(UserTravelStatus.ACTIVE_TRAVEL)
        assertTrue(manager.isTrackingGps.value)

        // Toggle master switch OFF
        manager.toggleLocationServices(false)

        assertFalse("GPS must pause when location services are toggled off", manager.isTrackingGps.value)
        assertFalse(fakeProvider.isTracking)
        assertEquals(LocationServiceState.PAUSED_MANUAL, manager.serviceState.value)

        // Toggle master switch back ON
        manager.toggleLocationServices(true)

        assertTrue("GPS must re-engage when location services are toggled on", manager.isTrackingGps.value)
        assertTrue(fakeProvider.isTracking)
        assertEquals(LocationServiceState.ACTIVE, manager.serviceState.value)
    }

    @Test
    fun `toggleUserTravelStatus flips between active travel and stationary`() {
        manager.updatePermissionStatus(granted = true)
        assertEquals(UserTravelStatus.STATIONARY, manager.userTravelStatus.value)

        manager.toggleUserTravelStatus()
        assertEquals(UserTravelStatus.ACTIVE_TRAVEL, manager.userTravelStatus.value)
        assertTrue(manager.isTrackingGps.value)

        manager.toggleUserTravelStatus()
        assertEquals(UserTravelStatus.STATIONARY, manager.userTravelStatus.value)
        assertFalse(manager.isTrackingGps.value)
    }
}

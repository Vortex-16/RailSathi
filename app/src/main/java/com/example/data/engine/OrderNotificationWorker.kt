package com.example.data.engine

import com.example.data.local.AppDatabase
import com.example.data.local.FoodRequestEntity
import com.example.data.model.OrderStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

data class OrderNotification(
    val notificationId: String,
    val clientRequestId: String,
    val requestId: Long,
    val trainNumber: String,
    val coachNumber: String,
    val targetStationCode: String,
    val foodItemName: String,
    val quantity: Int,
    val passengerName: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class NotificationDispatchResult {
    data class Dispatched(val recipientCount: Int, val notification: OrderNotification) : NotificationDispatchResult()
    data class DuplicateIgnored(val notificationId: String) : NotificationDispatchResult()
    data class Suppressed(val reason: String) : NotificationDispatchResult()
}

/**
 * Production notification dispatch and idempotency worker for RailSaathi.
 * Implements scoped notifications, message deduplication, retry safety,
 * and offline catch-up synchronization.
 */
class OrderNotificationWorker(
    private val db: AppDatabase,
    private val matchingService: HawkerMatchingService = HawkerMatchingService(db)
) {
    private val foodRequestDao = db.foodRequestDao()
    private val vendorDao = db.vendorDao()

    // Deduplication registry (Test A-08, A-09: duplicate network retry prevention)
    private val processedNotificationIds: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

    private val _incomingNotifications = MutableSharedFlow<OrderNotification>(extraBufferCapacity = 64)
    val incomingNotifications: SharedFlow<OrderNotification> = _incomingNotifications.asSharedFlow()

    /**
     * Dispatches an order notification to eligible vendors.
     * Enforces coach and train scoping (Scenario B & C).
     */
    suspend fun dispatchOrderNotification(request: FoodRequestEntity): NotificationDispatchResult = withContext(Dispatchers.IO) {
        val notificationId = "notif_${request.clientRequestId}_${request.id}"

        // Deduplication check
        if (processedNotificationIds.contains(notificationId)) {
            return@withContext NotificationDispatchResult.DuplicateIgnored(notificationId)
        }

        val currentStatus = try {
            OrderStatus.valueOf(request.status)
        } catch (_: Exception) {
            OrderStatus.REQUESTED
        }

        // Only dispatch for active pending requests
        if (!OrderStateMachine.canVendorClaim(currentStatus)) {
            return@withContext NotificationDispatchResult.Suppressed("Request is in non-claimable status ${request.status}")
        }

        val normalizedCoach = matchingService.normalizeCoach(request.coachNumber)
        val criteria = HawkerEligibilityCriteria(
            trainNumber = request.trainNumber,
            targetCoach = normalizedCoach,
            targetStationCode = request.targetStationCode,
            foodItemId = request.foodItemId,
            strictCoachScoping = true
        )

        val eligibleVendors = matchingService.findEligibleVendors(criteria)

        val notification = OrderNotification(
            notificationId = notificationId,
            clientRequestId = request.clientRequestId,
            requestId = request.id,
            trainNumber = request.trainNumber,
            coachNumber = normalizedCoach,
            targetStationCode = request.targetStationCode,
            foodItemName = request.foodItemName,
            quantity = request.quantity,
            passengerName = request.passengerName,
            timestamp = System.currentTimeMillis()
        )

        processedNotificationIds.add(notificationId)
        _incomingNotifications.tryEmit(notification)

        NotificationDispatchResult.Dispatched(
            recipientCount = eligibleVendors.size,
            notification = notification
        )
    }

    /**
     * Offline Reconnection Catch-Up (Scenario C Test 13).
     * When a vendor reconnects, delivers eligible pending work matching their train & coach.
     */
    suspend fun fetchPendingWorkForReconnectedVendor(
        trainNumber: String,
        coachNumber: String
    ): List<FoodRequestEntity> = withContext(Dispatchers.IO) {
        val normalizedCoach = matchingService.normalizeCoach(coachNumber)
        val pendingRequests = foodRequestDao.getPendingRequestsByTrainAndCoach(trainNumber, normalizedCoach)

        // Filter out expired or invalid requests
        val now = System.currentTimeMillis()
        pendingRequests.filter { req ->
            req.expiresAt > now && (req.status == OrderStatus.REQUESTED.name || req.status == OrderStatus.OFFERED_TO_VENDOR.name)
        }
    }

    /**
     * Clears deduplication cache (e.g. on shift change or testing reset).
     */
    fun clearCache() {
        processedNotificationIds.clear()
    }
}

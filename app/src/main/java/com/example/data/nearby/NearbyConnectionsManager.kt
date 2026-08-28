package com.example.data.nearby

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID

enum class P2PMessageType {
    NEARBY_FOOD_REQUEST,
    NEARBY_VENDOR_ACCEPT,
    NEARBY_PRICE_OFFER,
    NEARBY_CUSTOMER_CONFIRM,
    NEARBY_ORDER_COMPLETED
}

data class P2PMessage(
    val messageId: String,
    val senderDeviceId: String,
    val type: P2PMessageType,
    val requestId: String,
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Nearby Connections Manager for Passenger <-> Vendor P2P Local Interaction
 * Enables offline discovery, local food request delivery, price negotiation, and confirmation.
 */
class NearbyConnectionsManager(private val context: Context) {
    private val TAG = "NearbyConnections"
    private val SERVICE_ID = "com.example.railsathi.p2p"

    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _connectedEndpoints = MutableStateFlow<Set<String>>(emptySet())
    val connectedEndpoints: StateFlow<Set<String>> = _connectedEndpoints.asStateFlow()

    private val _incomingMessages = MutableStateFlow<P2PMessage?>(null)
    val incomingMessages: StateFlow<P2PMessage?> = _incomingMessages.asStateFlow()

    fun startAdvertising(roleName: String, coach: String) {
        // In real environment, uses Nearby.getConnectionsClient(context).startAdvertising(...)
        _isAdvertising.value = true
        Log.d(TAG, "Started local advertising as $roleName in coach $coach")
    }

    fun stopAdvertising() {
        _isAdvertising.value = false
        Log.d(TAG, "Stopped local advertising")
    }

    fun startDiscovery() {
        _isDiscovering.value = true
        Log.d(TAG, "Started local discovery for nearby vendors/passengers")
    }

    fun stopDiscovery() {
        _isDiscovering.value = false
        Log.d(TAG, "Stopped local discovery")
    }

    fun broadcastLocalFoodRequest(
        requestId: String,
        foodItemId: String,
        foodItemName: String,
        quantity: Int,
        coach: String,
        deviceId: String
    ) {
        val payload = JSONObject().apply {
            put("requestId", requestId)
            put("foodItemId", foodItemId)
            put("foodItemName", foodItemName)
            put("quantity", quantity)
            put("coach", coach)
        }.toString()

        val msg = P2PMessage(
            messageId = UUID.randomUUID().toString(),
            senderDeviceId = deviceId,
            type = P2PMessageType.NEARBY_FOOD_REQUEST,
            requestId = requestId,
            payloadJson = payload
        )

        _incomingMessages.value = msg
        Log.d(TAG, "Dispatched P2P message: ${msg.type} for request $requestId")
    }

    fun sendPriceOffer(
        requestId: String,
        vendorId: String,
        unitPrice: Int,
        totalPrice: Int,
        coach: String
    ) {
        val payload = JSONObject().apply {
            put("requestId", requestId)
            put("vendorId", vendorId)
            put("unitPrice", unitPrice)
            put("totalPrice", totalPrice)
            put("coach", coach)
        }.toString()

        val msg = P2PMessage(
            messageId = UUID.randomUUID().toString(),
            senderDeviceId = vendorId,
            type = P2PMessageType.NEARBY_PRICE_OFFER,
            requestId = requestId,
            payloadJson = payload
        )

        _incomingMessages.value = msg
    }

    fun sendCustomerConfirm(requestId: String, customerId: String) {
        val payload = JSONObject().apply {
            put("requestId", requestId)
            put("customerId", customerId)
        }.toString()

        val msg = P2PMessage(
            messageId = UUID.randomUUID().toString(),
            senderDeviceId = customerId,
            type = P2PMessageType.NEARBY_CUSTOMER_CONFIRM,
            requestId = requestId,
            payloadJson = payload
        )

        _incomingMessages.value = msg
    }
}

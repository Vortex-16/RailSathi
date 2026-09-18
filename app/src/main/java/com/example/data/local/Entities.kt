package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val phone: String,
    val role: String, // "TRAVELER", "VENDOR", "GUEST"
    val languageCode: String = "hi",
    val isSeniorMode: Boolean = false,
    val defaultTrain: String = "EMU 31821 (Sealdah - Ranaghat)",
    val defaultCoach: String = "GS-2",
    val monthlyBudgetLimit: Double = 1500.0,
    val sessionToken: String = ""
)

@Entity(tableName = "food_requests")
data class FoodRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientRequestId: String = "",
    val passengerName: String,
    val trainNumber: String,
    val trainName: String,
    val coachNumber: String,
    val seatDetail: String = "",
    val foodItemId: String,
    val foodItemName: String,
    val quantity: Int = 1,
    val price: Int = 0, // Fallback / displayed
    val offeredUnitPrice: Int? = null,
    val calculatedTotalPrice: Int? = null,
    val status: String = "REQUESTED", // "REQUESTED", "MATCHING", "OFFERED_TO_VENDOR", "VENDOR_ACCEPTED", "PRICE_CONFIRMED", "CUSTOMER_CONFIRMED", "FULFILLING", "COMPLETED", "REJECTED", "EXPIRED", "CUSTOMER_CANCELLED"
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 5 * 60 * 1000,
    val assignedVendorId: String? = null,
    val assignedVendorName: String? = null,
    val isDeliveredPaid: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val clientOrderId: String,
    val requestId: Long,
    val customerId: String,
    val vendorId: String,
    val trainNumber: String,
    val coachNumber: String,
    val foodItemName: String,
    val quantity: Int,
    val unitPrice: Int,
    val totalPrice: Int,
    val status: String = "CUSTOMER_CONFIRMED",
    val paymentStatus: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val idempotencyKey: String,
    val operationType: String,
    val payloadJson: String,
    val status: String = "PENDING", // PENDING, SYNCED, FAILED
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey val vendorId: String,
    val name: String,
    val badgeNumber: String,
    val specialityItemId: String,
    val specialityItemName: String,
    val currentTrain: String,
    val currentCoach: String,
    val currentStation: String,
    val todaySalesCount: Int = 0,
    val todayEarnings: Double = 0.0,
    val isOnline: Boolean = true,
    val lastSaleTimestamp: Long = 0L
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String,
    val coach: String,
    val trainNumber: String,
    val note: String = ""
)

@Entity(tableName = "sale_records")
data class SaleRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: String,
    val foodItemName: String,
    val amount: Double,
    val coachNumber: String,
    val trainNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String
)

@Entity(tableName = "journey_sessions")
data class JourneySessionEntity(
    @PrimaryKey val journeyId: String,
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
    val status: String, // "IDLE", "PLANNED", "ACTIVE", "PAUSED", "COMPLETED", "CANCELLED"
    val confidence: Int = 100,
    val lastLocationUpdate: Long = System.currentTimeMillis(),
    val isLiveTracking: Boolean = true,
    val trackingSource: String = "GPS"
)

@Entity(tableName = "trains")
data class TrainEntity(
    @PrimaryKey val trainNumber: String,
    val trainName: String,
    val originStationCode: String,
    val originStationName: String,
    val destStationCode: String,
    val destStationName: String,
    val departureTime: String,
    val arrivalTime: String = "",
    val platform: String = "PF 1",
    val type: String = "EMU Local",
    val zone: String = "Eastern Railway",
    val coachCodes: String = "CAB-1,LD-1,VND-1,GS-1,GS-2,GS-3,VND-2,LD-2,CAB-2",
    val stopsJson: String = "",
    val isLiveApiAvailable: Boolean = true,
    val delayMinutes: Int = 0,
    val statusSummary: String = "On Time",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val code: String,
    val nameEn: String,
    val nameHi: String = "",
    val nameBn: String = "",
    val zone: String = "Eastern Railway",
    val latitude: Double = 22.5697,
    val longitude: Double = 88.3712,
    val lastUpdated: Long = System.currentTimeMillis()
)


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
    val defaultCoach: String = "C-4",
    val monthlyBudgetLimit: Double = 1500.0
)

@Entity(tableName = "food_requests")
data class FoodRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val passengerName: String,
    val trainNumber: String,
    val trainName: String,
    val coachNumber: String,
    val seatDetail: String,
    val foodItemId: String,
    val foodItemName: String,
    val price: Int,
    val status: String, // "PENDING", "ASSIGNED", "IN_TRANSIT", "DELIVERED", "CANCELLED"
    val timestamp: Long = System.currentTimeMillis(),
    val assignedVendorId: String? = null,
    val assignedVendorName: String? = null,
    val isDeliveredPaid: Boolean = false
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


package com.example.data.repository

import android.content.Context
import com.example.AppConfig
import com.example.data.local.AppDatabase
import com.example.data.local.ExpenseEntity
import com.example.data.local.FoodRequestEntity
import com.example.data.local.OrderEntity
import com.example.data.local.SaleRecordEntity
import com.example.data.local.UserEntity
import com.example.data.local.VendorEntity
import com.example.data.model.FoodItem
import com.example.data.model.IndianLocalRailwayDatabase
import com.example.data.model.OrderStatus
import com.example.data.nearby.NearbyConnectionsManager
import com.example.data.remote.AcceptRequestPayload
import com.example.data.remote.ApiClient
import com.example.data.remote.ConfirmOrderPayload
import com.example.data.remote.CreateFoodRequestPayload
import com.example.data.remote.OfferPricePayload
import com.example.data.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class CollisionCheckResult {
    data class Allowed(val coachNumber: String, val message: String) : CollisionCheckResult()
    data class Conflict(
        val coachNumber: String,
        val conflictingVendorName: String,
        val itemName: String,
        val recommendedAlternativeCoach: String
    ) : CollisionCheckResult()
}

data class TrainRouteDetails(
    val trainNumber: String,
    val trainName: String,
    val stations: List<String>,
    var currentStationIndex: Int = 0,
    val currentPlatform: String = "PF 1",
    val nextStationHaltSecs: Int = 45,
    val coachCodes: List<String> = listOf("CAB-1", "LD-1", "VND-1", "GS-1", "GS-2", "GS-3", "VND-2", "LD-2", "CAB-2")
)

class RailSathiRepository(
    private val db: AppDatabase,
    val railwayDataProvider: RailwayDataProvider = HybridRailwayDataProvider(),
    context: Context? = null
) {
    private val userDao = db.userDao()
    private val foodRequestDao = db.foodRequestDao()
    private val orderDao = db.orderDao()
    private val vendorDao = db.vendorDao()
    private val expenseDao = db.expenseDao()
    private val saleRecordDao = db.saleRecordDao()
    private val journeyDao = db.journeySessionDao()

    val syncManager = SyncManager(db)
    val nearbyManager = context?.let { NearbyConnectionsManager(it) }

    val availableRoutes = IndianLocalRailwayDatabase.allSchedules.map { sched ->
        TrainRouteDetails(
            trainNumber = sched.trainNumber,
            trainName = sched.trainName,
            stations = sched.stops.map { "${it.stationName} (${it.stationCode})" },
            currentStationIndex = 0,
            currentPlatform = sched.stops.firstOrNull()?.platform ?: "PF 1",
            coachCodes = sched.coaches.map { it.coachCode }
        )
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultVendorsIfNeeded()
        }
    }

    private suspend fun seedDefaultVendorsIfNeeded() {
        val count = vendorDao.getVendorByIdDirect("vendor_jhalmuri_1")
        if (count == null) {
            val sampleVendors = listOf(
                VendorEntity(
                    vendorId = "vendor_jhalmuri_1",
                    name = "Subhash Da (ঝালমুড়ি)",
                    badgeNumber = "ER-SDAH-104",
                    specialityItemId = "jhalmuri_kol",
                    specialityItemName = "Kolkata Jhalmuri",
                    currentTrain = "31821",
                    currentCoach = "VND-1",
                    currentStation = "Barrackpore",
                    todaySalesCount = 2,
                    todayEarnings = 40.0,
                    isOnline = true,
                    lastSaleTimestamp = System.currentTimeMillis() - 1800000
                ),
                VendorEntity(
                    vendorId = "vendor_badam_2",
                    name = "Haradhan Kaka (বাদাম)",
                    badgeNumber = "ER-SDAH-219",
                    specialityItemId = "badam_roasted",
                    specialityItemName = "Roasted Peanuts (Badam)",
                    currentTrain = "31821",
                    currentCoach = "GS-2",
                    currentStation = "Barrackpore",
                    todaySalesCount = 1,
                    todayEarnings = 15.0,
                    isOnline = true,
                    lastSaleTimestamp = System.currentTimeMillis() - 3600000
                ),
                VendorEntity(
                    vendorId = "vendor_fruits_3",
                    name = "Anand Bhai (Fruits)",
                    badgeNumber = "MUM-WR-088",
                    specialityItemId = "fresh_cut_fruits",
                    specialityItemName = "Fresh Cut Fruits",
                    currentTrain = "31821",
                    currentCoach = "GS-1",
                    currentStation = "Dum Dum Jn",
                    todaySalesCount = 0,
                    todayEarnings = 0.0,
                    isOnline = true,
                    lastSaleTimestamp = 0L
                ),
                VendorEntity(
                    vendorId = "vendor_chai_4",
                    name = "Shankar Chaiwala (चाय)",
                    badgeNumber = "DL-NR-312",
                    specialityItemId = "masala_chai",
                    specialityItemName = "Cutting Masala Chai",
                    currentTrain = "31821",
                    currentCoach = "VND-2",
                    currentStation = "Barrackpore",
                    todaySalesCount = 4,
                    todayEarnings = 40.0,
                    isOnline = true,
                    lastSaleTimestamp = System.currentTimeMillis() - 900000
                ),
                VendorEntity(
                    vendorId = "vendor_vada_5",
                    name = "Ganesh Rao (वड़ा पाव)",
                    badgeNumber = "CR-CSMT-419",
                    specialityItemId = "vada_pav_mum",
                    specialityItemName = "Mumbai Vada Pav",
                    currentTrain = "31821",
                    currentCoach = "GS-3",
                    currentStation = "Dum Dum Jn",
                    todaySalesCount = 5,
                    todayEarnings = 100.0,
                    isOnline = true,
                    lastSaleTimestamp = System.currentTimeMillis() - 600000
                ),
                VendorEntity(
                    vendorId = "vendor_chana_6",
                    name = "Santosh Ji (चना जोर)",
                    badgeNumber = "ER-HWH-705",
                    specialityItemId = "chana_jor",
                    specialityItemName = "Spicy Chana Jor Garam",
                    currentTrain = "31821",
                    currentCoach = "VND-1",
                    currentStation = "Barrackpore",
                    todaySalesCount = 0,
                    todayEarnings = 0.0,
                    isOnline = true,
                    lastSaleTimestamp = 0L
                )
            )
            vendorDao.insertVendors(sampleVendors)
        }
    }

    // User Profile
    val activeUserFlow: Flow<UserEntity?> = userDao.getActiveUser()

    suspend fun getActiveUser(): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getActiveUserDirect()
    }

    suspend fun saveUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    // Food Requests Flow
    val activeRequestsFlow: Flow<List<FoodRequestEntity>> = foodRequestDao.getActiveRequests()
    val allRequestsFlow: Flow<List<FoodRequestEntity>> = foodRequestDao.getAllRequests()

    fun getRequestsForVendor(vendorId: String): Flow<List<FoodRequestEntity>> {
        return foodRequestDao.getRequestsForVendor(vendorId)
    }

    // Smart Fair Distribution Request Creation with NO predetermined price
    suspend fun createAndDispatchFoodRequest(
        passengerName: String,
        trainNumber: String,
        trainName: String,
        coachNumber: String,
        seatDetail: String,
        foodItem: FoodItem,
        quantity: Int = 1,
        note: String = ""
    ): FoodRequestEntity = withContext(Dispatchers.IO) {
        val clientRequestId = "req_${UUID.randomUUID()}"
        val validQty = quantity.coerceIn(1, AppConfig.MAX_ITEM_QUANTITY)

        val coachVendors = db.vendorDao().getVendorsInCoach(trainNumber, coachNumber)
        val eligibleVendors = if (coachVendors.isNotEmpty()) {
            coachVendors.filter { it.specialityItemId == foodItem.id || it.isOnline }
        } else {
            emptyList()
        }

        // Fair income score
        val bestVendor = eligibleVendors.minByOrNull { vendor ->
            val salesScore = vendor.todaySalesCount * 10
            val recencyPenalty = if (vendor.lastSaleTimestamp > 0) {
                ((System.currentTimeMillis() - vendor.lastSaleTimestamp) / 60000).toInt()
            } else 999
            salesScore - recencyPenalty
        }

        val request = FoodRequestEntity(
            clientRequestId = clientRequestId,
            passengerName = passengerName.ifBlank { "Traveler in $coachNumber" },
            trainNumber = trainNumber,
            trainName = trainName,
            coachNumber = coachNumber,
            seatDetail = seatDetail,
            foodItemId = foodItem.id,
            foodItemName = foodItem.nameEn,
            quantity = validQty,
            price = 0, // Customer NEVER sets the price
            offeredUnitPrice = null,
            calculatedTotalPrice = null,
            status = if (bestVendor != null) OrderStatus.OFFERED_TO_VENDOR.name else OrderStatus.REQUESTED.name,
            timestamp = System.currentTimeMillis(),
            assignedVendorId = bestVendor?.vendorId,
            assignedVendorName = bestVendor?.name
        )

        val insertedId = foodRequestDao.insertRequest(request)
        val insertedRequest = request.copy(id = insertedId)

        // Try Remote API if online
        try {
            val payload = CreateFoodRequestPayload(
                clientRequestId = clientRequestId,
                customerId = passengerName,
                journeyId = "active_journey",
                trainNumber = trainNumber,
                coachNumber = coachNumber,
                foodItemId = foodItem.id,
                foodItemName = foodItem.nameEn,
                quantity = validQty,
                note = note
            )
            ApiClient.apiService.createFoodRequest(clientRequestId, payload)
        } catch (_: Exception) {
            // Queue for offline sync
            val jsonObj = JSONObject().apply {
                put("clientRequestId", clientRequestId)
                put("passengerName", passengerName)
                put("trainNumber", trainNumber)
                put("coachNumber", coachNumber)
                put("foodItemId", foodItem.id)
                put("foodItemName", foodItem.nameEn)
                put("quantity", validQty)
            }
            syncManager.queueOfflineOperation(clientRequestId, "CREATE_REQUEST", jsonObj)
        }

        // Broadcast to P2P Nearby Connections
        nearbyManager?.broadcastLocalFoodRequest(
            requestId = insertedId.toString(),
            foodItemId = foodItem.id,
            foodItemName = foodItem.nameEn,
            quantity = validQty,
            coach = coachNumber,
            deviceId = passengerName
        )

        insertedRequest
    }

    // Vendor Accepts Request and Chooses Unit Price from ALLOWED_UNIT_PRICES
    suspend fun vendorAcceptAndOfferPrice(
        requestId: Long,
        vendorId: String,
        unitPrice: Int
    ) = withContext(Dispatchers.IO) {
        if (!AppConfig.ALLOWED_UNIT_PRICES.contains(unitPrice)) {
            throw IllegalArgumentException("Unit price ₹$unitPrice is not allowed.")
        }

        val request = foodRequestDao.getRequestById(requestId) ?: return@withContext
        val totalPrice = request.quantity * unitPrice

        foodRequestDao.updatePriceConfirmation(requestId, unitPrice, totalPrice)
        foodRequestDao.updateRequestStatus(requestId, OrderStatus.PRICE_CONFIRMED.name, vendorId, "Vendor Accepted")

        // Sync to cloud
        try {
            ApiClient.apiService.offerPrice(
                requestId = requestId.toString(),
                payload = OfferPricePayload(vendorId = vendorId, unitPrice = unitPrice)
            )
        } catch (_: Exception) {
            val jsonObj = JSONObject().apply {
                put("requestId", requestId)
                put("vendorId", vendorId)
                put("unitPrice", unitPrice)
                put("totalPrice", totalPrice)
            }
            syncManager.queueOfflineOperation("price_$requestId", "OFFER_PRICE", jsonObj)
        }

        // Send P2P Price Offer
        nearbyManager?.sendPriceOffer(
            requestId = requestId.toString(),
            vendorId = vendorId,
            unitPrice = unitPrice,
            totalPrice = totalPrice,
            coach = request.coachNumber
        )
    }

    // Customer Confirms the Order after seeing the vendor price
    suspend fun customerConfirmOrder(
        requestId: Long,
        customerId: String
    ) = withContext(Dispatchers.IO) {
        val request = foodRequestDao.getRequestById(requestId) ?: return@withContext
        foodRequestDao.confirmOrderByCustomer(requestId)

        val orderId = "ord_${UUID.randomUUID()}"
        val order = OrderEntity(
            orderId = orderId,
            clientOrderId = orderId,
            requestId = requestId,
            customerId = customerId,
            vendorId = request.assignedVendorId ?: "vendor_nearby",
            trainNumber = request.trainNumber,
            coachNumber = request.coachNumber,
            foodItemName = request.foodItemName,
            quantity = request.quantity,
            unitPrice = request.offeredUnitPrice ?: 15,
            totalPrice = request.calculatedTotalPrice ?: (request.quantity * 15),
            status = OrderStatus.CUSTOMER_CONFIRMED.name
        )
        orderDao.insertOrder(order)

        try {
            ApiClient.apiService.confirmOrder(requestId.toString(), ConfirmOrderPayload(customerId))
        } catch (_: Exception) {
            val jsonObj = JSONObject().apply {
                put("orderId", orderId)
                put("requestId", requestId)
                put("customerId", customerId)
                put("totalPrice", order.totalPrice)
            }
            syncManager.queueOfflineOperation("confirm_$orderId", "CONFIRM_ORDER", jsonObj)
        }

        nearbyManager?.sendCustomerConfirm(requestId.toString(), customerId)
    }

    // Coach Collision Prevention Check
    suspend fun checkCoachCollision(
        vendorId: String,
        specialityItemId: String,
        trainNumber: String,
        targetCoachNumber: String
    ): CollisionCheckResult = withContext(Dispatchers.IO) {
        val existingInCoach = vendorDao.getVendorsInCoach(trainNumber, targetCoachNumber)
            .filter { it.vendorId != vendorId }

        val sameItemVendor = existingInCoach.find { it.specialityItemId == specialityItemId }

        if (sameItemVendor != null) {
            val alternativeCoach = findBestAlternativeCoach(trainNumber, specialityItemId, targetCoachNumber)
            CollisionCheckResult.Conflict(
                coachNumber = targetCoachNumber,
                conflictingVendorName = sameItemVendor.name,
                itemName = sameItemVendor.specialityItemName,
                recommendedAlternativeCoach = alternativeCoach
            )
        } else {
            CollisionCheckResult.Allowed(
                coachNumber = targetCoachNumber,
                message = "Coach $targetCoachNumber is clear! Diverse snacks allowed together."
            )
        }
    }

    private fun findBestAlternativeCoach(
        trainNumber: String,
        specialityItemId: String,
        occupiedCoach: String
    ): String {
        val coaches = listOf("VND-1", "GS-1", "GS-2", "GS-3", "VND-2")
        return coaches.firstOrNull { it != occupiedCoach } ?: "VND-2"
    }

    suspend fun updateVendorCoach(vendorId: String, coachNumber: String) = withContext(Dispatchers.IO) {
        vendorDao.updateVendorCoach(vendorId, coachNumber)
    }

    suspend fun completeDeliveryAndRecordSale(
        requestId: Long,
        vendorId: String,
        foodItemName: String,
        amount: Double,
        coachNumber: String,
        trainNumber: String,
        buyerName: String
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val dateString = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(now))

        if (requestId > 0) {
            foodRequestDao.updateRequestStatus(requestId, OrderStatus.COMPLETED.name, vendorId, "Delivered by Vendor")
        }

        // Record Vendor Sale
        vendorDao.recordVendorSale(vendorId, amount, now)
        saleRecordDao.insertSaleRecord(
            SaleRecordEntity(
                vendorId = vendorId,
                foodItemName = foodItemName,
                amount = amount,
                coachNumber = coachNumber,
                trainNumber = trainNumber,
                timestamp = now,
                dateString = dateString
            )
        )

        // Record Passenger Expense
        expenseDao.insertExpense(
            ExpenseEntity(
                title = "$foodItemName ($coachNumber)",
                category = "Train Snacks",
                amount = amount,
                timestamp = now,
                dateString = dateString,
                coach = coachNumber,
                trainNumber = trainNumber,
                note = "Served fresh by vendor in local train"
            )
        )
    }

    suspend fun cancelRequest(requestId: Long) = withContext(Dispatchers.IO) {
        foodRequestDao.updateRequestStatus(requestId, OrderStatus.CUSTOMER_CANCELLED.name, null, null)
    }

    // Vendors list
    val allVendorsFlow: Flow<List<VendorEntity>> = vendorDao.getAllVendors()

    fun getVendorById(vendorId: String): Flow<VendorEntity?> = vendorDao.getVendorById(vendorId)

    // Expenses & Budget
    val allExpensesFlow: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalSpentFlow: Flow<Double> = expenseDao.getTotalSpent().map { it ?: 0.0 }

    suspend fun addExpenseDirect(
        title: String,
        category: String,
        amount: Double,
        coach: String,
        trainNumber: String,
        note: String
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val dateString = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(now))
        expenseDao.insertExpense(
            ExpenseEntity(
                title = title,
                category = category,
                amount = amount,
                timestamp = now,
                dateString = dateString,
                coach = coach,
                trainNumber = trainNumber,
                note = note
            )
        )
    }

    fun getVendorSales(vendorId: String): Flow<List<SaleRecordEntity>> = saleRecordDao.getSalesByVendor(vendorId)
    fun getVendorTotalEarnings(vendorId: String): Flow<Double> = saleRecordDao.getTotalVendorSales(vendorId).map { it ?: 0.0 }
}

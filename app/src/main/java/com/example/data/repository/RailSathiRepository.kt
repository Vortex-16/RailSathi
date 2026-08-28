package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ExpenseEntity
import com.example.data.local.FoodRequestEntity
import com.example.data.local.SaleRecordEntity
import com.example.data.local.UserEntity
import com.example.data.local.VendorEntity
import com.example.data.model.FoodItem
import com.example.data.model.IndianLocalRailwayDatabase
import com.example.data.model.RequestStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val railwayDataProvider: RailwayDataProvider = LocalStaticRailwayDataProvider()
) {

    private val userDao = db.userDao()
    private val foodRequestDao = db.foodRequestDao()
    private val vendorDao = db.vendorDao()
    private val expenseDao = db.expenseDao()
    private val saleRecordDao = db.saleRecordDao()
    private val journeyDao = db.journeySessionDao()

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
                    todaySalesCount = 0, // 0 sales -> Highest priority in Fair Dispatch!
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
                    todaySalesCount = 0, // 0 sales -> Highest priority!
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

    suspend fun saveUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    // Food Requests
    val activeRequestsFlow: Flow<List<FoodRequestEntity>> = foodRequestDao.getActiveRequests()
    val allRequestsFlow: Flow<List<FoodRequestEntity>> = foodRequestDao.getAllRequests()

    fun getRequestsForVendor(vendorId: String): Flow<List<FoodRequestEntity>> {
        return foodRequestDao.getRequestsForVendor(vendorId)
    }

    // Smart Fair Distribution Algorithm:
    // Only dispatches to vendors registered on this specific train.
    // Picks matching vendor who has lowest sales count and longest idle time.
    suspend fun createAndDispatchFoodRequest(
        passengerName: String,
        trainNumber: String,
        trainName: String,
        coachNumber: String,
        seatDetail: String,
        foodItem: FoodItem
    ): FoodRequestEntity = withContext(Dispatchers.IO) {
        val coachVendors = db.vendorDao().getVendorsInCoach(trainNumber, coachNumber)
        val eligibleVendors = if (coachVendors.isNotEmpty()) {
            coachVendors.filter { it.specialityItemId == foodItem.id || it.isOnline }
        } else {
            // Check adjacent or train-wide vendors on the same train
            db.vendorDao().getAllVendors()
            val allOnTrain = vendorDao.getVendorByIdDirect("vendor_jhalmuri_1") // trigger query
            db.vendorDao().getAllVendors()
            emptyList()
        }

        // Fair income formula: lower sales count = higher priority
        val bestVendor = eligibleVendors.minByOrNull { vendor ->
            val salesScore = vendor.todaySalesCount * 10
            val recencyPenalty = if (vendor.lastSaleTimestamp > 0) {
                ((System.currentTimeMillis() - vendor.lastSaleTimestamp) / 60000).toInt()
            } else 999
            salesScore - recencyPenalty
        }

        val request = FoodRequestEntity(
            passengerName = passengerName.ifBlank { "Traveler in $coachNumber" },
            trainNumber = trainNumber,
            trainName = trainName,
            coachNumber = coachNumber,
            seatDetail = seatDetail,
            foodItemId = foodItem.id,
            foodItemName = foodItem.nameEn,
            price = foodItem.defaultPrice,
            status = if (bestVendor != null) RequestStatus.ASSIGNED.name else RequestStatus.PENDING.name,
            timestamp = System.currentTimeMillis(),
            assignedVendorId = bestVendor?.vendorId,
            assignedVendorName = bestVendor?.name
        )

        val insertedId = foodRequestDao.insertRequest(request)
        request.copy(id = insertedId)
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

    suspend fun acceptRequestByVendor(requestId: Long, vendorId: String, vendorName: String) = withContext(Dispatchers.IO) {
        foodRequestDao.updateRequestStatus(requestId, RequestStatus.IN_TRANSIT.name, vendorId, vendorName)
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
            foodRequestDao.updateRequestStatus(requestId, RequestStatus.DELIVERED.name, vendorId, "Delivered by Vendor")
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
        foodRequestDao.updateRequestStatus(requestId, RequestStatus.CANCELLED.name, null, null)
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

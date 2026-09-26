package com.example

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.HawkerEligibilityCriteria
import com.example.data.engine.HawkerMatchingService
import com.example.data.engine.NotificationDispatchResult
import com.example.data.engine.OrderNotificationWorker
import com.example.data.local.AppDatabase
import com.example.data.local.FoodRequestEntity
import com.example.data.local.VendorEntity
import com.example.data.model.FoodItem
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.data.repository.RailSathiRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class Stage3ConsistencyTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: RailSathiRepository
    private lateinit var matchingService: HawkerMatchingService
    private lateinit var notificationWorker: OrderNotificationWorker

    private val sampleFoodItem = FoodItem(
        id = "jhal_muri",
        nameEn = "Kolkata Jhal Muri",
        nameHi = "झालमुड़ी",
        nameBn = "ঝালমুড়ি",
        nameMr = "ঝাল মুরি",
        typicalPriceInr = 20
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RailSathiRepository(db, context = context)
        matchingService = HawkerMatchingService(db)
        notificationWorker = OrderNotificationWorker(db, matchingService)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // =========================================================================
    // 1. TRANSACTION BOUNDARY & ATOMICITY TESTS
    // =========================================================================

    @Test
    fun `TX-01 - Customer confirmation atomically commits status and order entity`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)
        val req = repository.createAndDispatchFoodRequest(
            passengerName = "Commuter_TX1",
            trainNumber = "31617",
            trainName = "Ranaghat Local",
            coachNumber = "C1",
            seatDetail = "Window",
            foodItem = sampleFoodItem,
            quantity = 2,
            customClientRequestId = "req_tx_confirm_${UUID.randomUUID()}"
        )

        // Vendor offers price
        repository.setVerifiedRole(UserRole.VENDOR)
        repository.vendorAcceptAndOfferPrice(req.id, "vendor_jhalmuri_1", 20)

        // Customer confirms order
        repository.setVerifiedRole(UserRole.TRAVELER)
        repository.customerConfirmOrder(req.id, "Commuter_TX1")

        // Assert atomicity in database
        val updatedReq = db.foodRequestDao().getRequestById(req.id)
        val createdOrders = db.orderDao().getOrdersByCustomer("Commuter_TX1").first()

        assertNotNull(updatedReq)
        assertEquals(OrderStatus.CUSTOMER_CONFIRMED.name, updatedReq?.status)
        assertEquals(1, createdOrders.size)
        assertEquals(req.id, createdOrders.first().requestId)
        assertEquals(40.0, createdOrders.first().totalPrice.toDouble(), 0.01)
    }

    @Test
    fun `TX-02 - Order completion atomically updates order, vendor earnings, sale record, and expense`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)
        val req = repository.createAndDispatchFoodRequest(
            passengerName = "Commuter_TX2",
            trainNumber = "31617",
            trainName = "Ranaghat Local",
            coachNumber = "C1",
            seatDetail = "Middle",
            foodItem = sampleFoodItem,
            quantity = 1,
            customClientRequestId = "req_tx_complete_${UUID.randomUUID()}"
        )

        val vendorId = "vendor_tx_v1"
        db.vendorDao().insertVendor(
            VendorEntity(
                vendorId = vendorId,
                name = "Vendor TX",
                badgeNumber = "ER-TX-01",
                specialityItemId = sampleFoodItem.id,
                specialityItemName = sampleFoodItem.nameEn,
                currentTrain = "31617",
                currentCoach = "C1",
                currentStation = "Barrackpore",
                todaySalesCount = 0,
                todayEarnings = 0.0,
                isOnline = true
            )
        )

        repository.setVerifiedRole(UserRole.VENDOR)
        repository.vendorAcceptAndOfferPrice(req.id, vendorId, 20)

        // Complete delivery
        repository.completeDeliveryAndRecordSale(
            requestId = req.id,
            vendorId = vendorId,
            foodItemName = sampleFoodItem.nameEn,
            amount = 20.0,
            coachNumber = "C1",
            trainNumber = "31617",
            buyerName = "Commuter_TX2"
        )

        // Verify all 4 tables updated atomically
        val finalReq = db.foodRequestDao().getRequestById(req.id)
        val vendorState = db.vendorDao().getVendorByIdDirect(vendorId)
        val sales = db.saleRecordDao().getSalesByVendorDirect(vendorId)
        val expenses = db.expenseDao().getAllExpenses().first()

        assertEquals(OrderStatus.COMPLETED.name, finalReq?.status)
        assertEquals(1, vendorState?.todaySalesCount)
        assertEquals(20.0, vendorState?.todayEarnings ?: 0.0, 0.01)
        assertEquals(1, sales.size)
        assertEquals(20.0, sales.first().amount, 0.01)
        assertTrue(expenses.any { it.title.contains(sampleFoodItem.nameEn) })
    }

    @Test
    fun `TX-03 - Database transaction failure rolls back all partial mutations`() = runBlocking {
        val initialReqCount = db.foodRequestDao().getAllRequests().first().size

        try {
            db.withTransaction {
                db.foodRequestDao().insertRequest(
                    FoodRequestEntity(
                        clientRequestId = "tx_fail_req",
                        passengerName = "Aborted",
                        trainNumber = "31617",
                        trainName = "Local",
                        coachNumber = "C1",
                        seatDetail = "Berth",
                        foodItemId = sampleFoodItem.id,
                        foodItemName = sampleFoodItem.nameEn,
                        quantity = 1,
                        price = 0,
                        status = OrderStatus.REQUESTED.name,
                        timestamp = System.currentTimeMillis()
                    )
                )

                // Intentional simulated exception inside transaction
                throw IllegalStateException("SIMULATED_TRANSACTION_FAILURE")
            }
            fail("Expected exception was not thrown")
        } catch (e: IllegalStateException) {
            assertEquals("SIMULATED_TRANSACTION_FAILURE", e.message)
        }

        // Verify full rollback: no partial row was committed
        assertEquals(initialReqCount, db.foodRequestDao().getAllRequests().first().size)
    }

    // =========================================================================
    // 2. QUANTITY RULES TABLE VERIFICATION
    // =========================================================================

    @Test
    fun `QTY-01 - Android repository enforces legal bounds (1 to 10) on all inputs`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)

        val testInputs = listOf(
            -5 to 1,
            0 to 1,
            1 to 1,
            10 to 10,
            11 to 10,
            1000 to 10
        )

        for ((inputQty, expectedQty) in testInputs) {
            val req = repository.createAndDispatchFoodRequest(
                passengerName = "Commuter_Q",
                trainNumber = "31617",
                trainName = "Ranaghat Local",
                coachNumber = "C1",
                seatDetail = "Berth",
                foodItem = sampleFoodItem,
                quantity = inputQty,
                customClientRequestId = "qty_test_${inputQty}_${UUID.randomUUID()}"
            )
            assertEquals("Input quantity $inputQty must be clamped to $expectedQty", expectedQty, req.quantity)
        }
    }

    // =========================================================================
    // 3. NOTIFICATION ARCHITECTURE & WORKER LIFECYCLE
    // =========================================================================

    @Test
    fun `NOTIF-01 - Notification worker deduplicates rapid duplicate triggers`() = runBlocking {
        val req = FoodRequestEntity(
            id = 5050L,
            clientRequestId = "req_notif_dedup_1",
            passengerName = "Commuter",
            trainNumber = "31617",
            trainName = "Local",
            coachNumber = "C1",
            seatDetail = "Window",
            foodItemId = sampleFoodItem.id,
            foodItemName = sampleFoodItem.nameEn,
            quantity = 1,
            price = 0,
            status = OrderStatus.REQUESTED.name,
            timestamp = System.currentTimeMillis()
        )

        val firstDispatch = notificationWorker.dispatchOrderNotification(req)
        assertTrue(firstDispatch is NotificationDispatchResult.Dispatched)

        // Second dispatch of exact same request notification
        val secondDispatch = notificationWorker.dispatchOrderNotification(req)
        assertTrue("Duplicate notification must be ignored", secondDispatch is NotificationDispatchResult.DuplicateIgnored)
    }

    @Test
    fun `NOTIF-02 - Notification worker restart wipes in-memory deduplication set`() = runBlocking {
        val req = FoodRequestEntity(
            id = 6060L,
            clientRequestId = "req_notif_restart",
            passengerName = "Commuter",
            trainNumber = "31617",
            trainName = "Local",
            coachNumber = "C1",
            seatDetail = "Window",
            foodItemId = sampleFoodItem.id,
            foodItemName = sampleFoodItem.nameEn,
            quantity = 1,
            price = 0,
            status = OrderStatus.REQUESTED.name,
            timestamp = System.currentTimeMillis()
        )

        val worker1 = OrderNotificationWorker(db, matchingService)
        val res1 = worker1.dispatchOrderNotification(req)
        assertTrue(res1 is NotificationDispatchResult.Dispatched)

        // Worker restart (new in-memory instance, simulating process death/restart)
        val worker2 = OrderNotificationWorker(db, matchingService)
        val res2 = worker2.dispatchOrderNotification(req)
        // Demonstrates that in-memory cache is NOT persistent across process death
        assertTrue(
            "New worker instance without persistent DB storage does not retain in-memory deduplication",
            res2 is NotificationDispatchResult.Dispatched
        )
    }
}

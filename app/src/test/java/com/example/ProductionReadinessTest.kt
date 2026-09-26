package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.ClaimResult
import com.example.data.engine.CoachChangeResult
import com.example.data.engine.HawkerEligibilityCriteria
import com.example.data.engine.HawkerMatchingService
import com.example.data.engine.NotificationDispatchResult
import com.example.data.engine.OrderNotificationWorker
import com.example.data.engine.OrderStateMachine
import com.example.data.engine.TransitionResult
import com.example.data.local.AppDatabase
import com.example.data.local.FoodRequestEntity
import com.example.data.local.VendorEntity
import com.example.data.model.FoodItem
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.data.repository.RailSathiRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ProductionReadinessTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: RailSathiRepository
    private lateinit var matchingService: HawkerMatchingService
    private lateinit var notificationWorker: OrderNotificationWorker

    private val sampleFoodItem = FoodItem(
        id = "jhal_muri",
        nameEn = "Kolkata Jhal Muri",
        nameHi = "झालमुड़ी",
        nameBn = "ঝালমুড়ি",
        nameMr = "झाल मुरी",
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
    // SCENARIO A: 10 Hawkers & 10 Travelers on Same Train (Concurrency & Claims)
    // =========================================================================

    @Test
    fun `A-01 and A-02 - 10 travelers submit orders simultaneously on coaches C1 to C10`() = runBlocking {
        val trainNumber = "T001"
        val jobs = (1..10).map { index ->
            async {
                repository.createAndDispatchFoodRequest(
                    passengerName = "Passenger $index",
                    trainNumber = trainNumber,
                    trainName = "Express T001",
                    coachNumber = "C$index",
                    seatDetail = "Seat $index",
                    foodItem = sampleFoodItem,
                    quantity = 1,
                    targetStationCode = "S2",
                    customClientRequestId = "req_client_p$index"
                )
            }
        }

        val createdRequests = jobs.awaitAll()

        // 10 unique request IDs
        assertEquals(10, createdRequests.size)
        val uniqueIds = createdRequests.map { it.id }.toSet()
        assertEquals("All 10 orders must have unique primary IDs", 10, uniqueIds.size)

        // Coach is properly scoped for each order
        createdRequests.forEachIndexed { i, req ->
            val expectedCoach = "C${i + 1}"
            assertEquals(expectedCoach, req.coachNumber)
        }
    }

    @Test
    fun `A-04 and A-05 - Two vendors accept the same order simultaneously - atomic claim ensures only one succeeds`() = runBlocking {
        val request = repository.createAndDispatchFoodRequest(
            passengerName = "Traveler P01",
            trainNumber = "T001",
            trainName = "Express T001",
            coachNumber = "C1",
            seatDetail = "Berth 12",
            foodItem = sampleFoodItem,
            quantity = 2,
            targetStationCode = "S2"
        )

        val vendor1 = async {
            matchingService.claimOrder(
                requestId = request.id,
                vendorId = "V001",
                vendorName = "Vendor One"
            )
        }
        val vendor2 = async {
            matchingService.claimOrder(
                requestId = request.id,
                vendorId = "V002",
                vendorName = "Vendor Two"
            )
        }

        val results = listOf(vendor1.await(), vendor2.await())
        val successes = results.filterIsInstance<ClaimResult.Success>()
        val alreadyClaimed = results.filterIsInstance<ClaimResult.AlreadyClaimed>()

        assertEquals("Exactly one vendor must succeed claiming the single-vendor order", 1, successes.size)
        assertEquals("The competing vendor must receive AlreadyClaimed", 1, alreadyClaimed.size)

        val winningVendorId = successes.first().vendorId
        assertTrue(winningVendorId == "V001" || winningVendorId == "V002")

        // In database, the assigned vendor must match the winner
        val updatedDb = db.foodRequestDao().getRequestById(request.id)
        assertNotNull(updatedDb)
        assertEquals(winningVendorId, updatedDb?.assignedVendorId)
        assertEquals(OrderStatus.VENDOR_ACCEPTED.name, updatedDb?.status)
    }

    @Test
    fun `A-06 - Vendor V01 cannot access or overwrite an already accepted order`() = runBlocking {
        val request = repository.createAndDispatchFoodRequest(
            passengerName = "Traveler P02",
            trainNumber = "T001",
            trainName = "Express T001",
            coachNumber = "C2",
            seatDetail = "14",
            foodItem = sampleFoodItem,
            quantity = 1
        )

        // Vendor 2 claims it first
        val firstClaim = matchingService.claimOrder(request.id, "V002", "Vendor Two")
        assertTrue(firstClaim is ClaimResult.Success)

        // Vendor 1 tries to claim it afterwards
        val secondClaim = matchingService.claimOrder(request.id, "V001", "Vendor One")
        assertTrue(secondClaim is ClaimResult.AlreadyClaimed)
        assertEquals("V002", (secondClaim as ClaimResult.AlreadyClaimed).currentVendorId)
    }

    @Test
    fun `A-08 - Network retry with duplicate clientRequestId is idempotent and prevents duplicate orders`() = runBlocking {
        val clientReqId = "idempotency_key_p01_unique"
        val firstAttempt = repository.createAndDispatchFoodRequest(
            passengerName = "Traveler P01",
            trainNumber = "T001",
            trainName = "Express T001",
            coachNumber = "C1",
            seatDetail = "Berth 1",
            foodItem = sampleFoodItem,
            quantity = 1,
            customClientRequestId = clientReqId
        )

        val retryAttempt = repository.createAndDispatchFoodRequest(
            passengerName = "Traveler P01",
            trainNumber = "T001",
            trainName = "Express T001",
            coachNumber = "C1",
            seatDetail = "Berth 1",
            foodItem = sampleFoodItem,
            quantity = 1,
            customClientRequestId = clientReqId
        )

        assertEquals("Retry with same clientRequestId must return the identical existing record", firstAttempt.id, retryAttempt.id)
        val allRequests = db.foodRequestDao().getPendingRequestsByTrainAndCoach("T001", "C1")
        assertEquals("Database must contain exactly 1 record, not 2", 1, allRequests.size)
    }

    // =========================================================================
    // SCENARIO B: Same Train, Different Coaches (Isolation & Modifications)
    // =========================================================================

    @Test
    fun `Scenario B - Coach Scoping Isolation - C1 requests are isolated from C2 and C3`() = runBlocking {
        val trainNumber = "T001"
        // Seed vendors on different coaches
        db.vendorDao().insertVendors(
            listOf(
                VendorEntity(
                    vendorId = "V_C1",
                    name = "Vendor Coach 1",
                    badgeNumber = "B-01",
                    specialityItemId = sampleFoodItem.id,
                    specialityItemName = sampleFoodItem.nameEn,
                    currentTrain = trainNumber,
                    currentCoach = "C1",
                    currentStation = "S1",
                    isOnline = true
                ),
                VendorEntity(
                    vendorId = "V_C2",
                    name = "Vendor Coach 2",
                    badgeNumber = "B-02",
                    specialityItemId = sampleFoodItem.id,
                    specialityItemName = sampleFoodItem.nameEn,
                    currentTrain = trainNumber,
                    currentCoach = "C2",
                    currentStation = "S1",
                    isOnline = true
                )
            )
        )

        // Query eligible vendors for order on C1
        val eligibleForC1 = matchingService.findEligibleVendors(
            HawkerEligibilityCriteria(
                trainNumber = trainNumber,
                targetCoach = "C1",
                foodItemId = sampleFoodItem.id,
                strictCoachScoping = true
            )
        )

        assertEquals(1, eligibleForC1.size)
        assertEquals("V_C1", eligibleForC1.first().vendorId)
        assertFalse("Vendor in C2 must not be matched to order scoped strictly to C1", eligibleForC1.any { it.vendorId == "V_C2" })
    }

    @Test
    fun `Scenario B Test 5 - Coach identifier normalization trims casing, spaces, and hyphens`() {
        assertEquals("C1", matchingService.normalizeCoach(" c1 "))
        assertEquals("C1", matchingService.normalizeCoach("C1"))
        assertEquals("C1", matchingService.normalizeCoach("c-1"))
        assertEquals("GS-2", matchingService.normalizeCoach("gs 2"))
        assertEquals("GS-2", matchingService.normalizeCoach("GS-2"))
        assertEquals("GS-2", matchingService.normalizeCoach("  gs-2  "))
    }

    @Test
    fun `Scenario B Tests 7-10 - Coach modification allowed in pending, rejected after vendor acceptance`() = runBlocking {
        val request = repository.createAndDispatchFoodRequest(
            passengerName = "Traveler P01",
            trainNumber = "T001",
            trainName = "Express T001",
            coachNumber = "C1",
            seatDetail = "12",
            foodItem = sampleFoodItem,
            quantity = 1
        )

        // Change coach before acceptance -> Succeeds
        val change1 = matchingService.changeCoach(request.id, "C2")
        assertTrue("Changing coach before vendor acceptance must succeed", change1 is CoachChangeResult.Success)
        assertEquals("C2", (change1 as CoachChangeResult.Success).updatedCoach)

        // Vendor claims the order
        matchingService.claimOrder(request.id, "V001", "Vendor One")

        // Try changing coach after vendor acceptance -> Rejected
        val change2 = matchingService.changeCoach(request.id, "C3")
        assertTrue("Changing coach after vendor acceptance must be rejected", change2 is CoachChangeResult.Rejected)
    }

    // =========================================================================
    // SCENARIO C: Responses from Different Stations & Reconnection
    // =========================================================================

    @Test
    fun `Scenario C - Station Scoping isolates S2 orders from S3 orders on the same train`() = runBlocking {
        val train = "T001"
        repository.createAndDispatchFoodRequest(
            passengerName = "Traveler 1",
            trainNumber = train,
            trainName = "Express T001",
            coachNumber = "C1",
            seatDetail = "1",
            foodItem = sampleFoodItem,
            quantity = 1,
            targetStationCode = "S2"
        )
        repository.createAndDispatchFoodRequest(
            passengerName = "Traveler 2",
            trainNumber = train,
            trainName = "Express T001",
            coachNumber = "C1",
            seatDetail = "2",
            foodItem = sampleFoodItem,
            quantity = 1,
            targetStationCode = "S3"
        )

        val s2Orders = matchingService.getStationScopedPendingRequests(train, "S2")
        val s3Orders = matchingService.getStationScopedPendingRequests(train, "S3")

        assertEquals(1, s2Orders.size)
        assertEquals("S2", s2Orders.first().targetStationCode)

        assertEquals(1, s3Orders.size)
        assertEquals("S3", s3Orders.first().targetStationCode)
    }

    @Test
    fun `Scenario C Test 13 - Reconnected vendor catch-up synchronization delivers pending work`() = runBlocking {
        val train = "T001"
        val coach = "C1"
        repository.createAndDispatchFoodRequest(
            passengerName = "P1",
            trainNumber = train,
            trainName = "Express T001",
            coachNumber = coach,
            seatDetail = "1",
            foodItem = sampleFoodItem,
            quantity = 1
        )
        repository.createAndDispatchFoodRequest(
            passengerName = "P2",
            trainNumber = train,
            trainName = "Express T001",
            coachNumber = coach,
            seatDetail = "2",
            foodItem = sampleFoodItem,
            quantity = 1
        )

        val pendingWork = notificationWorker.fetchPendingWorkForReconnectedVendor(train, coach)
        assertEquals("Reconnected vendor must receive all active pending work for their train and coach", 2, pendingWork.size)
    }

    // =========================================================================
    // TRAIN & COACH IDENTIFICATION (TL-04, CL-05)
    // =========================================================================

    @Test
    fun `TL-04 - Train instance ID prevents collision across services sharing same train number on different dates`() {
        val trainNumber = "12301"
        val date1 = "20260920"
        val date2 = "20260921"

        val instance1 = matchingService.buildTrainInstanceId(trainNumber, date1)
        val instance2 = matchingService.buildTrainInstanceId(trainNumber, date2)

        assertEquals("12301_20260920", instance1)
        assertEquals("12301_20260921", instance2)
        assertTrue("Different operating dates must produce distinct train instance IDs", instance1 != instance2)
    }

    // =========================================================================
    // ORDER STATE MACHINE & NOTIFICATION WORKER
    // =========================================================================

    @Test
    fun `OrderStateMachine - Full legal order progression succeeds`() {
        var status = OrderStatus.REQUESTED

        val steps = listOf(
            OrderStatus.MATCHING,
            OrderStatus.OFFERED_TO_VENDOR,
            OrderStatus.VENDOR_ACCEPTED,
            OrderStatus.PRICE_CONFIRMED,
            OrderStatus.CUSTOMER_CONFIRMED,
            OrderStatus.FULFILLING,
            OrderStatus.COMPLETED
        )

        steps.forEach { nextStatus ->
            val result = OrderStateMachine.validateTransition(status, nextStatus)
            assertTrue("Transition from $status to $nextStatus should be legal", result is TransitionResult.Success)
            status = nextStatus
        }

        assertTrue("Completed is terminal", OrderStateMachine.isTerminal(status))
    }

    @Test
    fun `OrderStateMachine - Illegal regression from COMPLETED back to REQUESTED is rejected`() {
        val result = OrderStateMachine.validateTransition(OrderStatus.COMPLETED, OrderStatus.REQUESTED)
        assertTrue(result is TransitionResult.InvalidTransition)
        assertFalse(OrderStateMachine.canCustomerCancel(OrderStatus.COMPLETED))
    }

    @Test
    fun `NotificationWorker - Duplicate notification events are safely deduplicated`() = runBlocking {
        notificationWorker.clearCache()
        val request = repository.createAndDispatchFoodRequest(
            passengerName = "Traveler P01",
            trainNumber = "T001",
            trainName = "Express T001",
            coachNumber = "C1",
            seatDetail = "1",
            foodItem = sampleFoodItem,
            quantity = 1,
            customClientRequestId = "unique_req_notif"
        )

        // First dispatch -> Dispatched
        val firstDispatch = notificationWorker.dispatchOrderNotification(request)
        assertTrue(firstDispatch is NotificationDispatchResult.Dispatched)

        // Second duplicate dispatch -> DuplicateIgnored
        val secondDispatch = notificationWorker.dispatchOrderNotification(request)
        assertTrue("Duplicate notification must be ignored", secondDispatch is NotificationDispatchResult.DuplicateIgnored)
    }

    // =========================================================================
    // ROLE-BASED ACCESS CONTROL (RBAC) & AUTHORIZATION ENFORCEMENT
    // =========================================================================

    @Test
    fun `RBAC-01 - Traveler cannot execute vendor operations`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)

        // Traveler attempting to accept order and offer price
        var securityCaught = false
        try {
            repository.vendorAcceptAndOfferPrice(1L, "V001", 20)
        } catch (e: SecurityException) {
            securityCaught = true
            assertTrue(e.message?.contains("requires VENDOR") == true)
        }
        assertTrue("Traveler attempting vendorAcceptAndOfferPrice must throw SecurityException", securityCaught)

        // Traveler attempting to update vendor coach
        securityCaught = false
        try {
            repository.updateVendorCoach("V001", "C2")
        } catch (e: SecurityException) {
            securityCaught = true
        }
        assertTrue("Traveler attempting updateVendorCoach must throw SecurityException", securityCaught)

        // Traveler attempting to complete delivery and record sale
        securityCaught = false
        try {
            repository.completeDeliveryAndRecordSale(1L, "V001", "Chai", 20.0, "C1", "T001", "P1")
        } catch (e: SecurityException) {
            securityCaught = true
        }
        assertTrue("Traveler attempting completeDeliveryAndRecordSale must throw SecurityException", securityCaught)

        // Traveler attempting to toggle vendor availability
        securityCaught = false
        try {
            repository.setVendorAvailability("V001", true)
        } catch (e: SecurityException) {
            securityCaught = true
        }
        assertTrue("Traveler attempting setVendorAvailability must throw SecurityException", securityCaught)
    }

    @Test
    fun `RBAC-02 - Vendor cannot execute traveler operations`() = runBlocking {
        repository.setVerifiedRole(UserRole.VENDOR)

        // Vendor attempting to create a food request
        var securityCaught = false
        try {
            repository.createAndDispatchFoodRequest(
                passengerName = "Illegal Vendor",
                trainNumber = "T001",
                trainName = "Express",
                coachNumber = "C1",
                seatDetail = "1",
                foodItem = sampleFoodItem
            )
        } catch (e: SecurityException) {
            securityCaught = true
            assertTrue(e.message?.contains("requires TRAVELER") == true)
        }
        assertTrue("Vendor attempting createAndDispatchFoodRequest must throw SecurityException", securityCaught)

        // Vendor attempting to change passenger coach
        securityCaught = false
        try {
            repository.changeCoach(1L, "C2")
        } catch (e: SecurityException) {
            securityCaught = true
        }
        assertTrue("Vendor attempting changeCoach must throw SecurityException", securityCaught)

        // Vendor attempting to confirm customer order
        securityCaught = false
        try {
            repository.customerConfirmOrder(1L, "cust_1")
        } catch (e: SecurityException) {
            securityCaught = true
        }
        assertTrue("Vendor attempting customerConfirmOrder must throw SecurityException", securityCaught)

        // Vendor attempting to cancel customer request
        securityCaught = false
        try {
            repository.cancelRequest(1L)
        } catch (e: SecurityException) {
            securityCaught = true
        }
        assertTrue("Vendor attempting cancelRequest must throw SecurityException", securityCaught)
    }

    // =========================================================================
    // VENDOR AVAILABILITY & ELIGIBILITY ENFORCEMENT
    // =========================================================================

    @Test
    fun `AVAIL-01 - Offline vendor cannot accept order or offer price`() = runBlocking {
        val vendorId = "V_OFFLINE"
        db.vendorDao().insertVendor(
            VendorEntity(
                vendorId = vendorId,
                name = "Offline Vendor",
                badgeNumber = "B-99",
                specialityItemId = sampleFoodItem.id,
                specialityItemName = sampleFoodItem.nameEn,
                currentTrain = "T001",
                currentCoach = "C1",
                currentStation = "S1",
                isOnline = false
            )
        )

        repository.setVerifiedRole(UserRole.TRAVELER)
        val request = repository.createAndDispatchFoodRequest(
            passengerName = "P1",
            trainNumber = "T001",
            trainName = "Express",
            coachNumber = "C1",
            seatDetail = "1",
            foodItem = sampleFoodItem
        )

        repository.setVerifiedRole(UserRole.VENDOR)
        var offlineCaught = false
        try {
            repository.vendorAcceptAndOfferPrice(request.id, vendorId, 20)
        } catch (e: IllegalStateException) {
            offlineCaught = true
            assertTrue(e.message?.contains("Not Available") == true)
        }
        assertTrue("Offline vendor cannot accept order", offlineCaught)
    }

    @Test
    fun `AVAIL-02 - Toggling vendor availability to online enables order acceptance`() = runBlocking {
        val vendorId = "V_TOGGLE"
        db.vendorDao().insertVendor(
            VendorEntity(
                vendorId = vendorId,
                name = "Toggle Vendor",
                badgeNumber = "B-100",
                specialityItemId = sampleFoodItem.id,
                specialityItemName = sampleFoodItem.nameEn,
                currentTrain = "T001",
                currentCoach = "C1",
                currentStation = "S1",
                isOnline = false
            )
        )

        repository.setVerifiedRole(UserRole.TRAVELER)
        val request = repository.createAndDispatchFoodRequest(
            passengerName = "P1",
            trainNumber = "T001",
            trainName = "Express",
            coachNumber = "C1",
            seatDetail = "1",
            foodItem = sampleFoodItem
        )

        repository.setVerifiedRole(UserRole.VENDOR)
        // Toggle to online
        repository.setVendorAvailability(vendorId, true)

        val updatedVendor = db.vendorDao().getVendorByIdDirect(vendorId)
        assertNotNull(updatedVendor)
        assertTrue(updatedVendor?.isOnline == true)

        // Now acceptance succeeds
        repository.vendorAcceptAndOfferPrice(request.id, vendorId, 20)
        val updatedRequest = db.foodRequestDao().getRequestById(request.id)
        assertNotNull(updatedRequest)
        assertEquals(OrderStatus.PRICE_CONFIRMED.name, updatedRequest?.status)
        assertEquals(vendorId, updatedRequest?.assignedVendorId)
        assertEquals(20, updatedRequest?.offeredUnitPrice)
    }

    @Test
    fun `AVAIL-03 - Offline vendor is filtered out from automated hawker matching`() = runBlocking {
        val trainNumber = "T001"
        db.vendorDao().insertVendors(
            listOf(
                VendorEntity(
                    vendorId = "V_ON",
                    name = "Online Vendor",
                    badgeNumber = "B-ON",
                    specialityItemId = sampleFoodItem.id,
                    specialityItemName = sampleFoodItem.nameEn,
                    currentTrain = trainNumber,
                    currentCoach = "C1",
                    currentStation = "S1",
                    isOnline = true
                ),
                VendorEntity(
                    vendorId = "V_OFF",
                    name = "Offline Vendor",
                    badgeNumber = "B-OFF",
                    specialityItemId = sampleFoodItem.id,
                    specialityItemName = sampleFoodItem.nameEn,
                    currentTrain = trainNumber,
                    currentCoach = "C1",
                    currentStation = "S1",
                    isOnline = false
                )
            )
        )

        val eligible = matchingService.findEligibleVendors(
            HawkerEligibilityCriteria(
                trainNumber = trainNumber,
                targetCoach = "C1",
                foodItemId = sampleFoodItem.id
            )
        )

        assertEquals("Only online vendor should be eligible for assignment", 1, eligible.size)
        assertEquals("V_ON", eligible.first().vendorId)
    }

    @Test
    fun `E2E-01 - Complete order flow with role verification and sales recording`() = runBlocking {
        val vendorId = "V_E2E"
        db.vendorDao().insertVendor(
            VendorEntity(
                vendorId = vendorId,
                name = "Subhash Da",
                badgeNumber = "B-E2E",
                specialityItemId = sampleFoodItem.id,
                specialityItemName = sampleFoodItem.nameEn,
                currentTrain = "T001",
                currentCoach = "C1",
                currentStation = "S1",
                isOnline = true,
                todaySalesCount = 0,
                todayEarnings = 0.0
            )
        )

        // 1. Traveler creates request
        repository.setVerifiedRole(UserRole.TRAVELER)
        val request = repository.createAndDispatchFoodRequest(
            passengerName = "Amit Kumar",
            trainNumber = "T001",
            trainName = "Ranaghat Local",
            coachNumber = "C1",
            seatDetail = "Berth 12",
            foodItem = sampleFoodItem,
            quantity = 2
        )
        assertTrue(
            "Initial request should be REQUESTED or automatically MATCHED/OFFERED",
            request.status == OrderStatus.REQUESTED.name || request.status == OrderStatus.OFFERED_TO_VENDOR.name
        )

        // 2. Vendor accepts with allowed unit price (₹20)
        repository.setVerifiedRole(UserRole.VENDOR)
        repository.vendorAcceptAndOfferPrice(request.id, vendorId, 20)

        val priceConfirmed = db.foodRequestDao().getRequestById(request.id)
        assertNotNull(priceConfirmed)
        assertEquals(OrderStatus.PRICE_CONFIRMED.name, priceConfirmed?.status)
        assertEquals(40, priceConfirmed?.calculatedTotalPrice)

        // 3. Traveler confirms order
        repository.setVerifiedRole(UserRole.TRAVELER)
        repository.customerConfirmOrder(request.id, "cust_amit")

        val customerConfirmed = db.foodRequestDao().getRequestById(request.id)
        assertNotNull(customerConfirmed)
        assertEquals(OrderStatus.CUSTOMER_CONFIRMED.name, customerConfirmed?.status)

        // 4. Vendor delivers and records sale
        repository.setVerifiedRole(UserRole.VENDOR)
        repository.completeDeliveryAndRecordSale(
            requestId = request.id,
            vendorId = vendorId,
            foodItemName = sampleFoodItem.nameEn,
            amount = 40.0,
            coachNumber = "C1",
            trainNumber = "T001",
            buyerName = "Amit Kumar"
        )

        val completedRequest = db.foodRequestDao().getRequestById(request.id)
        assertNotNull(completedRequest)
        assertEquals(OrderStatus.COMPLETED.name, completedRequest?.status)

        // Verify vendor sales record in DB
        val vendorAfterSale = db.vendorDao().getVendorByIdDirect(vendorId)
        assertNotNull(vendorAfterSale)
        assertEquals(1, vendorAfterSale?.todaySalesCount)
        assertEquals(40.0, vendorAfterSale?.todayEarnings ?: 0.0, 0.01)

        val saleRecords = db.saleRecordDao().getSalesByVendorDirect(vendorId)
        assertEquals(1, saleRecords.size)
        assertEquals(40.0, saleRecords.first().amount, 0.01)
        assertEquals("C1", saleRecords.first().coachNumber)
    }
}

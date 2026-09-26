package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.ClaimResult
import com.example.data.engine.CoachChangeResult
import com.example.data.engine.HawkerEligibilityCriteria
import com.example.data.engine.HawkerMatchingService
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
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AdversarialProductionValidationTest {

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
    // PHASE 1 — ROLE SECURITY ADVERSARIAL BOUNDARIES
    // =========================================================================

    @Test
    fun `SEC-01 - Traveler cannot call vendorAcceptAndOfferPrice directly`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)
        try {
            repository.vendorAcceptAndOfferPrice(1L, "v_malicious", 20)
            fail("Expected SecurityException when traveler calls vendorAcceptAndOfferPrice")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("requires VENDOR role") == true)
        }
    }

    @Test
    fun `SEC-02 - Traveler cannot call rejectFoodRequest`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)
        try {
            repository.rejectFoodRequest(1L, "v_malicious")
            fail("Expected SecurityException when traveler calls rejectFoodRequest")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("requires VENDOR role") == true)
        }
    }

    @Test
    fun `SEC-03 - Traveler cannot change vendor availability`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)
        try {
            repository.setVendorAvailability("v_malicious", false)
            fail("Expected SecurityException when traveler calls setVendorAvailability")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("requires VENDOR role") == true)
        }
    }

    @Test
    fun `SEC-04 - Traveler cannot modify vendor coach context`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)
        try {
            repository.updateVendorCoach("v_malicious", "GS-1")
            fail("Expected SecurityException when traveler calls updateVendorCoach")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("requires VENDOR role") == true)
        }
    }

    @Test
    fun `SEC-05 - Traveler cannot complete vendor delivery and record sale`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)
        try {
            repository.completeDeliveryAndRecordSale(
                requestId = 1L,
                vendorId = "v_malicious",
                foodItemName = "Jhal Muri",
                amount = 20.0,
                coachNumber = "C1",
                trainNumber = "T001",
                buyerName = "Traveler"
            )
            fail("Expected SecurityException when traveler calls completeDeliveryAndRecordSale")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("requires VENDOR role") == true)
        }
    }

    @Test
    fun `SEC-06 - Vendor cannot dispatch traveler food requests`() = runBlocking {
        repository.setVerifiedRole(UserRole.VENDOR)
        try {
            repository.createAndDispatchFoodRequest(
                passengerName = "Vendor Posing As Passenger",
                trainNumber = "T001",
                trainName = "Express",
                coachNumber = "C1",
                seatDetail = "Berth 1",
                foodItem = sampleFoodItem,
                quantity = 1
            )
            fail("Expected SecurityException when vendor calls createAndDispatchFoodRequest")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("requires TRAVELER role") == true)
        }
    }

    @Test
    fun `SEC-07 - Vendor cannot modify traveler coach information`() = runBlocking {
        repository.setVerifiedRole(UserRole.VENDOR)
        try {
            repository.changeCoach(1L, "C2")
            fail("Expected SecurityException when vendor calls changeCoach")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("requires TRAVELER role") == true)
        }
    }

    @Test
    fun `SEC-08 - Vendor cannot perform customer confirmation`() = runBlocking {
        repository.setVerifiedRole(UserRole.VENDOR)
        try {
            repository.customerConfirmOrder(1L, "cust_victim")
            fail("Expected SecurityException when vendor calls customerConfirmOrder")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("requires TRAVELER role") == true)
        }
    }

    @Test
    fun `SEC-09 - Vendor cannot cancel a customer order`() = runBlocking {
        repository.setVerifiedRole(UserRole.VENDOR)
        try {
            repository.cancelRequest(1L)
            fail("Expected SecurityException when vendor calls cancelRequest")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("requires TRAVELER role") == true)
        }
    }

    // =========================================================================
    // PHASE 2 — VENDOR AVAILABILITY & SURVIVAL
    // =========================================================================

    @Test
    fun `AVAIL-01 - Vendor switches Available to Not Available and preserves accepted orders`() = runBlocking {
        val vendorId = "V_AVAIL_01"
        db.vendorDao().insertVendor(
            VendorEntity(
                vendorId = vendorId,
                name = "Ramesh Kumar",
                badgeNumber = "B-01",
                specialityItemId = sampleFoodItem.id,
                specialityItemName = sampleFoodItem.nameEn,
                currentTrain = "T001",
                currentCoach = "C1",
                currentStation = "S1",
                isOnline = true
            )
        )

        // 1. Create and accept order while online
        repository.setVerifiedRole(UserRole.TRAVELER)
        val req = repository.createAndDispatchFoodRequest(
            passengerName = "Passenger 1",
            trainNumber = "T001",
            trainName = "Express",
            coachNumber = "C1",
            seatDetail = "Berth 1",
            foodItem = sampleFoodItem,
            quantity = 1
        )

        repository.setVerifiedRole(UserRole.VENDOR)
        repository.vendorAcceptAndOfferPrice(req.id, vendorId, 20)

        // 2. Switch to Not Available
        repository.setVendorAvailability(vendorId, false)
        val vendorAfter = db.vendorDao().getVendorByIdDirect(vendorId)
        assertFalse(vendorAfter?.isOnline ?: true)

        // 3. Accepted order is preserved and remains actionable
        val existingOrder = db.foodRequestDao().getRequestById(req.id)
        assertNotNull(existingOrder)
        assertEquals(OrderStatus.PRICE_CONFIRMED.name, existingOrder?.status)
        assertEquals(vendorId, existingOrder?.assignedVendorId)

        // 4. While Not Available, vendor is excluded from new matching criteria
        val eligible = matchingService.findEligibleVendors(
            HawkerEligibilityCriteria(
                trainNumber = "T001",
                targetCoach = "C1",
                foodItemId = sampleFoodItem.id
            )
        )
        assertTrue("Offline vendor must NOT receive new matches", eligible.isEmpty())

        // 5. Vendor attempts to accept a new order while offline -> rejected
        val newReq = FoodRequestEntity(
            clientRequestId = "req_new_while_offline",
            passengerName = "Passenger 2",
            trainNumber = "T001",
            trainName = "Express",
            coachNumber = "C1",
            seatDetail = "Berth 2",
            foodItemId = sampleFoodItem.id,
            foodItemName = sampleFoodItem.nameEn,
            quantity = 1,
            price = 0,
            status = OrderStatus.REQUESTED.name,
            timestamp = System.currentTimeMillis()
        )
        val newId = db.foodRequestDao().insertRequest(newReq)

        try {
            repository.vendorAcceptAndOfferPrice(newId, vendorId, 20)
            fail("Expected IllegalStateException when offline vendor tries to accept new order")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Not Available") == true)
        }
    }

    // =========================================================================
    // PHASE 3 — CONCURRENT ORDER CLAIMING
    // =========================================================================

    @Test
    fun `CLAIM-01 - Ten vendors attempt to claim ONE order simultaneously`() = runBlocking {
        val targetRequestId = 1001L
        val originalRequest = FoodRequestEntity(
            id = targetRequestId,
            clientRequestId = "req_claim_contest",
            passengerName = "Contested Traveler",
            trainNumber = "T001",
            trainName = "Express",
            coachNumber = "C1",
            seatDetail = "Berth 5",
            foodItemId = sampleFoodItem.id,
            foodItemName = sampleFoodItem.nameEn,
            quantity = 1,
            price = 0,
            status = OrderStatus.REQUESTED.name,
            timestamp = System.currentTimeMillis()
        )
        db.foodRequestDao().insertRequest(originalRequest)

        // 10 vendors race concurrently
        val jobs = (1..10).map { index ->
            async {
                matchingService.claimOrder(
                    requestId = targetRequestId,
                    vendorId = "V_RACE_$index",
                    vendorName = "Vendor Race $index"
                )
            }
        }

        val results = jobs.awaitAll()

        val successes = results.filterIsInstance<ClaimResult.Success>()
        val alreadyClaimed = results.filterIsInstance<ClaimResult.AlreadyClaimed>()

        assertEquals("Exactly ONE vendor must successfully claim the order", 1, successes.size)
        assertEquals("Exactly 9 vendors must receive AlreadyClaimed", 9, alreadyClaimed.size)

        // Verify database state: exactly one assigned vendor
        val saved = db.foodRequestDao().getRequestById(targetRequestId)
        assertNotNull(saved)
        assertEquals(OrderStatus.VENDOR_ACCEPTED.name, saved?.status)
        assertEquals(successes.first().vendorId, saved?.assignedVendorId)
    }

    @Test
    fun `CLAIM-02 - Completed order cannot be claimed or accepted again`() = runBlocking {
        val reqId = 2002L
        db.foodRequestDao().insertRequest(
            FoodRequestEntity(
                id = reqId,
                clientRequestId = "req_already_done",
                passengerName = "Done Passenger",
                trainNumber = "T001",
                trainName = "Express",
                coachNumber = "C1",
                seatDetail = "Berth 1",
                foodItemId = sampleFoodItem.id,
                foodItemName = sampleFoodItem.nameEn,
                quantity = 1,
                price = 20,
                status = OrderStatus.COMPLETED.name,
                timestamp = System.currentTimeMillis()
            )
        )

        val claimResult = matchingService.claimOrder(reqId, "V_LATE", "Late Vendor")
        assertTrue("Claim must be rejected on completed order", claimResult is ClaimResult.InvalidState)
    }

    // =========================================================================
    // PHASE 4 — SAME TRAIN / DIFFERENT COACHES & NORMALIZATION
    // =========================================================================

    @Test
    fun `COACH-01 - Coach normalization prevents string mismatches`() {
        assertEquals("C1", matchingService.normalizeCoach("C1"))
        assertEquals("C1", matchingService.normalizeCoach("c1"))
        assertEquals("C1", matchingService.normalizeCoach(" c1 "))
        assertEquals("C1", matchingService.normalizeCoach("C-1"))
        assertEquals("GS-2", matchingService.normalizeCoach("GS-2"))
        assertEquals("GS-2", matchingService.normalizeCoach("gs 2"))
        assertEquals("GS-2", matchingService.normalizeCoach("gs-2"))
        assertEquals("VND-1", matchingService.normalizeCoach("vnd-1"))
    }

    @Test
    fun `COACH-02 - Coach isolation excludes requests from other coaches`() = runBlocking {
        val trainNumber = "T001"
        // Insert requests in C1 and C2
        db.foodRequestDao().insertRequest(
            FoodRequestEntity(
                clientRequestId = "req_c1",
                passengerName = "Passenger C1",
                trainNumber = trainNumber,
                trainName = "Express",
                coachNumber = "C1",
                seatDetail = "Berth 1",
                foodItemId = sampleFoodItem.id,
                foodItemName = sampleFoodItem.nameEn,
                quantity = 1,
                price = 0,
                status = OrderStatus.REQUESTED.name,
                timestamp = System.currentTimeMillis()
            )
        )
        db.foodRequestDao().insertRequest(
            FoodRequestEntity(
                clientRequestId = "req_c2",
                passengerName = "Passenger C2",
                trainNumber = trainNumber,
                trainName = "Express",
                coachNumber = "C2",
                seatDetail = "Berth 1",
                foodItemId = sampleFoodItem.id,
                foodItemName = sampleFoodItem.nameEn,
                quantity = 1,
                price = 0,
                status = OrderStatus.REQUESTED.name,
                timestamp = System.currentTimeMillis()
            )
        )

        val c1Requests = matchingService.getCoachScopedPendingRequests(trainNumber, "C1")
        assertEquals(1, c1Requests.size)
        assertEquals("C1", c1Requests.first().coachNumber)

        val c2Requests = matchingService.getCoachScopedPendingRequests(trainNumber, "C2")
        assertEquals(1, c2Requests.size)
        assertEquals("C2", c2Requests.first().coachNumber)
    }

    @Test
    fun `COACH-03 - Coach cannot be changed after vendor acceptance`() = runBlocking {
        val reqId = 3003L
        db.foodRequestDao().insertRequest(
            FoodRequestEntity(
                id = reqId,
                clientRequestId = "req_coach_change",
                passengerName = "Passenger",
                trainNumber = "T001",
                trainName = "Express",
                coachNumber = "C1",
                seatDetail = "Berth 1",
                foodItemId = sampleFoodItem.id,
                foodItemName = sampleFoodItem.nameEn,
                quantity = 1,
                price = 0,
                status = OrderStatus.VENDOR_ACCEPTED.name,
                timestamp = System.currentTimeMillis(),
                assignedVendorId = "V_ACCEPTED"
            )
        )

        val changeResult = matchingService.changeCoach(reqId, "C2")
        assertTrue("Coach change must be rejected after vendor acceptance", changeResult is CoachChangeResult.Rejected)
    }

    // =========================================================================
    // PHASE 5 — STATION ISOLATION
    // =========================================================================

    @Test
    fun `STATION-01 - Requests are isolated by target delivery station`() = runBlocking {
        val trainNumber = "T001"
        listOf("S2", "S3", "S4").forEachIndexed { idx, stn ->
            db.foodRequestDao().insertRequest(
                FoodRequestEntity(
                    clientRequestId = "req_stn_$idx",
                    passengerName = "Passenger at $stn",
                    trainNumber = trainNumber,
                    trainName = "Express",
                    coachNumber = "C1",
                    seatDetail = "Berth 1",
                    foodItemId = sampleFoodItem.id,
                    foodItemName = sampleFoodItem.nameEn,
                    quantity = 1,
                    price = 0,
                    status = OrderStatus.REQUESTED.name,
                    timestamp = System.currentTimeMillis(),
                    targetStationCode = stn
                )
            )
        }

        val s2Orders = matchingService.getStationScopedPendingRequests(trainNumber, "S2")
        assertEquals(1, s2Orders.size)
        assertEquals("S2", s2Orders.first().targetStationCode)

        val s3Orders = matchingService.getStationScopedPendingRequests(trainNumber, "S3")
        assertEquals(1, s3Orders.size)
        assertEquals("S3", s3Orders.first().targetStationCode)
    }

    // =========================================================================
    // PHASE 10 — PRICE AND ORDER INTEGRITY
    // =========================================================================

    @Test
    fun `INTEGRITY-01 - Invalid unit prices outside allowed tiers are strictly rejected`() = runBlocking {
        val reqId = 4004L
        db.foodRequestDao().insertRequest(
            FoodRequestEntity(
                id = reqId,
                clientRequestId = "req_price_test",
                passengerName = "Traveler",
                trainNumber = "T001",
                trainName = "Express",
                coachNumber = "C1",
                seatDetail = "Berth 1",
                foodItemId = sampleFoodItem.id,
                foodItemName = sampleFoodItem.nameEn,
                quantity = 1,
                price = 0,
                status = OrderStatus.REQUESTED.name,
                timestamp = System.currentTimeMillis()
            )
        )

        repository.setVerifiedRole(UserRole.VENDOR)

        // Allowed: 5, 10, 15, 20, 30, 40, 50
        // Test invalid prices: 17, 99, -5, 100
        val invalidPrices = listOf(17, 99, -5, 100, 25)
        for (badPrice in invalidPrices) {
            try {
                repository.vendorAcceptAndOfferPrice(reqId, "V_TEST", badPrice)
                fail("Expected IllegalArgumentException for disallowed price ₹$badPrice")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message?.contains("not allowed") == true)
            }
        }
    }

    @Test
    fun `INTEGRITY-02 - Quantity is coerced within bounds (1 to 10)`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)

        // Negative quantity coerced to 1
        val req1 = repository.createAndDispatchFoodRequest(
            passengerName = "Passenger Neg",
            trainNumber = "T001",
            trainName = "Express",
            coachNumber = "C1",
            seatDetail = "Berth 1",
            foodItem = sampleFoodItem,
            quantity = -5,
            customClientRequestId = "req_qty_neg"
        )
        assertEquals("Negative quantity must be coerced to 1", 1, req1.quantity)

        // Zero quantity coerced to 1
        val req2 = repository.createAndDispatchFoodRequest(
            passengerName = "Passenger Zero",
            trainNumber = "T001",
            trainName = "Express",
            coachNumber = "C1",
            seatDetail = "Berth 1",
            foodItem = sampleFoodItem,
            quantity = 0,
            customClientRequestId = "req_qty_zero"
        )
        assertEquals("Zero quantity must be coerced to 1", 1, req2.quantity)

        // Excessive quantity coerced to 10
        val req3 = repository.createAndDispatchFoodRequest(
            passengerName = "Passenger 99",
            trainNumber = "T001",
            trainName = "Express",
            coachNumber = "C1",
            seatDetail = "Berth 1",
            foodItem = sampleFoodItem,
            quantity = 99,
            customClientRequestId = "req_qty_99"
        )
        assertEquals("Excessive quantity must be coerced to 10", 10, req3.quantity)
    }

    // =========================================================================
    // PHASE 11 — STATE MACHINE ATTACKS
    // =========================================================================

    @Test
    fun `STATE-01 - Illegal state machine transitions are strictly rejected`() {
        val illegalTransitions = listOf(
            OrderStatus.COMPLETED to OrderStatus.REQUESTED,
            OrderStatus.COMPLETED to OrderStatus.FULFILLING,
            OrderStatus.CUSTOMER_CANCELLED to OrderStatus.COMPLETED,
            OrderStatus.REJECTED to OrderStatus.VENDOR_ACCEPTED,
            OrderStatus.EXPIRED to OrderStatus.VENDOR_ACCEPTED,
            OrderStatus.CUSTOMER_CONFIRMED to OrderStatus.REQUESTED,
            OrderStatus.VENDOR_ACCEPTED to OrderStatus.MATCHING,
            OrderStatus.FULFILLING to OrderStatus.PRICE_CONFIRMED
        )

        for ((from, to) in illegalTransitions) {
            val result = OrderStateMachine.validateTransition(from, to)
            assertTrue("Transition from $from to $to MUST be invalid", result is TransitionResult.InvalidTransition)
            assertFalse("canTransition from $from to $to must be false", OrderStateMachine.canTransition(from, to))
        }
    }

    // =========================================================================
    // PHASE 7 — OFFLINE-FIRST IDEMPOTENCY
    // =========================================================================

    @Test
    fun `SYNC-01 - Duplicate clientRequestId returns existing order idempotently`() = runBlocking {
        repository.setVerifiedRole(UserRole.TRAVELER)
        val fixedClientId = "req_idempotency_fixed_key_123"

        val first = repository.createAndDispatchFoodRequest(
            passengerName = "Passenger Idempotent",
            trainNumber = "T001",
            trainName = "Express",
            coachNumber = "C1",
            seatDetail = "Berth 1",
            foodItem = sampleFoodItem,
            quantity = 2,
            customClientRequestId = fixedClientId
        )

        val retry = repository.createAndDispatchFoodRequest(
            passengerName = "Passenger Idempotent",
            trainNumber = "T001",
            trainName = "Express",
            coachNumber = "C1",
            seatDetail = "Berth 1",
            foodItem = sampleFoodItem,
            quantity = 2,
            customClientRequestId = fixedClientId
        )

        assertEquals("Retry must return the same order ID", first.id, retry.id)
        assertEquals(fixedClientId, retry.clientRequestId)

        val allInDb = db.foodRequestDao().getRequestByClientId(fixedClientId)
        assertNotNull(allInDb)
        assertEquals(first.id, allInDb?.id)
    }
}

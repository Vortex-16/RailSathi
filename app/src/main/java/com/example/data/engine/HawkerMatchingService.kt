package com.example.data.engine

import com.example.data.local.AppDatabase
import com.example.data.local.FoodRequestEntity
import com.example.data.local.VendorEntity
import com.example.data.model.OrderStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ClaimResult {
    data class Success(val requestId: Long, val vendorId: String) : ClaimResult()
    data class AlreadyClaimed(val requestId: Long, val currentVendorId: String?) : ClaimResult()
    data class Ineligible(val reason: String) : ClaimResult()
    data class InvalidState(val currentStatus: String) : ClaimResult()
    object NotFound : ClaimResult()
}

sealed class CoachChangeResult {
    data class Success(val requestId: Long, val updatedCoach: String) : CoachChangeResult()
    data class Rejected(val requestId: Long, val currentStatus: String, val reason: String) : CoachChangeResult()
    object NotFound : CoachChangeResult()
}

data class HawkerEligibilityCriteria(
    val trainNumber: String,
    val operatingDate: String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()),
    val targetCoach: String,
    val targetStationCode: String = "",
    val foodItemId: String,
    val strictCoachScoping: Boolean = true
)

class HawkerMatchingService(
    private val db: AppDatabase
) {
    private val foodRequestDao = db.foodRequestDao()
    private val vendorDao = db.vendorDao()

    /**
     * Scenario B (Test 5) & CL-04: Normalize coach codes to prevent string mismatches.
     * e.g., " c1 ", "c-1", "c1" -> "C1"
     * "gs 2", "gs-2", "GS-2" -> "GS-2"
     */
    fun normalizeCoach(coach: String): String {
        val trimmed = coach.trim().uppercase(Locale.US)
        val letters = trimmed.filter { it.isLetter() }
        val digits = trimmed.filter { it.isDigit() }
        return when {
            letters.isNotEmpty() && digits.isNotEmpty() -> {
                if (letters.length == 1) {
                    "$letters$digits"
                } else {
                    "$letters-$digits"
                }
            }
            else -> trimmed
        }
    }

    /**
     * Train service instance identifier generator (TL-04, CL-05).
     * Prevents record collisions across services sharing the same train number on different dates.
     */
    fun buildTrainInstanceId(trainNumber: String, operatingDate: String): String {
        val cleanTrain = trainNumber.trim()
        val cleanDate = operatingDate.trim().ifBlank {
            SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        }
        return "${cleanTrain}_$cleanDate"
    }

    /**
     * Scoped vendor candidate matching.
     * Scenario A & B: Prioritizes vendors on target coach and matching speciality.
     */
    suspend fun findEligibleVendors(criteria: HawkerEligibilityCriteria): List<VendorEntity> = withContext(Dispatchers.IO) {
        val normalizedCoach = normalizeCoach(criteria.targetCoach)
        val allTrainVendors = vendorDao.getVendorsInTrain(criteria.trainNumber).filter { it.isOnline }

        // Filter vendors by coach
        val coachVendors = allTrainVendors.filter { normalizeCoach(it.currentCoach) == normalizedCoach }

        if (coachVendors.isNotEmpty()) {
            val specialityMatches = coachVendors.filter { it.specialityItemId == criteria.foodItemId }
            if (specialityMatches.isNotEmpty()) {
                return@withContext sortByFairIncome(specialityMatches)
            }
            return@withContext sortByFairIncome(coachVendors)
        }

        // If strict coach scoping is disabled, fallback to train-level vendors
        if (!criteria.strictCoachScoping) {
            val trainSpecialityMatches = allTrainVendors.filter { it.specialityItemId == criteria.foodItemId }
            if (trainSpecialityMatches.isNotEmpty()) {
                return@withContext sortByFairIncome(trainSpecialityMatches)
            }
            return@withContext sortByFairIncome(allTrainVendors)
        }

        emptyList()
    }

    /**
     * Fair Income Distribution Algorithm.
     * Rewards vendors who have fewer sales today or who have waited longer.
     */
    private fun sortByFairIncome(vendors: List<VendorEntity>): List<VendorEntity> {
        val now = System.currentTimeMillis()
        return vendors.sortedBy { vendor ->
            val salesScore = vendor.todaySalesCount * 10
            val idleMinutes = if (vendor.lastSaleTimestamp > 0) {
                ((now - vendor.lastSaleTimestamp) / 60000).toInt()
            } else 999
            salesScore - idleMinutes
        }
    }

    /**
     * Atomic Vendor Claim Operation (Scenario A, Test A-05 / Critical Database Rule).
     * Two vendors accepting simultaneously: only one claim succeeds, preventing race conditions.
     */
    suspend fun claimOrder(
        requestId: Long,
        vendorId: String,
        vendorName: String,
        vendorCoach: String? = null,
        strictCoachCheck: Boolean = false
    ): ClaimResult = withContext(Dispatchers.IO) {
        val request = foodRequestDao.getRequestById(requestId) ?: return@withContext ClaimResult.NotFound

        // If the request was already assigned to another vendor, return AlreadyClaimed directly
        if (request.assignedVendorId != null && request.assignedVendorId != vendorId) {
            return@withContext ClaimResult.AlreadyClaimed(requestId, request.assignedVendorId)
        }

        // Check state machine eligibility
        val currentStatus = try {
            OrderStatus.valueOf(request.status)
        } catch (_: Exception) {
            OrderStatus.REQUESTED
        }

        if (!OrderStateMachine.canVendorClaim(currentStatus)) {
            return@withContext ClaimResult.InvalidState(request.status)
        }

        // Check coach scoping eligibility if requested
        if (strictCoachCheck && vendorCoach != null) {
            if (normalizeCoach(vendorCoach) != normalizeCoach(request.coachNumber)) {
                return@withContext ClaimResult.Ineligible("Vendor is in coach $vendorCoach, but request is scoped to coach ${request.coachNumber}")
            }
        }

        // Execute atomic Room DB claim
        val rowsUpdated = foodRequestDao.atomicClaimRequest(
            id = requestId,
            vendorId = vendorId,
            vendorName = vendorName
        )

        if (rowsUpdated > 0) {
            ClaimResult.Success(requestId, vendorId)
        } else {
            // Already claimed by another concurrent vendor race
            val updated = foodRequestDao.getRequestById(requestId)
            ClaimResult.AlreadyClaimed(requestId, updated?.assignedVendorId)
        }
    }

    /**
     * Scenario B (Tests 7-10): Change selected coach with lifecycle enforcement.
     * Changing coach before acceptance succeeds; changing after acceptance or during fulfillment is rejected.
     */
    suspend fun changeCoach(requestId: Long, newCoach: String): CoachChangeResult = withContext(Dispatchers.IO) {
        val request = foodRequestDao.getRequestById(requestId) ?: return@withContext CoachChangeResult.NotFound
        val normalizedNewCoach = normalizeCoach(newCoach)

        val currentStatus = try {
            OrderStatus.valueOf(request.status)
        } catch (_: Exception) {
            OrderStatus.REQUESTED
        }

        if (!OrderStateMachine.canModifyCoach(currentStatus)) {
            return@withContext CoachChangeResult.Rejected(
                requestId = requestId,
                currentStatus = request.status,
                reason = "Coach cannot be modified once vendor has accepted or order is in state ${request.status}."
            )
        }

        val rowsUpdated = foodRequestDao.updateCoachIfPending(requestId, normalizedNewCoach)
        if (rowsUpdated > 0) {
            CoachChangeResult.Success(requestId, normalizedNewCoach)
        } else {
            val fresh = foodRequestDao.getRequestById(requestId)
            CoachChangeResult.Rejected(
                requestId = requestId,
                currentStatus = fresh?.status ?: "UNKNOWN",
                reason = "Could not update coach. Order status changed concurrently."
            )
        }
    }

    /**
     * Scenario C: Station Scoping.
     * Ensures requests for station S2 are isolated from S3 and S4.
     */
    suspend fun getStationScopedPendingRequests(trainNumber: String, stationCode: String): List<FoodRequestEntity> = withContext(Dispatchers.IO) {
        val cleanStation = stationCode.trim().uppercase(Locale.US)
        foodRequestDao.getPendingRequestsByTrainAndStation(trainNumber, cleanStation)
    }

    /**
     * Scenario B: Coach Scoped Requests.
     * Vendor queries requests for T001 + C1. Confirms C2 and C3 are excluded.
     */
    suspend fun getCoachScopedPendingRequests(trainNumber: String, coachNumber: String): List<FoodRequestEntity> = withContext(Dispatchers.IO) {
        val normalizedCoach = normalizeCoach(coachNumber)
        val all = foodRequestDao.getActiveRequestsByCoach(normalizedCoach)
        // Or directly from DAO:
        foodRequestDao.getPendingRequestsByTrainAndCoach(trainNumber, normalizedCoach)
    }
}

package com.example.data.engine

import com.example.data.model.OrderStatus

sealed class TransitionResult {
    data class Success(val from: OrderStatus, val to: OrderStatus) : TransitionResult()
    data class InvalidTransition(val from: OrderStatus, val to: OrderStatus, val reason: String) : TransitionResult()
}

/**
 * Robust Order State Machine for RailSaathi.
 * Enforces strict server/client state transition contracts to prevent illegal state mutations,
 * race conditions, cross-coach regressions, and unauthorized modifications.
 */
object OrderStateMachine {

    private val allowedTransitions: Map<OrderStatus, Set<OrderStatus>> = mapOf(
        OrderStatus.REQUESTED to setOf(
            OrderStatus.MATCHING,
            OrderStatus.OFFERED_TO_VENDOR,
            OrderStatus.CUSTOMER_CANCELLED,
            OrderStatus.EXPIRED
        ),
        OrderStatus.MATCHING to setOf(
            OrderStatus.OFFERED_TO_VENDOR,
            OrderStatus.CUSTOMER_CANCELLED,
            OrderStatus.EXPIRED
        ),
        OrderStatus.OFFERED_TO_VENDOR to setOf(
            OrderStatus.VENDOR_ACCEPTED,
            OrderStatus.REJECTED,
            OrderStatus.CUSTOMER_CANCELLED,
            OrderStatus.EXPIRED
        ),
        OrderStatus.VENDOR_ACCEPTED to setOf(
            OrderStatus.PRICE_CONFIRMED,
            OrderStatus.VENDOR_CANCELLED,
            OrderStatus.CUSTOMER_CANCELLED
        ),
        OrderStatus.PRICE_CONFIRMED to setOf(
            OrderStatus.CUSTOMER_CONFIRMED,
            OrderStatus.CUSTOMER_CANCELLED,
            OrderStatus.EXPIRED
        ),
        OrderStatus.CUSTOMER_CONFIRMED to setOf(
            OrderStatus.FULFILLING,
            OrderStatus.CUSTOMER_CANCELLED,
            OrderStatus.VENDOR_CANCELLED
        ),
        OrderStatus.FULFILLING to setOf(
            OrderStatus.COMPLETED,
            OrderStatus.VENDOR_CANCELLED
        ),
        // Terminal states - no further transitions allowed
        OrderStatus.COMPLETED to emptySet(),
        OrderStatus.REJECTED to emptySet(),
        OrderStatus.EXPIRED to emptySet(),
        OrderStatus.CUSTOMER_CANCELLED to emptySet(),
        OrderStatus.VENDOR_CANCELLED to emptySet()
    )

    fun canTransition(from: OrderStatus, to: OrderStatus): Boolean {
        return allowedTransitions[from]?.contains(to) == true
    }

    fun validateTransition(from: OrderStatus, to: OrderStatus): TransitionResult {
        if (from == to) {
            return TransitionResult.Success(from, to)
        }
        val allowed = allowedTransitions[from] ?: emptySet()
        return if (allowed.contains(to)) {
            TransitionResult.Success(from, to)
        } else {
            val terminalReason = if (isTerminal(from)) "Current state $from is terminal and cannot be modified." else ""
            val reason = if (terminalReason.isNotEmpty()) terminalReason else "Illegal state transition from $from to $to. Allowed target states: $allowed"
            TransitionResult.InvalidTransition(from, to, reason)
        }
    }

    fun isTerminal(status: OrderStatus): Boolean {
        return status in setOf(
            OrderStatus.COMPLETED,
            OrderStatus.REJECTED,
            OrderStatus.EXPIRED,
            OrderStatus.CUSTOMER_CANCELLED,
            OrderStatus.VENDOR_CANCELLED
        )
    }

    /**
     * Scenario B (Tests 7-10): Coach modification is ONLY permitted before vendor acceptance.
     */
    fun canModifyCoach(status: OrderStatus): Boolean {
        return status == OrderStatus.REQUESTED || status == OrderStatus.MATCHING
    }

    /**
     * Scenario A (Tests A-03 to A-05): A vendor can only claim an order while it's in pending states.
     */
    fun canVendorClaim(status: OrderStatus): Boolean {
        return status in setOf(
            OrderStatus.REQUESTED,
            OrderStatus.MATCHING,
            OrderStatus.OFFERED_TO_VENDOR
        )
    }

    /**
     * Traveler cancellation is allowed until order is fulfilled or in a terminal state.
     */
    fun canCustomerCancel(status: OrderStatus): Boolean {
        return !isTerminal(status) && status != OrderStatus.FULFILLING
    }
}

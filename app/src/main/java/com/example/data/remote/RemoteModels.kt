package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T? = null,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "meta") val meta: ApiMeta? = null
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "code") val code: String,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class ApiMeta(
    @Json(name = "traceId") val traceId: String? = null,
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "source") val source: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateSessionRequest(
    @Json(name = "deviceId") val deviceId: String,
    @Json(name = "installationId") val installationId: String,
    @Json(name = "role") val role: String,
    @Json(name = "displayName") val displayName: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "language") val language: String,
    @Json(name = "isSeniorMode") val isSeniorMode: Boolean
)

@JsonClass(generateAdapter = true)
data class SessionResponseData(
    @Json(name = "sessionToken") val sessionToken: String
)

@JsonClass(generateAdapter = true)
data class RemoteStationDto(
    @Json(name = "code") val code: String,
    @Json(name = "name") val name: String,
    @Json(name = "state") val state: String = "",
    @Json(name = "zone") val zone: String = "",
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null
)

@JsonClass(generateAdapter = true)
data class RemoteTrainDto(
    @Json(name = "trainNumber") val trainNumber: String,
    @Json(name = "trainName") val trainName: String,
    @Json(name = "originStationCode") val originStationCode: String,
    @Json(name = "originStationName") val originStationName: String,
    @Json(name = "destStationCode") val destStationCode: String,
    @Json(name = "destStationName") val destStationName: String,
    @Json(name = "departureTime") val departureTime: String = "",
    @Json(name = "platform") val platform: String = "",
    @Json(name = "type") val type: String = "EMU Local"
)

@JsonClass(generateAdapter = true)
data class CreateFoodRequestPayload(
    @Json(name = "clientRequestId") val clientRequestId: String,
    @Json(name = "customerId") val customerId: String,
    @Json(name = "journeyId") val journeyId: String,
    @Json(name = "trainNumber") val trainNumber: String,
    @Json(name = "coachNumber") val coachNumber: String,
    @Json(name = "foodItemId") val foodItemId: String,
    @Json(name = "foodItemName") val foodItemName: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "note") val note: String = ""
)

@JsonClass(generateAdapter = true)
data class AcceptRequestPayload(
    @Json(name = "vendorId") val vendorId: String
)

@JsonClass(generateAdapter = true)
data class OfferPricePayload(
    @Json(name = "vendorId") val vendorId: String,
    @Json(name = "unitPrice") val unitPrice: Int
)

@JsonClass(generateAdapter = true)
data class ConfirmOrderPayload(
    @Json(name = "customerId") val customerId: String
)

@JsonClass(generateAdapter = true)
data class RemoteLiveTrainDto(
    @Json(name = "trainNumber") val trainNumber: String,
    @Json(name = "trainName") val trainName: String,
    @Json(name = "scheduledDeparture") val scheduledDeparture: String = "",
    @Json(name = "actualDeparture") val actualDeparture: String = "",
    @Json(name = "estimatedDeparture") val estimatedDeparture: String = "",
    @Json(name = "status") val status: String = "ON_TIME",
    @Json(name = "delayMinutes") val delayMinutes: Int = 0,
    @Json(name = "platform") val platform: String = "PF 1",
    @Json(name = "destination") val destination: String = ""
)

@JsonClass(generateAdapter = true)
data class SyncPayloadItem(
    @Json(name = "idempotencyKey") val idempotencyKey: String,
    @Json(name = "operationType") val operationType: String,
    @Json(name = "payload") val payload: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class SyncRequest(
    @Json(name = "items") val items: List<SyncPayloadItem>
)

@JsonClass(generateAdapter = true)
data class GoogleAuthRequest(
    @Json(name = "idToken") val idToken: String? = null,
    @Json(name = "googleId") val googleId: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "displayName") val displayName: String? = null,
    @Json(name = "photoUrl") val photoUrl: String? = null,
    @Json(name = "role") val role: String = "TRAVELER",
    @Json(name = "language") val language: String = "ENGLISH"
)

@JsonClass(generateAdapter = true)
data class GoogleUserData(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "displayName") val displayName: String,
    @Json(name = "photoUrl") val photoUrl: String? = null,
    @Json(name = "role") val role: String,
    @Json(name = "language") val language: String,
    @Json(name = "sessionToken") val sessionToken: String
)

@JsonClass(generateAdapter = true)
data class GoogleAuthResponseData(
    @Json(name = "user") val user: GoogleUserData? = null,
    @Json(name = "sessionToken") val sessionToken: String = ""
)

@JsonClass(generateAdapter = true)
data class TrainCoachesResponseData(
    @Json(name = "trainNumber") val trainNumber: String,
    @Json(name = "coaches") val coaches: List<String> = emptyList(),
    @Json(name = "source") val source: String = "live"
)

@JsonClass(generateAdapter = true)
data class RemoteProfileDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "displayName") val displayName: String? = null,
    @Json(name = "fullName") val fullName: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "isSeniorMode") val isSeniorMode: Boolean? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "preferredStation") val preferredStation: String? = null,
    @Json(name = "regularRoute") val regularRoute: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateProfilePayload(
    @Json(name = "displayName") val displayName: String,
    @Json(name = "phone") val phone: String = "",
    @Json(name = "language") val language: String = "ENGLISH",
    @Json(name = "isSeniorMode") val isSeniorMode: Boolean = false,
    @Json(name = "bio") val bio: String = "",
    @Json(name = "preferredStation") val preferredStation: String = "",
    @Json(name = "regularRoute") val regularRoute: String = ""
)

@JsonClass(generateAdapter = true)
data class RemoteBudgetDto(
    @Json(name = "userId") val userId: String,
    @Json(name = "monthYear") val monthYear: String,
    @Json(name = "monthlyLimit") val monthlyLimit: Double = 1500.0,
    @Json(name = "spent") val spent: Double = 0.0,
    @Json(name = "remaining") val remaining: Double = 1500.0
)

@JsonClass(generateAdapter = true)
data class UpdateBudgetPayload(
    @Json(name = "monthlyLimit") val monthlyLimit: Double,
    @Json(name = "spent") val spent: Double? = null
)


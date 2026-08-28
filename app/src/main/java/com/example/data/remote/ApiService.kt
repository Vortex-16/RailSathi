package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/health")
    suspend fun checkHealth(): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/session")
    suspend fun createSession(
        @Body request: CreateSessionRequest
    ): Response<ApiEnvelope<SessionResponseData>>

    @GET("api/stations/search")
    suspend fun searchStations(
        @Query("q") query: String
    ): Response<ApiEnvelope<List<RemoteStationDto>>>

    @GET("api/stations/{code}/trains")
    suspend fun getStationTrains(
        @Path("code") stationCode: String
    ): Response<ApiEnvelope<List<RemoteTrainDto>>>

    @POST("api/requests")
    suspend fun createFoodRequest(
        @Header("X-Idempotency-Key") idempotencyKey: String,
        @Body payload: CreateFoodRequestPayload
    ): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/requests/{id}/accept")
    suspend fun acceptFoodRequest(
        @Path("id") requestId: String,
        @Body payload: AcceptRequestPayload
    ): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/orders/{id}/price")
    suspend fun offerPrice(
        @Path("id") requestId: String,
        @Body payload: OfferPricePayload
    ): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/orders/{id}/confirm")
    suspend fun confirmOrder(
        @Path("id") requestId: String,
        @Body payload: ConfirmOrderPayload
    ): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/sync")
    suspend fun syncOfflineQueue(
        @Body syncRequest: SyncRequest
    ): Response<ApiEnvelope<Map<String, Any>>>
}

package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    @GET("api/lookup/stations")
    suspend fun getStationDirectory(): Response<ApiEnvelope<List<RemoteStationDto>>>

    @GET("api/stations/{code}/trains")
    suspend fun getStationTrains(
        @Path("code") stationCode: String
    ): Response<ApiEnvelope<List<RemoteTrainDto>>>

    @GET("api/stations/{code}/live")
    suspend fun getStationLiveBoard(
        @Path("code") stationCode: String
    ): Response<ApiEnvelope<List<RemoteLiveTrainDto>>>

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

    @POST("api/auth/google")
    suspend fun authenticateGoogle(
        @Body request: GoogleAuthRequest
    ): Response<ApiEnvelope<GoogleAuthResponseData>>

    @GET("api/trains/{number}/coaches")
    suspend fun getTrainCoaches(
        @Path("number") trainNumber: String
    ): Response<ApiEnvelope<TrainCoachesResponseData>>

    @GET("api/profile")
    suspend fun getProfile(
        @Header("X-User-Id") userId: String
    ): Response<ApiEnvelope<RemoteProfileDto>>

    @POST("api/profile")
    suspend fun updateProfile(
        @Header("X-User-Id") userId: String,
        @Body payload: UpdateProfilePayload
    ): Response<ApiEnvelope<RemoteProfileDto>>

    @DELETE("api/profile")
    suspend fun deleteProfile(
        @Header("X-User-Id") userId: String
    ): Response<ApiEnvelope<Map<String, Any>>>

    @GET("api/budget")
    suspend fun getBudget(
        @Header("X-User-Id") userId: String
    ): Response<ApiEnvelope<RemoteBudgetDto>>

    @POST("api/budget")
    suspend fun updateBudget(
        @Header("X-User-Id") userId: String,
        @Body payload: UpdateBudgetPayload
    ): Response<ApiEnvelope<RemoteBudgetDto>>

    @DELETE("api/budget")
    suspend fun resetBudget(
        @Header("X-User-Id") userId: String
    ): Response<ApiEnvelope<Map<String, Any>>>
}

package com.cara.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Mirrors the "API Endpoints (initial scope)" list in the backend's CLAUDE.md.
interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<UserDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<UserDto>

    @GET("recommendations")
    suspend fun getRecommendations(
        @Query("user_id") userId: Int,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("budget") budget: Double? = null,
        @Query("text_input") textInput: String? = null,
    ): Response<RecommendationsResponse>

    @POST("interactions")
    suspend fun postInteraction(@Body body: InteractionRequest): Response<Unit>

    @GET("places/{place_id}")
    suspend fun getPlace(@Path("place_id") placeId: Int): Response<PlaceDetailDto>

    @GET("users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: Int): Response<UserDto>

    @GET("users/{user_id}/preferences")
    suspend fun getPreferences(@Path("user_id") userId: Int): Response<List<PreferenceDto>>

    @GET("users/{user_id}/saved-places")
    suspend fun getSavedPlaces(@Path("user_id") userId: Int): Response<List<SavedPlaceDto>>

    @DELETE("users/{user_id}/saved-places/{place_id}")
    suspend fun deleteSavedPlace(
        @Path("user_id") userId: Int,
        @Path("place_id") placeId: Int,
    ): Response<Unit>

    @PUT("users/{user_id}/home-location")
    suspend fun updateHomeLocation(
        @Path("user_id") userId: Int,
        @Body body: LocationUpdateRequest,
    ): Response<Unit>

    @PUT("users/{user_id}/college-location")
    suspend fun updateCollegeLocation(
        @Path("user_id") userId: Int,
        @Body body: LocationUpdateRequest,
    ): Response<Unit>
}

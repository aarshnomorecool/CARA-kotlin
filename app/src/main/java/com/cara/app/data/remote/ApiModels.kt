package com.cara.app.data.remote

import com.google.gson.annotations.SerializedName

// Field names/casing here mirror the backend's Pydantic schemas exactly
// (snake_case on the wire, per FastAPI defaults) — see the backend repo's
// CLAUDE.md for the source of truth on this contract.

data class RecommendationsResponse(
    val context: RecommendationContextDto,
    val results: List<PlaceRecommendationDto>,
)

// The resolved context the recommendations were actually computed from -
// this is what drives the Home screen's Context Strip (see
// CLAUDE_android.md's "Signature element: the Context Strip").
data class RecommendationContextDto(
    @SerializedName("time_slot") val timeSlot: String,
    @SerializedName("weather_condition") val weatherCondition: String,
    @SerializedName("temp_celsius") val tempCelsius: Double,
    val emotion: String,
    // one of: HOME | COLLEGE | OUTSIDE
    @SerializedName("location_context") val locationContext: String,
)

data class PlaceRecommendationDto(
    @SerializedName("place_id") val placeId: Int,
    val name: String,
    val category: String,
    @SerializedName("sub_category") val subCategory: String?,
    val area: String?,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("approx_rating") val approxRating: Double?,
    @SerializedName("price_range") val priceRange: String?,
    @SerializedName("avg_price_inr") val avgPriceInr: Double?,
    @SerializedName("eco_friendly") val ecoFriendly: Boolean,
    // Rendered explanation string — never raw SHAP values (see backend's
    // Explainability Contract).
    val reason: String,
    @SerializedName("reason_tags") val reasonTags: List<String> = emptyList(),
    // Already-computed scoring components, exposed so Home's curated rows
    // (2026-07-16 layout) can re-sort this same list client-side (low
    // crowd/matches taste/fits budget) without extra API calls.
    @SerializedName("crowd_score") val crowdScore: Double,
    @SerializedName("budget_fit") val budgetFit: Double,
    @SerializedName("preference_weight") val preferenceWeight: Double,
)

data class InteractionRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("place_id") val placeId: Int,
    // one of: click | bookmark | dismiss | order_intent
    val action: String,
    @SerializedName("context_snapshot") val contextSnapshot: Map<String, Any?>? = null,
)

data class PreferenceDto(
    val category: String,
    val weight: Double,
    @SerializedName("last_updated") val lastUpdated: String,
)

data class LocationUpdateRequest(
    val lat: Double,
    val lon: Double,
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class UserDto(
    @SerializedName("user_id") val userId: Int,
    val name: String,
    val email: String,
    @SerializedName("home_lat") val homeLat: Double?,
    @SerializedName("home_lon") val homeLon: Double?,
    @SerializedName("college_lat") val collegeLat: Double?,
    @SerializedName("college_lon") val collegeLon: Double?,
)

// Backend's PlaceRead - deliberately a smaller field set than
// PlaceRecommendationDto, since saved places have no reason/reason_tags
// (those only exist relative to the recommendation query that produced
// them, not as standalone place data).
data class SavedPlaceDto(
    @SerializedName("place_id") val placeId: Int,
    val name: String,
    val category: String,
    val area: String?,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("approx_rating") val approxRating: Double?,
    @SerializedName("avg_price_inr") val avgPriceInr: Double?,
)

// Backend's GET /places/{id} - PlaceRead has more fields than this (lat/lon,
// sub_category, is_indoor, etc.) but Gson ignores JSON fields with no
// matching property, so only what PlaceDetailsScreen actually renders is
// declared here. No reason/reason_tags - those only exist relative to a
// /recommendations query, never as standalone place data (see
// RecommendationCache.kt's comment).
data class PlaceDetailDto(
    @SerializedName("place_id") val placeId: Int,
    val name: String,
    val category: String,
    val area: String?,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("approx_rating") val approxRating: Double?,
    @SerializedName("price_range") val priceRange: String?,
    @SerializedName("avg_price_inr") val avgPriceInr: Double?,
)

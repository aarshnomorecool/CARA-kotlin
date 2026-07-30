package com.cara.app.data.cache

import com.cara.app.data.remote.PlaceRecommendationDto

// In-memory cache of the most recent /recommendations results, keyed by
// place_id. reason/reason_tags only exist as part of a specific
// recommendation response (they're computed relative to the context that
// produced them, per CLAUDE(CARA-BACKEND).md's Explainability Contract) -
// there's no standalone "get this place's details" endpoint, so
// PlaceDetailsScreen reads from here instead of a second network call.
// In-memory only (cleared on process death); if a place isn't found here
// (e.g. deep link, or the app was killed), PlaceDetailsScreen shows a
// "not found" state rather than crashing.
object RecommendationCache {
    private val cache = mutableMapOf<Int, PlaceRecommendationDto>()

    fun put(results: List<PlaceRecommendationDto>) {
        results.forEach { cache[it.placeId] = it }
    }

    fun get(placeId: Int): PlaceRecommendationDto? = cache[placeId]
}

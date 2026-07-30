package com.cara.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cara.app.data.cache.RecommendationCache
import com.cara.app.data.local.CachedRecommendationEntity
import com.cara.app.data.local.DatabaseProvider
import com.cara.app.data.remote.NetworkModule
import com.cara.app.data.remote.RecommendationsResponse
import com.cara.app.data.session.UserSession
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: RecommendationsResponse, val isOffline: Boolean = false) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = DatabaseProvider.get(application).cachedRecommendationDao()
    private val gson = Gson()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadRecommendations(lat: Double, lon: Double, budget: Double? = null, textInput: String? = null) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val response = NetworkModule.apiService.getRecommendations(
                    userId = UserSession.userId,
                    lat = lat,
                    lon = lon,
                    budget = budget,
                    textInput = textInput,
                )
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _uiState.value = HomeUiState.Success(body, isOffline = false)
                    RecommendationCache.put(body.results)
                    dao.upsert(
                        CachedRecommendationEntity(
                            userId = UserSession.userId,
                            responseJson = gson.toJson(body),
                            cachedAt = System.currentTimeMillis(),
                        )
                    )
                } else {
                    fallBackToCache("Couldn't load recommendations (${response.code()})")
                }
            } catch (e: Exception) {
                // Network boundary - offline, timeout, DNS failure, etc. are
                // all expected here, not exceptional; fall back to cache per
                // CLAUDE_android.md's "Local cache" requirement.
                fallBackToCache(e.message ?: "Network error")
            }
        }
    }

    private suspend fun fallBackToCache(errorMessage: String) {
        val cached = dao.get(UserSession.userId)
        if (cached != null) {
            val body = gson.fromJson(cached.responseJson, RecommendationsResponse::class.java)
            _uiState.value = HomeUiState.Success(body, isOffline = true)
            RecommendationCache.put(body.results)
        } else {
            _uiState.value = HomeUiState.Error(errorMessage)
        }
    }
}

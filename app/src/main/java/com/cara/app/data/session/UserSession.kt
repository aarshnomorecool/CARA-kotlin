package com.cara.app.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

private const val PREFS_NAME = "cara_session"
private const val KEY_USER_ID = "user_id"
private const val NO_USER = -1

// userId stays a non-null Int (not Int?) so every existing call site
// (HomeViewModel, PlaceDetailsScreen, ProfileScreen, etc.) that already
// reads UserSession.userId as a plain Int keeps compiling unchanged -
// navigation gating (see CaraNavHost/MainActivity) guarantees those screens
// are never reached while isLoggedIn is false, so NO_USER (-1) is never
// actually rendered against.
object UserSession {
    private lateinit var prefs: SharedPreferences
    var userId by mutableIntStateOf(NO_USER)
        private set

    val isLoggedIn: Boolean get() = userId != NO_USER

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getInt(KEY_USER_ID, NO_USER)
    }

    fun login(id: Int) {
        userId = id
        prefs.edit().putInt(KEY_USER_ID, id).apply()
    }

    fun logout() {
        userId = NO_USER
        prefs.edit().remove(KEY_USER_ID).apply()
    }
}

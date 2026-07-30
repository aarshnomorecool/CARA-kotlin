package com.cara.app.data.session

// TODO: no login/register screen exists yet - it was never part of
// CLAUDE_android.md's original 5-screen scope (Home/PlaceDetails/
// ContextInput/Profile/Saved). Hardcoded to the one real test user created
// against the live backend (POST /auth/register, user_id=1) until a real
// session/auth flow is designed. Replace this with real persisted session
// state (e.g. DataStore) once that's built.
object UserSession {
    const val userId: Int = 1
}

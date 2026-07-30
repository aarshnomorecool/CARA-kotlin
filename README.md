# CARA — Android App

Native Kotlin + Jetpack Compose frontend for **CARA** (Context-Aware Recommendation
Agent), a final-year B.Tech FYP: a real-time, explainable recommendation system for
places in Nagpur. This app collects context (GPS, mood, budget), sends it to the
[CARA backend](https://github.com/aarshnomorecool/CARA-backend-) (FastAPI, deployed on
Cloud Run), and presents ranked recommendations with plain-language explanations.

See `CLAUDE_android.md` in the backend repo for the full design system, tech stack, and
API contract this app implements.

## Tech stack

- Kotlin, Jetpack Compose, Material 3 (custom design tokens, not stock Material)
- MVVM — ViewModel + StateFlow, one ViewModel per screen
- Retrofit + OkHttp + Gson for networking
- Room for offline recommendation caching
- Navigation Compose
- Coil for image loading
- Google Maps SDK + maps-compose for map pins

## Current progress

- [x] **Login / Register** — email+password, backed by the backend's existing
      bcrypt auth endpoints, session persisted locally (survives app restart),
      gates navigation so Home/Saved/Profile are unreachable while logged out
- [x] **Home** — Context Strip (live weather/time/location/mood), category
      quick-filter tiles, a swipeable top-3 spotlight carousel, and curated rows
      (best match / low crowd / matches taste / fits budget), all from one
      `/recommendations` call
- [x] **Context Input** — mood text + budget bottom sheet
- [x] **Place Details** — photo, rating/price, full XAI reason chips, bookmark
      toggle, a map pin for the place's location. Resolves by `place_id`
      regardless of how the screen was reached (Home, Saved, or a future deep
      link), not just from an in-memory cache
- [x] **Saved** — bookmarked places list
- [x] **Profile** — user info, home/college locations with map pins,
      per-category preference bars, dark/light theme toggle, log out
- [x] Backend deployed and live on Cloud Run, with working CI — every push to
      the backend's `main` now auto-deploys
- [x] Offline fallback via Room cache with a "showing saved results" banner

## In progress / next up

- [ ] **Map pins need a real API key** — the map Composables are wired up on
      Place Details and Profile, but `local.properties`' `MAPS_API_KEY` is
      still unset. Needs a Maps SDK for Android key from Google Cloud Console,
      restricted to this app's package name + debug SHA-1 (get the SHA-1 via
      Android Studio's Gradle panel → app → Tasks → android → `signingReport`).

## Known gaps (not yet started)

- No automated tests (unit or instrumentation)
- Light theme has a minor contrast shortfall for small text drawn on the
  citrus accent color (documented, not yet fixed — see project notes)
- No "dismiss" interaction wired up in the UI (only click/bookmark currently
  feed the preference-learning system; the backend supports dismiss already)
- Gradle wrapper jar isn't committed — first build needs Android Studio to
  bootstrap it, or run `gradle wrapper` once with a local Gradle install

## Setup

1. Copy `local.properties.example` → `local.properties`, fill in your
   `MAPS_API_KEY` (Android Studio will add `sdk.dir` itself on first sync).
2. `app/build.gradle.kts`'s `debug`/`release` `BASE_URL` already points at the
   live Cloud Run backend — no local backend needed to run the app.
3. Open in Android Studio, sync Gradle, run on a device or emulator.

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
- Google Maps SDK (dependency present, not yet wired into any screen)

## Current progress

- [x] **Home** — Context Strip (live weather/time/location/mood), category
      quick-filter tiles, a swipeable top-3 spotlight carousel, and curated rows
      (best match / low crowd / matches taste / fits budget), all from one
      `/recommendations` call
- [x] **Context Input** — mood text + budget bottom sheet
- [x] **Place Details** — photo, rating/price, full XAI reason chips, bookmark
      toggle. Resolves by `place_id` regardless of how the screen was reached
      (Home, Saved, or a future deep link), not just from an in-memory cache
- [x] **Saved** — bookmarked places list
- [x] **Profile** — user info, home/college locations (text only, no map yet),
      per-category preference bars, dark/light theme toggle
- [x] Backend deployed and live on Cloud Run — app talks to it over the public
      internet, no longer tied to a shared dev WiFi network
- [x] Offline fallback via Room cache with a "showing saved results" banner

## In progress / next up

- [ ] **Real authentication** — backend already has working `POST /auth/register` /
      `POST /auth/login` (bcrypt-hashed, tokenless by design). Missing piece is
      purely this app: a Login/Register screen, persisting the logged-in
      `user_id` locally, and gating navigation so login comes before Home.
      Currently `UserSession.userId` is hardcoded to `1`, one shared test user.
- [ ] **Map pins** — Place Details and Profile's home/college locations are
      plain lat/lon text right now. Google Maps SDK is the confirmed provider
      (dependency + manifest entry already present) — needs an actual map
      Composable added to both screens, and a real `MAPS_API_KEY` in
      `local.properties` (currently unset — see `local.properties.example`).
- [ ] **Fix backend CI** — the backend's Cloud Run auto-deploy trigger is
      currently broken (see the backend repo's README/memory for why); backend
      changes need a manual redeploy for now.

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

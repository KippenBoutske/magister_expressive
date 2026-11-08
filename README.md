# Magister Expressive

Unofficial Android client for [Magister](https://www.magister.net) (Schoolmaster/Iddink), built with
Jetpack Compose and **Material 3 Expressive** (material3 `1.4.0`), including:

- **Material 3 Expressive** — `MaterialExpressiveTheme` + `MotionScheme.expressive()`,
  `MediumFlexibleTopAppBar`, `LoadingIndicator`, `CircularWavyProgressIndicator`, dynamic color,
  rounded expressive shapes.
- **Haptics** — `HapticFeedback` wrappers (`click`, `confirm`, `reject`, `tick`) wired to buttons,
  list items, bottom-nav switches, pull-to-refresh and error states (`ui/haptics/Haptics.kt`).
- **Android Live Updates** (Android 16 / API 36) — the current lesson is posted as a *promoted
  ongoing* notification with `NotificationCompat.ProgressStyle` (segmented progress),
  `setRequestPromotedOngoing(true)` and `setShortCriticalText("nog X min")` (status-bar chip).
  A WorkManager worker refreshes it every 15 min (`liveupdate/`). Requires
  `android.permission.POST_PROMOTED_NOTIFICATIONS` (declared) and the runtime
  `POST_NOTIFICATIONS` permission (requested in `MainActivity`).
- **Animations** — shared-element transitions between agenda list and detail
  (`SharedTransitionLayout` + `sharedBounds`), spring-based screen transitions driven by
  `MaterialTheme.motionScheme`, `animateItemPlacement` in lists, `PullToRefreshBox`,
  `AnimatedContent` for the grade average, animated lesson-progress ring.

## Screens

- **Login** — school tenant + OAuth2 authorization-code flow with PKCE in a WebView
  (redirect scheme `magisterex://oauth/callback`), token exchange + refresh via
  `TokenAuthenticator`. A **Demo modus** seeds sample data without credentials.
- **Agenda** — current lesson card with live progress, day-grouped sticky headers, pull-to-refresh.
- **Cijfers** — grade chips colored by value, per-subject grouping, animated average.
- **Huiswerk** — homework from appointments, locally persisted done-state with haptics.
- **Detail** — shared-element title, homework text, "start Live Update" action.

## Magister API

The networking layer talks to the community-documented Magister endpoints
(`data/remote/MagisterApi.kt`, base `https://<school>.magister.net`):

- `POST /api/token` — `authorization_code` (PKCE) and `refresh_token` grants
- `GET /api/v1/leerling` — current student
- `GET /api/v1/leerlingen/{id}/afspraken?van=..&tot=..` — agenda/homework
- `GET /api/v1/leerlingen/{id}/cijfers/kolommen?actief=true` and `GET .../cijfers?kolomId=..` — grades

Note: Iddink Digital requires third-party API users to be accredited and to use a registered
`client_id` / `redirect_uri`. Adjust `MagisterConfig.CLIENT_ID` / `REDIRECT_URI` to your own
registered values. All endpoints are isolated in `MagisterApi` / `MagisterRepository` if they
need to change.

## Build

- JDK 17, Android Studio recent stable, `compileSdk 36`, `minSdk 26`, `targetSdk 36`
- AGP `8.13.0`, Kotlin `2.2.20`, Compose BOM `2025.09.00`, material3 `1.4.0`, core-ktx `1.17.0`
- Gradle wrapper is configured for Gradle `8.14.2`; if `gradlew`/`gradle-wrapper.jar` are
  missing, run once locally: `gradle wrapper --gradle-version 8.14.2`
- Then: `./gradlew assembleDebug` and run on a device/emulator (Live Updates need Android 16+)

Sessions (tokens) are stored in app-private DataStore (`data/session/SessionStore.kt`).

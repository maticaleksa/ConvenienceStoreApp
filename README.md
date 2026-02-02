# Convenience Store Stock Management

## Setup Instructions
1. Open the project in Android Studio.
2. Let Gradle sync finish (Android Studio will prompt automatically).
3. Run the `app` configuration on an emulator or device (API 24+ recommended).

Optional:
- If you want a clean build: `./gradlew clean assembleDebug`

## Features Implemented
- **Authentication (mocked)**: login with hardcoded credentials (username: `admin`, password: `admin`).
- **Dashboard**: low-stock overview and recent products/transactions summary.
- **Products**: list with search, category filter, and filter indicator.
- **Product Add/Edit**: create or update products with validation.
- **Suppliers**: list with search and supplier edit.
- **Transactions**: history list with type/date filtering and filter indicator.
- **Stock Management**: add stock or record sales, with product search/autocomplete.
- **Offline handling**: no‑internet banner + error feedback.
- **Loading/empty states** across lists.

Bonus/UI Enhancements:
- Swipe‑to‑refresh on applicable screens.
- Viewmodel unit tests
- Offline mode

## Architecture Overview
- **Multi‑module** structure:
  - `app`: UI (Fragments, ViewModels, adapters)
  - `domain`: models + use cases
  - `data`: repositories + Room data sources
  - `network`: mocked auth/network layer
  - `core-arch`: shared architecture utilities
- **MVVM** with `StateFlow` for UI state and `SharedFlow` for one‑off events.
- **Repository pattern** abstracting data sources.
- **Room** for local persistence (single source of truth).
- **Hilt** for dependency injection.
- **SyncCoordinator**: centralizes sync/error reporting via channels so UI can surface sync state consistently.
- **Fake network layer**: Ktor `MockEngine` + JSON serialization simulate API calls and responses, enabling repository sync flows without a real backend.

## Assumptions / Design Decisions
- Authentication is intentionally mocked for assessment purposes (no real backend).
- Local database is the source of truth; UI updates are driven by Flow.
- Search and filter are client‑side.
- Stock transactions are applied remotely first, then persisted locally on success.
- The app uses a MaterialComponents theme and shared styles for consistency.

## Known Issues / Limitations
- Authentication is fake (single hardcoded user).
- Sync logic is implemented, but it targets the fake network layer (MockEngine) rather than a real backend; offline state is simulated via connectivity checks.
- Selection handle styling may vary across OEM devices (custom handles are used for visibility).
- No automated tests included.
- UI polish is limited due to time constraints; layouts prioritize correctness and state handling over visual refinement.  
  However, the UI layer is intentionally thin and well-isolated (state-driven via ViewModels), making it easy to swap or upgrade the UI (e.g., redesigns or migration to Jetpack Compose) without impacting domain or data layers.


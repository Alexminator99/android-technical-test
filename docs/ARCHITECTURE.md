# Architecture Choices

A summary of the architecture decisions, design patterns, and library choices I applied while restructuring the Leboncoin recruitment test application.

---

## 1. Architecture Overview

I restructured the original monolithic app into a **Clean Architecture** layout with a **multi-module Gradle project**. The guiding principle is the **dependency rule**: the domain layer is the innermost layer and depends on nothing — outer layers (data, network, UI) depend inward. This produces several concrete benefits:

- **Build isolation** -- changing a file in `:core:network` does not recompile `:feature:albums` or `:core:database`.
- **Enforced boundaries** -- the domain layer is a pure Kotlin/JVM module (`java-library` plugin, no Android dependency). It is physically impossible for a use case to import an Android class.
- **Testability** -- every layer can be tested in isolation with fakes or mocks; no Robolectric required for business logic.

The project contains **8 modules** total:

| Module | Plugin | Responsibility |
|---|---|---|
| `:app` | `com.android.application` | Hilt entry point, navigation host, Timber initialization |
| `:feature:albums` | `com.android.library` | Album list and detail screens, ViewModels, MVI contracts |
| `:core:domain` | `java-library` | Domain model (`Album`), use cases, repository interfaces, `Result`/`DataError` types — the innermost layer with zero external dependencies |
| `:core:data` | `com.android.library` | Repository implementations, DTO-to-Entity mappers |
| `:core:network` | `com.android.library` | Retrofit service, DTOs, `NetworkModule` |
| `:core:database` | `com.android.library` | Room database, DAOs, entities, `DatabaseModule` |
| `:core:analytics` | `com.android.library` | Analytics abstraction, `TimberAnalyticsHelper`, `TestAnalyticsHelper` |
| `:core:ui` | `com.android.library` | Shared Compose utilities: `UiText`, `ObserveAsEvents`, `DataError` extensions |

---

## 2. Module Structure

```
:app
 +-- :feature:albums
 |    +-- :core:domain       (innermost — zero dependencies)
 |    +-- :core:analytics
 |    +-- :core:ui           --> :core:domain
 |
 +-- :core:data
 |    +-- :core:domain
 |    +-- :core:network
 |    +-- :core:database
 |
 +-- :core:analytics
 +-- :core:ui
 +-- :core:network
 +-- :core:database
 +-- :core:domain
```

Key observations:

- `:core:domain` is a **pure JVM** module (`java-library` plugin) with **zero external module dependencies**. It contains the `Album` entity, use cases, repository interfaces, and the `Result`/`DataError` types. No Android framework imports are possible — this is enforced at the build system level.
- `:feature:albums` depends on domain abstractions (`:core:domain`) but never on concrete implementations (`:core:data`, `:core:network`, `:core:database`). Hilt wires the implementations at the `:app` level.
- `:core:network` and `:core:database` are sibling modules that never depend on each other. Only `:core:data` bridges them.
- `:core:ui` depends on `:core:domain` only for the `DataError` type, so it can map domain errors to `UiText` string resources.

---

## 3. Design Patterns

### MVI (Model-View-Intent)

Every screen follows the MVI pattern with three clearly separated contracts:

- **Action** (`sealed interface AlbumsAction`) -- represents what the user did (tap, refresh, toggle favorite).
- **State** (`data class AlbumsUiState`) -- a single immutable snapshot exposed as `StateFlow`. The UI recomposes reactively via `collectAsStateWithLifecycle()`.
- **Event** (`sealed interface AlbumsEvent`) -- one-time side effects (navigation, snackbar) delivered through a `Channel` and consumed via `ObserveAsEvents`.

The ViewModel exposes a single `onAction(action)` entry point. This makes it trivial to trace every user interaction and simplifies testing -- call `onAction`, then assert on `state` and `events`.

### Repository Pattern (Offline-First)

Room is the **single source of truth**. The data flow is:

1. UI observes `dao.getAlbums()` as a `Flow<List<AlbumEntity>>`.
2. On refresh, the repository calls `api.getAlbums()` and upserts results into Room.
3. Room's reactive `Flow` automatically emits the updated data to the UI.

This offline-first approach means the app shows cached data immediately and updates seamlessly when the network responds. Network refreshes use a custom `INSERT ... ON CONFLICT(id) DO UPDATE SET` query that explicitly omits the `isFavorite` column from the update clause. This ensures favorites survive refreshes -- a standard `@Upsert` would overwrite `isFavorite` back to `false` since the API has no concept of favorites.

### Use Cases

Each use case has a **single responsibility** and is injectable via `@Inject constructor`. I use `operator fun invoke()` so call sites read naturally:

```kotlin
val albums = getAlbumsUseCase()           // returns Flow<List<Album>>
val result = refreshAlbumsUseCase()        // returns EmptyResult<DataError>
val result = toggleFavoriteUseCase(albumId) // returns EmptyResult<DataError>
```

### Dual Error Handling with Retry

Error presentation adapts to context:

- **Empty list + error** -- full-screen error message with a "Retry" button. The error lives in `AlbumsUiState.error` as persistent state, so it survives recomposition and is visible until the user acts.
- **Cached data + error** -- a Snackbar with a "Retry" action label. The cached list remains visible while the error is non-blocking. The error is delivered as a one-time `AlbumsEvent.ShowError` via Channel.

Both paths converge on the same `refreshAlbums()` call when the user taps retry. The same pattern applies to the detail screen -- the error state shows a retry button that re-triggers `loadAlbum()`.

### ScreenRoot / Screen Separation

I split every Compose screen into two composables:

- **`AlbumsScreenRoot`** -- wired to the ViewModel via `hiltViewModel()`. Collects state, observes events, passes lambdas down.
- **`AlbumsScreen`** -- a **stateless** composable that receives `state` and `onAction` as parameters. This enables `@Preview` without a ViewModel and makes Compose UI testing straightforward.

---

## 4. Library Choices

### Hilt

**Why chosen:** Compile-time dependency injection with full Android lifecycle integration. `@HiltViewModel` makes ViewModel injection seamless, and `hiltViewModel()` in Compose eliminates manual factory boilerplate. Hilt catches missing bindings at compile time, not at runtime.

**Why not Koin:** Koin resolves dependencies at runtime, so missing bindings only surface as crashes in production. For a production app, compile-time safety is non-negotiable.

### Room

**Why chosen:** Reactive persistence via `Flow`, compile-time SQL validation, and support for custom `ON CONFLICT` queries that preserve local-only columns (like `isFavorite`) during network refreshes. Room's `Flow`-based DAOs make the offline-first pattern trivial -- the UI auto-updates when the database changes.

**Why not DataStore:** DataStore is designed for key-value preferences, not relational data with 5000 items. A structured query like "get album by ID" requires a relational database.

### Navigation Compose

**Why chosen:** Type-safe routes via `@Serializable` data classes (`AlbumDetailRoute(albumId: Int)`). Single-Activity architecture with `NavHost`. `SavedStateHandle.toRoute<T>()` extracts arguments with full type safety -- no more string-based `getInt("albumId")`.

**Why not multi-Activity:** The original codebase had a `DetailsActivity` with `MAIN/LAUNCHER` intent filter (a bug). Multi-Activity fragments the back stack and makes shared state management painful. A single `NavHost` is the modern Android standard.

### Retrofit + OkHttp

**Why chosen:** Industry standard HTTP client with native `suspend fun` support. OkHttp's interceptor architecture makes it trivial to add header-level debug logging only in debug builds (using `Level.HEADERS` to avoid dumping 5000-item response bodies into logcat). Retrofit's annotation-based API definition is concise and well-understood.

**Why not Ktor:** While Ktor is Kotlin-native, Retrofit's ecosystem for Android (interceptors, converters, tooling) is significantly more mature. For a pure Android project, Retrofit remains the pragmatic choice.

### Kotlinx Serialization

**Why chosen:** No reflection, compile-time code generation, multiplatform-ready. `@Serializable` annotation on DTOs generates efficient serialization code at compile time. Combined with the Retrofit converter, it handles JSON parsing without runtime overhead.

**Why not Moshi or Gson:** Both rely on reflection (or require separate codegen plugins). Kotlinx Serialization is the Kotlin-first solution and is ready for KMP if the project expands.

### Coil

**Why chosen:** Kotlin-first image loading with native `AsyncImage` composable. Coroutine-based pipeline. Coil 3 uses OkHttp under the hood (`coil-network-okhttp`), so it shares the same HTTP stack as Retrofit.

**Why not Glide:** Glide's API is Java-centric and its Compose integration is a wrapper. Coil was built for Kotlin and Compose from the ground up.

### Timber

**Why chosen:** Lightweight logging facade. `Timber.plant(DebugTree())` in debug builds provides automatic tag management. In release builds, no tree is planted, so all log calls become no-ops with zero overhead.

**Why not `android.util.Log`:** No tag management, no way to disable logging in release builds, and no extensibility for crash reporting integration.

### Spark Design System

**Why chosen:** Leboncoin's own design system. Using `SparkTheme`, `Scaffold`, and `Spinner` from Spark ensures the app matches the company's design language. This was a deliberate choice to show awareness of the company's tooling.

### MockK + Turbine

**Why chosen:** MockK is Kotlin-first with native support for `suspend fun`, `Flow`, and `StateFlow`. Turbine provides `awaitItem()` for deterministic Flow testing -- no flaky `delay()`-based assertions.

**Why not Mockito:** Mockito's Kotlin support is bolted on. It struggles with coroutines, sealed classes, and inline functions. MockK handles all of these natively.

### LeakCanary (debug only)

**Why chosen:** Automatic memory leak detection during development. Added as `debugImplementation` so it has zero impact on release builds.

---

## 5. Utility Patterns

### `Result<D, E>`

A typed result wrapper that replaces Kotlin's `kotlin.Result` (which erases the error type) and avoids try/catch proliferation:

```kotlin
sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : Error>(val error: E) : Result<Nothing, E>
}
```

Extension functions (`map`, `onSuccess`, `onError`, `asEmptyDataResult`) enable fluent chaining. `EmptyResult<E>` is a typealias for `Result<Unit, E>`, used for operations that either succeed without data or fail with an error.

### `DataError`

A sealed interface hierarchy with `Network` and `Local` enums:

```kotlin
sealed interface DataError : Error {
    enum class Network : DataError { NO_INTERNET, REQUEST_TIMEOUT, SERVER_ERROR, SERIALIZATION, UNKNOWN }
    enum class Local : DataError { DATABASE_ERROR, NOT_FOUND, UNKNOWN }
}
```

The key benefit: `when` expressions over `DataError` are **exhaustive**. If I add a new error variant, the compiler flags every unhandled case. This is impossible with string error messages or generic exceptions.

### `UiText`

Bridges domain errors to user-facing strings without leaking Android resources into the domain layer:

```kotlin
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(@StringRes val id: Int, val args: Array<Any> = arrayOf()) : UiText
}
```

Two `asString()` overloads: one `@Composable` (for Compose UI) and one taking `Context` (for non-Compose contexts like `ObserveAsEvents`). The `DataError.toUiText()` extension in `:core:ui` maps every error variant to a string resource -- exhaustively.

### `ObserveAsEvents`

A lifecycle-aware composable for consuming one-time events from a `Channel`:

```kotlin
@Composable
fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEvent)
            }
        }
    }
}
```

This replaces the naive `LaunchedEffect { flow.collect {} }` pattern, which can miss events during configuration changes or process death. Using `repeatOnLifecycle(STARTED)` ensures events are only processed when the UI is visible.

### NIA-Inspired Analytics Pattern

Inspired by the Now in Android project:

- **Single-method interface** (`AnalyticsHelper.logEvent`) -- keeps the contract minimal.
- **Extension functions per feature** (`AnalyticsHelper.logAlbumSelected()`, `logAlbumFavoriteToggled()`) -- feature-specific analytics without bloating the interface.
- **`CompositionLocal`** (`LocalAnalyticsHelper`) -- Compose screens access analytics without explicit injection.
- **`TestAnalyticsHelper`** -- captures events in a list for test assertions (`hasLogged(event)`).
- **`TimberAnalyticsHelper`** -- production implementation that logs to Timber. No `Context` dependency (the original code stored a `Context` reference, which was a memory leak).

---

## 6. Bug Fixes

The original codebase contained 10 bugs that I identified and fixed during the restructuring:

| # | Bug | Original Location | Fix Applied |
|---|-----|-------------------|-------------|
| 1 | `GlobalScope.launch` used for coroutines | `AlbumsViewModel` | Replaced with `viewModelScope.launch` -- coroutines are now cancelled when the ViewModel is cleared, preventing memory leaks and work-after-destroy crashes |
| 2 | `MutableSharedFlow` with no replay for state | `AlbumsViewModel` | Replaced with `MutableStateFlow(AlbumsUiState())` -- new subscribers immediately receive the current state instead of seeing nothing until the next emission |
| 3 | Exceptions silently swallowed | `AlbumsViewModel` | `Result.Error` now maps to `AlbumsEvent.ShowError(UiText)`, surfaced as a Snackbar. Users see "No internet connection" instead of a silent blank screen |
| 4 | Inverted logging condition (`!BuildConfig.DEBUG`) | `DataModule` | Moved logging setup to `NetworkModule` with correct `if (BuildConfig.DEBUG)` check. Debug logging now only appears in debug builds, not in release |
| 5 | `DetailsActivity` declared with `MAIN/LAUNCHER` | `AndroidManifest.xml` | Deleted `DetailsActivity` entirely. Detail screen is now a Compose destination within the single-Activity `NavHost` |
| 6 | `DetailsActivity` with `exported=true` | `AndroidManifest.xml` | Deleted along with bug #5. No more security risk from an exported activity that accepts arbitrary intents |
| 7 | No data passed to detail screen | `MainActivity` intent launch | Type-safe `AlbumDetailRoute(albumId = albumId)` via Navigation Compose. `SavedStateHandle.toRoute<AlbumDetailRoute>()` extracts the ID in the ViewModel |
| 8 | `AnalyticsHelper` stores `Context` reference | `AnalyticsHelper` class | Replaced with `TimberAnalyticsHelper` that uses Timber (no `Context` needed). Eliminates the Activity/Application context leak |
| 9 | Test constructor mismatch with ViewModel | `AlbumsViewModelTest` | Deleted the old test. Wrote new behavior-oriented tests with the updated constructor (use cases + analytics helper) |
| 10 | Useless `androidTest` dependencies in data module | `data/build.gradle.kts` | Old monolithic `data` module deleted. New `:core:data` module has only the dependencies it actually needs |

---

## 7. Testing Strategy

### ViewModel Tests

Behavior-oriented testing following the pattern "when user does X, state becomes Y and event Z is emitted":

- **MockK** for use case mocking -- `coEvery { refreshAlbumsUseCase() } returns Result.Success(Unit)`.
- **`TestAnalyticsHelper`** for analytics assertions -- `assertThat(analyticsHelper.hasLogged(event)).isTrue()`.
- **Turbine** for `Channel`-based event testing -- `viewModel.events.test { awaitItem() shouldBe AlbumsEvent.NavigateToDetail(1) }`.
- **`UnconfinedTestDispatcher`** from `kotlinx-coroutines-test` to make coroutine execution deterministic.

### Repository Tests

I use **fakes** (`FakeAlbumDao`, `FakeAlbumApiService`) instead of mocks. Fakes implement the real interface with in-memory data structures, which catches integration bugs that mocks would miss. For example, a fake DAO with a `MutableList` verifies that `@Upsert` preserves favorites, something a mock would never catch because it does not execute real logic.

### Mapper Tests

Pure function assertions on the `AlbumDto -> AlbumEntity -> Album` mapping chain. These are fast, deterministic, and catch field-mapping regressions (e.g., accidentally swapping `url` and `thumbnailUrl`).

### Compose UI Tests

Stateless `*Screen` composables are tested with Robolectric in `src/test/` (no emulator needed). The test creates the composable with sample state and a captured action lambda, then asserts on rendered content and user interactions.

---

## 8. Future Evolution

### Convention Plugins

The current `build.gradle.kts` files share significant boilerplate (compile SDK, JVM target, Hilt setup). I would extract this into a `build-logic/` module with convention plugins like `leboncoin.android.library` and `leboncoin.android.feature`, reducing each module's build file to ~10 lines.

### Pagination

The API returns 5000 items. Currently all are loaded into memory. The next step would be **Paging 3** with `RemoteMediator` (for offline-first paging from Room) and `PagingSource`. This would reduce memory usage and initial load time significantly.

### Search

A `SearchBar` with local Room FTS (Full-Text Search) would let users filter the 5000 albums instantly. Since Room is already the single source of truth, adding an FTS table is straightforward.

### Deep Linking

Navigation Compose already uses `@Serializable` routes, which map naturally to deep link URIs. Adding `deepLinks` to `composable<AlbumDetailRoute>` would enable direct navigation from notifications or external links.

### CI/CD

GitHub Actions for automated build, test, and lint on every pull request. Release APK generation on tag push. The version catalog and single `libs.versions.toml` make dependency updates atomic across all modules.

### Feature Flags

Firebase Remote Config for gradual feature rollouts. The modular architecture makes this easy -- a feature module can check a flag before registering its navigation graph.

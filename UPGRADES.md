# BacklogOrder - Code Audit & Upgrades Report

This report documents bugs, architectural inconsistencies, deprecations, and optimization opportunities identified across the BacklogOrder codebase and architecture.

---

## 1. Architecture & Dependency Injection

### [Architecture Mismatch] Hilt vs. Manual AppContainer
- **Observation**: The project includes Hilt dependencies (`libs.hilt.android`, `libs.hilt.android.compiler`) in [build.gradle.kts](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/build.gradle.kts) and defines a Hilt module in [`AppModule.kt`](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/di/AppModule.kt). However, [`BacklogOrderApplication.kt`](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/BacklogOrderApplication.kt) is **not** annotated with `@HiltAndroidApp`. Instead, the app manually instantiates `DefaultAppContainer` (`AppContainer`) and uses custom `ViewModelProvider.Factory` implementations across screens (e.g., in [`HomeScreen.kt`](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/ui/home/HomeScreen.kt)).
- **Impact**: Dead code/unused dependency container (`AppModule`) or incomplete Hilt integration, causing confusion and maintenance overhead.
- **Recommendation**: Either fully adopt Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, and `@Inject constructor`) or remove the unused Hilt framework dependencies and `AppModule.kt`.

---

## 2. Security & Cryptography

### [Deprecation] Deprecated MasterKeys in EncryptedApiKeyProvider
- **Observation**: [`EncryptedApiKeyProvider.kt`](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/data/secure/EncryptedApiKeyProvider.kt) uses `MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)` from `androidx.security.crypto`.
- **Impact**: `MasterKeys` is deprecated in Jetpack Security Crypto.
- **Recommendation**: Migrate to `MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()`.

---

## 3. Database & Repository Performance

### [Optimization] Redundant Flow Collection in RoomGamesRepository
- **Observation**: In [`RoomGamesRepository.kt`](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/data/repository/RoomGamesRepository.kt), `observeEntriesForList` and `observeAllEntries` manually wrap dao flows:
  ```kotlin
  override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> = flow {
      database.gamesDao().observeEntriesForList(listId).collect { entries ->
          emit(resolveEntries(entries))
      }
  }
  ```
- **Impact**: Verbose boilerplate compared to idiomatic Flow operators.
- **Recommendation**: Simplify using `.map`:
  ```kotlin
  override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> =
      database.gamesDao().observeEntriesForList(listId).map { entries ->
          resolveEntries(entries)
      }
  ```

### [Performance] N+1 Query Overhead in ExportData
- **Observation**: In [`RoomSettingsRepository.kt`](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/data/repository/RoomSettingsRepository.kt), `exportData()` iterates through lists and fetches games sequentially in a loop:
  ```kotlin
  for (list in lists) {
      val gamesInList = database.gamesDao().listGamesFor(list.id)
      for (link in gamesInList) {
          val game = database.gamesDao().getGameById(link.gameId)
          ...
      }
  }
  ```
- **Impact**: Performs multiple round-trip queries to SQLite for each list and game during export.
- **Recommendation**: Use a single Room `@Transaction` query with SQL JOINs to fetch all lists, games, and list-games in one efficient query.

---

## 4. Jetpack Compose UI & Lifecycle

### [Lifecycle Optimization] Compose State Collection in MainActivity
- **Observation**: In [`MainActivity.kt`](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/MainActivity.kt), settings flows are collected using standard `.collectAsState(initial = ...)`:
  ```kotlin
  val themeId by container.settingsRepository.theme.collectAsState(initial = ThemeId.VIOLET)
  ```
- **Impact**: Continues collecting flows even when the activity is in the background (stopped state).
- **Recommendation**: Use `.collectAsStateWithLifecycle(initial = ...)` from `androidx.lifecycle.compose` to pause flow collection when the lifecycle is stopped, saving CPU and battery.

---

## 5. Code Quality & Static Analysis (Detekt)

### [Code Quality] Detekt Findings
- **Observation**: Running `:app:detekt` reported 17 weighted issues. Furthermore, [`detekt.yml`](file:///home/miguel/AndroidStudioProjects/BacklogOrder/config/detekt/detekt.yml) disables many checks (`active: false` for comments, complexity, exceptions, naming, style).
- **Impact**: Potential code smells, magic numbers, or naming inconsistencies go unchecked.
- **Recommendation**: Enable detekt rule sets incrementally, resolve identified code smells, and incorporate detekt into the CI/CD build pipeline.

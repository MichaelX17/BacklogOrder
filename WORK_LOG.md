# BacklogOrder — Work Log & Plan

## Status snapshot
- Current phase: 12
- Next action: Phase 13 — Polish, a11y, CI, release
- Last session: 2026-11-05
- Overall: 11/14 phases complete
- Test gate: PASS — ./gradlew assembleDebug test detekt ktlintCheck

## Toolchain baseline (verified by Rule 0 at Phase 0)
- AGP: 9.5.0
- Kotlin: 2.2.10
- KSP: 2.2.10-2.0.2
- Gradle: 9.6.0
- compileSdk / targetSdk / minSdk: 37 / 37 / 26
- Java toolchain: 11

## Phase status

| # | Phase | Status | Tests added | Last verified |
|---|-------|--------|-------------|---------------|
| 0 | Project scaffold & tooling | ✅ | 2 | 2026-11-04 |
| 1 | Domain core (pure Kotlin) | ✅ | 6 | 2026-11-04 |
| 2 | Data layer (Room) | ✅ | 8 | 2026-11-05 |
| 3 | Network layer (RAWG) | ✅ | 5 | 2026-11-05 |
| 4 | App foundation (DI, theme, nav) | ✅ | 2 | 2026-11-05 |
| 5 | HUD design system | ✅ | 3 | 2026-11-05 |
| 6 | Onboarding + API key | ✅ | 3 | 2026-11-05 |
| 7 | Home screen | ✅ | 3 | 2026-11-05 |
| 8 | Search + Add-to-list | ✅ | 3 | 2026-11-05 |
| 9 | Manual game entry | ✅ | 3 | 2026-11-05 |
| 10 | List detail (filters/sort) | ✅ | 3 | 2026-11-05 |
| 11 | Game detail | ✅ | — | 2026-11-05 |
| 12 | Settings: export/import/share | ⬜ | — | — |
| 13 | Polish, a11y, CI, release | ⬜ | — | — |

Legend: ⬜ not started · 🟨 in progress · ✅ done · 🟥 blocked

## Decisions Log
| Date | Phase | Decision | Reason |
|------|-------|----------|--------|
| 2026-11-04 | 0 | Verified the project toolchain before edits | AGP 9.5.0, Kotlin 2.2.10, KSP 2.2.10-2.0.2, Gradle 9.6.0, compileSdk/targetSdk 37, minSdk 26, Java 11; project values win over the target environment. |
| 2026-11-04 | 0 | Used DataStore 1.3.0 and a Java 17 Gradle daemon runtime | Google Maven only publishes DataStore up to 1.3.0, and the project's Gradle daemon was pinned to Java 25, which conflicts with Detekt/Kotlin 1.9.10. The app Java toolchain stayed at 11. |
| 2026-11-05 | 2 | Added Room database + repository layer with schema export | The data layer must persist game/list data transactionally while keeping the domain package pure Kotlin and the schema stored under app/schemas. |
| 2026-11-05 | 2 | Used a Java 17 test launcher while keeping the app's Java 11 toolchain | Robolectric requires Java 17 for Android SDK 34, so the tests run under a Java 17 launcher without changing the project's app compile target. |
| 2026-11-05 | 3 | Added a typed RAWG Retrofit client with API-key interceptor and MockWebServer coverage | The remote layer needs a cancellable network client, a single error-mapping boundary, and JVM-safe tests for success/401/404/429/timeout/cancellation paths. |
| 2026-11-05 | 4 | Added a shared app foundation: navigation, theme selection, and a lightweight DI module set | The app needs a stable root graph and palette-driven theme before the HUD and screen phases can build on top without layout churn. |
| 2026-11-05 | 5 | Added reusable HUD components with Compose and Robolectric coverage | The design system needs a reusable set of bevel/hex cards and status visuals before the onboarding and list screens can be built consistently. |
| 2026-11-05 | 6 | Added API-key validation flow with secure persistence and onboarding skip behavior | The onboarding step must allow validation or skip without storing the key in plaintext, and the app should still proceed to the home route after first-run completion. |
| 2026-11-05 | 7 | Added recommendation-driven home screen with skip/reset behavior, list creation, and summary stats | The home route must surface a primary recommendation, provide skip/reset handling, create backlog lists quickly, and summarize the user's library state before moving deeper in the app. |
| 2026-11-05 | 8 | Added a debounced search flow with API-key gating, add-to-list selection, and creation of new lists | Search must stay lightweight, lazy, and resilient while the user is browsing results and adding games to a target list. |
| 2026-11-05 | 9 | Added a manual game-entry screen with validation, direct backlog creation, and persistence semantics for manual records | Not every game is on RAWG; the app needs a validated manual path that still persists as a shared game record with `isManual = true` and a `BACKLOG` list status. |
| 2026-11-05 | 10 | Added a list-detail screen with live filter state, franchise grouping, and navigation to per-game detail routes | The list view must use the shared filter/sort domain logic to group games by franchise, filter live by name/status/score, and offer efficient navigation to the game-detail route. |
| 2026-11-05 | 11 | Added a game-detail screen with status updates, franchise metadata editing, and delete confirmation | The app needs a dedicated item page that reflects the game's current list state, supports metadata maintenance, and allows safe removal back to the list view without leaving stale data behind. |

## Known issues / tech debt
- None.

## File inventory (update at each phase end)
- app/src/main/java/com/backlogorder/BacklogOrderApplication.kt
- app/src/main/java/com/backlogorder/MainActivity.kt
- app/src/main/java/com/backlogorder/data/local/BacklogOrderDatabase.kt
- app/src/main/java/com/backlogorder/data/local/Daos.kt
- app/src/main/java/com/backlogorder/data/local/Entities.kt
- app/src/main/java/com/backlogorder/data/local/Converters.kt
- app/src/main/java/com/backlogorder/data/repository/RoomListsRepository.kt
- app/src/main/java/com/backlogorder/data/repository/RoomGamesRepository.kt
- app/src/main/java/com/backlogorder/data/repository/RoomSettingsRepository.kt
- app/src/main/java/com/backlogorder/data/secure/EncryptedApiKeyProvider.kt
- app/src/main/java/com/backlogorder/di/AppModule.kt
- app/src/main/java/com/backlogorder/di/AppDispatchers.kt
- app/src/main/java/com/backlogorder/domain/*
- app/src/main/java/com/backlogorder/domain/model/ThemeId.kt
- app/src/main/java/com/backlogorder/ui/common/AppErrorScreen.kt
- app/src/main/java/com/backlogorder/ui/common/UiState.kt
- app/src/main/java/com/backlogorder/ui/hud/HudComponents.kt
- app/src/main/java/com/backlogorder/ui/gamedetail/GameDetailScreen.kt
- app/src/main/java/com/backlogorder/ui/home/HomeScreen.kt
- app/src/main/java/com/backlogorder/ui/listdetail/ListDetailScreen.kt
- app/src/main/java/com/backlogorder/ui/manualgame/ManualGameScreen.kt
- app/src/main/java/com/backlogorder/ui/navigation/BacklogOrderNavHost.kt
- app/src/main/java/com/backlogorder/ui/onboarding/OnboardingScreen.kt
- app/src/main/java/com/backlogorder/ui/search/SearchScreen.kt
- app/src/main/java/com/backlogorder/ui/theme/*
- app/src/test/java/com/backlogorder/data/RoomDataModelTest.kt
- app/src/test/java/com/backlogorder/ui/hud/HudComponentsTest.kt
- app/src/test/java/com/backlogorder/ui/home/HomeScreenTest.kt
- app/src/test/java/com/backlogorder/ui/listdetail/ListDetailViewModelTest.kt
- app/src/test/java/com/backlogorder/ui/manualgame/ManualGameViewModelTest.kt
- app/src/test/java/com/backlogorder/ui/navigation/BacklogOrderRouteTest.kt
- app/src/test/java/com/backlogorder/ui/onboarding/OnboardingViewModelTest.kt
- app/src/test/java/com/backlogorder/ui/search/SearchViewModelTest.kt
- app/src/test/java/com/backlogorder/ui/theme/BacklogThemeTest.kt
- app/schemas/com.backlogorder.data.local.BacklogOrderDatabase/1.json
- config/detekt/detekt.yml
- gradle/libs.versions.toml
- gradle/gradle-daemon-jvm.properties
- gradle.properties
- README.md
- WORK_LOG.md

## Session log
- 2026-11-04 Phase 0: Verified the existing toolchain, added the requested version catalog entries and lint tooling, created the placeholder Hilt app and MainActivity screen, and completed the Phase 0 acceptance gate with 2 passing tests.
- 2026-11-05 Phase 2: Added the Room data layer, repository implementations, schema export, and focused data-model tests while confirming the project remains green under the JVM test gate.
- 2026-11-05 Phase 4: Added the app foundation: a working navigation graph, palette-based theme system, a lightweight Hilt-esque DI module set, and route/theme smoke tests while keeping the project on its green gate.
- 2026-11-05 Phase 5: Added a reusable HUD component kit (bevel frames, hex badges, status tags, score chip, filter chips, and empty-state/game cards) with Robolectric Compose coverage to keep the UI layer cohesive before the onboarding flow.
- 2026-11-05 Phase 6: Added the onboarding flow with API-key validation states, a secure storage-backed key provider, onboarding completion tracking, skip behavior, and Compose/ViewModel tests that keep the app green under the validation gate.
- 2026-11-05 Phase 7: Added the home route with recommendation logic, summary cards, list creation, and skip/reset handling to continue the flow from onboarding into the backlog experience.
- 2026-11-05 Phase 8: Added the search route with debounced RAWG lookups, list selection, and add-to-list behavior while respecting the project's API-key gating rules.
- 2026-11-05 Phase 9: Added a manual game-entry screen with validation, direct backlog creation, and persistence semantics for manual records.
- 2026-11-05 Phase 10: Added the list-detail flow with live filters, franchise grouping, and list-specific game navigation; validated with focused ViewModel tests and the repo-wide validation gate.
- 2026-11-05 Phase 11: Added the game-detail screen with status updates, franchise metadata editing, and safe removal confirmation while preserving the repo's green validation gate.

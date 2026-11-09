# Architectural Decision Memo: Dependency Injection Strategy (Hilt vs. Manual AppContainer)

## Current State Analysis
BacklogOrder currently exhibits a hybrid DI state:
- **Manual DI Touchpoints**: `AppContainer`, `DefaultAppContainer`, and custom `ViewModelProvider.Factory` factories used across all UI screens (`HomeScreen`, `GameDetailScreen`, `SearchScreen`, `SettingsScreen`, `OnboardingScreen`, `ListDetailScreen`, `ListsScreen`, `ManualGameScreen`).
- **Hilt Touchpoints**: `AppModule.kt` defines Hilt `@Module` bindings (`@InstallIn(SingletonComponent::class)`), and Hilt dependencies (`hilt.android`, `hilt.android.compiler`) are declared in [build.gradle.kts](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/build.gradle.kts). However, `BacklogOrderApplication` lacks `@HiltAndroidApp`, and zero ViewModels or Activities use `@HiltViewModel` or `@AndroidEntryPoint`.

---

## Option A — Finish Hilt (Adopt Hilt Fully)

### What it entails
1. Annotate `BacklogOrderApplication` with `@HiltAndroidApp`.
2. Annotate `MainActivity` with `@AndroidEntryPoint`.
3. Migrate all ViewModels to `@HiltViewModel` with `@Inject constructor`.
4. Remove `AppContainer` / `DefaultAppContainer` and all custom `ViewModelProvider.Factory` factory blocks across 8+ UI screens.
5. Update unit and instrumentation tests to use Hilt testing rules (`@HiltAndroidTest`).

### Estimates & Assessment
- **Effort**: ~10–12 files modified, ~4–6 hours of engineering.
- **Risk**: Medium. Potential runtime DI resolution issues or test harness reconfiguration with Robolectric/androidTest.
- **Reversibility**: Low-to-Medium.

---

## Option B — Remove Hilt (Pure Manual AppContainer DI)

### What it entails
1. Remove Hilt plugins and dependencies (`hilt.android`, `hilt.android.compiler`) from [build.gradle.kts](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/build.gradle.kts).
2. Delete unused [AppModule.kt](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/di/AppModule.kt).
3. Rely fully on the existing `AppContainer` / `DefaultAppContainer` pattern already active across the entire app.

### Estimates & Assessment
- **Effort**: 2 files modified (`build.gradle.kts` and removal of `AppModule.kt`), ~1 hour.
- **Risk**: Very Low. The app already runs entirely on `AppContainer` and manual ViewModel factories.
- **Reversibility**: High.

---

> [!IMPORTANT]
> **Action Required**: Please review this memo and indicate your preferred path (**Option A** or **Option B**) to guide the next phase.

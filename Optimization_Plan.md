================================================================================
BACKLOGORDER — PHASED REMEDIATION PLAN
================================================================================
A send-one-phase-at-a-time plan designed for a local AI executor.
Each phase is self-contained, has explicit file scope, verification gates,
and a fixed output format.

DO NOT skip ahead. DO NOT merge phases.
================================================================================


--------------------------------------------------------------------------------
HOW TO USE THIS WITH YOUR LOCAL AI
--------------------------------------------------------------------------------

1. Send the PREAMBLE below ONCE at the start of the session.
2. Send PHASE 0. Wait for the report. Verify gates pass.
3. Only then send PHASE 1. Same pattern for each phase.
4. If a phase fails verification, paste the failure back and ask for a
   MINIMAL fix — do not re-run the whole phase from scratch.
5. If the AI tries to touch files outside "Files in Scope", reply:
     OUT OF SCOPE. Re-output following the file scope list.
6. Never let the AI run two phases in one message. Local models drift hard
   when they do.


================================================================================
PREAMBLE — SEND ONCE AT SESSION START
================================================================================

# SESSION RULES
You are executing a phased remediation plan for an Android / Kotlin / Jetpack
Compose app called "BacklogOrder".

RULES (violating any invalidates your output):
1. Execute exactly ONE phase per message. Do not start the next phase.
2. Only modify files listed under "Files in Scope" for the current phase.
   Touching any other file is a failure.
3. Do not refactor, rename, or "clean up" outside the phase's explicit goal.
4. Do not add new dependencies unless the phase explicitly instructs it.
5. Do not invent APIs. If a class/method is unclear, output
   NEEDS_CONTEXT: <file path> and stop.
6. After completing a phase, output a PHASE REPORT in the format below.
7. Wait for the user to say PROCEED TO PHASE <N> before continuing.

# PHASE REPORT FORMAT (output this at the end of every phase)

## Phase <N> Report — <phase name>
- Files changed:
  - path/to/file.kt (lines: X-Y, brief description)
- Files created:
- Files deleted:
- Dependencies added/removed:
- Verification commands run:
  - <command> -> <output summary>
- Verification status: PASS | FAIL
- Known issues introduced: <or "none">
- Next phase readiness: READY | BLOCKED (reason)

# END SESSION RULES


================================================================================
PHASE 0 — BASELINE MEASUREMENT (no code changes)
================================================================================

GOAL: Capture a "before" snapshot so every later perf claim is provable.

FILES IN SCOPE: none (read-only)

STEPS:

  1. Build release APK as-is
       ./gradlew :app:assembleRelease

  2. Capture startup logcat (cold start, 3 runs, take median)
       adb shell am force-stop com.backlogorder
       adb logcat -c
       adb shell am start -W -n com.backlogorder/.MainActivity
       -- note the "TotalTime" value; repeat 2 more times

  3. Capture jank on Home screen scroll
       Manual — open Android Studio Profiler, record 10s scroll, save .trace

ACCEPTANCE GATE:
  - Three startup TotalTime values recorded (median noted).
  - Profiler trace saved as baseline-home-scroll.trace in docs/perf/.

SEND PHASE 0 TO AI: yes — it will produce the report format with
"files changed: none" and the numbers you pasted.


================================================================================
PHASE 1 — CRITICAL: MAIN-THREAD I/O & DISPATCHER INJECTION
================================================================================

FIXES: F-002 (main-thread file write), F-008 (hardcoded Dispatchers.IO).
WHY TOGETHER: both are dispatcher-correctness fixes in the data layer.

FILES IN SCOPE (only these):
  - app/src/main/java/com/backlogorder/data/repository/RoomSettingsRepository.kt
  - app/src/main/java/com/backlogorder/data/repository/RoomGamesRepository.kt
    (read only - verify no similar I/O)
  - app/src/main/java/com/backlogorder/data/remote/RawgRemoteDataSource.kt
  - app/src/main/java/com/backlogorder/ui/settings/SettingsViewModel.kt
  - app/src/main/java/com/backlogorder/di/AppDispatchers.kt (read only)
  - app/src/main/AndroidManifest.xml (ONLY to register a FileProvider if absent)
  - app/src/main/res/xml/file_paths.xml (create if missing)

INSTRUCTIONS TO AI:

  ## Task
  Make shareExportedData() main-safe. It currently writes to external storage
  on the caller's dispatcher (Main via viewModelScope).

  ## Required changes
  1. Inject AppDispatchers into RoomSettingsRepository (constructor param).
  2. Wrap the entire body of shareExportedData() in withContext(dispatchers.io).
  3. Change the return type from Result<Unit> to Result<Uri> — obtain a
     content Uri via FileProvider.getUriForFile(...). Configure the
     FileProvider in AndroidManifest.xml and res/xml/file_paths.xml if absent.
  4. Update SettingsViewModel.exportData() to launch the share sheet on Main
     using the returned Uri — do NOT pass File to Intent directly.
  5. In RawgRemoteDataSource, replace every hardcoded Dispatchers.IO with
     the injected dispatchers.io. Add AppDispatchers to its constructor.

  ## Constraints
  - Do not change any other method signature.
  - Do not introduce GlobalScope or runBlocking.
  - Preserve existing logging.
  - If a Hilt @Provides for AppDispatchers does not exist, add it to
    AppModule.kt — this is the only exception to "don't touch DI".

  ## Acceptance
  - ./gradlew :app:assembleDebug compiles.
  - Static grep shows no Dispatchers.IO in RawgRemoteDataSource.kt and no
    raw FileOutputStream outside a withContext block in
    RoomSettingsRepository.kt.
  - Export on a debug device shows no StrictMode violation for disk write.

VERIFICATION GATE:
  [ ] ./gradlew :app:assembleDebug succeeds.
  [ ] grep -n "Dispatchers.IO" \\
        app/src/main/java/com/backlogorder/data/remote/RawgRemoteDataSource.kt
        -> no matches.
  [ ] Manual: trigger export -> share sheet opens, no ANR.

ROLLBACK: git revert the phase-1 commit if any gate fails.


================================================================================
PHASE 2 — COROUTINE SAFETY: CANCELLATION & ERROR SURFACING
================================================================================

FIXES: F-014 (empty catch swallows cancellation).

FILES IN SCOPE:
  - app/src/main/java/com/backlogorder/ui/game/GameDetailScreen.kt
  - app/src/main/java/com/backlogorder/ui/game/GameDetailViewModel.kt
    (if it exists; otherwise read-only)

INSTRUCTIONS TO AI:

  ## Task
  The reloadRawgCover catch block currently swallows CancellationException
  and provides no user feedback.

  ## Required changes
  1. Add catch (e: CancellationException) { throw e } BEFORE the generic
     catch (e: Exception) in reloadRawgCover (and anywhere else the same
     pattern exists in this file).
  2. In the generic catch, update _uiState with a new
     coverReloadError: String? field so the UI can display a retry prompt.
  3. In finally, clear only isReloadingCover = false; do NOT clear the error
     field — the user dismisses it explicitly.
  4. Add a @Composable retry banner below the cover image that shows when
     coverReloadError != null, with a Retry button that calls reloadRawgCover()
     and clears the error.

  ## Constraints
  - Add exactly one new field (coverReloadError) to the UI state.
  - Do not alter any other behavior.
  - Use Log.w (or the project's existing logger) in the generic catch.

  ## Acceptance
  - CancellationException is rethrown before any generic catch.
  - New field wired to a visible retry affordance.

VERIFICATION GATE:
  [ ] ./gradlew :app:assembleDebug succeeds.
  [ ] Add a small unit test in
      app/src/test/.../GameDetailViewModelTest.kt:
      launch reload, cancel the job immediately, assert job.isCancelled
      and no crash.
  [ ] ./gradlew :app:testDebugUnitTest passes.


================================================================================
PHASE 3 — CRASH PREVENTION: DUPLICATE LAZY KEYS
================================================================================

FIXES: F-006 (duplicate displayName key).

FILES IN SCOPE:
  - app/src/main/java/com/backlogorder/domain/FranchiseSorter.kt
    (or wherever FranchiseGroup is defined)
  - app/src/main/java/com/backlogorder/ui/listdetail/ListDetailScreen.kt
  - app/src/main/java/com/backlogorder/ui/listdetail/ListDetailUiState.kt

INSTRUCTIONS TO AI:

  ## Task
  FranchiseGroup is keyed by displayName, which is not unique — two games
  with the same name in different lists will throw IllegalArgumentException
  at runtime.

  ## Required changes
  1. Add a stable id: String field to FranchiseGroup. Derive it from the
     franchise id when the group is a franchise, and from the game id when
     the group is standalone. Prefix with franchise: / game: to avoid
     collisions.
  2. Update FranchiseSorter.sort() to populate id for every group.
  3. Change ListDetailScreen to use key = { it.id }.
  4. Update any test that constructs FranchiseGroup directly.

  ## Constraints
  - Do not change the group ordering logic.
  - Do not remove displayName — it's still used for display.

  ## Acceptance
  - ./gradlew :app:testDebugUnitTest passes including a new test:
    duplicateDisplayNames_produceUniqueGroupIds.

VERIFICATION GATE:
  [ ] New unit test proves two groups with identical displayName produce
      different ids.
  [ ] ./gradlew :app:testDebugUnitTest passes.
  [ ] Manual: navigate to a list with same-name games -> no crash.


================================================================================
PHASE 4 — COMPOSE HOT PATHS: MOVE COMPUTATION OUT OF GETTERS
================================================================================

FIXES: F-004, F-005 (getters computing in composition), plus F-003 (unbounded
cache).
WHY TOGETHER: F-004/F-005 are the same pattern (derived state as getter).
F-003 is memory — bundling avoids a trivial extra phase.

FILES IN SCOPE:
  - app/src/main/java/com/backlogorder/ui/home/HomeUiState.kt
  - app/src/main/java/com/backlogorder/ui/home/HomeViewModel.kt
  - app/src/main/java/com/backlogorder/ui/home/HomeScreen.kt
  - app/src/main/java/com/backlogorder/ui/listdetail/ListDetailUiState.kt
  - app/src/main/java/com/backlogorder/ui/listdetail/ListDetailViewModel.kt
  - app/src/main/java/com/backlogorder/ui/listdetail/ListDetailScreen.kt
  - app/src/main/java/com/backlogorder/domain/FranchiseSorter.kt
  - app/src/main/java/com/backlogorder/data/remote/RawgRemoteDataSource.kt
    (cache only)

INSTRUCTIONS TO AI:

  ## Task A — Home
  Move topRankedGames from a computed getter to a val field computed in
  HomeViewModel's combine {} pipeline. Update HomeScreen to read the field
  directly (no functional change to UI).

  ## Task B — ListDetail
  - Convert FranchiseSorter from class to object (it is stateless).
  - Move groups from a getter to a val field computed in the ViewModel.
  - Update ListDetailScreen to read uiState.groups directly.

  ## Task C — Cache bound
  Replace the internal ConcurrentHashMap in RawgSearchCache with an
  accessOrder = true LinkedHashMap subclass that overrides removeEldestEntry
  to cap at 20 entries. Keep the existing TTL logic.

  ## Constraints
  - Do NOT change any visual output.
  - Do NOT alter field ordering in *UiState data classes (breaks callers).
  - Do NOT remove the existing getter without updating every call site —
    grep for .topRankedGames and .groups first and list the call sites in
    your report.

  ## Acceptance
  - ./gradlew :app:assembleDebug succeeds.
  - grep -rn "get() = " app/src/main/java/com/backlogorder/ui/ shows no
    computational getters in UiState files.
  - RawgSearchCache has a hard cap of 20 entries (unit test).

VERIFICATION GATE:
  [ ] Compile clean.
  [ ] Unit test for RawgSearchCache: insert 30 entries -> size <= 20.
  [ ] Manual: Home screen renders identical to before.
  [ ] Re-run the Phase 0 jank trace — record as
      post-phase4-home-scroll.trace. Compare frame counts (empirical proof).


================================================================================
PHASE 5 — BASELINE PROFILE + STARTUP PROFILE
================================================================================

FIXES: I-015.

FILES IN SCOPE: Gradle build files, new :baselineprofile module.
NO source changes.

INSTRUCTIONS TO AI:

  ## Task
  Add a Baseline Profile and Startup Profile to the app.

  ## Required changes
  1. Create new module :baselineprofile (androidTest, consumerProguardFiles off).
  2. Add baselineProfile configuration and benchmark buildType to :app
     (initWith release, debug signing).
  3. Create BaselineProfileGenerator.kt in the new module that calls
     collect() { pressHome(); startActivityAndWait(); ... }.
  4. In :app, android { defaultConfig { ... } } and dependencies, wire
     baselineProfile(project(":baselineprofile")).
  5. Add macrobenchmark module :benchmark with a StartupBenchmark class
     measuring TTID and TTFD using StartupTimingMetric.

  ## Constraints
  - Do NOT modify application source files.
  - Use only versions already present in gradle/libs.versions.toml; if a
    needed artifact is missing, add it to the TOML with the latest stable at
    the time you execute — note the version in your report.
  - The benchmark buildType must not enable minifyEnabled differently than
    release.

  ## Acceptance
  - ./gradlew :app:generateBaselineProfile succeeds.
  - A baseline-prof.txt exists in the app's assets (merged into release).
  - ./gradlew :benchmark:connectedBenchmarkAndroidTest reports TTID.

VERIFICATION GATE:
  [ ] ./gradlew :app:assembleRelease includes baseline profile (verify via
      unzip -l app-release.apk | grep baseline-prof).
  [ ] Startup TTID improved by >= 10% vs Phase 0 median (KPI gate — if it
      doesn't improve, something is wrong and we diagnose before proceeding).


================================================================================
PHASE 6 — BATCHED QUICK WINS
================================================================================

FIXES: F-007, F-011, F-012, F-009, B-3, B-4, B-5.

FILES IN SCOPE:
  - AndroidManifest.xml
  - res/xml/backup_rules.xml (create)
  - res/xml/data_extraction_rules.xml (create)
  - ui/games/GamesScreen.kt
  - ui/game/GameDetailScreen.kt (StatusButton only)
  - data/repository/RoomGamesRepository.kt
  - data/repository/RoomSettingsRepository.kt (export only)
  - MainActivity.kt
  - app/build.gradle.kts (dependency check only)

INSTRUCTIONS TO AI:

  ## Task — Checklist (complete ALL items, report each individually)

  [ ] F-007 — Create backup_rules.xml + data_extraction_rules.xml that
      EXCLUDE the encrypted-prefs file from backup. Reference both from
      <application>. Do not disable allowBackup.

  [ ] F-011 — GamesScreen: change remember { mutableStateOf("") } for the
      search query to rememberSaveable { mutableStateOf("") }.

  [ ] F-012 — GameDetailScreen StatusButton: replace .height(42.dp) with
      .heightIn(min = 48.dp).

  [ ] F-009 — In app/build.gradle.kts: verify via
      ./gradlew :app:analyzeReleaseBundle that okhttp-logging-interceptor
      is or is not present. If absent, remove the
      implementation(libs.okhttp.logging.interceptor) line. If present,
      report PRESENT and DO NOT remove.

  [ ] B-3 — RoomGamesRepository: replace
      flow { database.gamesDao().observeX().collect { emit(f(it)) } }
      with .map { f(it) } for observeEntriesForList and observeAllEntries.

  [ ] B-4 — RoomSettingsRepository.exportData(): refactor to a Room
      @Transaction method returning ListWithGames via @Relation. Replace
      the N+1 loop with dao.getAllListsWithGames(). Keep the existing
      ExportData DTO shape.

  [ ] B-5 — MainActivity: replace all .collectAsState(initial = ...) with
      .collectAsStateWithLifecycle(initial = ...). Ensure
      androidx.lifecycle:lifecycle-runtime-compose is on the classpath
      (add to TOML if missing).

  ## Constraints
  - Do not touch anything outside the file list.
  - Each checklist item gets its own line in the PHASE REPORT with
    PASS/SKIP.

VERIFICATION GATE:
  [ ] ./gradlew :app:assembleRelease succeeds.
  [ ] ./gradlew :app:lint — no NEW warnings vs baseline.
  [ ] Export still works (manual, on a real device).
  [ ] Rotation of GamesScreen preserves search query.
  [ ] Startup still works and settings apply.


================================================================================
PHASE 7 — HILT DECISION (design-only, no code)
================================================================================

FIXES: F-001 / B-1.

FILES IN SCOPE: none. This phase produces a DECISION DOCUMENT only.

INSTRUCTIONS TO AI:

  ## Task
  Produce a one-page decision memo: adopt Hilt fully OR remove Hilt.

  ## Analysis required
  - Count current DI touchpoints (grep for ViewModelProvider.Factory,
    AppContainer, @InstallIn, @HiltViewModel, @AndroidEntryPoint).
  - Estimate migration effort (file count, test coverage impact).
  - Enumerate the pros/cons of each path FOR THIS CODEBASE, not in the
    abstract.

  ## Output
  Two sections: "Option A — Finish Hilt" and "Option B — Remove Hilt".
  Each with: effort estimate (in file count and rough hour range), risk,
  reversibility. No recommendation — the user decides.

DO NOT auto-execute. The user reads the memo, picks a path, and sends it
back as a new phase request.


================================================================================
PHASE 8 — SECURITY MIGRATION (EncryptedSharedPreferences)
================================================================================

FIXES: B-2.

FILES IN SCOPE:
  - app/src/main/java/com/backlogorder/data/secure/EncryptedApiKeyProvider.kt
  - gradle/libs.versions.toml
  - Its test file(s)

INSTRUCTIONS TO AI:

  ## Task
  MasterKeys and EncryptedSharedPreferences are deprecated in
  androidx.security-crypto.

  ## Path
  Migrate to com.google.crypto.tink:tink-android with an Android Keystore
  keyset. Replace the public surface of EncryptedApiKeyProvider (keep
  getApiKey, setApiKey, clear) but reimplement internals on Tink.

  ## Constraints
  - Preserve all existing public method signatures exactly.
  - Write a migration test: write with the old API on a clean Keystore, then
    read with the new API (use a temporary test keystore alias) — if the old
    format is incompatible, document the required wipe and ship a
    "settings must be re-entered" notice in the changelog.
  - Do NOT change how the API key is consumed by the network layer.

VERIFICATION GATE:
  [ ] ./gradlew :app:testDebugUnitTest passes, including new migration test.
  [ ] Manual: enter API key -> restart app -> key still present.
  [ ] grep -rn "MasterKeys" app/src/main -> no matches.


================================================================================
PHASE 9 — DETEKT: RE-ENABLE & FIX
================================================================================

FIXES: B-6.

FILES IN SCOPE:
  - config/detekt/detekt.yml

INSTRUCTIONS TO AI:

  ## Task
  Re-enable Detekt rules incrementally — one rule set per PR.

  ## Steps
  1. Run ./gradlew :app:detekt and paste the full output.
  2. For each disabled rule set (comments, complexity, exceptions, naming,
     style), re-enable it in isolation, run detekt, and list every new issue.
  3. Produce a remediation table: issue -> file:line -> suggested fix.
  4. Do NOT modify source in this phase — output the table only.

VERIFICATION GATE:
  [ ] The remediation table is complete; user decides which subset to fix
      per turn.


================================================================================
CROSS-PHASE RULES FOR THE LOCAL AI
================================================================================
Send this as a FOOTER after every phase request:

  ## STOP CONDITIONS — halt and report if any occur
  - You are about to touch a file not listed in "Files in Scope".
  - You cannot find a referenced class/method — output NEEDS_CONTEXT and stop.
  - A ./gradlew command fails twice — paste the error, do not guess again.
  - You are tempted to refactor something "while you're in the file" — NO.

  ## AFTER YOU FINISH
  Output the PHASE REPORT. Then stop and wait for PROCEED TO PHASE N.


================================================================================
RECOMMENDED HUMAN CADENCE
================================================================================

  Day 1 (AM)   Phase 0    Numbers in hand, no code
  Day 1 (PM)   Phase 1    Critical safety fix
  Day 2 (AM)   Phase 2    Coroutine safety
  Day 2 (PM)   Phase 3    Crash prevention
  Day 3        Phase 4    Compose perf — measure before/after
  Day 4        Phase 5    Baseline Profile — the big win
  Day 5        Phase 6    Quick wins, batched
  Day 6        Phase 7    Decision memo — read carefully
  Later        Phase 8    Security, when you can test on a real device
  Later        Phase 9    Detekt, ongoing

TOTAL for P0/P1 scope (phases 1–5): ~4 working days, with a hard-stop
measurement gate at Phase 5. If Baseline Profiles don't deliver >= 10%
startup improvement, that's a signal — inspect the benchmark output before
spending the remaining phases.


================================================================================
STARTER COMMAND FOR TODAY
================================================================================
Send this to your local AI verbatim to begin:

  Execute PHASE 0 of the BacklogOrder Remediation Plan.
  Output: the Phase 0 Report per the PHASE REPORT format in the session rules.
  Files in Scope: none.

Then run the three startup measurements yourself and paste the numbers into
the report.


================================================================================
END OF PLAN
================================================================================
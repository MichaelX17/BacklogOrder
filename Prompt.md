Read MASTER_BUILD_PROMPT.md in full, then read BacklogOrder App Analysis.txt as the product reference.

Project state:
- Repo: ~/AndroidStudioProjects/BacklogOrder
- Gradle build is green: `./gradlew test` → BUILD SUCCESSFUL
- WORK_LOG.md does NOT exist yet — creating it is the first task of Phase 0

Your first session's task is exactly Phase 0 of the master prompt. Do this in this order and stop after Phase 0 is ✅:

1. Read the three version files listed in Rule 0 of Section A:
   - gradle/libs.versions.toml
   - gradle/wrapper/gradle-wrapper.properties
   - app/build.gradle.kts
   Record the actual AGP / Kotlin / KSP / Gradle / compileSdk / targetSdk / minSdk / Java toolchain values. The project's values are authoritative — Section C of the prompt describes the target, not a mandate.

2. Create WORK_LOG.md at the repo root using the exact template in Section B. Populate:
   - Status snapshot: Current phase = 0, test gate = PASS
   - Toolchain baseline: the values you just read
   - Phase status: all ⬜ except Phase 0 = 🟨
   - Decisions Log: one entry noting the actual toolchain baseline

3. Implement Phase 0 deliverables per Section G:
   - Extend gradle/libs.versions.toml with Hilt, Room + KSP, Retrofit + kotlinx-serialization, Coil 3, EncryptedSharedPreferences, DataStore
   - Configure ktlint + detekt (runnable via `./gradlew detekt ktlintCheck`)
   - Confirm Hilt Application class and MainActivity exist and render a placeholder screen
   - Write one trivial unit test proving the test infrastructure runs
   - Ensure .gitignore and README.md exist

4. Run the Phase 0 acceptance gate:
   ./gradlew assembleDebug test detekt ktlintCheck
   All four must be green before you mark Phase 0 ✅.

5. Update WORK_LOG.md: Phase 0 → ✅, record test count, update file inventory and session log, set Next action to "Phase 1 — Domain core".

Rules that apply:
- Do NOT modify gradle/wrapper/gradle-wrapper.properties or gradle-wrapper.jar.
- Do NOT upgrade AGP, Kotlin, KSP, or Gradle.
- Do NOT change the Java toolchain — use whatever the project already has.
- If any library version doesn't resolve, pick the closest compatible version and log the choice in Decisions Log.
- Stop after Phase 0 is ✅. Do not start Phase 1 in the same session.

Begin by replying with the Section A status block (WORK LOG READ / Completed / Current / Test gate / Toolchain / Plan), then start Phase 0.
```

---

## What a correct first response looks like

The AI's opening message should look **almost exactly** like this:

```
WORK LOG READ
Completed: —
Current:   Phase 0 — Project scaffold & tooling
Test gate: PASS (1 test)
Toolchain: AGP 9.x, Kotlin 2.x, Gradle 9.x, compileSdk 37, Java 2x
Plan:      Read version files, create WORK_LOG.md with toolchain baseline,
           add Hilt/Room/Retrofit/Coil/ktlint/detekt to the version catalog,
           prove ./gradlew assembleDebug test detekt ktlintCheck is green.
```

The **`Toolchain:`** line is the tell — it proves the AI actually opened the three files (Rule 0). If that line is missing or vague (`Toolchain: latest`), it did not read Rule 0 and you should send the prompt back.

---

## If something goes wrong

| AI does this | You do this |
|---|---|
| Skips WORK_LOG.md and starts writing code | Reply: *"Stop. Section A Rule 1 requires WORK_LOG.md first. Restart Phase 0 with WORK_LOG.md as task 1."* |
| Doesn't print the Section A status block | Reply: *"Section A Rule 1.e requires the status block before anything else. Print it, then continue."* |
| Tries to upgrade AGP/Gradle/Kotlin "to fix something" | Reply: *"Section C toolchain stability rule. Revert the version change, fix the actual build error instead."* |
| Reports BUILD SUCCESSFUL on only `./gradlew test` and marks Phase 0 ✅ | Reply: *"Section G Phase 0 acceptance requires all four: assembleDebug, test, detekt, ktlintCheck. Run all four."* |
| Tries to start Phase 1 in the same session | Reply: *"Section A Rule 2: one phase per session. Stop after Phase 0 is ✅."* |
| Compiles but a library fails to resolve | That's fine — the prompt permits picking a compatible version. Confirm it logs the choice in Decisions Log. |

---

## For every session after Phase 0

Same prompt, with two changes:

1. **Attach `WORK_LOG.md`** alongside the master prompt and analysis file.
2. **Replace step 1 with:** *"Read WORK_LOG.md. Run `./gradlew test` and confirm all completed phases are green. Then begin the phase listed under `Next action`."*

Everything else stays identical. This is why the prompt was designed around a single status file — you paste the same message every time, and the AI reconstructs state from `WORK_LOG.md`.

---

## The one thing to do before sending

Make sure the project is committed so you have a rollback point when the AI modifies the build files:

```zsh
cd ~/AndroidStudioProjects/BacklogOrder
git status                       # should show clean (or uncommitted wizard files)
git add . && git commit -m "Green baseline before Phase 0" --allow-empty
```

30 seconds, and it saves you if Phase 0's version-catalog edits go sideways.

---

## What you're walking away with

When Phase 0 comes back ✅, you will have:

- A green `./gradlew assembleDebug test detekt ktlintCheck`
- All dependencies pinned in `libs.versions.toml`
- ktlint + detekt enforcing code style
- A `WORK_LOG.md` recording the real toolchain baseline for every future session
- Phase 1 (pure Kotlin domain logic — your highest-value, lowest-risk phase) queued as the next action

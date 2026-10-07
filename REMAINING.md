# Remaining Work

Phases 6, 7, and 8 are not yet started. This file tracks what is left.

---

## Phase 6 — Filters, Sagas, Recommendation (IN PROGRESS)

### 6.1 Filters (pure logic — already implemented)

`src/utils/filters.ts` already exists and is complete:

- `FilterState`, `DEFAULT_FILTERS`, `isFilterActive`
- `matchesName`, `matchesPlaytime`, `matchesRating`, `matchesScore`, `matchesStatus`
- `applyFilters(entries, filters) -> ListEntry[]`

**Still TODO:**

- [ ] Write `src/utils/__tests__/filters.test.ts` covering every matcher and
      `applyFilters` (name substring case-insensitive, playtime min/max,
      rating min/max using normalized rating, score min/max, status multi-select,
      empty filters = pass-through, combination).
- [ ] Build `FilterSheet` component (slide-up sheet with name input, playtime
      min/max, rating min/max, score min/max, status chips, clear/apply).

### 6.2 Sagas (franchise UX)

- [ ] Make `FranchiseGroup` collapsible (tap header to expand/collapse).
- [ ] Render the "some games in this saga have no franchise order" warning
      (already partially present — needs to be wired to actual data and be
      tappable to scroll to the offending game).

### 6.3 Recommendation

`src/utils/recommendation.ts` already exists and is complete:

- Priority: Playing (highest score) -> Backlog (highest score) -> null.
- `skipCount` support for "Skip" button.

**Still TODO:**

- [ ] Write `src/utils/__tests__/recommendation.test.ts` (Playing wins over
      Backlog; Backlog when no Playing; empty state when no scored entries;
      skip advances the candidate).
- [ ] Rewrite `app/index.tsx` Home screen to use `findRecommendation` and expose
      "Start playing" (sets state to `Playing`) and "Skip" (next candidate,
      no state change) buttons.

### 6.4 Wire filters into list/[id].tsx

- [ ] Add a filter button (floating or in action bar) to `list/[id].tsx`.
- [ ] Open `FilterSheet`, apply filters, re-render the filtered list without
      breaking the stored ordering.
- [ ] Show an active-filter indicator (e.g. badge count) and a clear button.

### 6.5 Quality gates for Phase 6

- [ ] `tsc --noEmit`: 0 errors
- [ ] `lint`: 0 errors, 0 warnings
- [ ] `format:check`: clean
- [ ] `test`: all pass (including new filters + recommendation tests)
- [ ] Manual smoke: filter hides items, saga collapses, Home recommendation
      cycles with Skip, Start playing changes state.

---

## Phase 7 — Export / Import / Settings

Not started. See spec §1.12 for the export JSON format.

- [ ] Install `expo-file-system`, `expo-sharing`, `expo-document-picker`.
- [ ] Implement `src/services/export.ts`:
  - `exportList(listId) -> share JSON of one list`.
  - `exportAll() -> share JSON of every list`.
  - Build the exact export shape: `{ version, exportedAt, lists: [...] }`.
- [ ] Implement `src/services/import.ts`:
  - Schema validation (zod or manual guards — no `any`).
  - Deduplicate by `rawgId` if present, else by normalized `name`.
  - Idempotent upsert into `games` + `list_games`.
- [ ] Screen `app/settings.tsx`:
  - Export all / Export single list.
  - Import.
  - View / change API key.
  - RAWG attribution.
  - App version (from `expo-constants`).
- [ ] Tests for export/import pure helpers (dedup logic, schema validation).

---

## Phase 8 — Polish & Ship

Not started.

- [ ] Light/dark theme (system-aware) — currently hard-coded light palette.
- [ ] Icon + splash screen in `assets/`.
- [ ] Empty states everywhere, loaders, error boundaries.
- [ ] RAWG attribution visible in settings (already in onboarding).
- [ ] EAS Build config `eas.json` with preview (APK) and production (AAB).
- [ ] Generate APK, install on real device, verify all flows.
- [ ] Update README with screenshots, roadmap, real scripts.
- [ ] Create release `v0.1.0` on GitHub with APK attached.

---

## Blocked / Needs Supervisor Input

None yet. Phase 6 can proceed autonomously.

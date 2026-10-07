# Phases Completed

Phases 1 through 5 are complete and committed. The repository is in a working,
quality-gated state.

## Phase 1 — Project Foundation

- Expo project `backlogorder` (blank-typescript), Expo Router configured.
- Folder structure per spec §4 with `.gitkeep` files.
- Path alias `@/*` -> `./src/*` in `tsconfig.json`.
- `SPEC.md`, `README.md`, `MIT LICENSE`, `.gitignore`, `.gitattributes`.
- Single initial commit: `3d96423 chore: Phase 1 - project foundation`.

## Phase 2 — Testing Infrastructure

- Jest + jest-expo + @testing-library/react-native installed.
- `jest.config.js` and `jest.setup.js` with virtual mocks for `expo-secure-store`,
  `expo-router`, and `expo-sqlite`.
- Scripts: `test`, `test:watch`, `test:coverage`.
- Smoke test in `src/app/__tests__/home.test.tsx`.
- Commit: `15ca136 test: Phase 2 - testing infrastructure`.

## Phase 3 — Data Layer

- `expo-sqlite` + Drizzle ORM. Schema (`src/db/schema.ts`) matches spec §3 exactly.
- Migrations: `src/db/migrations/v1.ts` + `runner.ts` (idempotent, versioned).
- Repositories: `settingsRepo`, `listsRepo`, `gamesRepo`, `listGamesRepo`,
  `mappers.ts` — all fully typed, no `any`.
- `src/utils/score.ts` (pure formula) and `src/utils/sorting.ts` (franchise grouping).
- Unit tests for score, sorting, parseCsv, mapper, errors.
- Commit: `b81f02a feat: Phase 3 - data layer`.

## Phase 4 — RAWG Client & Onboarding

- `src/services/rawg/types.ts`, `client.ts`, `mapper.ts`, `errors.ts`.
- Multiplayer exclusion enforced at mapper level (`mapRawgGame` returns `null`).
- `src/services/secureStore.ts` (get/set/delete API key).
- Screens: `onboarding.tsx` (validation + redirect), `search.tsx`
  (debounced search, multiplayer filtering, error handling for 401/429/404/timeout/network).
- `AddToListModal.tsx` (pick list + initial state, create-new-list support).
- Commit: `40dd5a1 feat: Phase 4 - RAWG client and onboarding`.

## Phase 5 — Core UI

- Zustand stores: `listsStore.ts`, `gamesStore.ts` (thin, call repositories).
- Components: `GameCard`, `StatusBadge`, `EmptyState`, `FranchiseGroup`.
- Screens:
  - `app/index.tsx` (Home): lists + top-pick card + create-list.
  - `app/list/[id].tsx`: games ordered by formula + franchise grouping.
  - `app/game/[id].tsx`: detail, change state, edit franchise, remove from list.
  - `app/manual-game.tsx`: create manual game with validation.
- Full navigation flow works end-to-end.
- Commit: `25629a5 feat: Phase 5 - core UI`.

## Quality Gates (all green at end of Phase 5)

- `tsc --noEmit`: PASS (0 errors)
- `lint`: PASS (0 errors, 0 warnings)
- `format:check`: PASS
- `test`: PASS (11 tests across score, sorting, parseCsv, mapper, errors,
  EmptyState, StatusBadge, home screen)

## Current State

- Baseline verified immediately before starting Phase 6: typecheck exit 0.
- No `any`, no `as unknown as`, no `// @ts-ignore` anywhere.
- No third-party HTTP/state libraries — uses `fetch`, Zustand, Drizzle only.

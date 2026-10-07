# BacklogOrder — Specification

BacklogOrder is an offline-first Android app that helps gamers decide what to
play next from a large backlog. It ranks games using a fixed mathematical
formula that favors high critical ratings and short playtimes.

Open-source, MIT licensed, personal-use-first, no accounts, no cloud.

## 1. The Formula (IMMUTABLE)

```
Score = NormalizedRating / Playtime
```

- `NormalizedRating` = Metacritic score (0–100) if available.
- If Metacritic is missing → `rating` from RAWG (0–5) × 20.
- If neither exists → game has NO SCORE.
- `Playtime` = RAWG `playtime` field, in hours.
- If `playtime` is 0, null, or missing → game has NO SCORE.
- Games with NO SCORE go to the end of the list, sorted alphabetically by name.
- Games with a valid score are sorted by score DESCENDING.

The user CANNOT modify, override, or weight the formula. It is frozen.

## 2. Multiplayer Exclusion

Games tagged as multiplayer (RAWG tags: "Multiplayer", "Co-op",
"Massively Multiplayer") are EXCLUDED from lists. Enforced at mapper level.

## 3. Game States

Exactly one state per (list, game) pair:

- `Backlog` (default)
- `Playing`
- `Completed`
- `Dropped`

State is per list-entry, not global. A game in two lists has two states.

## 4. Franchise / Saga Grouping

Optional fields on each game:

- `franchise` (string, nullable)
- `franchiseOrder` (integer, nullable)

Rendering rules:

- Games with the same `franchise` render as ONE visual group.
- The group inherits the POSITION of its highest-scoring member.
- Inside the group, games sort by `franchiseOrder` ASCENDING.
- Games without `franchise` are singleton groups ordered by their own score.
- If `franchise` present but `franchiseOrder` missing → warn the user in UI,
  render the game last inside the group.

## 5. Lists

Users create any number of custom lists (name is a free string). A game can
belong to multiple lists. Each list is independent.

## 6. Filters

Client-side, combinable, non-destructive:

- Name (substring, case-insensitive)
- Playtime (min/max hours)
- Normalized rating (min/max 0–100)
- Score (min/max)
- Status (Backlog / Playing / Completed / Dropped)

Filters never alter the stored order; they only hide items.

## 7. Home & Recommendation

Priority order:

1. If any game is `Playing` (across all lists) → recommend the highest-scoring one.
2. Else → recommend the highest-scoring `Backlog` game.
3. Else → empty state prompting to add games.

Buttons: "Start playing" (sets state to `Playing`), "Skip" (next candidate,
no state change).

## 8. RAWG Integration

- Users provide their OWN RAWG API key during onboarding.
- Key stored in `expo-secure-store`. Never transmitted anywhere except RAWG.
- Attribution required in-app and in README:
  "Includes data from the RAWG API — https://rawg.io"
- Handle: no network, invalid key (401), rate limit (429), not found (404).

## 9. Offline Behavior

- Everything except search/detail fetches works offline.
- SQLite is the source of truth. Once a game is added, all its data is local.
- Onboarding validation and search require network; everything else does not.

## 10. Manual Game Creation

Users can create games not present in RAWG. Fields:

- Required: `name`
- Optional: `cover`, `metacritic`, `rating`, `playtime`, `genres`,
  `platforms`, `franchise`, `franchiseOrder`

Manual games follow the same formula and rules.

## 11. Export / Import

- Export ALL data or a single list to JSON, shared via `expo-sharing`.
- Import a JSON file (via `expo-document-picker`), validated, deduplicated by
  `rawgId` if present, else by normalized `name`.

Export format:

```json
{
  "version": 1,
  "exportedAt": 1234567890,
  "lists": [
    {
      "id": "…",
      "name": "…",
      "createdAt": 1234567890,
      "games": [
        {
          "rawgId": 12345,
          "name": "…",
          "cover": "…",
          "metacritic": 92,
          "rating": 4.5,
          "playtime": 30,
          "genres": ["…"],
          "platforms": ["…"],
          "franchise": "…",
          "franchiseOrder": 1,
          "status": "Backlog",
          "isManual": false
        }
      ]
    }
  ]
}
```

## 12. Non-Goals (v1)

- No accounts, cloud sync, or backend.
- No social features, achievements, gamification.
- No Steam/PSN/Xbox/GOG/Epic integration.
- No auto-detected franchises (manual only).
- No iOS, Web, or widgets in v1.

## 13. Locked Stack

| Concern        | Choice                                           |
| -------------- | ------------------------------------------------ |
| Runtime        | React Native + Expo (managed workflow)           |
| Language       | TypeScript (strict, no any)                      |
| Routing        | Expo Router (file-based, src/app/)               |
| Persistence    | expo-sqlite + Drizzle ORM                        |
| Global state   | Zustand                                          |
| Secure storage | expo-secure-store                                |
| File system    | expo-file-system                                 |
| Sharing        | expo-sharing                                     |
| File picking   | expo-document-picker                             |
| Testing        | Jest + jest-expo + @testing-library/react-native |
| Lint / format  | ESLint + Prettier                                |
| Path alias     | @/* → ./src/*                                    |

Do not introduce Redux, MobX, React Query, TanStack, Axios,
styled-components, NativeWind, Tamagui, Reanimated (unless justified), or any
state/HTTP library. Use fetch, Zustand, and StyleSheet.

## 14. Data Model (SQLite via Drizzle)

```sql
lists:
  id          TEXT PRIMARY KEY
  name        TEXT NOT NULL
  createdAt   INTEGER NOT NULL
  updatedAt   INTEGER NOT NULL

games:
  id             TEXT PRIMARY KEY
  rawgId         INTEGER UNIQUE NULL
  name           TEXT NOT NULL
  cover          TEXT NULL
  metacritic     INTEGER NULL
  rating         REAL NULL
  playtime       REAL NULL
  genres         TEXT NULL   -- JSON array of strings
  platforms      TEXT NULL   -- JSON array of strings
  tags           TEXT NULL   -- JSON array of strings
  isMultiplayer  INTEGER NOT NULL DEFAULT 0
  isManual       INTEGER NOT NULL DEFAULT 0
  franchise      TEXT NULL
  franchiseOrder INTEGER NULL
  createdAt      INTEGER NOT NULL

list_games:
  listId   TEXT NOT NULL
  gameId   TEXT NOT NULL
  status   TEXT NOT NULL   -- Backlog | Playing | Completed | Dropped
  addedAt  INTEGER NOT NULL
  PRIMARY KEY (listId, gameId)
  FOREIGN KEY (listId) REFERENCES lists(id) ON DELETE CASCADE
  FOREIGN KEY (gameId) REFERENCES games(id) ON DELETE CASCADE

settings:
  key    TEXT PRIMARY KEY
  value  TEXT NOT NULL
```

## 15. Code Conventions

- Files: camelCase.ts for logic, PascalCase.tsx for components.
- Components: functional, one per file, default export.
- Hooks: useThing in src/features/<area>/useThing.ts.
- Pure functions in src/utils/. No side effects.
- DB access ONLY through repositories in src/db/repositories/.
- No direct expo-sqlite calls outside src/db/.
- Zustand stores are thin: they hold state and call repositories.
- No console.log in committed code (except console.warn / console.error).
- All user-facing strings inline in English for now (i18n is post-v1).
- No any. No as unknown as. No // @ts-ignore without a written reason.

## 16. Quality Gates

Every phase must end with:

- `npx tsc --noEmit` → 0 errors.
- `npm run lint` → 0 errors, 0 warnings.
- `npm run format:check` → clean.
- `npm test` → all tests pass.
- Manual smoke: the app boots and the phase's main flow works.

If any gate fails, FIX IT before ending the phase. Do not report success with
failing gates.

# BacklogOrder Export Schema v1

This document defines the JSON export format for BacklogOrder.

```json
{
  "version": 1,
  "app": "BacklogOrder",
  "appVersion": "1.0.0",
  "format": "json",
  "contentHashAlgorithm": "sha256-v1",
  "exportedAt": 1735689600000,
  "lists":   [ { "id":"…","name":"…","createdAt":0,"updatedAt":0 } ],
  "games":   [ { "id":"…","rawgId":null,"name":"…","backgroundImage":null,"backgroundImageUrl":null,
                 "metacritic":null,"rating":null,"playtimeHours":null,
                 "genres":[],"platforms":[],"tags":[],
                 "isManual":false,"franchiseName":null,"franchiseOrder":null,
                 "createdAt":0,"updatedAt":0,"contentHash":"…" } ],
  "listGames": [ { "id":"…","listId":"…","gameId":"…","status":"BACKLOG","addedAt":0 } ]
}
```

Single-list export uses the same shape with only that list and its games.

## Required Fields

- `version` (integer): Must be 1 for schema v1
- `app` (string): Always "BacklogOrder"
- `exportedAt` (number): Epoch milliseconds
- `lists` (array): Array of list objects
- `games` (array): Array of game objects (includes ALL games: manual and RAWG-sourced)
- `listGames` (array): Array of list-game relationship objects

## Additive Extensions (Phase 2 Upgrade)

1. **Unfiltered Game Export**: Exports **all** games in the user library (RAWG-sourced and manual entries). The `isManual` boolean property designates game origin (`true` for manual, `false` for RAWG).
2. **Envelope Fields**:
   - `appVersion` (string): Application version name (e.g. `"1.0.0"` or `"unknown"`)
   - `format` (string): Output format identifier, always `"json"`
   - `contentHashAlgorithm` (string): Hash algorithm identifier, always `"sha256-v1"`
3. **Two-Field Image Model**:
   - `backgroundImage` (string / null): Absolute local file path. Nulled at export if the local file does not exist on disk or is empty (0-bytes).
   - `backgroundImageUrl` (string / null): Remote cover URL string (verbatim from database).
4. **Game Content Hash**:
   - `contentHash` (string): Lowercase SHA-256 hex string calculated over canonical UTF-8 game metadata (`name|rawgId|metacritic|rating|playtimeHours|sorted(genres)|sorted(platforms)|sorted(tags)|isManual|franchiseName|franchiseOrder`).

## List Object Schema

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| id | string | Yes | UUID |
| name | string | Yes | Non-empty |
| createdAt | number | Yes | Epoch milliseconds |
| updatedAt | number | Yes | Epoch milliseconds |

## Game Object Schema

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| id | string | Yes | UUID |
| rawgId | number | No | null for manual games |
| name | string | Yes | Non-empty |
| backgroundImage | string | No | Local file path or null if missing/unreachable |
| backgroundImageUrl | string | No | Remote cover URL string or null |
| metacritic | number | No | 0-100 or null |
| rating | number | No | 0-5 or null |
| playtimeHours | number | No | ≥0 or null |
| genres | array | Yes | Array of genre strings |
| platforms | array | Yes | Array of platform strings |
| tags | array | Yes | Array of tag strings |
| isManual | boolean | Yes | false for RAWG games, true for manual |
| franchiseName | string | No | Can be null |
| franchiseOrder | number | No | Can be null |
| createdAt | number | Yes | Epoch milliseconds |
| updatedAt | number | Yes | Epoch milliseconds |
| contentHash | string | Yes | SHA-256 canonical metadata hash |

## ListGames Object Schema

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| id | string | Yes | UUID |
| listId | string | Yes | References a list |
| gameId | string | Yes | References a game |
| status | string | Yes | BACKLOG/PLAYING/COMPLETED/DROPPED |
| addedAt | number | Yes | Epoch milliseconds |

## Validation Rules

1. **JSON Parsing**: Must be valid JSON
2. **Version Check**: `version` must be exactly 1
3. **Required Fields**: All fields marked as required must be present
4. **Field Types**: Field types must match (numbers, strings, arrays)
5. **Value Constraints**:
   - `status` must be one of: BACKLOG, PLAYING, COMPLETED, DROPPED
   - `metacritic` must be 0-100 if present
   - `rating` must be 0.0-5.0 if present
   - `playtimeHours` must be ≥0 if present
   - `isManual` must be boolean
6. **Reference Integrity**:
   - All `listGames.listId` values must reference an existing list in the same export
   - All `listGames.gameId` values must reference an existing game in the same export
   - Each list-game pair must be unique (combination of listId and gameId)

## Export/Import Guarantees

### Atomic Write & Verification
- Export writes to a temporary file (`BacklogOrder_Export_<ts>.json.tmp`) in `DIRECTORY_DOCUMENTS`.
- Post-write verification parses the temporary file, asserts entity counts match, verifies referential integrity, and confirms all exported cover image files exist.
- Upon successful verification, the temporary file is renamed atomically to `BacklogOrder_Export_<ts>.json`.
- If verification fails, the `.tmp` file is deleted and an `ExportError.VerificationFailed` error is returned.
- Stale `.tmp` export files (>1 hour old) are automatically cleaned up on startup.

### Import Strictness & Transactional Safety
- Import parser uses `coerceInputValues = false` to enforce strict schema validation.
- If any validation error occurs, no changes are written to the database (transactional rollback).
- Re-importing the same valid export produces no duplicate data (idempotent).

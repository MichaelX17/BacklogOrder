# BacklogOrder Export Schema v1

This document defines the JSON export format for BacklogOrder.

```json
{
  "version": 1,
  "app": "BacklogOrder",
  "exportedAt": "2025-01-01T00:00:00Z",
  "lists":   [ { "id":"…","name":"…","createdAt":0,"updatedAt":0 } ],
  "games":   [ { "id":"…","rawgId":null,"name":"…","backgroundImage":null,
                 "metacritic":null,"rating":null,"playtimeHours":null,
                 "genres":[],"platforms":[],"tags":[],
                 "isManual":false,"franchiseName":null,"franchiseOrder":null,
                 "createdAt":0,"updatedAt":0 } ],
  "listGames": [ { "id":"…","listId":"…","gameId":"…","status":"BACKLOG","addedAt":0 } ]
}
```

Single-list export uses the same shape with only that list and its games.

## Required Fields

- `version` (integer): Must be 1 for schema v1
- `app` (string): Always "BacklogOrder"
- `exportedAt` (string): ISO-8601 timestamp
- `lists` (array): Array of list objects
- `games` (array): Array of game objects
- `listGames` (array): Array of list-game relationship objects

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
| backgroundImage | string | No | Can be null |
| metacritic | number | No | 0-100 or null |
| rating | number | No | 0-5 or null |
| playtimeHours | number | No | ≥0 or null |
| genres | array | Yes | JSON array string |
| platforms | array | Yes | JSON array string |
| tags | array | Yes | JSON array string |
| isManual | boolean | Yes | false for RAWG games, true for manual |
| franchiseName | string | No | Can be null |
| franchiseOrder | number | No | Can be null |
| createdAt | number | Yes | Epoch milliseconds |
| updatedAt | number | Yes | Epoch milliseconds |

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

## Validation Errors

Return `AppError.Validation` with descriptive message for:
- Invalid JSON or malformed structure
- Wrong version (expect 1, got X)
- Missing required fields
- Wrong field types
- Invalid enum values
- Invalid value ranges
- Broken reference integrity (missing list or game)

## Export/Import Guarantees

### Import is Transactional
- If any validation error occurs, no changes are written to the database
- Either the entire export is imported, or nothing is imported

### Import is Idempotent  
- Re-importing the same valid export produces no duplicate data
- Existing `lists`, `games`, and `listGames` entries are preserved
- `listGames` relationships are preserved if they already exist

### Data Integrity
- Deleting a list deletes **only** its `list_games` rows, **never** the `games` rows
- Shared games remain intact when their lists are deleted
- Manual games can be deduplicated by `rawgId` when present, else by `isManual = 1 AND name COLLATE NOCASE`

## Export Limitations

- Only exports data from this app (BacklogOrder)
- Schema version 1 only
- Maximum export size limited by device storage
- No external references (e.g., RAWG API keys stored externally)

# BacklogOrder Project Analysis

## Project Overview

**BacklogOrder** is an offline-first Android mobile application designed to help gamers decide what to play next from their game backlog. The application uses a fixed mathematical formula to rank games based on critical ratings and short playtimes, providing deterministic recommendations for the user's next gaming session.

**Status**: Early development (v0.1.0 in progress) - Core features implemented, Phase 5 complete per project documentation

## Technical Stack

### Frontend
- **Framework**: React Native + Expo (managed workflow, version 57.0.27)
- **Language**: TypeScript (strict mode, no `any` types)
- **Routing**: Expo Router (file-based, src/app/ directory)
- **State Management**: Zustand (lightweight state container)
- **Database**: expo-sqlite + Drizzle ORM (SQLite, offline-first)
- **Styling**: NativeWind excluded, using StyleSheet with thematic design
- **Security**: expo-secure-store for API key storage
- **Utilities**: expo-file-system, expo-sharing, expo-document-picker
- **Testing**: Jest + jest-expo + @testing-library/react-native
- **Code Quality**: ESLint + Prettier

### Backend Integration
- **API**: RAWG (rawg.io) for game data, user-provided API key
- **Authentication**: Local storage of API key, no accounts or cloud services

## Core Functionality

### The Ranking Formula (IMMUTABLE)
```
Score = NormalizedRating / Playtime
```

**NormalizedRating Rules**:
- Metacritic score (0-100) if available
- If Metacritic missing → RAWG rating (0-5) × 20
- If neither exists → game has NO SCORE

**Multiplayer Exclusion**: Games tagged as "Multiplayer", "Co-op", or "Massively Multiplayer" are automatically excluded from lists

### Game States
Each game in a list can be in one of four states:
- **Backlog** (default)
- **Playing**
- **Completed**
- **Dropped**

States are per (list, game) pair - a game in two lists can have different states

### Franchise / Saga Grouping
- Optional `franchise` and `franchiseOrder` fields on games
- Games with same franchise render as visual groups
- Groups inherit position of highest-scoring member
- Manual franchise management only

### Home & Recommendation System
Priority order:
1. If any game is `Playing` → recommend highest-scoring `Playing` game
2. Else → recommend highest-scoring `Backlog` game
3. Else → empty state prompting to add games

**UI Actions**: "Start playing" (sets state to `Playing`), "Skip" (next candidate, no state change)

## Code Architecture

### Directory Structure
```
src/
├── app/                    # Expo Router screens (15+ files)
│   ├── _layout.tsx       # Root navigator
│   ├── index.tsx         # Home screen
│   ├── list/[id].tsx      # List detail with filters and franchise groups
│   ├── game/[id].tsx      # Game details with status changes
│   ├── search.tsx        # RAWG search with debouncing
│   ├── manual-game.tsx   # Manual game creation
│   └── onboarding.tsx    # RAWG API key setup
├── components/            # Reusable UI components
│   ├── GameCard.tsx      # Game display card
│   ├── StatusBadge.tsx   # Status indicator
│   ├── EmptyState.tsx    # Empty data states
│   └── FranchiseGroup.tsx # Franchise grouping UI
├── features/              # Feature-specific modules
│   ├── home/            # Home feature (empty - structure ready)
│   ├── games/           # Games utilities
│   │   └── useDebouncedValue.ts
│   ├── lists/           # Lists utilities (empty - structure ready)
│   └── settings/        # Settings utilities (empty - structure ready)
├── stores/               # Zustand stores
│   ├── gamesStore.ts     # Games state management
│   └── listsStore.ts     # Lists state management
├── db/                   # Database layer
│   ├── schema.ts        # Drizzle ORM schema
│   ├── client.ts        # Database initialization
│   ├── repositories/     # Repository pattern implementations
│   └── webStorage.ts     # Web fallback storage
├── services/             # Service layer
│   ├── rawg/            # RAWG API integration
│   │   ├── client.ts     # API client with error handling
│   │   ├── mapper.ts     # Data mapping (multiplayer filtering)
│   │   └── types.ts      # TypeScript definitions
│   ├── secureStore.ts    # API key storage
│   ├── export.ts         # Data export functionality
│   └── import.ts         # Data import functionality
├── utils/                # Pure utility functions
│   ├── score.ts          # Core scoring formula
│   ├── recommendation.ts  # Recommendation algorithm
│   ├── filters.ts        # Filter utilities
│   ├── sorting.ts        # Franchise grouping logic
│   └── parseCsv.ts        # CSV parsing utilities
└── theme/               # Design system
    └── hudTheme.ts       # Theme constants and styles
```

### Design Principles

1. **Repository Pattern**: All database access through repositories in `src/db/repositories/`
2. **Pure Functions**: Logic utilities in `src/utils/` with no side effects
3. **Component Architecture**: One component per file, functional components
4. **Type Safety**: Full TypeScript strict mode, no `any` or `as unknown as`
5. **Offline-First**: SQLite as source of truth, everything works offline after initial data sync
6. **Immutable Formula**: The ranking formula cannot be modified by users

## Key Features Implemented

### 1. Lists Management
- Create custom lists with per-list game states
- Each list maintains independent ordering and filtering
- Lists can overlap (games can exist in multiple lists)

### 2. Core Ranking & Recommendations
- Deterministic ranking using frozen formula
- Home screen shows top recommendation with "Start playing" / "Skip" actions
- Playing games prioritized for recommendations

### 3. Game Search & Discovery
- RAWG API integration with search and game details
- Multiplayer filtering at mapper level
- Game detail fetching with pagination

### 4. Manual Game Creation
- Create games not present in RAWG database
- Validation for name, Metacritic (0-100), rating (0-5), playtime, franchise order
- Manual games follow same ranking rules

### 5. Franchise Management
- Manual franchise and saga grouping
- Visual grouping with proper ordering
- Warning system for missing franchise order

### 6. Filtering & Sorting
- Name, playtime, rating, score, and status filters
- Franchise-based grouping with hierarchical sorting
- Client-side, non-destructive filtering

### 7. Data Export / Import
- Export all data or single lists to JSON
- Import JSON files with validation and deduplication
- Expo Sharing for easy data transfer

### 8. Offline Capabilities
- SQLite stores all game data locally
- All features work offline after initial sync
- Network required only for RAWG API integration

## Development Approach

### Phase-Based Development
1. **Phase 1**: Project foundation (Expo Router, folder structure, basic configs)
2. **Phase 2**: Testing infrastructure (Jest setup, unit tests)
3. **Phase 3**: Data layer (Database schema, repositories, pure utilities)
4. **Phase 4**: RAWG client and onboarding (API integration, search)
5. **Phase 5**: Core UI (Stores, components, main screens, navigation)

### Quality Gates
Every phase must end with:
- `npx tsc --noEmit` → 0 errors
- `npm run lint` → 0 errors, 0 warnings
- `npm run format:check` → clean
- `npm test` → all tests pass
- Manual smoke test of main functionality

## Notable Implementation Details

### Database Schema
```sql
lists: id, name, createdAt, updatedAt
games: id, rawgId, name, cover, metacritic, rating, playtime, genres, platforms, tags, isMultiplayer, isManual, franchise, franchiseOrder, createdAt
list_games: listId, gameId, status, addedAt (composite PK)
settings: key, value
```

### Key Algorithms

**Scoring** (`src/utils/score.ts`):
- Handles Metacritic vs RAWG rating normalization
- Zero playtime results in infinite score (∞)
- Returns null for games without sufficient data

**Recommendation** (`src/utils/recommendation.ts`):
- Prioritizes Playing games, then Backlog
- Uses skipCount for recommendation carousel
- Integrates with home screen navigation

**Franchise Grouping** (`src/utils/sorting.ts`):
- Complex hierarchical sorting based on franchise, score, and manual order
- Groups games by franchise for better UX

### Error Handling
- Comprehensive RAWG API error handling (401, 429, 404, timeout, network)
- Form validation with user-friendly error messages
- Graceful degradation for API failures

## Project Status

**Current State**: Phase 6 complete, ready for Phase 7 (Export/Import features)

**Quality Gates Met**:
- TypeScript: ✅ PASS (0 errors)
- Lint: ✅ PASS (0 errors, 0 warnings)
- Format: ✅ PASS
- Tests: ✅ PASS (11 tests across all modules)

**Next Steps**: Export/Import functionality (Phase 6), additional mobile features

## User Experience

The application provides a clean, mobile-first interface with:
- Deterministic ranking ensures consistent results
- Offline-first design works everywhere
- Simple onboarding process with RAWG API key
- Visual franchise grouping improves discoverability
- Responsive design optimized for mobile gaming
- Dark-themed interface with accent colors

## Conclusion

BacklogOrder is a well-architected, type-safe React Native application that solves a specific problem (game recommendation) with a clear, immutable formula and comprehensive offline capabilities. The project follows modern development practices with phase-based delivery, strict quality gates, and maintainable code structure.

The implementation demonstrates deep understanding of React Native, Expo best practices, TypeScript, and mobile app development patterns while staying focused on the core user need: helping gamers efficiently choose their next play session from a large backlog.
# BacklogOrder

An offline-first Android app that helps gamers decide what to play next from a
large backlog. It ranks games using a fixed mathematical formula that favors
high critical ratings and short playtimes.

**Status: early development (v0.1.0 in progress).** Not yet feature-complete —
see the roadmap below.

## The Formula

```
Score = NormalizedRating / Playtime
```

- `NormalizedRating` = Metacritic score (0–100), or RAWG rating (0–5) × 20
  when Metacritic is missing.
- `Playtime` = RAWG playtime in hours.
- Games without a computable score are listed at the end, alphabetically.

The formula is frozen — it cannot be modified, overridden, or weighted.

Multiplayer games (RAWG tags: "Multiplayer", "Co-op", "Massively
Multiplayer") are excluded automatically.

## Features (planned / in progress)

- Custom lists with per-list game states (Backlog / Playing / Completed / Dropped)
- Deterministic ranking via the frozen formula
- Franchise / saga grouping with manual ordering
- Combinable, non-destructive filters
- Home recommendation: highest-scoring `Playing` game, else best `Backlog`
- RAWG search with your own API key (stored in secure storage, never transmitted)
- Manual game creation
- JSON export / import
- Fully offline after data is added (SQLite is the source of truth)

## Tech Stack

React Native + Expo (managed workflow), TypeScript (strict), Expo Router,
expo-sqlite + Drizzle ORM, Zustand, expo-secure-store, expo-file-system,
expo-sharing, expo-document-picker, Jest, ESLint, Prettier.

## Getting Started

```bash
npm install
npm start          # start the Expo dev server
npm run android    # run on Android (device or emulator)
npm test           # run unit tests
npm run lint       # lint
npm run typecheck  # typecheck
```

## Data Attribution

Includes data from the RAWG API — https://rawg.io

## License

MIT — see [LICENSE](./LICENSE).

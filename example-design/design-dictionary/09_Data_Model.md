# 9. Data Model
> Tags: `Data` `Models` `Types`
> **Tags:** `Data` `Model` `Game`

### Types (from `lib/games.ts`)
> Tags: `Data` `Types` `GameStatus` `Game` `HudTheme`

#### `GameStatus`
> Tags: `Data` `Types` `GameStatus`
> Tags: `Data` `Type` `GameStatus`
```ts
export type GameStatus = 'playing' | 'backlog'
```
Note: The web design only has two statuses (playing/backlog). The main Expo project has four (Backlog, Playing, Completed, Dropped). When porting, you may need to extend the status types or map them.

#### `Game`
> Tags: `Data` `Types` `Game`
> Tags: `Data` `Type` `Game`
```ts
export type Game = {
  id: string
  title: string
  cover: string       // local asset path: '/covers/metro-2033.png'
  playtimeHours: number
  metacritic: number
  status: GameStatus
  price: number
}
```

#### `HudTheme`
> Tags: `Data` `Types` `Theme`
> Tags: `Data` `Type` `Theme`
```ts
export type HudTheme = {
  id: 'violet' | 'emerald' | 'crimson'
  version: string
  name: string
  className: string
  swatches: string[]  // array of 3 hex colors for palette preview
}
```

### Sample Data (4 games)
> Tags: `Data` `Sample-Data` `Games`

| id | title | cover | playtimeHours | metacritic | status | price |
|---|---|---|---|---|---|---|
| metro-2033 | Metro 2033 | /covers/metro-2033.png | 10 | 81 | playing | 39.5 |
| tormented-souls | Tormented Souls | /covers/tormented-souls.png | 8 | 72 | backlog | 19.0 |
| resident-evil-4 | Resident Evil 4 (2005) | /covers/resident-evil-4.png | 15 | 96 | backlog | 16.6 |
| devil-may-cry-5 | Devil May Cry 5 | /covers/devil-may-cry-5.png | 12 | 89 | playing | 9.78 |

### Computed Values in the UI
> Tags: `Data` `Computed` `Calculations`

- **Total price:** Sum of `price` for all *visible* games (based on active filter). Rendered as `${total.toFixed(2)}`.
- **Entry count:** `visibleGames.length.toString().padStart(2, '0')` → renders like "04 entries"

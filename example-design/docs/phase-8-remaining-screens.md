## PHASE 8 — Remaining screens (same design language)

Apply the same primitives. Don't invent new features.

### 8.1 Game detail (`game/[id].tsx`)
- Header: back `HexIcon` + eyebrow `// GAME_DETAIL` + game name (`screenTitleSm`, 2 lines max).
- A hero cover like the Home hero (height 180) with `StatusBadge` and the **same Formula readout**.
- Status changer: 4 `HexPill` chips in a 2×2 grid or a row (Backlog / Playing / Completed / Dropped). Active chip filled with **that status's color** and black text. Inactive: outline in the status color.
- Info rows (genres, platforms, franchise + order): label `microLabel`, value `bodySm`, separated by `GradientRule`s.
- Destructive actions (remove from list): secondary `HudButton` with the border and text in `#ff4d6d` (add `danger: '#ff4d6d'` to the fixed tokens), plus confirmation as today.

### 8.2 Search (`search.tsx`)
- Header: eyebrow `// RAWG_SEARCH`, title `SEARCH`.
- A HUD search input (see 8.4) with a `Search` icon. Keep the existing debounce.
- Results: a compact `GameCard` variant (no rank chip). Instead of the status badge, show an `ADD` pill button (`HexPill`, `secondary` outline). Show `ADDED` in `completed` when the game is already in the list.
- Loading: 3 skeleton cards (BevelFrame with fill `surface`, opacity pulse respecting reduce motion). Errors (401/429/404/timeout/network): `EmptyState` with `TriangleAlert` in `playing` color and the existing error copy.

### 8.3 Manual game (`manual-game.tsx`)
- A form inside one `BevelFrame` panel. Fields: name, Metacritic (0–100), RAWG rating (0–5), playtime, franchise, franchise order.
- Show the **live Formula readout** under the fields, using the same `computeScore` utility, so the user sees the score before saving.
- Validation errors: Rajdhani 500, 12, `#ff4d6d`, below the field, linked with `accessibilityLabelledBy` / an announced error.
- Save = primary `HudButton` full width. Cancel = secondary.

### 8.4 HUD text input (new component `HudTextInput`)
`BevelFrame` cut `sm`, border `withAlpha(secondary, 0.35)` (focused: `primary` + glow), fill `rgba(0,0,0,0.6)`, height 44, paddingHorizontal 12. Floating label above the field (`microLabel`). Input text Rajdhani 600, 15, white. Placeholder color muted. `selectionColor = primary`.

### 8.5 Onboarding (`onboarding.tsx`)
- Eyebrow `// SYSTEM_INIT`, title `LINK RAWG` with glow.
- A short explanation (Rajdhani 500, 15, `text` color), then a `HudTextInput` for the API key (`secureTextEntry`, with an eye toggle as a diamond `HexIcon`).
- Primary `CONNECT` button, plus a link-style secondary to open rawg.io.
- Keep storage in `expo-secure-store` exactly as today.

### 8.6 Settings (if/where it exists)
Add a **Theme** section: 3 `BevelFrame` rows (cut `md`), one per theme. Each row shows the version (`microLabel`), the name (Orbitron 700, 13, white), and 3 swatch diamonds (`bgTo`, `primary`, `secondary`; 16px squares rotated 45° with a `rgba(255,255,255,0.15)` border). The selected row gets a `primary` border + glow and `accessibilityState.selected`. Tapping calls `setTheme` (Phase 2), and the whole app recolors immediately. Put export/import buttons here as secondary `HudButton`s if they already live in settings.


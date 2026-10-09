## PHASE 4 — Domain components (restyle `src/components/`)

Keep the current props and usage. Change **only the visuals** unless I say otherwise.

### 4.1 `StatusBadge`
- `HexPill` with a 1px outline in the status color and inner fill `rgba(0,0,0,0.85)`, padding `10 × 3`.
- Content: a 6px `DiamondDot` + label (`badge` preset) in the status color.
- Status colors: Backlog → `backlog`, Playing → `playing`, Completed → `completed`, Dropped → `dropped`.
- **Playing**: the dot pulses (`Animated` loop, opacity 1 → 0.4 → 1, 1s, `useNativeDriver: true`). Disable the pulse when `AccessibilityInfo.isReduceMotionEnabled()` is true.
- **Dropped**: no glow, the dot is an outline (not filled), and the label has `textDecorationLine: 'line-through'`.
- All other statuses glow in their status color.

### 4.2 `ScoreTag` (new, used by GameCard)
- `BevelFrame` cut `sm`, 1px border `withAlpha(secondary, 0.8)` (selected: `primary`), fill `rgba(0,0,0,0.8)`, `minWidth: 54`, padding `8 × 4`, right-aligned content.
- Top line: `SCORE` (`microLabel`). Bottom line: the value (`scoreValue`; secondary, or primary when selected).
- Display formatting: `null` → `—`, `Infinity` → `∞`, otherwise `toFixed(2)`. Put this in `formatScore` in `src/utils/` if it doesn't exist, and unit-test it.

### 4.3 `GameCard`
Layout: a horizontal row inside a `BevelFrame` cut `lg`.

- Outer border: `withAlpha(secondary, 0.3)`. **Selected**: `primary` border + `primary` glow. Pressed: border `withAlpha(secondary, 0.6)`.
- Inner fill: `panelInk` (`withAlpha(bgFrom, 0.82)`), plus a subtle horizontal overlay gradient `rgba(255,255,255,0.04)` → transparent → `withAlpha(primary, 0.06)`.
- Padding 10, gap 12, `alignItems: 'center'`.
- **Left**: `BevelImage` 56×56 (cut `sm`). Show a `RankChip` bottom-left for ungrouped games, or a `SagaOrderChip` inside a franchise group. **Completed or Dropped** games are dimmed (cover opacity 0.6, title white @ 70%).
- **Middle** (`flex: 1`, `minWidth: 0`):
  - Title: `cardTitle`, `numberOfLines={1}`.
  - Meta row (gap 10, marginTop 2): `Clock` icon 12 + `{playtime}h`, and `Star` icon 12 + rating source. Rating source text: `MC 96` if Metacritic exists, else `RAWG 4.6`, else italic `No rating`. Screen reader text: "Metacritic 96" / "RAWG rating 4.6".
  - `StatusBadge` with marginTop 6.
- **Right**: `ScoreTag` (highlighted when selected).
- The whole card is one `Pressable`. Pressing it opens `game/[id]` as it does today. Keep the existing navigation. `accessibilityLabel`: `"{name}, {status}, score {formatted}"`.

### 4.4 `FranchiseGroup`
- **Header row** (gap 8, paddingBottom 6): `RankChip` (the group's rank), `Layers` icon 14 in `secondary` with glow, title `SAGA · {NAME}` (`sagaTitle`, with "SAGA · " in muted), then a `GradientRule` (flex 1), then `BEST {score}` (`microLabel`, the score in `secondary`).
- **Body**: a container with a 1px left border `withAlpha(secondary, 0.4)` and `paddingLeft: 10`. Place a 6px rotated-45° `secondary` diamond at the top-left and bottom-left of that border (absolute, `left: -3`).
- Members are `GameCard`s (gap 8) in `franchiseOrder` order, each showing a `SagaOrderChip`.
- When any member has no `franchiseOrder`: show a warning row (marginTop 6) with `TriangleAlert` 12 + "Missing saga order on some entries" (Rajdhani 500, 11, `playing` color).
- The group's position and score come from the existing `sorting.ts` logic. Don't re-implement it.

### 4.5 `EmptyState`
Centered column (gap 12): a 32px lucide icon in `secondary` with glow (use the icon the current component passes, or `ListPlus` by default), and the message in Orbitron 500, 10, letterSpacing 2.5, UPPERCASE, muted. If it has an action button, use `HudButton` `secondary`.

### 4.6 `HudSectionHeader` (new)
Label (`sectionLabel`) + `GradientRule`, gap 8. Used for "THEN", "{N} GAMES · RATING ÷ HOURS", and similar headings.

### 4.7 `HudStat` (new)
`BevelFrame` cut `sm`, fill `surface`, paddingVertical 6, centered: label (`microLabel`, letterSpacing 1.54) and the value zero-padded to 2 digits (`statValue` in the status color).

---


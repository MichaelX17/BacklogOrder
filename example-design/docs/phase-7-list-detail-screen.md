## PHASE 7 — List detail screen (`src/app/list/[id].tsx`)

Keep the existing filtering, sorting, and franchise grouping logic. Restyle as follows, padding 16:

1. **Header row** (gap 12, `alignItems: 'center'`):
   - Back button: `HexIcon` md, `secondary`, with `ArrowLeft`, `accessibilityLabel="Go back"`.
   - Middle (flex 1): eyebrow `// LIST_{nn}` (or keep the list id/name style you prefer, zero-padded), then the list name in `screenTitleSm` with glow, `numberOfLines={1}`.
   - Right (right-aligned): `QUEUE` (`microLabel`, letterSpacing 2) over the **total hours of Backlog + Playing games** in the currently visible set (Orbitron 700, 14, `primary`, e.g. `36h`).
2. **Actions** (marginTop 12, row, gap 10, each `flex: 1`, height 40): primary `SEARCH RAWG` (`Search` icon) → existing search route. Secondary `ADD MANUALLY` (`Plus` icon) → `manual-game`.
3. **Filter panel** (marginTop 12): `BevelFrame` cut `sm`, border `withAlpha(secondary, 0.25)`, fill `surface`, padding `8 × 6`.
   - Top row: diamond `HexIcon` sm with `SlidersHorizontal`, label `STATUS` (Orbitron 700, 9, letterSpacing 1.8, muted), spacer, current sort label (e.g. `SCORE ↓`, `microLabel` at 8), and a `ArrowDownUp` icon button (14, `secondary`, glow, `accessibilityLabel="Change sort"`) that opens the existing sort options (name / playtime / rating / score).
   - Chips row (marginTop 6, horizontal `ScrollView`, gap 4): `ALL`, `BACKLOG`, `PLAYING`, `COMPLETED`, `DROPPED`. Each chip is a `HexPill`, padding `10 × 4`, `chip` text. Active: fill `primary`, black text. Inactive: fill `rgba(255,255,255,0.05)`, muted text. `accessibilityState={{ selected }}`.
   - If the screen also has name/playtime/rating filters today, keep them inside this same panel, styled as HUD inputs (see Phase 8.4).
4. **Meta divider** (marginTop 12): `{NN} GAMES · RATING ÷ HOURS` (`microLabel`, 8, letterSpacing 2.4) + `GradientRule` + a 4px `primary` diamond.
5. **List**: use `FlatList` (not a mapped `ScrollView`). Gap 12, paddingBottom so the last item clears the tab bar. Items are either a `FranchiseGroup` or a ranked `GameCard`, from the existing grouped-sort output. `keyExtractor`: the franchise name or the game id. `ListEmptyComponent`: "NO GAMES IN THIS STATE" (empty-state text style, paddingVertical 40).
6. Selected/highlight state: if the screen has no selection concept, the `primary` "selected" styling applies to the **pressed** state only. Don't add new state for it.

---

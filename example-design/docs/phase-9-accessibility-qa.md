## PHASE 9 — Accessibility, performance, QA

- **Contrast**: confirm muted text on `bgFrom` and all status colors on `rgba(0,0,0,0.85)` meet at least 4.5:1 for text 14 and under. Report any failures with the exact pair, and propose a token tweak rather than silently changing colors.
- **Screen reader pass** (TalkBack): cards read "name, status, score". Ranks read "Rank 1". The formula reads as a sentence. Decorative SVG layers, diamonds, and rules are hidden.
- **Touch targets** ≥ 44dp everywhere (chips can use `hitSlop`).
- **Reduce motion**: no pulsing when enabled.
- **Performance**: `HudBackground`, `BevelFrame`, `HexIcon`, `GameCard` are `React.memo`. Styles come from memoized `makeStyles(colors)`. Lists use `FlatList` with stable keys. `onLayout`-based SVGs only re-measure when the size actually changes. No inline `StyleSheet.create` in render.
- **Theme switching**: switch through all 3 themes on every screen and confirm nothing is hardcoded to violet. Search the codebase for leftover old hex colors from the previous `hudTheme.ts`.
- **Tests to add**: `formatScore`, `withAlpha`/`mix`, the `StatusBadge` label for each status, `GameCard` rendering the rating source (MC / RAWG / No rating) and the `∞` score for playtime 0, `FranchiseGroup` showing the missing-order warning.
- Final report: before/after screenshot list for Home, List, Game detail, Search, Manual game, Onboarding, Settings in **Violet**, plus Home and List in Emerald and Crimson.


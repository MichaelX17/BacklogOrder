## PHASE 6 — Home screen (`src/app/index.tsx`)

Keep the existing recommendation logic (Playing first, then Backlog, `skipCount`, "Start playing" sets status to Playing, "Skip" moves to the next candidate without changing state). Restyle as follows, top to bottom, horizontal padding 16:

1. **Header** (row, `justifyContent: 'space-between'`, `alignItems: 'flex-end'`):
   - Left: eyebrow `// NEXT_SESSION`, then title `UP NEXT` (`screenTitle`, with glow).
   - Right: `HexPill` with fill `rgba(255,255,255,0.05)`, padding `10 × 4`, containing a 6px `completed`-colored diamond + `OFFLINE READY` (Orbitron 700, 8, letterSpacing 1.44, `completed` color).
2. **Scroll area** (`ScrollView`, paddingTop 12, gap 12, no scroll indicator):
   - **Hero pick card**: `BevelFrame` cut `lg`, 1px `primary` border, `primary` glow, fill `bgFrom`, overflow hidden.
     - Cover image, full width, height **144**, with a vertical `LinearGradient` overlay `transparent → withAlpha(bgFrom, 0.3) → bgFrom` (top to bottom).
     - Top-left tag (absolute, 8/8): background `rgba(0,0,0,0.7)`, padding `6 × 2`, text `PICK {index+1}/{total}` in Orbitron 700, 8, letterSpacing 1.6, `primary`.
     - Content block overlapping the image bottom (`marginTop: -32`, padding `0 12 12`): `StatusBadge`, then the game name (`heroTitle`, marginTop 6), then the **Formula readout**.
     - Wrap the card in a container with `accessibilityLiveRegion="polite"` so the change is announced on Skip.
   - **Formula readout** (row, gap 6, marginTop 10, `alignItems: 'stretch'`). It makes the frozen formula visible:
     - Cell 1: `BevelFrame` cut `sm`, border `withAlpha(secondary, 0.4)`, fill `rgba(0,0,0,0.7)`. Label `METACRITIC` or `RAWG ×20` (`microLabel`), value = normalized rating (Orbitron 700, 14, white).
     - `÷` (Orbitron, 14, muted).
     - Cell 2: same style. Label `HOURS`, value `{playtime}h`.
     - `=`
     - Result cell: `BevelFrame` cut `sm`, `primary` border, fill `rgba(0,0,0,0.85)`. Label `SCORE`, value = `formatScore` (Orbitron 900, 16, `primary`).
     - Each cell `flex: 1`, padding `8 × 4`, centered. The whole row gets one `accessibilityLabel`: `"Score 6.40: Metacritic 96 divided by 15 hours"`. Hide the individual cells from screen readers.
   - **Action row**: `flexDirection: 'row'`, gap 10. A primary button takes `flex: 1`: if the current pick is already Playing, label `CONTINUE` with the `Gamepad2` icon, otherwise `START PLAYING` with `Play` (filled). Then a secondary `SKIP` button with `SkipForward` (auto width, paddingHorizontal 16), disabled when fewer than 2 candidates exist. Height 44 for both.
   - **"Then" queue** (only when there are more candidates): `HudSectionHeader` "THEN", then the next 2 candidates (wrapping around the list) as compact rows: `BevelFrame` cut `sm`, fill `surface`, padding `10 × 6`, gap 10. Each row has the position number zero-padded (Orbitron 900, 10, `secondary`), the name (`bodySm`, `numberOfLines={1}`, flex 1), and the score (Orbitron 700, 11, `primary`).
   - **Stats row**: 3 `HudStat` in a row (gap 8, each `flex: 1`): `PLAYING` (playing color), `BACKLOG` (backlog color), `DONE` = completed count (completed color).
3. **Empty state** (no candidates): the `EmptyState` from 4.5 with `ListPlus` and "ADD GAMES TO GET A PICK", plus a secondary button that goes to search / add game (whatever the current empty state links to).

---

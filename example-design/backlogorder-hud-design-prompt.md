# PROMPT — Apply the "Dark Fantasy Cyber HUD" design to BacklogOrder (React Native + Expo)

You are working on **BacklogOrder**, an offline-first Android app built with React Native + Expo (managed), Expo Router (`src/app/`), TypeScript strict, Zustand, expo-sqlite + Drizzle, and **StyleSheet** styling (NativeWind is intentionally NOT used). Your job is to apply a new visual design — a dark fantasy / cyber HUD look — to the existing app **without changing any business logic**.

Work in the phases below, **in order**. At the end of every phase:

1. Run the project's quality gates: `npx tsc --noEmit`, `npm run lint`, `npm run format:check`, `npm test`. All must pass with 0 errors and 0 warnings.
2. Write a short report: files created, files changed, anything you were unsure about.
3. **Stop and wait for my approval** before starting the next phase.

---

## 0. NON-NEGOTIABLE RULES (read before anything)

- **Do not change the ranking formula or its behavior.** `Score = NormalizedRating / Playtime`. NormalizedRating = Metacritic (0–100) if present, otherwise RAWG rating (0–5) × 20, otherwise no score (`null`). Playtime 0 → `Infinity`. `src/utils/score.ts`, `recommendation.ts`, `sorting.ts`, `filters.ts` are the source of truth. You may **only add** pure display helpers (e.g. `formatScore`) if they don't exist already.
- **Don't touch** `src/db/**`, `src/services/**`, or store logic, except to read or persist the selected theme through the existing `settings` table / repository pattern (Phase 2).
- Keep TypeScript strict: no `any`, no `as unknown as`, no `@ts-ignore`.
- One component per file, functional components, named exports (follow the existing conventions in `src/components/`).
- Keep `StyleSheet.create`. Do **not** install NativeWind/Tailwind.
- Don't rename routes or move screens. Restyle them in place.
- Every interactive element must keep (or gain) `accessibilityRole`, `accessibilityLabel` where the label isn't visible text, and `accessibilityState` (`selected`, `disabled`).
- Existing tests must keep passing. Update snapshot/text assertions only when the visible copy intentionally changed, and say so in your report.
- Look at the current file before you edit it. If a file or route I mention doesn't exist, **tell me** instead of inventing one.

---

## PHASE 0 — Recon (no code changes)

Read and summarize:

- `src/theme/hudTheme.ts` (current tokens and how screens use them)
- `src/app/_layout.tsx` and every screen in `src/app/`. Tell me whether navigation is a `Stack`, `Tabs`, or both, and which route acts as the "Lists" overview (if any).
- `src/components/GameCard.tsx`, `StatusBadge.tsx`, `EmptyState.tsx`, `FranchiseGroup.tsx`
- `src/utils/score.ts`, `recommendation.ts`, `sorting.ts`. Confirm the function names and return shapes for: computing a score, the recommendation candidate list + `skipCount`, and the franchise-grouped list (group name, members sorted by `franchiseOrder`, group score = best member score, missing-order warning).
- `package.json`: check whether these are already installed: `react-native-svg`, `expo-linear-gradient`, `expo-font`, `@expo-google-fonts/orbitron`, `@expo-google-fonts/rajdhani`, `lucide-react-native`, `@react-native-masked-view/masked-view`, `expo-image`, `expo-blur`.

Deliverable: a mapping table "existing file → what changes in it", plus the list of new files you'll create. No edits yet.

---

## PHASE 1 — Dependencies and fonts

Install only what is missing, always with `npx expo install` so versions match the SDK:

```
npx expo install react-native-svg expo-linear-gradient expo-font @expo-google-fonts/orbitron @expo-google-fonts/rajdhani @react-native-masked-view/masked-view
npm install lucide-react-native
```

(`expo-image` is optional; use it for covers only if it's already installed. Skip `expo-blur`; the design works without blur.)

Fonts — exactly two families:

| Role | Family | Weights to load |
|---|---|---|
| **Display** (titles, labels, numbers, buttons, badges) | Orbitron | `Orbitron_500Medium`, `Orbitron_700Bold`, `Orbitron_900Black` |
| **Body** (game names, metadata, paragraphs, inputs) | Rajdhani | `Rajdhani_400Regular`, `Rajdhani_500Medium`, `Rajdhani_600SemiBold`, `Rajdhani_700Bold` |

- Load them in `src/app/_layout.tsx` with `useFonts`. Keep the splash screen up (`expo-splash-screen` `preventAutoHideAsync` / `hideAsync`) until the fonts are ready.
- With custom fonts on Android, **select the weight through `fontFamily`, never with `fontWeight`**.
- Set `<StatusBar style="light" />` (expo-status-bar) and make the root background the theme's `bgFrom`, so there's no white flash.

---

## PHASE 2 — Theme tokens (`src/theme/hudTheme.ts`)

Replace or extend the current tokens with this structure. There are **3 themes**. **Violet is the default (V1).** Emerald (V2) and Crimson (V3) are alternatives. Only the colors change between themes. Layout, sizes, and shapes stay identical.

```ts
export type HudThemeId = 'violet' | 'emerald' | 'crimson'

export type HudColors = {
  bgFrom: string    // darkest, bottom of the screen
  bgVia: string     // mid gradient
  bgTo: string      // top glow tint
  primary: string   // main accent: selected states, CTA, score of the pick
  secondary: string // secondary accent: borders, icons, grid lines
  surface: string   // translucent panel fill
  text: string      // general foreground
  muted: string     // secondary text / labels
  playing: string
  backlog: string
  completed: string
  dropped: string
}

export const hudThemes: Record<HudThemeId, { name: string; version: string; colors: HudColors }> = {
  violet: {
    name: 'Violet Arcana', version: 'V1 · Default',
    colors: {
      bgFrom: '#07040d', bgVia: '#140826', bgTo: '#2a0d4a',
      primary: '#ff2bd6', secondary: '#22e6ff',
      surface: 'rgba(28,12,52,0.55)', text: '#f4ecff', muted: '#a79bbf',
      playing: '#ffb020', backlog: '#22e6ff', completed: '#6dffb0', dropped: '#8a7f9e',
    },
  },
  emerald: {
    name: 'Emerald Circuit', version: 'V2 · Alternative',
    colors: {
      bgFrom: '#020a07', bgVia: '#05211a', bgTo: '#0a3d2b',
      primary: '#b6ff3b', secondary: '#2df5d0',
      surface: 'rgba(8,40,30,0.55)', text: '#ecfff6', muted: '#93b8a8',
      playing: '#ffc43d', backlog: '#2df5d0', completed: '#8cff7a', dropped: '#6f8c80',
    },
  },
  crimson: {
    name: 'Crimson Forge', version: 'V3 · Alternative',
    colors: {
      bgFrom: '#0b0203', bgVia: '#26050b', bgTo: '#4a0a14',
      primary: '#ff7a1a', secondary: '#ffd23f',
      surface: 'rgba(52,10,18,0.55)', text: '#fff1ea', muted: '#c4a29b',
      playing: '#ffd23f', backlog: '#3fd8ff', completed: '#9dff6a', dropped: '#8f7470',
    },
  },
}
```

Also add to the theme folder:

- **Color helpers** (pure, unit-tested): `withAlpha(hex, alpha) → 'rgba(r,g,b,a)'` and `mix(hexA, hexB, ratioOfA) → hex`. The design needs derived colors like "primary at 25%" or "secondary mixed 25% with black". Don't hardcode those.
- **Fixed tokens**:
  - `white: '#ffffff'` (titles, game names), `black: '#000000'` (text on primary chips/buttons).
  - `panelInk: withAlpha(bgFrom, 0.82)` = card inner fill. `ink85 = 'rgba(0,0,0,0.85)'`, `ink80 = 'rgba(0,0,0,0.8)'`, `ink70 = 'rgba(0,0,0,0.7)'`.
- **Spacing scale** (dp): `0.5:2, 1:4, 1.5:6, 2:8, 2.5:10, 3:12, 4:16, 6:24`.
- **Bevel cut sizes**: `sm: 6`, `md: 10`, `lg: 14`.
- **Typography presets.** `letterSpacing` in RN is in dp, so it's `fontSize × em`. Use these exact values:

| Preset | Font | Size | Letter spacing | Case | Color |
|---|---|---|---|---|---|
| `eyebrow` | Orbitron 500 | 8 | 2.8 | UPPER | secondary @ 80% |
| `screenTitle` (Home) | Orbitron 900 | 24 | 2.9 | UPPER | white + text glow |
| `screenTitleSm` (List) | Orbitron 900 | 20 | 2.0 | UPPER | white + text glow |
| `heroTitle` | Rajdhani 700 | 20 | 0 | normal | white |
| `cardTitle` | Rajdhani 700 | 15 (lineHeight 18) | 0 | normal | white (dimmed: white @ 70%) |
| `meta` | Rajdhani 500 | 11 | 0 | normal | muted |
| `bodySm` | Rajdhani 600 | 14 | 0 | normal | white |
| `button` | Orbitron 700 | 10 | 1.4 | UPPER | — |
| `badge` | Orbitron 700 | 8 | 1.44 | UPPER | status color |
| `chip` | Orbitron 700 | 8 | 0.96 | UPPER | — |
| `sectionLabel` | Orbitron 700 | 8 | 2.4 | UPPER | muted |
| `sagaTitle` | Orbitron 700 | 10 | 2.2 | UPPER | white (prefix "Saga · " in muted) |
| `microLabel` | Orbitron 500 | 7 | 1.4 | UPPER | muted |
| `scoreValue` | Orbitron 700 | 13 | 0 | — | secondary (selected: primary) |
| `statValue` | Orbitron 900 | 16 | 0 | — | status color |
| `rankChip` | Orbitron 900 | 9 | 0 | — | black on primary |
| `navLabel` | Orbitron 700 | 7 | 1.4 | UPPER | muted (active: primary) |

  All numbers (ranks, scores, hours, counts) use `fontVariant: ['tabular-nums']`. Use `maxFontSizeMultiplier={1.3}` on the tiny Orbitron labels so system font scaling can't break the HUD layout.

- **Theme state**: add `useHudTheme()` that returns `{ id, colors, setTheme }`. Back it with a small Zustand slice (or extend an existing store). Persist the id through the **existing settings repository** with key `'hudTheme'`. Default is `'violet'`. Provide colors through a `HudThemeProvider` (React context) mounted in `_layout.tsx`. Build styles with a `makeStyles(colors)` factory memoized per theme, so components don't call `StyleSheet.create` on every render.

Tests: unit-test `withAlpha`, `mix`, and that every theme has every key.

---

## PHASE 3 — HUD primitives (`src/components/hud/`)

Build these low-level pieces first. Every screen is made from them. Web CSS `clip-path`, `color-mix`, and `backdrop-filter` don't exist in RN, so use these translations:

### 3.1 `BevelFrame`
The signature shape: a rectangle with the **top-left and bottom-right corners cut diagonally**.

- Polygon points for size `w × h` and cut `c`: `(c,0) (w,0) (w,h-c) (w-c,h) (0,h) (0,c)`.
- Implementation: a `View` that measures itself with `onLayout`, then renders an absolutely positioned `react-native-svg` `<Svg>` behind its children with one `<Polygon>`: `fill = background`, `stroke = borderColor`, `strokeWidth = 1`. Inset the points by 0.5 so the stroke isn't clipped.
- Props: `cut: 'sm' | 'md' | 'lg'`, `borderColor`, `background` (string **or** `{ gradient: [from, to] }` for horizontal `LinearGradient` fills), `glowColor?`, `style`, `children`. Children are padded so they never overlap the cut corners.
- **Glow** (the design uses colored `drop-shadow` everywhere): when `glowColor` is set, add a second `Polygon` behind it with the same points, `stroke = glowColor`, `strokeWidth = 4`, `strokeOpacity = 0.35`. On iOS, also apply `shadowColor: glowColor, shadowOpacity: 0.7, shadowRadius: 6, shadowOffset: {0,0}`. Don't rely on Android `elevation`; it can't produce colored glows.

### 3.2 `BevelImage`
Cover art with beveled corners. Use `MaskedView` with a `BevelFrame`-shaped SVG mask (cut `sm` for 56×56 thumbnails, `lg` for the hero). Wrap it in a 1px beveled border (`secondary @ 70%`, or `primary` when selected). Props: `uri`, `size` or `style`, `dimmed?` (dimmed means `opacity: 0.6`), `overlayBadge?` (a node pinned bottom-left). Fall back to a dark `bgFrom` fill with a small `Gamepad2` icon in `secondary` when there's no cover.

### 3.3 `HexShape` / `HexIcon`
- Hexagon points (percent of size): `(25%,3%) (75%,3%) (100%,50%) (75%,97%) (25%,97%) (0,50%)`.
- `HexIcon` props: `tone: 'primary' | 'secondary'`, `size: 'sm' (28) | 'md' (36)`, `active?`, `shape?: 'hex' | 'diamond'`, `children` (a lucide icon).
  - Hex: outer polygon filled with the tone color (that's the 1px border), inner polygon inset 1px filled with `bgFrom`. When active, fill with `mix(tone, '#000', 0.30)` for primary or `mix(tone, '#000', 0.25)` for secondary. Icon: tone color, `strokeWidth 1.5`, size 14 (sm) / 16 (md). Always glow in the tone color.
  - Diamond: a square rotated 45°, inset 18%, 1px tone border, fill `rgba(0,0,0,0.4)` (active: tone @ 25%), with the icon (14) centered and not rotated.
  - Mark it decorative (`accessibilityElementsHidden` / `importantForAccessibility="no-hide-descendants"`). The parent button carries the label.

### 3.4 `HexPill`
A capsule with pointed ends. Points: `(7,0) (w-7,0) (w,h/2) (w-7,h) (7,h) (0,h/2)`. Same measure-then-SVG approach. Props: `fill`, `stroke?`, `children`. It's used for status badges, filter chips, and the "Offline ready" tag.

### 3.5 `HudBackground`
A full-screen, absolutely positioned, non-interactive (`pointerEvents="none"`) SVG with 4 layers, bottom to top:
1. Vertical `LinearGradient`: `bgVia` (top) → `bgFrom` (bottom).
2. `RadialGradient` centered at (50%, 0%), radius ≈ 120% width × 60% height: `bgTo` at 0% → transparent at 60%.
3. `RadialGradient` centered at (100%, 100%), radius ≈ 90% × 50%: `withAlpha(primary, 0.14)` → transparent at 70%.
4. **Grid**: SVG `<Pattern>` 22×22 with 1px horizontal and vertical lines in `withAlpha(secondary, 0.09)`. Mask it with a radial mask centered at (50%, 30%), radius 80% × 70%: opaque up to 10%, fading to transparent at 85%.
5. **Scanlines**: `<Pattern>` 3px tall containing a 1px-tall rect of `rgba(255,255,255,0.025)`.

Wrap it in `React.memo`. It must not re-render on scroll.

### 3.6 `HudButton`
Two variants. Height 44 (Home) / 40 (List): add a `size` prop.
- `primary`: `BevelFrame` cut `md`, horizontal gradient `primary → secondary`, label in `button` preset colored **black**, icon black (16, strokeWidth 2.25), glow `primary`.
- `secondary`: `BevelFrame` cut `md`, 1px `secondary` border, fill `withAlpha(bgFrom, 0.9)`, label + icon in `secondary`, glow `secondary`.
- Pressed state: `primary` brightens slightly (overlay `rgba(255,255,255,0.1)`). `secondary` fills `withAlpha(secondary, 0.15)`. Disabled: `opacity: 0.4` + `accessibilityState.disabled`.
- Use `Pressable`. Minimum touch target is 44dp (use `hitSlop` when visually smaller).

### 3.7 Small pieces
- `RankChip`: rect with `primary` background, `rankChip` text, `paddingHorizontal: 4`, value zero-padded (`01`, `02`). Accessible label `"Rank 1"`.
- `SagaOrderChip`: same, but `secondary` background, text `#1`, `#2`, or `?` when the order is missing.
- `GradientRule`: a 1px horizontal line that fades from `withAlpha(secondary, 0.5)` to transparent (`flex: 1`). Used after section labels.
- `DiamondDot`: a 4–6px square rotated 45° (`primary` or `currentColor`).
- `TextGlow` helper style: `textShadowColor: withAlpha(primary, 0.8), textShadowRadius: 8, textShadowOffset: {0,0}`.

Add a dev-only screen or Storybook-style test render that shows every primitive in all 3 themes (it can be a hidden `__dev/hud-kit.tsx` route excluded from production, or just a jest render test). Ask me which one I prefer if unsure.

---

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

## PHASE 5 — App shell (`src/app/_layout.tsx`)

- Mount `HudThemeProvider`. Render `HudBackground` once behind the navigator, and set every navigator's `contentStyle` / `sceneStyle` / `cardStyle` background to `transparent` so the HUD background shows through. Hide the default headers (`headerShown: false`). Screens render their own HUD headers.
- Use `SafeAreaView` / `useSafeAreaInsets` (react-native-safe-area-context). **Don't draw a fake status bar.** The real one is light-styled over the background.
- **Bottom navigation (HUD tab bar).** Four items: **Home**, **Lists**, **Search**, **Settings**, with lucide icons `House`, `LayoutList`, `Search`, `Settings`.
  - Container: `borderTopWidth: 1`, `borderTopColor: withAlpha(secondary, 0.25)`, background `rgba(0,0,0,0.5)`, paddingHorizontal 24, paddingTop 8, paddingBottom `8 + insets.bottom`. On top of the top border, add a 1px line that fades transparent → `primary` → transparent, inset 40dp from each side.
  - Item: a `HexIcon` size `sm` (active: tone `primary`, `active`; inactive: tone `secondary`, opacity 0.6) above a `navLabel` (gap 4).
  - `accessibilityRole="tab"` and `accessibilityState={{ selected }}`.
  - If the app already uses `Tabs`, pass this as a custom `tabBar`. If it uses only a `Stack`, **tell me in your Phase 0 report** and propose the smallest change: either convert the top-level routes to a `(tabs)` group, or render this bar as a persistent component in the root layout. Don't restructure routes without my approval. Map "Lists" to whichever route you identified in Phase 0.

---

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

---

## PHASE 9 — Accessibility, performance, QA

- **Contrast**: confirm muted text on `bgFrom` and all status colors on `rgba(0,0,0,0.85)` meet at least 4.5:1 for text 14 and under. Report any failures with the exact pair, and propose a token tweak rather than silently changing colors.
- **Screen reader pass** (TalkBack): cards read "name, status, score". Ranks read "Rank 1". The formula reads as a sentence. Decorative SVG layers, diamonds, and rules are hidden.
- **Touch targets** ≥ 44dp everywhere (chips can use `hitSlop`).
- **Reduce motion**: no pulsing when enabled.
- **Performance**: `HudBackground`, `BevelFrame`, `HexIcon`, `GameCard` are `React.memo`. Styles come from memoized `makeStyles(colors)`. Lists use `FlatList` with stable keys. `onLayout`-based SVGs only re-measure when the size actually changes. No inline `StyleSheet.create` in render.
- **Theme switching**: switch through all 3 themes on every screen and confirm nothing is hardcoded to violet. Search the codebase for leftover old hex colors from the previous `hudTheme.ts`.
- **Tests to add**: `formatScore`, `withAlpha`/`mix`, the `StatusBadge` label for each status, `GameCard` rendering the rating source (MC / RAWG / No rating) and the `∞` score for playtime 0, `FranchiseGroup` showing the missing-order warning.
- Final report: before/after screenshot list for Home, List, Game detail, Search, Manual game, Onboarding, Settings in **Violet**, plus Home and List in Emerald and Crimson.

---

## Quick visual reference (what the design looks like)

- A deep near-black background with a colored glow at the top (`bgTo`), a faint secondary-colored 22px grid that fades out toward the edges, and very subtle scanlines.
- Panels and cards use **beveled corners** (top-left and bottom-right cut) and **thin 1px neon outlines**, with a soft glow in the accent color. There are no rounded corners anywhere in the HUD.
- **Hexagons** are used for nav/back icons, **hex pills** (pointed capsules) for badges and chips, and **rotated diamonds** as bullets and status dots.
- **Orbitron**, uppercase with wide letter spacing, for every label, number, and button. **Rajdhani** for game names and readable text.
- Hierarchy: `primary` = the one most important thing (current pick, CTA, selected, score result). `secondary` = structure (borders, icons, grid, secondary buttons). Status colors only for game state.
- Primary CTA = beveled bar with a `primary → secondary` gradient and black uppercase text. Secondary CTA = dark beveled bar with a `secondary` outline and text.

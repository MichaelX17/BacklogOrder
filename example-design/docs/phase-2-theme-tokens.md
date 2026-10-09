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

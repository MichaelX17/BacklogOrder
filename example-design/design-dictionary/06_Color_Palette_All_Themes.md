# 5. Color Palette (All Themes)
> Tags: `Theme` `Colors` `Palette`
> **Tags:** `Colors` `Palette` `Theme`

### Violet Arcana (Default)
> Tags: `Theme` `Color-Palette` `Violet`

| Token | Value | Usage |
|---|---|---|
| `--hud-bg-from` | `#07040d` | Bottom of background gradient |
| `--hud-bg-via` | `#140826` | Mid gradient |
| `--hud-bg-to` | `#2a0d4a` | Top gradient (radial highlight origin) |
| `--hud-primary` | `#ff2bd6` | Neon pink — selection, glows, active labels |
| `--hud-secondary` | `#22e6ff` | Cyan — badges, inactive icons, price tags |
| `--hud-surface` | `rgb(28 12 52 / 0.55)` → `rgba(28, 12, 52, 0.55)` | Panel fill |
| `--hud-text` | `#f4ecff` | Primary text |
| `--hud-muted` | `#a79bbf` | Secondary text |
| `--hud-playing` | `#ffb020` | Playing badge color (amber) |
| `--hud-backlog` | `#22e6ff` | Backlog badge color (cyan) |

### Emerald Circuit
> Tags: `Theme` `Color-Palette` `Emerald`

| Token | Value |
|---|---|
| `--hud-bg-from` | `#020a07` |
| `--hud-bg-via` | `#05211a` |
| `--hud-bg-to` | `#0a3d2b` |
| `--hud-primary` | `#b6ff3b` |
| `--hud-secondary` | `#2df5d0` |
| `--hud-surface` | `rgba(8, 40, 30, 0.55)` |
| `--hud-text` | `#ecfff6` |
| `--hud-muted` | `#93b8a8` |
| `--hud-playing` | `#ffc43d` |
| `--hud-backlog` | `#2df5d0` |

### Crimson Forge
> Tags: `Theme` `Color-Palette` `Crimson`

| Token | Value |
|---|---|
| `--hud-bg-from` | `#0b0203` |
| `--hud-bg-via` | `#26050b` |
| `--hud-bg-to` | `#4a0a14` |
| `--hud-primary` | `#ff7a1a` |
| `--hud-secondary` | `#ffd23f` |
| `--hud-surface` | `rgba(52, 10, 18, 0.55)` |
| `--hud-text` | `#fff1ea` |
| `--hud-muted` | `#c4a29b` |
| `--hud-playing` | `#ffd23f` |
| `--hud-backlog` | `#3fd8ff` |

### Derived / Computed Colors

> Tags: `Theme` `Color-Palette` `Utilities` `Color-Mix`

Several elements use **`color-mix(in oklab, <color> <N>%, transparent)`** to create tinted overlays. Translate these to `rgba()` in JS:

| CSS Expression | Approx. RGBA (Violet) | Usage |
|---|---|---|
| `color-mix(in oklab, var(--hud-primary) 14%, transparent)` | `rgba(255, 43, 214, 0.14)` | Top-right corner accent glow |
| `color-mix(in oklab, var(--hud-primary) 70%, transparent)` | `rgba(255, 43, 214, 0.70)` | Primary glow filter |
| `color-mix(in oklab, var(--hud-primary) 80%, transparent)` | `rgba(255, 43, 214, 0.80)` | Primary text glow |
| `color-mix(in oklab, var(--hud-primary) 50%, transparent)` | `rgba(255, 43, 214, 0.50)` | Primary text glow (far) |
| `color-mix(in oklab, var(--hud-secondary) 9%, transparent)` | `rgba(34, 230, 255, 0.09)` | Grid line tint |
| `color-mix(in oklab, var(--hud-secondary) 60%, transparent)` | `rgba(34, 230, 255, 0.60)` | Secondary glow filter |
| `color-mix(in oklab, var(--hud-secondary) 25%, #000)` | `rgba(...)` ~ `rgba(13, 139, 144, 0.25)` | Hex icon inner bg (active secondary) |
| `color-mix(in oklab, var(--hud-primary) 30%, #000)` | ~ `rgba(97, 6, 86, 0.30)` | Hex icon inner bg (active primary) |
| `var(--hud-bg-from) 82%` with transparency → `rgba(var(--hud-bg-from), 0.82)` | `rgba(7, 4, 13, 0.82)` | Game card background (tinted) |

**Recommendation for RN:** Create a `mixColor(base: string, amount: number)` utility that parses a hex color and returns `rgba(r, g, b, amount/100)`.

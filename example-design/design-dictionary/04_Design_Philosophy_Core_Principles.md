# 3. Design Philosophy & Core Principles
> Tags: `Design` `Principles` `Theme` `Visual-Effects`
> **Tags:** `Philosophy` `Principles` `Design`

### Principle 1: Theme-Driven Color Token System
> Tags: `Theme` `Color-Tokens` `Principles`

Everything color-related flows through **theme tokens** named `--hud-*`:

| Token | Purpose |
|---|---|
| `--hud-bg-from` | Darkest background gradient stop |
| `--hud-bg-via` | Mid-tone background gradient stop |
| `--hud-bg-to` | Lightest top gradient stop |
| `--hud-primary` | **Primary accent** — used for active selection, glows, text highlights |
| `--hud-secondary` | **Secondary accent** — used for badges, inactive icons, price tags |
| `--hud-surface` | Semi-transparent panel fill color |
| `--hud-text` | Primary text color |
| `--hud-muted` | Secondary/muted text color |
| `--hud-playing` | Color for "Playing" status badges |
| `--hud-backlog` | Color for "Backlog" status badges |

**Key insight:** To add a 4th theme (e.g., "azure"), you only define these 10 tokens. The layout never changes.

### Principle 2: Bevel Cut Geometry
> Tags: `Bevel` `Principles` `Geometry`

The defining visual texture. Instead of rounded corners alone, panels use **angled cuts** (clip-path polygons) on specific corners:

See [Section 7: Visual Effects](#7-visual-effects--css-utilities) for the `.bevel`, `.bevel-sm`, `.bevel-lg` definitions.

### Principle 3: Glows Over Shadows
> Tags: `Glow` `Principles` `Shadow`

Shadows are **neon-colored glows** (drop-shadow with theme colors), not generic gray shadows. Active/selected items glow with `--hud-primary`; secondary items glow with `--hud-secondary`.

### Principle 4: Hexagonal UI Language
> Tags: `Hex` `Principles` `Shape`

Icons are encased in either:
- **Hexagon** (`clip-path: polygon(25% 3%, 75% 3%, 100% 50%, 75% 97%, 25% 97%, 0 50%)`)
- **Diamond** (`clip-path: polygon(7px 0, calc(100% - 7px) 0, 100% 50%, calc(100% - 7px) 100%, 7px 100%, 0 50%)`)

### Principle 5: Scanlines + Grid Overlay (Atmospheric)
> Tags: `Scanlines` `Grid` `Overlay` `Principles`

Two absolutely-positioned, pointer-events-none layers cover the entire screen:
1. A **grid** (`hud-grid`) — 22px×22px diagonal grid lines tinted `--hud-secondary` at 9%
2. **Scanlines** (`hud-scanlines`) — repeating horizontal 1px white lines at 2.5% opacity

These are decorative atmospheric layers, not interactive.

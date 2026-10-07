---
# YAML FRONTMATTER — machine-readable summary (read this first, ~300 tokens)
name: "Backlog HUD"
style: "dark fantasy cyber"           # dark base + neon accents + hex/bevel geometry
source: "Vercel v0 design (Next.js web, Tailwind CSS, shadcn/ui, lucide-react)"
target: "React Native + Expo Router"
themes:
  - { id: violet,    name: "Violet Arcana",    version: "V1 · Default",  primary: "#ff2bd6", secondary: "#22e6ff", bg: "#07040d" }
  - { id: emerald,   name: "Emerald Circuit",  version: "V2 · Alternative", primary: "#b6ff3b", secondary: "#2df5d0", bg: "#020a07" }
  - { id: crimson,   name: "Crimson Forge",     version: "V3 · Alternative", primary: "#ff7a1a", secondary: "#ffd23f", bg: "#0b0203" }
screens:
  - { id: backlog-list, file: "src/app/hud.tsx", components: [BacklogScreen, GameCard, HexIcon, StatusBadge, PriceTag, BottomNav, StatusBar] }
components:
  - BacklogScreen   # main screen, owns filter + selectedId state
  - GameCard        # list row: cover, title, playtime, metacritic, status badge, price
  - HexIcon         # hexagon / diamond icon wrapper (SVG clip-path)
  - StatusBadge     # Playing / Backlog badge, pulse on Playing
  - PriceTag        # price chip with glow
  - BottomNav       # 4-item tab bar (Home / List / Playing / Profile)
  - StatusBar       # time + signal / wifi / battery
  - PhoneFrame      # web-only preview mockup (skip for RN)
fonts:
  display: { family: "Orbitron", weights: [500, 700, 900] }   # headers, labels, badges
  body:    { family: "Rajdhani", weights: [400, 500, 600, 700] } # UI text
icons: { library: "lucide-react-native", count: 13 }
key_effects: [bevel_clip, hex_clip, neon_glow, scanlines, grid_overlay, backdrop_blur, radial_bg]
state: { filter: "'all' | 'playing' | 'backlog'", selectedId: "string" }
computed: { visibleGames: "filter === 'all' ? games : games.filter(g => g.status === filter)",
             total: "sum of visible game prices, toFixed(2)",
             entryCount: "visibleGames.length.padStart(2,'0')" }
tags: [Overview, Dashboard, Layout, Screen, Config, Tooling, Dependencies, Project,
       Philosophy, Principles, Design, Theme, Colors, Tokens, Palette, Typography, Fonts,
       Effects, Glow, Bevel, Grid, Scanline, Blur, Visual, Data, Model, Game,
       Screen, StatusBar, Header, Filter, Buttons, Nav, List,
       Components, Card, Badge, HexIcon, Button, PriceTag, Icons, Icon,
       RN, Expo, Translation, React-Native, Assets, Images, Accessibility, A11y,
       State, Interaction, Animation, Checklist, Implementation]
---

# Design Analysis & Implementation Guide: "Backlog HUD" — Dark Fantasy Cyber Mobile UI

> **Purpose:** This document is a complete, machine-readable design specification for the "Backlog HUD" UI concept found in the `example-design/` directory. It was generated from a Vercel v0 Design and is intended to be read by another AI agent to reproduce this design in a **React Native / Expo** project.
>
> **Source project type:** Next.js web app (Tailwind CSS, shadcn/ui, lucide-react, CSS clip-path, CSS `drop-shadow` filters)
>
> **Target project type:** React Native + Expo Router (StyleSheet, Pressable, RN `Image`, no native CSS filters or clip-path)

---

## AI Quick Reference (read this before any section)

- **One screen, three themes.** Everything is in `BacklogScreen`; swapping the theme object re-skins the whole UI. Layout/spacing/typography never change.
- **Two fonts only:** Orbitron (display/headers) + Rajdhani (body). Load via `expo-font`.
- **Geometry language:** bevel cuts (diagonal corner slice) + hexagon/diamond clips + neon glows (not gray shadows). All three need `react-native-svg` `ClipPath`/`Polygon` in RN.
- **Atmospheric layers:** diagonal grid (22px, secondary @9%) + horizontal scanlines (white @2.5%) — decorative, `pointer-events: none`.
- **Color tokens** (`--hud-bg-from/via/to`, `primary`, `secondary`, `surface`, `text`, `muted`, `playing`, `backlog`) — see [Quick Find by Tag](#quick-find-by-tag) → `Colors` / `Theme`.
- **Data:** `Game { id, title, cover, playtimeHours, metacritic, status: 'playing'|'backlog', price }`. 4 sample games in Section 8.
- **State:** `filter` ('all'|'playing'|'backlog', default 'all') + `selectedId` (default `games[0].id`). Filter changes `visibleGames`, `total`, and `entryCount`.
- **RN gotchas:** no CSS `drop-shadow`/`clip-path`/`backdrop-blur`/`color-mix` → use `shadowColor` props, SVG `ClipPath`, `expo-blur` `BlurView`, and the `mixColor`/`rgba` utilities in Section 12.5.
- **Search this file:** use the [Quick Find by Tag](#quick-find-by-tag) index or grep `## N.` for sections.

---

## Table of Contents

> ⚡ **AI Quick Reference** — [Jump to Quick Find by Tag](#quick-find-by-tag)
>
> 1. [High-Level Overview](#1-high-level-overview)
> 2. [Project & Tooling Configuration](#2-project--tooling-configuration)
> 3. [Design Philosophy & Core Principles](#3-design-philosophy--core-principles)
> 4. [Theme System](#4-theme-system)
> 5. [Color Palette (All Themes)](#5-color-palette-all-themes)
> 6. [Typography](#6-typography)
> 7. [Visual Effects & CSS Utilities](#7-visual-effects--css-utilities)
> 8. [Data Model](#8-data-model)
> 9. [Screen Breakdown](#9-screen-breakdown)
> 10. [Component Breakdown](#10-component-breakdown)
> 11. [Icon Usage](#11-icon-usage)
> 12. [React Native / Expo Translation Guide](#12-react-native--expo-translation-guide)
> 13. [Asset Requirements](#13-asset-requirements)
> 14. [Accessibility Specification](#14-accessibility-specification)
> 15. [State & Interactions Summary](#15-state--interactions-summary)
> 16. [Implementation Checklist](#16-implementation-checklist)
>
> **Quick Find by Tag** — See the [`Tag Index`](#quick-find-by-tag) section below for a
> searchable list of all tags and their associated sections.

---

## Quick Find by Tag (`Tag Index`)
> Jump to a section using the tag keyword above, or grep `> Tags:` in this file
> to find every location associated with a tag. Each major section (## N.) and
> subsection (### N.N) carries an inline `> Tags:` line for fast AI lookup.

| Tag | Description | Key Sections |
|---|---|---|
| `Theme` | Theme system, color tokens, palette | 4, 5, 7.1, 12.3 |
| `Colors` / `Palette` | All theme color values | 5, 5.1–5.3 |
| `Typography` / `Fonts` | Font stack, sizes, tracking, RN loading | 6, 12.4, 13.1 |
| `Visual-Effects` | CSS utilities overview | 7 |
| `Bevel` | Diagonal corner cuts | 7.4, 12.6 |
| `Glow` | Neon drop-shadow effects | 7.7, 7.8 |
| `Clip-Path` | Hex, diamond, hex-pill shapes | 7.4–7.6, 10.5, 10.6, 10.7, 10.8 |
| `Grid` | Diagonal grid overlay | 7.2 |
| `Scanlines` | Horizontal scanline effect | 7.3 |
| `Background` | Screen background gradient | 7.1 |
| `Buttons` | Action buttons | 9.3, 7.6 |
| `FilterBar` | Filter pills, sort button | 9.4, 10.4 |
| `GameCard` | Game list row component | 9.6, 10.5 |
| `StatusBadge` | Playing/Backlog badge | 10.7 |
| `HexIcon` | Hexagon/diamond icon wrapper | 10.8 |
| `PriceTag` | Price tag sub-component | 10.6, 10.5 |
| `BottomNav` | Bottom navigation bar | 9.7, 10.3 |
| `Header` | Top header with title + total | 9.2 |
| `StatusBar` | Time, signal, battery icons | 9.1, 10.2 |
| `GameList` | Scrollable game list (FlatList) | 9.6 |
| `Scrollbars` | Hidden scrollbar | 7.9 |
| `SafeArea` | Notch / home bar insets | 12.8 |
| `Icons` | All icon usage + RN translation | 11, 11.1, 11.2 |
| `Data` / `Model` | Data models, types, sample data | 8, 8.1–8.3 |
| `Configuration` | Project setup, deps, tsconfig | 2, 2.1–2.4 |
| `State` | Client-side state variables | 15, 15.1 |
| `Interaction` | Tap/click interactions | 15, 15.2 |
| `Animation` | Pulse, glow, brightness, scale | 7.8, 15, 15.4 |
| `RN` / `Expo` | React Native conversion guide | 2.2, 4.3, 6.3, 12, 12.1–12.8 |
| `Assets` | Font + image asset requirements | 13, 13.1–13.3 |
| `Accessibility` / `A11y` | ARIA, focus, screen readers | 14, 14.1–14.3 |
| `Checklist` / `Implementation` | Prioritized task list | 16, 16.1–16.4 |
| `Utilities` | cn, mixColor, color helpers | 10.10, 12.5 |

---

## Quick Find by Tag

Use these tags to jump to relevant sections. Each section header is tagged inline above.

| Tag | Sections |
|---|---|
| `A11y` | [14](#14) |
| `Accessibility` | [14](#14) |
| `Animation` | [15](#15) |
| `Assets` | [13](#13) |
| `Badge` | [10](#10) |
| `Bevel` | [7](#7) |
| `Blur` | [7](#7) |
| `Button` | [10](#10) |
| `Buttons` | [9](#9) |
| `Card` | [10](#10) |
| `Checklist` | [16](#16) |
| `Colors` | [4](#4), [5](#5) |
| `Components` | [10](#10) |
| `Config` | [2](#2) |
| `Dashboard` | [1](#1), [9](#9) |
| `Data` | [8](#8) |
| `Dependencies` | [2](#2) |
| `Design` | [3](#3) |
| `Effects` | [7](#7) |
| `Expo` | [12](#12) |
| `Filter` | [9](#9) |
| `Fonts` | [6](#6), [13](#13) |
| `Game` | [8](#8) |
| `Glow` | [7](#7) |
| `Grid` | [7](#7) |
| `Header` | [9](#9) |
| `HexIcon` | [10](#10) |
| `Icon` | [11](#11) |
| `Icons` | [11](#11) |
| `Images` | [13](#13) |
| `Implementation` | [16](#16) |
| `Interaction` | [15](#15) |
| `Layout` | [1](#1), [9](#9) |
| `List` | [9](#9) |
| `Model` | [8](#8) |
| `Nav` | [9](#9) |
| `Overview` | [1](#1) |
| `Palette` | [5](#5) |
| `Philosophy` | [3](#3) |
| `PriceTag` | [10](#10) |
| `Principles` | [3](#3) |
| `Project` | [2](#2) |
| `RN` | [12](#12) |
| `React-Native` | [12](#12) |
| `Scanline` | [7](#7) |
| `Screen` | [1](#1), [9](#9) |
| `State` | [15](#15) |
| `StatusBar` | [9](#9) |
| `Theme` | [4](#4), [5](#5) |
| `Tokens` | [4](#4) |
| `Tooling` | [2](#2) |
| `Translation` | [12](#12) |
| `Typography` | [6](#6) |
| `Visual` | [7](#7) |

## 1. High-Level Overview
> Tags: `Theme` `Overview` `UI` `Screens`
> **Tags:** `Overview` `Dashboard` `Layout` `Screen`

### What This Design Is
> Tags: `Overview` `Design`

A **dark fantasy cyber mobile UI** for tracking a video game backlog. The visual language combines:
- **Dark, desaturated base** (#07060a / #07040d)
- **Neon accent glows** (violet/magenta, emerald/teal, or crimson/orange per theme)
- **Glitch/cyber-grid** overlays and **scanlines**
- **Angled bevel cuts** on cards and panels
- **Hexagonal** and **diamond-shaped** UI elements
- **Military/digital typography** (Orbitron headers, Rajdhani body)
- **Phone frame mockup** for design preview (web-only)

The single most important architectural concept: **one layout, one set of components — three theme variants** achieved by swapping a single CSS class (`theme-violet`, `theme-emerald`, `theme-crimson`) that redefines a handful of CSS custom properties (`--hud-*`). The layout, spacing, typography, and component structure never change.

### Screens / Sections
> Tags: `Screens` `Overview` `Layout`

The design has **one screen** — the **Backlog List screen** — rendered inside a **PhoneFrame** for preview:

| Area | Purpose |
|---|---|
| Status Bar (top) | Simulates iOS/Android status bar: time (21:47), cellular signal, WiFi, battery |
| Header | Back button (hex icon), screen title "LIST", archive label, total price |
| Primary Actions | "Search RAWG" (primary, gradient glow), "Add manually" (secondary, bordered) |
| Filter Bar | Label with hex-diamond icon, 3 filter pills (All / Playing / Backlog), sort button |
| Entry Count | "NN entries" label with divider line and diamond accent |
| Game List | Scrollable list of GameCard components |
| Bottom Navigation | 4-item nav bar (Home/List/Playing/Profile) with hex icons, active = "List" |

---

## 2. Project & Tooling Configuration
> Tags: `Configuration` `Tooling` `Dependencies` `RN`
> **Tags:** `Config` `Tooling` `Dependencies` `Project`

### Dependencies (from `package.json`)
> Tags: `Dependencies` `Package-Json` `Configuration`

| Category | Packages | Purpose |
|---|---|---|
| Framework | `next@16.4.0`, `react@19`, `react-dom@19` | Web framework (not needed for RN target) |
| Styling | `tailwindcss@4.3.3`, `@tailwindcss/postcss@4.3.3`, `clsx@2.1.1`, `tailwind-merge@3.3.1`, `class-variance-authority@0.7.1` | CSS utility framework |
| Animation | `tw-animate-css@1.4.0` | CSS keyframe animations (for `animate-pulse` etc.) |
| shadcn/ui | `shadcn@^4.11.0` | Component library base |
| Icons | `lucide-react@1.16.0` | Icon set — **CRITICAL**: replace with `lucide-react-native` for RN |
| Analytics | `@vercel/analytics@1.6.1` | Web analytics (optional, web-only) |
| Base UI | `@base-ui/react@1.5.0` | Headless UI primitives (Button component) |

### React Native / Expo Equivalents
> Tags: `RN` `Expo` `Translation` `Equivalents`

| Web (example-design) | React Native / Expo |
|---|---|
| `next/image` (`Image` with `fill`) | `react-native` `Image` (uri-based, no layout fill) |
| `lucide-react` | `lucide-react-native` |
| `@base-ui/react` (Button primitive) | `react-native` `Pressable` |
| Tailwind CSS | `tailwind-react-native-classnames` (optional) **or** StyleSheet (recommended) |
| CSS `drop-shadow` filter | `react-native-svg` filter (limited) — **use a shadow approach** or skip |
| CSS `clip-path` polygon | `react-native-svg` `ClipPath` with `Polygon` |
| CSS `backdrop-blur` | `expo-blur` (`BlurView`) |
| CSS `color-mix()` | Compute in JS: `mixColor(primary, amount)` utility |
| CSS `@theme inline` custom properties | JS theme object passed via Context / props |
| CSS `@layer components` | N/A — apply styles via StyleSheet / inline styles |

### TypeScript Configuration
> Tags: `TypeScript` `Configuration` `tsconfig`

- `compilerOptions.jsx: "react-jsx"`
- `compilerOptions.paths: { "@/*": ["./*"] }` (Next.js) — in RN/Expo, use `"@/*": ["./src/*"]`
- `strict: true`
- For RN target, extend `expo/tsconfig.base`

### Build / Dev Commands (web)
> Tags: `Commands` `Build` `Dev` `Configuration`

```json
"dev": "next dev", "build": "next build", "start": "next start"
```

For RN target, use `expo start`.

---

## 3. Design Philosophy & Core Principles
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

---

## 4. Theme System
> Tags: `Theme` `RN` `Configuration` `Colors`
> **Tags:** `Theme` `Colors` `Tokens`

### How Themes Work (Web)
> Tags: `Theme` `CSS` `Configuration`

```css
/* globals.css */
@theme inline {
  --color-hud-bg: var(--hud-bg-from);
  --color-hud-primary: var(--hud-primary);
  --color-hud-secondary: var(--hud-secondary);
  --color-hud-surface: var(--hud-surface);
  --color-hud-text: var(--hud-text);
  --color-hud-muted: var(--hud-muted);
  --color-hud-playing: var(--hud-playing);
  --color-hud-backlog: var(--hud-backlog);
}

/* Theme variant — swap this class, everything reskins */
.theme-violet {
  --hud-bg-from: #07040d;
  --hud-bg-via: #140826;
  --hud-bg-to: #2a0d4a;
  --hud-primary: #ff2bd6;    /* neon pink */
  --hud-secondary: #22e6ff;   /* cyan */
  --hud-surface: rgb(28 12 52 / 0.55);
  --hud-text: #f4ecff;        /* off-white, slightly violet-tinted */
  --hud-muted: #a79bbf;
  --hud-playing: #ffb020;     /* amber */
  --hud-backlog: #22e6ff;     /* cyan */
}

.theme-emerald { /* ... */ }
.theme-crimson { /* ... */ }
```

The `BacklogScreen` component receives a `themeClassName` prop (e.g. `"theme-violet"`) and applies it to its root div:

```tsx
<div className={cn(themeClassName, 'hud-screen relative flex h-full flex-col ...')}>
```

### Theme Data (from `lib/games.ts`)
> Tags: `Theme` `Data` `Games`

```ts
export const hudThemes: HudTheme[] = [
  {
    id: 'violet',
    version: 'V1 · Default',
    name: 'Violet Arcana',
    className: 'theme-violet',
    swatches: ['#2a0d4a', '#ff2bd6', '#22e6ff'],
  },
  {
    id: 'emerald',
    version: 'V2 · Alternative',
    name: 'Emerald Circuit',
    className: 'theme-emerald',
    swatches: ['#0a3d2b', '#b6ff3b', '#2df5d0'],
  },
  {
    id: 'crimson',
    version: 'V3 · Alternative',
    name: 'Crimson Forge',
    className: 'theme-crimson',
    swatches: ['#4a0a14', '#ff7a1a', '#ffd23f'],
  },
]
```

### React Native / Expo Translation
> Tags: `RN` `Expo` `Translation`

**Create a theme object in TypeScript:**

```ts
// theme.ts (RN target)
export type HudTheme = {
  id: 'violet' | 'emerald' | 'crimson';
  version: string;
  name: string;
  swatches: string[];
  colors: {
    bgFrom: string;
    bgVia: string;
    bgTo: string;
    primary: string;
    secondary: string;
    surface: string;
    text: string;
    muted: string;
    playing: string;
    backlog: string;
  };
};

export const hudThemes: HudTheme[] = [
  {
    id: 'violet',
    version: 'V1 · Default',
    name: 'Violet Arcana',
    swatches: ['#2a0d4a', '#ff2bd6', '#22e6ff'],
    colors: {
      bgFrom: '#07040d',
      bgVia: '#140826',
      bgTo: '#2a0d4a',
      primary: '#ff2bd6',
      secondary: '#22e6ff',
      surface: 'rgba(28, 12, 52, 0.55)',
      text: '#f4ecff',
      muted: '#a79bbf',
      playing: '#ffb020',
      backlog: '#22e6ff',
    },
  },
  // ... emerald, crimson with same structure
];

export const defaultTheme = hudThemes[0]; // violet
```

Pass the theme object down via props or a ThemeContext. Use `theme.colors.primary` etc. everywhere in place of CSS variables.

---

## 5. Color Palette (All Themes)
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

---

## 6. Typography
> Tags: `Typography` `Fonts` `RN`
> **Tags:** `Typography` `Fonts`

### Font Stack
> Tags: `Typography` `Fonts` `Font-Stack`

| Role | Font Family | Font Weights Used | CSS Variable |
|---|---|---|---|
| Display (headers, labels, captions) | `Orbitron` | 500, 700, 900 | `--font-orbitron` → `font-display` |
| Body (UI text, button text) | `Rajdhani` | 400, 500, 600, 700 | `--font-rajdhani` → `font-sans` |

Global CSS:
```css
--font-sans: var(--font-rajdhani), ui-sans-serif, system-ui, sans-serif;
--font-display: var(--font-orbitron), ui-sans-serif, system-ui, sans-serif;
```

### Font Application in the Design
> Tags: `Typography` `Fonts` `Application`

- `font-display` class → Orbitron. Applied to: screen title, section labels, badges, nav labels, filter pills, button text, card titles, status badges.
- `font-sans` class → Rajdhani. Applied to body container (`.font-sans` on root).

### Typography Sizes & Styles
> Tags: `Typography` `Font-Sizes` `Styles` `Spec`

| Element | Size | Weight | Tracking | Color | Transform |
|---|---|---|---|---|---|
| Page header subtitle | `text-[10px]` | medium (500) | `tracking-[0.4em]` | `text-neutral-500` | uppercase |
| Page title ("DARK FANTASY CYBER") | `text-3xl` (md: `text-4xl`) | black (900) | `tracking-[0.08em]` | `text-white` | uppercase |
| Page description | `text-base` | — | — | `text-neutral-400` | — |
| Theme version label | `text-[9px]` | — | `tracking-[0.3em]` | `text-neutral-500` | uppercase |
| Theme name | `text-sm` | bold (700) | `tracking-[0.12em]` | — | uppercase |
| Screen title ("LIST") | `text-2xl` | black (900) | `tracking-[0.12em]` | `text-white` | uppercase |
| Archive label | `text-[8px]` | medium (500) | `tracking-[0.35em]` | `text-hud-secondary/80` | — |
| Total label ("Total") | `text-[8px]` | — | `tracking-[0.25em]` | `text-hud-muted` | — |
| Total value (`$XX.XX`) | `text-sm` | bold (700) | — | `text-hud-primary` | — |
| Search button | `text-[10px]` | bold (700) | `tracking-[0.14em]` | `text-black` | uppercase |
| "Add manually" button | `text-[10px]` | bold (700) | `tracking-[0.14em]` | `text-hud-secondary` | uppercase |
| Filter label | `text-[9px]` | bold (700) | `tracking-[0.2em]` | `text-hud-muted` | uppercase |
| Filter pills | `text-[8px]` | bold (700) | `tracking-[0.14em]` | active: `text-black`, inactive: `text-hud-muted` | uppercase |
| Entry count | `text-[8px]` | — | `tracking-[0.3em]` | `text-hud-muted` | uppercase |
| Game title | `text-[15px]` | bold (700) | — | `text-white` | — |
| Game metadata (playtime, metacritic) | `text-[11px]` | medium (500) | — | `text-hud-muted` | — |
| Price label | `text-[9px]` | medium (500) | `tracking-[0.2em]` | `text-hud-muted` | uppercase |
| Price value | `text-[13px]` | bold (700) | — | active: `text-hud-primary`, inactive: `text-hud-secondary` | — |
| Price `$` sub-label | `text-[9px]` | — | — | `opacity-70` | — |
| Status badge | `text-[8px]` | bold (700) | `tracking-[0.18em]` | `text-hud-playing` or `text-hud-backlog` | uppercase |
| Bottom nav labels | `text-[7px]` | bold (700) | `tracking-[0.2em]` | active: `text-hud-primary`, inactive: `text-hud-muted` | uppercase |
| Status bar time | `text-[11px]` | bold (700) | `tracking-wider` | `text-hud-text` | — |
| Theme swatch color title (figcaption) | `text-xs` | — | — | — | — |

**Key patterns:**
- All labels, captions, and tags are **uppercase** with heavy letter-tracked font-display (Orbitron)
- Font sizes use **pixel values** like `text-[8px]`, `text-[9px]`, `text-[10px]`, `text-[11px]`, `text-[13px]`, `text-[15px]` — not standard Tailwind steps
- `tabular-nums` is applied to price and total values for consistent digit width
- `text-balance` and `text-pretty` used for titles/descriptions on the web page wrapper (not inside HUD)

### React Native / Expo Translation
> Tags: `RN` `Expo` `Translation`

1. **Fonts:** Load Orbitron and Rajdhani via `expo-font`:
   ```ts
   await Font.loadAsync({
     orbitron: require('@/assets/fonts/Orbitron-Medium.ttf'), // plus Bold, Black
     rajdhani: require('@/assets/fonts/Rajdhani-Regular.ttf'), // plus Medium, SemiBold, Bold
   });
   ```
   Download from Google Fonts.

2. **Font family mapping in RN:**
   - `font-display` → `fontFamily: 'Orbitron'`
   - `font-sans` (default) → `fontFamily: 'Rajdhani'`

3. **Text component mapping table:**

| Tailwind | RN StyleSheet |
|---|---|
| `text-[8px] font-medium tracking-[0.3em] uppercase text-hud-muted` | `{ fontSize: 8, fontWeight: '500', letterSpacing: 2.4, textTransform: 'uppercase', color: theme.colors.muted }` |
| `text-[10px] font-bold tracking-[0.14em] uppercase text-black` | `{ fontSize: 10, fontWeight: '700', letterSpacing: 1.4, textTransform: 'uppercase', color: '#000' }` |
| `text-[8px] uppercase tracking-[0.35em] text-hud-secondary/80` | `{ fontSize: 8, letterSpacing: 2.8, textTransform: 'uppercase', color: colorWithOpacity(theme.colors.secondary, 0.8) }` |
| `text-2xl font-black uppercase tracking-[0.12em] text-white` | `{ fontSize: 24, fontWeight: '900', letterSpacing: 2.4, textTransform: 'uppercase', color: '#fff' }` |

**Letter-spacing conversion:** Tailwind `tracking-[0.4em]` = `0.4 × fontSize` in pixels.
- `text-[8px]` + `tracking-[0.4em]` → `letterSpacing: 8 * 0.4 = 3.2`
- `text-[9px]` + `tracking-[0.18em]` → `letterSpacing: 9 * 0.18 = 1.62`
- `text-[10px]` + `tracking-[0.14em]` → `letterSpacing: 10 * 0.14 = 1.4`
- `text-sm` (≈14px) + `tracking-[0.12em]` → `letterSpacing: 14 * 0.12 = 1.68`
- `text-[7px]` + `tracking-[0.2em]` → `letterSpacing: 7 * 0.2 = 1.4`
- `text-[8px]` + `tracking-[0.35em]` → `letterSpacing: 8 * 0.35 = 2.8`

**Tabular numerals:** React Native `Text` doesn't have a direct equivalent. Use a mono-spaced numeral font variant or set `fontVariant: ['tabular-nums']` on the style object:
```ts
{ fontVariant: ['tabular-nums'] }
```

---

## 7. Visual Effects & CSS Utilities
> Tags: `Visual-Effects` `CSS` `RN` `Bevel` `Glow` `Clip-Path` `Grid` `Scanlines` `Background`
> **Tags:** `Effects` `Glow` `Bevel` `Grid` `Scanline` `Blur` `Visual`

These are defined in `@layer components` in `globals.css`. Each must be replicated in RN.

### 7.1 `.hud-screen` — Screen Background
> Tags: `Background` `Gradient` `Theme`

**CSS:**
```css
.hud-screen {
  background:
    radial-gradient(120% 60% at 50% 0%, var(--hud-bg-to) 0%, transparent 60%),
    radial-gradient(90% 50% at 100% 100%, color-mix(in oklab, var(--hud-primary) 14%, transparent) 0%, transparent 70%),
    linear-gradient(180deg, var(--hud-bg-via) 0%, var(--hud-bg-from) 100%);
  color: var(--hud-text);
}
```

**3-layer gradient:**
1. **Top radial glow:** 120%×60% radial gradient at top-center, fading from `--hud-bg-to` to transparent
2. **Corner accent:** 90%×50% radial gradient at bottom-right, tinted `--hud-primary` at 14% opacity
3. **Base linear:** vertical linear gradient from `--hud-bg-via` (top) to `--hud-bg-from` (bottom)

**RN translation:** Use a custom SVG component or a `LinearGradient` from `expo-linear-gradient` combined with a `radial-gradient` via `react-native-svg`. Since RN doesn't natively support radial gradients, use `react-native-svg`:

```tsx
import Svg, { RadialGradient, LinearGradient, Stop, Rect } from 'react-native-svg';

function HudBackground({ theme }: { theme: HudTheme }) {
  return (
    <Svg style={StyleSheet.absoluteFill} width="100%" height="100%">
      {/* Top radial gradient */}
      <RadialGradient id="top-radial" cx="50%" cy="0%" r="60%" fx="50%" fy="0%">
        <Stop offset="0%" stopColor={theme.colors.bgTo} />
        <Stop offset="100%" stopColor="transparent" />
      </RadialGradient>
      {/* Corner accent */}
      <RadialGradient id="corner-accent" cx="100%" cy="100%" r="50%">
        <Stop offset="0%" stopColor={theme.colors.primary} stopOpacity={0.14} />
        <Stop offset="100%" stopColor="transparent" />
      </RadialGradient>
      {/* Base linear gradient */}
      <LinearGradient id="base-linear" x1="0%" y1="0%" x2="0%" y2="100%">
        <Stop offset="0%" stopColor={theme.colors.bgVia} />
        <Stop offset="100%" stopColor={theme.colors.bgFrom} />
      </LinearGradient>
      <Rect x="0" y="0" width="100%" height="100%" fill="url(#base-linear)" />
      <Rect x="0" y="0" width="100%" height="100%" fill="url(#corner-accent)" />
      <Rect x="0" y="0" width="100%" height="100%" fill="url(#top-radial)" />
    </Svg>
  );
}
```

**Simpler approach (recommended):** Since the gradient is subtle, approximate with `expo-linear-gradient` for the linear base and a solid `backgroundColor` for `--hud-bg-from`. The corner accent can be a semi-transparent overlay `View`:

```tsx
<View style={{ flex: 1, backgroundColor: theme.colors.bgFrom }}>
  <LinearGradient
    colors={[theme.colors.bgVia, theme.colors.bgFrom]}
    style={StyleSheet.absoluteFill}
  />
  {/* Corner accent overlay */}
  <View style={[StyleSheet.absoluteFill, {
    backgroundColor: theme.colors.primary,
    opacity: 0.07, // approximates 14% with blending
    borderBottomLeftRadius: 0,
    borderTopRightRadius: 0,
  }]} />
</View>
```

### 7.2 `.hud-grid` — Grid Overlay
> Tags: `Grid` `Overlay` `Theme`

**CSS:**
```css
.hud-grid {
  background-image:
    linear-gradient(color-mix(in oklab, var(--hud-secondary) 9%, transparent) 1px, transparent 1px),
    linear-gradient(90deg, color-mix(in oklab, var(--hud-secondary) 9%, transparent) 1px, transparent 1px);
  background-size: 22px 22px;
  mask-image: radial-gradient(80% 70% at 50% 30%, black 10%, transparent 85%);
}
```

A diagonal grid (22px cells) tinted `--hud-secondary` at 9% opacity, with a **radial mask** that fades the bottom portion.

**RN translation:** Use `react-native-svg`:
```tsx
import Svg, { Pattern, Rect, Line, RadialGradient, Stop, Defs } from 'react-native-svg';

<Svg style={StyleSheet.absoluteFill} width="100%" height="100%">
  <Defs>
    <Pattern id="grid" width={22} height={22} patternUnits="userSpaceOnUse">
      <Rect width={22} height={22} fill="transparent" />
      <Line x1={0} y1={22} x2={22} y2={0} strokeWidth={1} stroke={tintColor(theme.colors.secondary, 0.09)} />
      <Line x1={0} y1={0} x2={0} y2={22} strokeWidth={1} stroke={tintColor(theme.colors.secondary, 0.09)} />
    </Pattern>
  </Defs>
  <Rect x="0" y="0" width="100%" height="100%" fill="url(#grid)" />
  {/* Radial mask overlay */}
  <Rect x="0" y="0" width="100%" height="100%" fill={theme.colors.bgFrom} opacity={0.9} />
</Svg>
```

**Simpler approach:** Render as a dotted or lined overlay using `react-native-svg` `Path`, or skip the grid for RN and rely on a static PNG texture tiled via `Image` with `resizeMode="repeat"`.

### 7.3 `.hud-scanlines` — Scanline Effect
> Tags: `Scanlines` `Overlay` `Background`

**CSS:**
```css
.hud-scanlines {
  background-image: repeating-linear-gradient(
    0deg,
    rgb(255 255 255 / 0.025) 0px,
    rgb(255 255 255 / 0.025) 1px,
    transparent 1px,
    transparent 3px
  );
}
```

Horizontal scanlines: white at 2.5% opacity, 1px tall, spaced every 3px.

**RN translation:** Use a `repeating-linearGradient` via `react-native-svg`:
```tsx
import Svg, { Defs, LinearGradient, Stop, Rect } from 'react-native-svg';

<Svg style={StyleSheet.absoluteFill} width="100%" height="100%">
  <Defs>
    <LinearGradient id="scanlines" x1="0%" y1="0%" x2="0%" y2="100%" gradientTransform="scale(1, 3)">
      <Stop offset="0%" stopColor="white" stopOpacity={0.025} />
      <Stop offset="33%" stopColor="white" stopOpacity={0.025} />
      <Stop offset="33%" stopColor="transparent" />
      <Stop offset="67%" stopColor="transparent" />
    </LinearGradient>
  </Defs>
  <Rect x="0" y="0" width="100%" height="100%" fill="url(#scanlines)" />
</Svg>
```

### 7.4 `.bevel` — Bevel Cut
> Tags: `Bevel` `Clip-Path` `Corner-Cut`

**CSS:**
```css
.bevel {
  --cut: 10px;
  clip-path: polygon(
    var(--cut) 0,
    100% 0,
    100% calc(100% - var(--cut)),
    calc(100% - var(--cut)) 100%,
    0 100%,
    0 var(--cut)
  );
}
.bevel-sm { --cut: 6px; }
.bevel-lg { --cut: 14px; }
```

This creates a **diagonal cut** on the **top-left** and **bottom-right** corners of an element. The cut depth is 10px (default), 6px (sm), or 14px (lg).

**Visual:** Think of slicing off a corner of a rectangle with a diagonal line. Top-left corner starts at `(10px, 0)` instead of `(0, 0)`, and bottom-right ends at `(100% - 10px, 100%)` instead of `(100%, 100%)`.

**RN translation:** Use `react-native-svg` `ClipPath` + `Polygon`:

```tsx
import Svg, { ClipPath, Polygon, Rect, G } from 'react-native-svg';

function BeveledView({ cut = 10, children, style, ...props }) {
  // The polygon points form a hexagon-ish shape with two corners cut
  const points = [
    `${cut},0`,
    `100%,0`,
    `100%,calc(100% - ${cut})`, // SVG doesn't support calc, must compute in JS
    `calc(100% - ${cut}),100%`,
    `0,100%`,
    `0,${cut}`,
  ];
  // In practice, compute in pixels based on known dimensions
}
```

**Important note:** SVG clip-path coordinates are in the SVG's own coordinate system, not the element's pixel size. You need to know the element's width/height to create the polygon points. Use `react-native-svg`'s `ClipPath` with `Polygon` where coordinates are in SVG user space.

**Helper approach:** Create a reusable `BeveledContainer` component:

```tsx
interface BeveledProps {
  cut?: number;      // corner cut size in px (default 10)
  children?: React.ReactNode;
  style?: StyleProp<ViewStyle>;
}

function BeveledContainer({ cut = 10, children, style }: BeveledProps) {
  const { width, height } = /* measured or known dimensions */;
  const points = [
    `${cut},0`,
    `${width},0`,
    `${width},${height - cut}`,
    `${width - cut},${height}`,
    `0,${height}`,
    `0,${cut}`,
  ].join(' ');

  return (
    <Svg width={width} height={height} style={style}>
      <ClipPath id={`clip-${cut}-${width}-${height}`}>
        <Polygon points={points} />
      </ClipPath>
      <G clipPath="url(#...)">
        {children}
      </G>
    </Svg>
  );
}
```

**Alternative (recommended for RN):** Since `react-native-svg` clip-path with dynamic dimensions is complex, consider using `react-native-skia` if available, or approximate the bevel effect with a **border trick**: render a parent with `overflow: hidden` and an absolutely positioned overlay that covers the cut corners with the background color. Or use a **custom SVG mask image** asset.

For simpler implementation, you can use `react-native`'s `overflow: 'hidden'` + rounded corners and skip the precise bevel cut. The bevel is primarily decorative.

### 7.5 `.hex` — Hexagon Clip
> Tags: `Hex` `Clip-Path` `Shape`

**CSS:**
```css
.hex {
  clip-path: polygon(25% 3%, 75% 3%, 100% 50%, 75% 97%, 25% 97%, 0 50%);
}
```

A regular hexagon shape — used for the outer border of hex icons.

### 7.6 `.hex-pill` — Pill with Hex Ends
> Tags: `Hex-Pill` `Clip-Path` `Shape` `Buttons`

**CSS:**
```css
.hex-pill {
  clip-path: polygon(7px 0, calc(100% - 7px) 0, 100% 50%, calc(100% - 7px) 100%, 7px 100%, 0 50%);
}
```

A pill/rounded rectangle with hexagon-style angled ends (7px cut on left, 7px chamfer on right). Used for filter pills and status badges.

**RN translation:** Use `react-native-svg` `ClipPath` with a polygon. Compute points dynamically:

```tsx
const pillPoints = (width: number, height: number, cut = 7) => [
  `${cut},0`,
  `${width - cut},0`,
  `${width},${height / 2}`,
  `${width - cut},${height}`,
  `${cut},${height}`,
  `0,${height / 2}`,
].join(' ');
```

### 7.7 Glow Classes (Drop Shadows)
> Tags: `Glow` `Shadow` `Drop-Shadow` `Theme`

**CSS:**
```css
.glow-primary {
  filter: drop-shadow(0 0 6px color-mix(in oklab, var(--hud-primary) 70%, transparent));
}
.glow-secondary {
  filter: drop-shadow(0 0 6px color-mix(in oklab, var(--hud-secondary) 60%, transparent));
}
.glow-current {
  filter: drop-shadow(0 0 5px currentColor);
}
```

**RN translation:** React Native doesn't support arbitrary CSS `drop-shadow` filters. Alternatives:

1. **`react-native-svg` filters** — Limited support for SVG filter primitives in RN.
2. **`react-native-fast-blur` / `expo-blur`** — Not a shadow replacement.
3. **Box-shadow with colored shadow:** RN `shadowColor` + `shadowOffset` + `shadowOpacity` + `shadowRadius`. While this produces a spread-out shadow rather than a tight glow, with `shadowOffset: { width: 0, height: 0 }` and a small radius it approximates a glow:
   ```tsx
   {
     shadowColor: theme.colors.primary,
     shadowOffset: { width: 0, height: 0 },
     shadowOpacity: 0.7,
     shadowRadius: 6,
   }
   ```
   **Note:** Only works on iOS and Android (not web). For cross-platform, use `react-native-svg` with a `Defs` filter or wrap the element in an SVG.

4. **Best approach:** Create a `GlowWrapper` component that wraps children in an SVG `filter` or uses `react-native-reanimated` + `react-native-svg` for cross-platform glow.

### 7.8 `.text-glow` — Text Glow Shadow
> Tags: `Glow` `Text-Glow` `Text-Shadow` `Theme`

**CSS:**
```css
.text-glow {
  text-shadow:
    0 0 6px color-mix(in oklab, var(--hud-primary) 80%, transparent),
    0 0 18px color-mix(in oklab, var(--hud-primary) 50%, transparent);
}
```

A dual-layer neon text glow applied to the main screen title ("LIST").

**RN translation:**
- iOS: `textShadowColor`, `textShadowOffset`, `textShadowRadius`:
  ```tsx
  {
    textShadowColor: theme.colors.primary,
    textShadowOffset: { width: 0, height: 0 },
    textShadowRadius: 9, // average of the two layers
  }
  ```
- Android: Supports `textShadow` via the same props but renders as a single shadow. Accept the approximation.

### 7.9 `.no-scrollbar` — Hidden Scrollbar
> Tags: `Scrollbar` `Scrolling` `RN`

**CSS:**
```css
.no-scrollbar { scrollbar-width: none; }
.no-scrollbar::-webkit-scrollbar { display: none; }
```

Applied to the game list `ul` (overflow-y-auto).

**RN translation:** `react-native`'s `FlatList` / `ScrollView` doesn't show scrollbars by default on most platforms. Set `showsVerticalScrollIndicator={false}`.

---

## 8. Data Model
> Tags: `Data` `Models` `Types`
> **Tags:** `Data` `Model` `Game`

### Types (from `lib/games.ts`)
> Tags: `Data` `Types` `GameStatus` `Game` `HudTheme`

#### `GameStatus`
> Tags: `Data` `Types` `GameStatus`
```ts
export type GameStatus = 'playing' | 'backlog'
```
Note: The web design only has two statuses (playing/backlog). The main Expo project has four (Backlog, Playing, Completed, Dropped). When porting, you may need to extend the status types or map them.

#### `Game`
> Tags: `Data` `Types` `Game`
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

---

## 9. Screen Breakdown
> Tags: `Screens` `StatusBar` `Header` `Buttons` `FilterBar` `GameList` `BottomNav`
> **Tags:** `Screen` `Layout` `StatusBar` `Header` `Filter` `Buttons` `Nav` `Dashboard` `List`

### 9.1 `StatusBar` (Top Bar)
> Tags: `StatusBar` `Screens` `Icons`

**Location:** First child of BacklogScreen root div, before header.

**Structure:** A horizontal flex row with:
- Left: Time "21:47" — `font-display text-[11px] font-bold tracking-wider text-hud-text`
- Right: 3 icons in a row:
  - `SignalHigh` (cellular) — `size-3.5`
  - `Wifi` — `size-3.5`
  - `BatteryFull` — `size-4`

**Spacing:** `px-6` horizontal padding, `pb-2 pt-3` vertical padding. `justify-between` for left/right split.

**RN translation:**
```tsx
<View style={{
  paddingHorizontal: 24,
  paddingBottom: 8,
  paddingTop: 12,
  flexDirection: 'row',
  justifyContent: 'space-between',
  alignItems: 'center',
}}>
  <Text style={{ fontFamily: 'Orbitron', fontSize: 11, fontWeight: '700', letterSpacing: 1, color: theme.colors.text }}>
    21:47
  </Text>
  <View style={{ flexDirection: 'row', gap: 6, alignItems: 'center' }}>
    <SignalHigh size={14} color={theme.colors.text} />
    <Wifi size={14} color={theme.colors.text} />
    <BatteryFull size={16} color={theme.colors.text} />
  </View>
</View>
```

### 9.2 Header
> Tags: `Header` `Screens` `Back-Button` `Title`

**Structure:**
```
[← HexIcon] [Title block]        [Total block]
```

- **Back button:** `HexIcon` (hex shape, secondary tone) wrapping `<ArrowLeft />`. Button style: `rounded-none outline-none focus-visible:scale-110`.
- **Title block (left):**
  - Archive label: `// Archive_01` — `font-display text-[8px] font-medium uppercase tracking-[0.35em] text-hud-secondary/80`
  - Screen title: `LIST` — `font-display text-2xl font-black uppercase tracking-[0.12em] text-white text-glow`
- **Total block (right):**
  - Label: `Total` — `font-display text-[8px] uppercase tracking-[0.25em] text-hud-muted`
  - Value: `$XX.XX` — `font-display text-sm font-bold tabular-nums text-hud-primary`

**Wrap:** `px-4 pt-1` padding, `flex flex-col gap-4 md:flex-row md:items-end md:justify-between` (on mobile it's column, on desktop row — for RN mobile, always column gap).

**RN translation:**
```tsx
<View style={{ paddingHorizontal: 16, paddingTop: 4, gap: 16 }}>
  <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
    <Pressable style={{ alignItems: 'center', justifyContent: 'center' }} android_ripple={...}>
      <HexIcon shape="hex" tone="secondary">
        <ArrowLeft size={16} color={theme.colors.secondary} />
      </HexIcon>
    </Pressable>
    <View style={{ flex: 1, marginLeft: 12 }}>
      <Text style={{ fontFamily: 'Orbitron', fontSize: 8, fontWeight: '500', letterSpacing: 2.8, color: colorWithOpacity(theme.colors.secondary, 0.8) }}>
        // Archive_01
      </Text>
      <Text style={{
        fontFamily: 'Orbitron',
        fontSize: 24,
        fontWeight: '900',
        letterSpacing: 2.4,
        color: '#fff',
        textShadowColor: theme.colors.primary,
        textShadowOffset: { width: 0, height: 0 },
        textShadowRadius: 9,
      }}>
        LIST
      </Text>
    </View>
    <View style={{ alignItems: 'flex-end' }}>
      <Text style={{ fontFamily: 'Orbitron', fontSize: 8, letterSpacing: 2, color: theme.colors.muted, textTransform: 'uppercase' }}>
        Total
      </Text>
      <Text style={{ fontFamily: 'Orbitron', fontSize: 14, fontWeight: '700', color: theme.colors.primary, fontVariant: ['tabular-nums'] }}>
        ${total.toFixed(2)}
      </Text>
    </View>
  </View>
  ...
</View>
```

### 9.3 Primary Action Buttons (2-button grid)
> Tags: `Buttons` `Screens` `Search` `Add-Manually`

**Structure:** A 2-column grid (`grid grid-cols-2 gap-2.5`).

#### Button 1: "Search RAWG" (Primary)
> Tags: `Buttons` `Search` `Primary-Button`
- Outer: `<button className="glow-primary group outline-none">`
- Inner: `<span className="bevel flex h-11 items-center justify-center gap-2 bg-gradient-to-r from-hud-primary to-hud-secondary font-display text-[10px] font-bold uppercase tracking-[0.14em] text-black transition-[filter] group-hover:brightness-110 group-focus-visible:brightness-125">`
  - `glow-primary` — primary neon glow
  - `bevel` — 10px corner cut
  - `h-11` — 44px height
  - `bg-gradient-to-r from-hud-primary to-hud-secondary` — gradient background from primary to secondary
  - `text-black` — black text (readable on bright gradient)
  - `group-hover:brightness-110` — hover brightens 10%
  - `group-focus-visible:brightness-125` — focus brightens 25%
- Content: `<Search className="size-4 stroke-[2.25]" />` + text "Search RAWG"

#### Button 2: "Add manually" (Secondary)
> Tags: `Buttons` `Add` `Secondary-Button`
- Outer: `<button className="glow-secondary group outline-none">`
- Inner wrapper 1: `<span className="bevel block bg-hud-secondary p-px">` — bevel cut, 1px border using secondary color as background
- Inner wrapper 2: `<span className="bevel flex h-[42px] items-center justify-center gap-2 bg-hud-bg/90 font-display text-[10px] font-bold uppercase tracking-[0.14em] text-hud-secondary ...">`
  - `bg-hud-bg/90` — 90% opacity of `--hud-bg-from` (dark background)
  - `text-hud-secondary` — secondary color text
  - `group-hover:bg-hud-secondary/15` — hover adds secondary tint
  - `group-focus-visible:bg-hud-secondary/25` — focus adds more secondary tint
- Content: `<Plus className="size-4 stroke-[2.25]" />` + text "Add manually"

**Key visual:** Button 1 is a bright gradient glow button; Button 2 is a bordered secondary button with a dark interior. Both use the bevel cut and hex glow.

### 9.4 Filter Bar
> Tags: `FilterBar` `Screens` `Buttons` `Sort`

**Structure:**
```
[HexIcon (diamond, secondary)] [Label: "Filter"] [Pill: All] [Pill: Playing] [Pill: Backlog] [Sort button]
```

- Outer wrapper: `<div className="bevel bevel-sm mt-3 bg-hud-secondary/25 p-px">`
  - `bevel bevel-sm` — 6px corner cut
  - `bg-hud-secondary/25` — 25% opacity secondary color as "border"
  - `p-px` — 1px padding
- Inner: `<div className="bevel bevel-sm flex items-center gap-2 bg-hud-surface px-2 py-1.5 backdrop-blur-md">`
  - `bg-hud-surface` — semi-transparent surface color
  - `backdrop-blur-md` — blurred background
  - `px-2 py-1.5` — 8px horizontal, 6px vertical padding
- Content:
  - `HexIcon` with `shape="diamond"`, `size="sm"`, `tone="secondary"` wrapping `<SlidersHorizontal />`
  - `<span className="font-display text-[9px] font-bold uppercase tracking-[0.2em] text-hud-muted">Filter</span>`
  - **Filter pills group:** `<div role="group" aria-label="Filter by status" className="ml-auto flex gap-1">`
    - 3 pills with `hex-pill` shape, `px-2.5 py-1`, `font-display text-[8px] font-bold uppercase tracking-[0.14em]`
    - Active pill: `bg-hud-primary text-black`
    - Inactive pill: `bg-white/5 text-hud-muted hover:bg-white/10 hover:text-white focus-visible:text-white`
  - **Sort button:** `<button className="text-hud-secondary outline-none hover:text-white focus-visible:text-white">`
    - `<ArrowDownUp className="size-3.5 glow-current" />`

### 9.5 Entry Count Divider
> Tags: `Divider` `Screens` `Entry-Count`

**Structure:**
```
[Label: "NN entries"] [Divider line] [Diamond accent]
```

- `<div className="mt-3 flex items-center gap-2" aria-hidden="true">`
- Left: `<span className="font-display text-[8px] uppercase tracking-[0.3em] text-hud-muted">{count.toString().padStart(2, '0')} entries</span>`
- Center: `<span className="h-px flex-1 bg-gradient-to-r from-hud-secondary/50 to-transparent" />` — thin horizontal gradient divider
- Right: `<span className="size-1 rotate-45 bg-hud-primary" />` — a 4px square rotated 45° (diamond) in primary color

### 9.6 Game List
> Tags: `GameList` `Screens` `GameCard` `FlatList`

**Structure:**
```tsx
<ul className="no-scrollbar relative mt-2 flex flex-1 flex-col gap-2.5 overflow-y-auto px-4 pb-4 pt-1">
  {visibleGames.map((game) => (
    <GameCard key={game.id} game={game} selected={selectedId === game.id} onSelect={() => setSelectedId(game.id)} />
  ))}
</ul>
```

- `flex-1 flex-col` — full-height column, scrollable
- `gap-2.5` — 10px gap between cards
- `px-4 pb-4 pt-1` — horizontal padding 16px, bottom padding 16px, top padding 4px
- `overflow-y-auto` — vertical scroll
- `no-scrollbar` — hidden scrollbar
- `relative` — for z-index context

**RN translation:** Replace `ul` with `FlatList`:
```tsx
<FlatList
  data={visibleGames}
  keyExtractor={(item) => item.id}
  renderItem={({ item }) => (
    <GameCard
      game={item}
      selected={selectedId === item.id}
      onSelect={() => setSelectedId(item.id)}
    />
  )}
  scrollEnabled={true}
  showsVerticalScrollIndicator={false}
  contentContainerStyle={{
    marginTop: 8,
    paddingHorizontal: 16,
    paddingBottom: 16,
    gap: 10,
  }}
/>
```

### 9.7 Bottom Navigation
> Tags: `BottomNav` `Screens` `Navigation` `Icons`

**Structure:**
```tsx
<nav className="relative border-t border-hud-secondary/25 bg-black/50 px-6 pb-4 pt-2 backdrop-blur-md">
  <span aria-hidden="true" className="absolute inset-x-10 -top-px h-px bg-gradient-to-r from-transparent via-hud-primary to-transparent" />
  <ul className="flex items-center justify-between">
    {items.map(...)}
  </ul>
</nav>
```

- `border-t border-hud-secondary/25` — 1px top border, secondary at 25% opacity
- `bg-black/50` — 50% black background
- `backdrop-blur-md` — blur effect
- `px-6 pb-4 pt-2` — 24px horizontal, 16px bottom, 8px top
- **Glow line:** An absolutely positioned 1px line at `top: -1px` (using `-top-px`), spanning `inset-x-10` (10px from each side), with a gradient from transparent → `--hud-primary` → transparent. This creates a subtle neon divider at the top of the nav bar.

**Nav items (4):**
| Label | Icon | Active? |
|---|---|---|
| Home | `House` | No |
| List | `LayoutList` | Yes |
| Playing | `Gamepad2` | No |
| Profile | `UserRound` | No |

Each item:
```tsx
<li>
  <a href="#" aria-current={active ? 'page' : undefined} className="flex flex-col items-center gap-1 outline-none">
    <HexIcon size="sm" tone={active ? 'primary' : 'secondary'} active={active} className={active ? '' : 'opacity-60'}>
      <Icon />
    </HexIcon>
    <span className={cn(
      'font-display text-[7px] font-bold uppercase tracking-[0.2em]',
      active ? 'text-hud-primary' : 'text-hud-muted'
    )}>
      {label}
    </span>
  </a>
</li>
```

**RN translation:** Use a bottom tab bar or a `View` with `flexDirection: 'row', justifyContent: 'space-between'`:
```tsx
<View style={{
  flexDirection: 'row',
  justifyContent: 'space-between',
  borderTopWidth: 1,
  borderTopColor: colorWithOpacity(theme.colors.secondary, 0.25),
  backgroundColor: 'rgba(0, 0, 0, 0.5)',
  paddingHorizontal: 24,
  paddingBottom: 16,
  paddingTop: 8,
}}>
  {items.map((item) => (
    <TouchableOpacity key={item.label} onPress={item.onPress} style={{ alignItems: 'center', gap: 4 }}>
      <HexIconRN ... />
      <Text style={{...}}>{item.label}</Text>
    </TouchableOpacity>
  ))}
</View>
```

The glow divider line can be a thin `View` with a gradient background or an SVG `<Rect>` with a linear gradient.

---

## 10. Component Breakdown
> Tags: `Components` `GameCard` `HexIcon` `StatusBadge` `PriceTag` `BottomNav` `Header`
> **Tags:** `Components` `Card` `Badge` `HexIcon` `Button` `PriceTag`

### 10.1 `BacklogScreen`
> Tags: `BacklogScreen` `Screens` `Components`

**File:** `components/hud/backlog-screen.tsx`

**Props:** `{ themeClassName: string }`

**State:**
- `filter: Filter` (type: `'all' | 'playing' | 'backlog'`) — default `'all'`
- `selectedId: string` — default `games[0].id` (first game)

**Structure (top-level):**
```tsx
<div className={cn(themeClassName, 'hud-screen relative flex h-full flex-col overflow-hidden font-sans')}>
  <div className="hud-grid pointer-events-none absolute inset-0" />
  <div className="hud-scanlines pointer-events-none absolute inset-0" />
  <StatusBar />
  <header>...</header>
  <ul className="no-scrollbar ...">  // Game list
    {visibleGames.map(game => <GameCard .../>)}
  </ul>
  <BottomNav />
</div>
```

**Key behaviors:**
- Theme applied by adding `themeClassName` to root element (e.g. `theme-violet`)
- Two absolutely positioned decorative layers cover the entire screen
- Filter changes reduce the visible games list and update the total/count
- Selected game ID controls which card is "active" (glowing)

### 10.2 `StatusBar` (internal sub-component)
> Tags: `StatusBar` `Components`

**Defined inside `backlog-screen.tsx`.**

A static status bar — no interactivity. Shows hardcoded time "21:47" and 3 icons.

### 10.3 `BottomNav` (internal sub-component)
> Tags: `BottomNav` `Components`

**Defined inside `backlog-screen.tsx`.**

Static 4-item navigation. The "List" item is active. No interactivity (href="#" placeholders).

### 10.4 `PhoneFrame`
> Tags: `PhoneFrame` `Components` `Preview`

**File:** `components/hud/phone-frame.tsx`

**Props:** `{ children: React.ReactNode; label: string }`

**Structure:**
```tsx
<div role="region" aria-label={label} className="relative w-full max-w-[380px] rounded-[44px] bg-gradient-to-b from-neutral-700 via-neutral-900 to-neutral-800 p-[10px] shadow-[...]">
  <div className="relative aspect-[9/16] w-full overflow-hidden rounded-[34px] bg-black">
    <div aria-hidden="true" className="absolute left-1/2 top-2 z-20 h-5 w-24 -translate-x-1/2 rounded-full bg-black" />
    {children}
  </div>
</div>
```

This is a **web-only preview container** that shows the design in a phone mockup:
- Outer frame: 10px padding, 44px corner radius, gradient border (neutral grays)
- Inner screen: `aspect-[9/16]` (portrait phone ratio), 34px corner radius, black background
- **Notch/cutout:** A 24px-wide × 5px-high black bar at the top center (`top-2`, `z-20`) simulating the phone's front camera cutout

**RN translation:** For the actual mobile app, this is not needed (the screen fills the viewport). For a design-preview mode, render the screen directly full-screen.

### 10.5 `GameCard`
> Tags: `GameCard` `Components`

**File:** `components/hud/game-card.tsx`

**Props:** `{ game: Game; selected: boolean; onSelect: () => void }`

**Structure:**
```tsx
<li className={cn('transition-[filter]', selected && 'glow-primary')}>
  <button className={cn(
    'bevel bevel-lg block w-full p-px text-left outline-none transition-colors focus-visible:bg-hud-primary',
    selected ? 'bg-hud-primary' : 'bg-hud-secondary/30 hover:bg-hud-secondary/60'
  )}>
    <div className="bevel bevel-lg relative flex items-center gap-3 bg-[color-mix(in_oklab,var(--hud-bg-from)_82%,transparent)] p-2.5 backdrop-blur-md">
      {/* Highlight overlay */}
      <div className="pointer-events-none absolute inset-0 bg-gradient-to-r from-white/[0.04] via-transparent to-hud-primary/[0.06]" />
      
      {/* Cover image wrapper */}
      <div className={cn(
        'bevel bevel-sm relative shrink-0 p-px',
        selected ? 'bg-hud-primary' : 'bg-hud-secondary/70'
      )}>
        <div className="bevel bevel-sm relative size-14 overflow-hidden bg-black">
          <Image src={game.cover} alt={`${game.title} cover art`} fill sizes="56px" className="object-cover" />
        </div>
      </div>

      {/* Game info */}
      <div className="relative min-w-0 flex-1">
        <h3 className="truncate text-[15px] font-bold leading-tight text-white">{game.title}</h3>
        <div className="mt-0.5 flex items-center gap-2.5 text-[11px] font-medium text-hud-muted">
          <span><Clock size={12} /> {game.playtimeHours}h</span>
          <span><Star size={12} /> MC {game.metacritic}</span>
        </div>
        <StatusBadge status={game.status} className="mt-1.5" />
      </div>

      {/* Price tag */}
      <PriceTag price={game.price} highlighted={selected} />
    </div>
  </button>
</li>
```

**Visual hierarchy:**
1. **Outer `<li>`** — adds `glow-primary` filter when selected (the whole card glows)
2. **Outer `<button>`** — bevel-lg cut, 1px border, full width
   - Selected state: border is `--hud-primary` color
   - Unselected: `--hud-secondary` at 30% opacity
3. **Inner content div** — bevel-lg cut, dark tinted background (`--hud-bg-from` at 82% opacity), `backdrop-blur-md`
   - Contains a subtle highlight overlay: left-to-right gradient from white[4%] to transparent to primary[6%]
4. **Cover image container** — nested bevel cuts, 56×56px (size-14), black background, image fills with `object-cover`
   - When selected: outer ring is `--hud-primary`; when not: `--hud-secondary` at 70% opacity
5. **Game info** — title (bold, white, 15px, truncated), metadata row (playtime + metacritic)
6. **PriceTag** — on the right side

### 10.6 `PriceTag` (internal sub-component of GameCard)
> Tags: `PriceTag` `Components`

**Props:** `{ price: number; highlighted: boolean }`

**Structure:**
```tsx
<div className={cn('relative shrink-0', highlighted ? 'glow-primary' : 'glow-secondary')}>
  <div className={cn('bevel bevel-sm p-px', highlighted ? 'bg-hud-primary' : 'bg-hud-secondary/80')}>
    <div className="bevel bevel-sm flex min-w-[58px] flex-col items-end bg-black/80 px-2 py-1">
      <span className="font-display text-[7px] font-medium uppercase tracking-[0.2em] text-hud-muted">Price</span>
      <span className={cn('font-display text-[13px] font-bold tabular-nums', highlighted ? 'text-hud-primary' : 'text-hud-secondary')}>
        <span className="text-[9px] opacity-70">$</span>
        {price.toFixed(2)}
      </span>
    </div>
  </div>
</div>
```

- Outer div: glow (primary or secondary depending on selection)
- Second layer: bevel-sm cut, border color (primary or secondary at 80%)
- Third layer: bevel-sm cut, black at 80% opacity fill, padding 8px×4px
- Text: "Price" label (7px, muted), then value (13px, bold, tabular nums)
- `$` symbol is smaller (9px, 70% opacity) and inline with the value

**RN translation:**
```tsx
<View style={{
  marginLeft: 10,
  shadowColor: highlighted ? theme.colors.primary : theme.colors.secondary,
  shadowOffset: { width: 0, height: 0 },
  shadowOpacity: 0.7,
  shadowRadius: 6,
}}>
  <View style={{
    borderRadius: 4, // for bevel-sm, you'd need SVG clip
    padding: 1,
    borderColor: highlighted ? theme.colors.primary : colorWithOpacity(theme.colors.secondary, 0.8),
    borderWidth: 1,
  }}>
    <View style={{
      borderRadius: 4,
      minWidth: 58,
      alignItems: 'flex-end',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      paddingHorizontal: 8,
      paddingVertical: 4,
    }}>
      <Text style={{ fontFamily: 'Orbitron', fontSize: 7, fontWeight: '500', letterSpacing: 1.4, color: theme.colors.muted, textTransform: 'uppercase' }}>
        Price
      </Text>
      <Text style={{ fontFamily: 'Orbitron', fontSize: 13, fontWeight: '700', color: highlighted ? theme.colors.primary : theme.colors.secondary, fontVariant: ['tabular-nums'] }}>
        <Text style={{ fontSize: 9, opacity: 0.7 }}>$</Text>
        {price.toFixed(2)}
      </Text>
    </View>
  </View>
</View>
```

### 10.7 `StatusBadge`
> Tags: `StatusBadge` `Components` `Badge`

**File:** `components/hud/status-badge.tsx`

**Props:** `{ status: GameStatus; className?: string }`

**Structure:**
```tsx
<span className={cn('inline-flex glow-current', isPlaying ? 'text-hud-playing' : 'text-hud-backlog', className)}>
  <span className="hex-pill bg-current p-px">
    <span className="hex-pill flex items-center gap-1.5 bg-black/85 px-2.5 py-[3px]">
      <span className={cn('size-1.5 rotate-45 bg-current', isPlaying && 'animate-pulse')} />
      <span className="font-display text-[8px] font-bold uppercase tracking-[0.18em]">
        {isPlaying ? 'Playing' : 'Backlog'}
      </span>
    </span>
  </span>
</span>
```

**Visual layers:**
1. Outer `<span>` — `glow-current` (drop-shadow with current color), text color set to playing/backlog color
2. Middle `<span>` — `hex-pill` cut, `bg-current` (1px border using the text color), `p-px`
3. Inner `<span>` — `hex-pill` cut, `bg-black/85` (dark fill), `px-2.5 py-[3px]` (10px×3px padding)
4. **Dot indicator:** A 6×6px square (`size-1.5`) rotated 45° (diamond), `bg-current` (inherits the status color). **Animating:** `animate-pulse` only on "Playing" status
5. **Text:** Status label, 8px, bold, uppercase, tracked

**RN translation:**
- The `rotate-45` on a `View` creates a diamond: use `transform: [{ rotate: '45deg' }]`
- `animate-pulse` → Use `react-native-reanimated` or `Animated` API with opacity animation, or `react-native`'s `Animated.timing` loop
- Hex-pill clipping → SVG `ClipPath` or custom drawable

```tsx
<View style={{
  shadowColor: isPlaying ? theme.colors.playing : theme.colors.backlog,
  shadowOffset: { width: 0, height: 0 },
  shadowOpacity: 0.6,
  shadowRadius: 5,
  backgroundColor: 'transparent',
  alignSelf: 'flex-start',
}}>
  {/* Outer hex-pill border (approximate with border) */}
  <View style={{
    borderWidth: 1,
    borderColor: isPlaying ? theme.colors.playing : theme.colors.backlog,
    borderRadius: 8, // approximate
    padding: 1,
  }}>
    <View style={{
      flexDirection: 'row',
      alignItems: 'center',
      gap: 6,
      backgroundColor: 'rgba(0, 0, 0, 0.85)',
      paddingHorizontal: 10,
      paddingVertical: 3,
      borderRadius: 6,
    }}>
      <Animated.View style={{
        width: 6,
        height: 6,
        borderRadius: 1,
        backgroundColor: isPlaying ? theme.colors.playing : theme.colors.backlog,
        transform: [{ rotate: '45deg' }],
        opacity: pulseAnim, // animated value for playing status
      }} />
      <Text style={{
        fontFamily: 'Orbitron',
        fontSize: 8,
        fontWeight: '700',
        letterSpacing: 1.44,
        color: isPlaying ? theme.colors.playing : theme.colors.backlog,
        textTransform: 'uppercase',
      }}>
        {isPlaying ? 'Playing' : 'Backlog'}
      </Text>
    </View>
  </View>
</View>
```

### 10.8 `HexIcon`
> Tags: `HexIcon` `Components` `Icons` `Hex` `Diamond`

**File:** `components/hud/hex-icon.tsx`

**Props:**
```ts
{
  children: React.ReactNode
  shape?: 'hex' | 'diamond'   // default: 'hex'
  tone?: 'primary' | 'secondary'  // default: 'secondary'
  size?: 'sm' | 'md'         // default: 'md'
  active?: boolean           // default: false
  className?: string
}
```

**Two render paths:**

#### Diamond shape (`shape === 'diamond'`)
> Tags: `HexIcon` `Diamond` `Components`

```tsx
<span className="relative inline-flex shrink-0 items-center justify-center"
  className={cn(dims, tone === 'primary' ? 'glow-primary' : 'glow-secondary', className)}>
  <span className="absolute inset-[18%] rotate-45 border {tone border color} {active ? bg : bg-black/40}" />
  <span className="relative [&_svg]:size-3.5 [&_svg]:stroke-[1.5] {tone text color}">{children}</span>
</span>
```

- Outer: positioned relative, flex center, glow filter
- Inner border span: absolutely positioned at `inset-[18%]` (18% padding on all sides), rotated 45° (diamond), border in tone color
  - When active: fill with tone color at 25% opacity
  - When inactive: fill with black at 40% opacity
- Icon: positioned relative, SVG sized to `size-3.5` (14px), stroke width 1.5

#### Hex shape (default)
> Tags: `HexIcon` `Hexagon` `Components`

```tsx
<span className="relative inline-flex shrink-0" className={cn(dims, glow, className)}>
  {/* Outer hex border */}
  <span className="hex absolute inset-0 {tone border color}" />
  {/* Inner hex fill */}
  <span className="hex absolute inset-px flex items-center justify-center {bg color} {tone text color}" className={cn(...)}>
    {children}
  </span>
</span>
```

- Outer: positioned relative, glow filter
- Layer 1 (background hex): `hex` clip-path, fills entire element, border = tone color (creates the hex outline)
- Layer 2 (inner hex): `hex` clip-path, `inset-px` (1px smaller on all sides), center-aligned, fill = tone color at 25% + black (for active) or `--hud-bg` (for inactive)
- Icon: SVG with size `size-3.5` (sm) or `size-4` (md), stroke 1.5

**Size mapping:**
- `sm`: `size-7` (28×28px container), SVG `size-3.5` (14px)
- `md`: `size-9` (36×36px container), SVG `size-4` (16px)

**RN translation:**
- Hex/diamond clipping → Use `react-native-svg` `ClipPath` + `Polygon`, or pre-rendered PNG masks
- Glow → `shadowColor` + `shadowRadius`
- `inset-[18%]` → computed as `width * 0.18` in pixels
- `stroke-[1.5]` → `strokeWidth={1.5}` on SVG icon
- `rotate-45` → `transform: [{ rotate: '45deg' }]`

```tsx
// Hexagon points for a 28x28 container:
// (25% of 28 = 7, 75% of 28 = 21, 50% of 28 = 14)
const hexPoints = "7,0.84 21,0.84 28,14 21,27.16 7,27.16 0,14";
// Diamond points (45° rotated square):
const diamondPoints = (size: number) => {
  const inset = size * 0.18;
  return `${inset},${size/2} ${size/2},${inset} ${size-inset},${size/2} ${size/2},${size-inset}`;
};
```

### 10.9 `Button` (UI primitive)
> Tags: `Buttons` `Components` `shadcn`

**File:** `components/ui/button.tsx`

This is a **standard shadcn/ui Button** component — it does NOT appear to be used in the main screen. It wraps `@base-ui/react`'s Button primitive with CVA (class-variance-authority) for variant/size management.

**Variants:** `default`, `outline`, `secondary`, `ghost`, `destructive`, `link`

**Sizes:** `default` (h-8), `xs` (h-6), `sm` (h-7), `lg` (h-9), `icon`, `icon-xs`, `icon-sm`, `icon-lg`

**Note:** This component is from the shadcn template and is **not used** in the BacklogScreen UI. When porting to RN, you can either skip it or implement it using `react-native`'s `Pressable` with the same variant logic, or use `expo-modules-core`'s `Pressable` from the `@goring/react-native` ecosystem.

### 10.10 `cn` Utility
> Tags: `Utilities`

**File:** `lib/utils.ts`

```ts
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
```

Standard shadcn utility — merges class names with deduplication ( Tailwind Merge). In RN, this maps to conditional style merging. If using StyleSheet, replace with conditional style application or `twMerge` equivalent for RN classnames.

---

## 11. Icon Usage
> **Tags:** `Icons` `Icon`

The design uses **lucide-react** icons throughout. The following icons appear:

| Icon | Used In | Notes |
|---|---|---|
| `ArrowLeft` | Back button in header | `size-4`? No — it's inside HexIcon so it uses `size-3.5` (sm) |
| `ArrowDownUp` | Sort button in filter bar | `size-3.5` (sm), `glow-current` |
| `BatteryFull` | Status bar | `size-4`, no glow |
| `Clock` | Game metadata | `size-3` (inside flex, small) |
| `Gamepad2` | Bottom nav (Playing tab) | Inside `HexIcon` size="sm" |
| `House` | Bottom nav (Home tab) | Inside `HexIcon` size="sm" |
| `LayoutList` | Bottom nav (List tab) | Inside `HexIcon` size="sm" — **active** tab |
| `Plus` | "Add manually" button | `size-4`, `stroke-[2.25]` |
| `Search` | "Search RAWG" button | `size-4`, `stroke-[2.25]` |
| `SignalHigh` | Status bar | `size-3.5` |
| `SlidersHorizontal` | Filter bar label | Inside `HexIcon` size="sm" |
| `Star` | Game metadata | `size-3` |
| `UserRound` | Bottom nav (Profile tab) | Inside `HexIcon` size="sm" |
| `Wifi` | Status bar | `size-3.5` |

### Icon Details
> Tags: `Icons` `Details` `Stroke` `Sizes`

- **`stroke-[2.25]`** is applied to `Search` and `Plus` icons in buttons — thicker stroke for visibility on gradient/different backgrounds.
- **`stroke-[1.5]`** is applied to all `HexIcon` children via the class `[&_svg]:stroke-[1.5]`.
- Icon sizes:
  - Status bar: 12px (SignalHigh, Wifi = size-3.5), 14px (BatteryFull = size-4)
  - Button icons: size-4 (16px)
  - Metadata: size-3 (12px)
  - HexIcon children: 14px (sm) or 16px (md)

### React Native Translation
> Tags: `Icons` `RN` `Translation` `lucide-react-native`

Replace `lucide-react` with `lucide-react-native`:
```bash
npx expo install lucide-react-native
```
Change imports:
```ts
// Web:
import { Search } from 'lucide-react';
// RN:
import { Search } from 'lucide-react-native';
```
SVG stroke control in RN: `strokeWidth` prop, e.g., `<Search size={16} strokeWidth={2.25} />`

---

## 12. React Native / Expo Translation Guide
> **Tags:** `RN` `Expo` `Translation` `React-Native`

### 12.1 Environment Setup
> Tags: `RN` `Expo` `Setup` `Installation`

```bash
# Install dependencies
npx expo install expo-linear-gradient expo-blur lucide-react-native
npx expo install react-native-svg
npm install clsx tailwind-merge class-variance-authority  # if using tailwind
```

### 12.2 Project Structure
> Tags: `RN` `Expo` `Project-Structure` `File-Organization`

```
src/
├── app/
│   ├── _layout.tsx          # Root layout (expo-router Stack)
│   ├── hud.tsx              # Main BacklogScreen (port)
│   └── ... (existing screens)
├── components/
│   └── hud/
│       ├── BacklogScreen.tsx      # Main screen (port of backlog-screen.tsx)
│       ├── GameCard.tsx           # Game card (port of game-card.tsx)
│       ├── HexIcon.tsx            # Hex/diamond icon wrapper (port of hex-icon.tsx)
│       ├── StatusBadge.tsx        # Status badge (port of status-badge.tsx)
│       └── (PhoneFrame.tsx — skip, not needed for native app)
├── lib/
│   ├── games.ts        # Theme definitions + game data (port to TS theme objects)
│   └── utils.ts        # cn() utility (adapt for RN)
├── theme/
│   └── hudTheme.ts     # Theme objects + context
└── assets/
    ├── fonts/
    │   ├── Orbitron-Medium.ttf
    │   ├── Orbitron-Bold.ttf
    │   ├── Orbitron-Black.ttf
    │   ├── Rajdhani-Regular.ttf
    │   └── ...
    └── images/
        ├── covers/
        │   ├── metro-2033.png
        │   ├── tormented-souls.png
        │   ├── resident-evil-4.png
        │   └── devil-may-cry-5.png
```

### 12.3 Theme Context
> Tags: `RN` `Theme` `Context` `Provider`

Create a theme system that mirrors the CSS variable approach:

```tsx
// src/theme/hudTheme.ts
import { createContext, useContext } from 'react';
import { hudThemes } from '@/lib/games';

export type HudTheme = {
  id: 'violet' | 'emerald' | 'crimson';
  version: string;
  name: string;
  swatches: string[];
  colors: Record<string, string>;
};

// Default to violet
export const defaultHudTheme: HudTheme = hudThemes[0];
export const HudThemeContext = createContext<HudTheme>(defaultHudTheme);
export const useHudTheme = () => useContext(HudThemeThemeContext);

// Provider wraps the whole app
export function HudThemeProvider({ theme, children }: { theme: HudTheme; children: React.ReactNode }) {
  return <HudThemeContext.Provider value={theme}>{children}</HudThemeContext.Provider>;
}
```

### 12.4 Font Loading
> Tags: `RN` `Fonts` `expo-font` `Setup`

```tsx
// src/app/_layout.tsx or App.tsx
import { useEffect, useState } from 'react';
import { Font } from 'expo-font';
import * as SplashScreen from 'expo-splash-screen';

SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const [fontsLoaded] = Font.useFonts({
    'Orbitron-Medium': require('@/assets/fonts/Orbitron-Medium.ttf'),
    'Orbitron-Bold': require('@/assets/fonts/Orbitron-Bold.ttf'),
    'Orbitron-Black': require('@/assets/fonts/Orbitron-Black.ttf'),
    'Orbitron-Regular': require('@/assets/fonts/Orbitron-Regular.ttf'),
    'Rajdhani-Regular': require('@/assets/fonts/Rajdhani-Regular.ttf'),
    'Rajdhani-Medium': require('@/assets/fonts/Rajdhani-Medium.ttf'),
    'Rajdhani-SemiBold': require('@/assets/fonts/Rajdhani-SemiBold.ttf'),
    'Rajdhani-Bold': require('@/assets/fonts/Rajdhani-Bold.ttf'),
  });

  useEffect(() => {
    if (fontsLoaded) {
      SplashScreen.hideAsync();
    }
  }, [fontsLoaded]);

  if (!fontsLoaded) return null;

  return (
    <HudThemeProvider theme={defaultHudTheme}>
      {/* ... */}
    </HudThemeProvider>
  );
}
```

### 12.5 Utility Functions to Port
> Tags: `RN` `Utilities` `mixColor` `cn`

#### `cn` (class name merger)
> Tags: `Utilities` `cn` `class-variance-authority`

If using Tailwind in RN (via `tailwind-react-native-classnames` or `nativewind`):
```ts
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
export function cn(...inputs) { return twMerge(clsx(inputs)); }
```
If using StyleSheet:
```ts
// Merge conditional styles
export function mergeStyles(...styles) { return styles.filter(Boolean); }
```

#### `mixColor` — Replace `color-mix(in oklab, ...)`)
> Tags: `Utilities` `mixColor` `Color-Mix` `rgba`

CSS `color-mix` is not available in RN. Create a utility:
```ts
function hexToRgb(hex: string): { r: number; g: number; b: number } | null {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result
    ? { r: parseInt(result[1], 16), g: parseInt(result[2], 16), b: parseInt(result[3], 16) }
    : null;
}

function rgbToHex(r: number, g: number, b: number): string {
  return '#' + [r, g, b].map(x => Math.round(x).toString(16).padStart(2, '0')).join('');
}

export function mixWithTransparent(baseHex: string, opacity: number): string {
  // opacity is 0–1 (or 0–100, divide by 100)
  const o = opacity > 1 ? opacity / 100 : opacity;
  const rgb = hexToRgb(baseHex);
  if (!rgb) return baseHex;
  return rgbToHex(rgb.r * o, rgb.g * o, rgb.b * o);
}

export function rgba(hex: string, opacity: number): string {
  const rgb = hexToRgb(hex);
  if (!rgb) return hex;
  const o = opacity > 1 ? opacity / 100 : opacity;
  return `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${o})`;
}

export function tintWithBlack(baseHex: string, opacity: number): string {
  // color-mix(in oklab, var(--x) <pct>%, #000)
  const o = opacity > 1 ? opacity / 100 : opacity;
  const rgb = hexToRgb(baseHex);
  if (!rgb) return '#000';
  return `rgba(${rgb.r * o}, ${rgb.g * o}, ${rgb.b * o}, 1)`;
}
```

### 12.6 SVG Effects for RN
> Tags: `RN` `SVG` `Bevel` `Clip-Path` `react-native-svg`

Three critical visual effects require `react-native-svg`:

1. **Bevel cuts** — Use `ClipPath` with `Polygon`
2. **Hex clips** — Same approach
3. **Glow filters** — Approximate with `shadowColor` / `shadowRadius` OR use SVG filters (limited in RN)

Example bevel component:

```tsx
import Svg, { ClipPath, Polygon, G } from 'react-native-svg';

interface BeveledContainerProps {
  cut?: number;
  width: number;
  height: number;
  children?: React.ReactNode;
  style?: StyleProp<ViewStyle>;
}

export function BeveledContainer({ cut = 10, width, height, children, style }: BeveledContainerProps) {
  const points = [
    `${cut},0`,
    `${width},0`,
    `${width},${height - cut}`,
    `${width - cut},${height}`,
    `0,${height}`,
    `0,${cut}`,
  ].join(' ');

  return (
    <Svg width={width} height={height} style={style}>
      <ClipPath id="bevel-clip">
        <Polygon points={points} />
      </ClipPath>
      <G clipPath="url(#bevel-clip)">
        {children}
      </G>
    </Svg>
  );
}
```

For the hex shape:
```tsx
export function HexClip({ size, children }: { size: number; children: React.ReactNode }) {
  const w = size;
  const h = size;
  const p = [
    `${w * 0.25},${h * 0.03}`,
    `${w * 0.75},${h * 0.03}`,
    `${w},${h * 0.5}`,
    `${w * 0.75},${h * 0.97}`,
    `${w * 0.25},${h * 0.97}`,
    `0,${h * 0.5}`,
  ].join(' ');
  // ... same pattern
}
```

### 12.7 Layout Dimensions
> Tags: `RN` `Layout` `Dimensions` `SafeArea`

For a mobile screen (portrait, ~390px wide in the web preview):

| Area | Web (PhoneFrame) | RN Mobile | Notes |
|---|---|---|---|
| Screen container | `max-w-[380px] aspect-[9/16]` | Full device screen | Use SafeAreaView |
| Padding | `px-4` (16px) | `paddingHorizontal: 16` | Same |
| Card height | implicit | ~80px | Depends on content |
| Cover size | `size-14` = 56px | 56px | Same |
| Button height | `h-11` = 44px | 44px | Same |
| Bottom nav | `px-6` (24px) | `paddingHorizontal: 24` | Same |
| Status bar | `px-6` (24px) | `paddingHorizontal: 24` | Same |

### 12.8 Safe Areas
> Tags: `RN` `SafeArea` `SafeAreaView` `Insets`

The web design does NOT handle safe areas (it's a static mockup). In RN, you MUST handle:
- **Top inset** (notch): wrap the status bar area
- **Bottom inset** (home bar): add padding to `BottomNav`

```tsx
import { SafeAreaView } from 'react-native-safe-area-context';

<SafeAreaView style={{ flex: 1, backgroundColor: theme.colors.bgFrom }}>
  {/* ...screen content... */}
</SafeAreaView>
```

For the bottom nav, add `paddingBottom: insets.bottom` via `useSafeAreaInsets()`.

---

## 13. Asset Requirements
> **Tags:** `Assets` `Fonts` `Images`

### Fonts
> Tags: `Assets` `Fonts` `Orbitron` `Rajdhani`

Download and install these fonts in `assets/fonts/`:

| Font | Weights Used | Source |
|---|---|---|
| **Orbitron** | 500 (Medium), 700 (Bold), 900 (Black) | Google Fonts |
| **Rajdhani** | 400 (Regular), 500 (Medium), 600 (SemiBold), 700 (Bold) | Google Fonts |

Usage mapping:
- `--font-orbitron` → display header font (Orbitron)
- `--font-rajdhani` → body font (Rajdhani)
- `font-display` class → `fontFamily: 'Orbitron'`
- `font-sans` class → `fontFamily: 'Rajdhani'`

### Images
> Tags: `Assets` `Images` `Covers` `Icons`

| Asset | Path | Dimensions | Purpose |
|---|---|---|---|
| metro-2033.png | public/covers/ | 1024×1024 | Game cover |
| tormented-souls.png | public/covers/ | 1024×1024 | Game cover |
| resident-evil-4.png | public/covers/ | 1024×1024 | Game cover |
| devil-may-cry-5.png | public/covers/ | 1024×1024 | Game cover |
| icon.svg | public/ | — | App icon |
| apple-icon.png | public/ | — | iOS app icon |
| icon-light-32x32.png | public/ | 32×32 | Web favicon |
| icon-dark-32x32.png | public/ | 32×32 | Web favicon |

### Icons
> Tags: `Icons` `Assets` `lucide-react-native`

All icons come from `lucide-react`. In RN, use `lucide-react-native`:

```bash
npx expo install lucide-react-native
```

Import change:
```diff
- import { Search } from 'lucide-react'
+ import { Search } from 'lucide-react-native'
```

Icon rendering differences:
- Web: `<Search className="size-4 stroke-[2.25]" />`
- RN: `<Search size={16} strokeWidth={2.25} color={...} />`

---

## 14. Accessibility Specification
> **Tags:** `Accessibility` `A11y`

### ARIA Labels & Roles
> Tags: `Accessibility` `ARIA` `Roles` `Labels`

| Element | aria-* props | Notes |
|---|---|---|
| PhoneFrame root | `role="region"`, `aria-label={label}` | "label" = theme name + "theme preview" |
| Back button | `aria-label="Go back"` | Icon-only button |
| Filter group | `role="group"`, `aria-label="Filter by status"` | Group of 3 pills |
| Filter pills | `aria-pressed={active}` | Toggle button state |
| Sort button | `aria-label="Sort list"` | Icon-only button |
| Entry count divider | `aria-hidden="true"` | Decorative |
| Game cover | `alt={`${game.title} cover art`}` | Image alt text |
| Status badge dot | `aria-hidden="true"` | Decorative indicator |
| Metacritic value | `<span className="sr-only">Metacritic</span>` | Screen reader text |
| Bottom nav | `aria-label="Primary"` (nav) | Navigation landmark |
| Active nav link | `aria-current="page"` | Current page indicator |
| Theme swatches | `title={color}` | Color preview |
| Theme swatch sr-only | `sr-only` | Screen reader color name |

### Focus Management
> Tags: `Accessibility` `Focus` `Keyboard` `Ring`

- **Focus rings:** `outline-none` is used on buttons, but `focus-visible:` variants provide alternative visual feedback (brightness increase, background change). This is intentional — focus is indicated via state change, not a ring.
- **Keyboard navigation:** All interactive elements are semantic `<button>` elements. In RN, `Pressable` with `onAccessibilityTap` for screen readers.

### Screen Reader Text
> Tags: `Accessibility` `Screen-Reader` `sr-only`

- `sr-only` class hides content visually but keeps it available to screen readers
- Used for: "Metacritic" label, color hex values on swatches

---

## 15. State & Interactions Summary
> **Tags:** `State` `Interaction` `Animation`

### Client-Side State
> Tags: `State` `Client-State` `React`

The entire `BacklogScreen` is a **client component** (`'use client'` directive).

| State | Type | Default | Purpose |
|---|---|---|---|
| `filter` | `'all' \| 'playing' \| 'backlog'` | `'all'` | Filters the visible game list |
| `selectedId` | `string` | `games[0].id` | Which game card is "selected" (glowing) |

### Interactions
> Tags: `Interaction` `Click` `Tap` `State`

| Element | Interaction | Effect |
|---|---|---|
| Filter pills | Click/tap | Changes `filter` state, updates list + total |
| Sort button | Click/tap | (No handler in code — placeholder) |
| Search button | Click/tap | (No handler — placeholder) |
| Add manually button | Click/tap | (No handler — placeholder) |
| Game cards | Click/tap | Sets `selectedId`, triggers glow animation |
| Bottom nav links | Click/tap | `href="#"` (placeholders, no routing) |

### Computed Values
> Tags: `State` `Computed` `Derivations`

| Value | Derivation | Location |
|---|---|---|
| `visibleGames` | `filter === 'all' ? games : games.filter(g => g.status === filter)` | `BacklogScreen` |
| `total` | `visibleGames.reduce((sum, g) => sum + g.price, 0)` | `BacklogScreen` |
| Entry count | `visibleGames.length.toString().padStart(2, '0')` | `BacklogScreen` |

### Animations
> Tags: `Animation` `Pulse` `Glow` `Brightness` `Focus`

| Element | Animation | Trigger |
|---|---|---|
| Playing status dot | `animate-pulse` (CSS opacity pulse) | Only when `status === 'playing'` |
| Hover brightness | `group-hover:brightness-110` or `brightness-125` | Mouse hover/focus on buttons |
| Focus scale | `focus-visible:scale-110` | Focus on back button |
| Selected card | `glow-primary` filter | When `selected === true` |

---

## 16. Implementation Checklist
> **Tags:** `Checklist` `Implementation`

### Essentials
> Tags: `Checklist` `Essentials` `Dependencies` `Fonts` `Assets`

- [ ] Install `lucide-react-native` (replace `lucide-react`)
- [ ] Install `expo-font` / `expo-splash-screen`
- [ ] Install `expo-linear-gradient` and `expo-blur`
- [ ] Install `react-native-svg`
- [ ] Download and place Orbitron + Rajdhani fonts
- [ ] Download and place game cover images (metro-2033.png, tormented-souls.png, resident-evil-4.png, devil-may-cry-5.png — 1024×1024px)
- [ ] Create theme object system (replace CSS vars with JS object)
- [ ] Create `mixColor`/`rgba` utility to replace `color-mix()`
- [ ] Create `cn` or style-merging utility

### Components (in priority order)
> Tags: `Checklist` `Components` `Implementation`

- [ ] Theme context (`HudThemeContext`)
- [ ] `StatusBar` — top bar with time + icons
- [ ] `HexIcon` — hexagon/diamond icon wrapper with SVG clipping
- [ ] `StatusBadge` — playing/backlog badge with pulse animation
- [ ] `GameCard` + `PriceTag` — game list row
- [ ] `BacklogScreen` — main screen composing all sub-components
- [ ] Bottom nav bar
- [ ] Header with back button, title, and total price

### RN-Specific Adaptations
> Tags: `Checklist` `RN` `Adaptations` `Clip-Path` `Blur`

- [ ] Replace all CSS clip-path with SVG `ClipPath` + `Polygon`
- [ ] Replace CSS `drop-shadow` filters with `shadowColor`/`shadowRadius`
- [ ] Replace CSS `backdrop-blur` with `<BlurView>` from `expo-blur`
- [ ] Replace Tailwind classes with `StyleSheet.create()` or `nativewind`
- [ ] Handle safe areas (top notch, bottom home bar)
- [ ] Convert `ul`/`li` lists to `FlatList` / `View` + map
- [ ] Convert `<button>` to `Pressable` or `TouchableOpacity`
- [ ] Convert `next/image` to RN `Image` with `source={{ uri }}`
- [ ] Add `textShadow` for `.text-glow` effect (iOS)
- [ ] Implement pulse animation with `Animated` for "Playing" dot

### Theme Switching
> Tags: `Checklist` `Theme` `Switching` `Context`

- [ ] Create a theme toggle/switch mechanism (e.g., segmented control or dropdown)
- [ ] Allow switching between violet / emerald / crimson themes
- [ ] Theme changes propagate via Context

### Testing Considerations
> Tags: `Checklist` `Testing` `iOS` `Android` `Accessibility`

- [ ] Test on both iOS and Android (shadow/pulse/glow differs)
- [ ] Test with screen readers (VoiceOver / TalkBack)
- [ ] Verify font rendering at small sizes (8-11px text)
- [ ] Verify SVG clipping at different screen densities
- [ ] Check color contrast ratios (neon-on-dark may fail accessibility)
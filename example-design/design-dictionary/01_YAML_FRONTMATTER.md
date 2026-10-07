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

# AI Quick Reference (read this before any section)

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

1. [High-Level Overview](#1-high-level-overview)
2. [Project & Tooling Configuration](#2-project--tooling-configuration)
3. [Design Philosophy & Core Principles](#3-design-philosophy--core-principles)
4. [Theme System](#4-theme-system)
5. [Color Palette (All Themes)](#5-color-palette-all-themes)
6. [Typography](#6-typography)
7. [Visual Effects & CSS Utilities](#7-visual-effects--css-utilities)
8. [Data Model](#8-data-model)
9. [Screen Breakdown](#9-screen-breakdown)
10. [Component Breakdown](#10-component-breakdown)
11. [Icon Usage](#11-icon-usage)
12. [React Native / Expo Translation Guide](#12-react-native--expo-translation-guide)
13. [Asset Requirements](#13-asset-requirements)
14. [Accessibility Specification](#14-accessibility-specification)
15. [State & Interactions Summary](#15-state--interactions-summary)
16. [Implementation Checklist](#16-implementation-checklist)
---

## Tag Index

> **`Theme`** — Theme system, color tokens, palette for all theme variants
> **`Visual-Effects`** — CSS utilities: glows, bevels, clips, grids, scanlines
> **`Typography`** — Font stack, sizes, weights, letter-spacing, RN translation
> **`Components`** — Component-level specs: HexIcon, GameCard, StatusBadge, etc.
> **`Screens`** — Screen layout breakdown: StatusBar, Header, FilterBar, GameList, BottomNav
> **`Data`** — Data models and types: Game, GameStatus, sample data
> **`Icons`** — All icon usage and RN translation
> **`RN`** — React Native / Expo conversion guidance
> **`Assets`** — Font and image asset requirements
> **`Accessibility`** — ARIA roles, labels, focus management, screen readers
> **`State`** — Client-side state variables
> **`Interaction`** — Tap/click interactions and their effects
> **`Animation`** — Pulse, glow, brightness, scale animations
> **`Checklist`** — Prioritized implementation checklist
> **`Utilities`** — Helper functions: cn, mixColor, color utilities
> **`Configuration`** — Project setup, dependencies, tsconfig
> **`Bevel`** — Angled corner cuts: `.bevel`, `.bevel-sm`, `.bevel-lg`
> **`Glow`** — Neon drop-shadow effects: `.glow-primary`, `.glow-secondary`
> **`Clip-Path`** — Hexagon, diamond, hex-pill shapes
> **`Grid`** — `.hud-grid` diagonal grid overlay
> **`Scanlines`** — `.hud-scanlines` horizontal scanline effect
> **`Buttons`** — Action buttons: Search RAWG, Add manually
> **`FilterBar`** — Filter pills and sort button
> **`GameCard`** — Game list row component
> **`StatusBadge`** — Playing/Backlog status badge
> **`HexIcon`** — Hexagon/diamond icon wrapper
> **`PriceTag`** — Price tag sub-component
> **`BottomNav`** — Bottom navigation bar
> **`Header`** — Top header with title and total
> **`Scrollbar`** — Hidden scrollbar handling
> **`SafeArea`** — Safe area insets for notch/home bar
---
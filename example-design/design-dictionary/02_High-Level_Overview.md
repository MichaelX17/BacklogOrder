# 1. High-Level Overview
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

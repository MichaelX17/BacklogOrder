# 15. State & Interactions Summary
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

# 6. Typography
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

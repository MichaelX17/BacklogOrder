# 10. Component Breakdown
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

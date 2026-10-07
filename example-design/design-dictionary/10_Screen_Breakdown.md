# 10. Screen Breakdown
> Tags: `Screens` `StatusBar` `Header` `Buttons` `FilterBar` `GameList` `BottomNav`
> **Tags:** `Screen` `Layout` `StatusBar` `Header` `Filter` `Buttons` `Nav` `Dashboard` `List`

### 10.1 `StatusBar` (Top Bar)
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

### 10.2 Header
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

### 10.3 Primary Action Buttons (2-button grid)
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

### 10.4 Filter Bar
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

### 10.5 Entry Count Divider
> Tags: `Divider` `Screens` `Entry-Count`

**Structure:**
```
[Label: "NN entries"] [Divider line] [Diamond accent]
```

- `<div className="mt-3 flex items-center gap-2" aria-hidden="true">`
- Left: `<span className="font-display text-[8px] uppercase tracking-[0.3em] text-hud-muted">{count.toString().padStart(2, '0')} entries</span>`
- Center: `<span className="h-px flex-1 bg-gradient-to-r from-hud-secondary/50 to-transparent" />` — thin horizontal gradient divider
- Right: `<span className="size-1 rotate-45 bg-hud-primary" />` — a 4px square rotated 45° (diamond) in primary color

### 10.6 Game List
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

### 10.7 Bottom Navigation
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

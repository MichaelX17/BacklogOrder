# 17. Implementation Checklist
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

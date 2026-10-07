# 7. Visual Effects & CSS Utilities
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

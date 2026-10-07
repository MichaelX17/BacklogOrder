# 12. React Native / Expo Translation Guide
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

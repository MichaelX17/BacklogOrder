# 11. Icon Usage
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

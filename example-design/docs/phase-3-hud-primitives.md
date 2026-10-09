## PHASE 3 — HUD primitives (`src/components/hud/`)

Build these low-level pieces first. Every screen is made from them. Web CSS `clip-path`, `color-mix`, and `backdrop-filter` don't exist in RN, so use these translations:

### 3.1 `BevelFrame`
The signature shape: a rectangle with the **top-left and bottom-right corners cut diagonally**.

- Polygon points for size `w × h` and cut `c`: `(c,0) (w,0) (w,h-c) (w-c,h) (0,h) (0,c)`.
- Implementation: a `View` that measures itself with `onLayout`, then renders an absolutely positioned `react-native-svg` `<Svg>` behind its children with one `<Polygon>`: `fill = background`, `stroke = borderColor`, `strokeWidth = 1`. Inset the points by 0.5 so the stroke isn't clipped.
- Props: `cut: 'sm' | 'md' | 'lg'`, `borderColor`, `background` (string **or** `{ gradient: [from, to] }` for horizontal `LinearGradient` fills), `glowColor?`, `style`, `children`. Children are padded so they never overlap the cut corners.
- **Glow** (the design uses colored `drop-shadow` everywhere): when `glowColor` is set, add a second `Polygon` behind it with the same points, `stroke = glowColor`, `strokeWidth = 4`, `strokeOpacity = 0.35`. On iOS, also apply `shadowColor: glowColor, shadowOpacity: 0.7, shadowRadius: 6, shadowOffset: {0,0}`. Don't rely on Android `elevation`; it can't produce colored glows.

### 3.2 `BevelImage`
Cover art with beveled corners. Use `MaskedView` with a `BevelFrame`-shaped SVG mask (cut `sm` for 56×56 thumbnails, `lg` for the hero). Wrap it in a 1px beveled border (`secondary @ 70%`, or `primary` when selected). Props: `uri`, `size` or `style`, `dimmed?` (dimmed means `opacity: 0.6`), `overlayBadge?` (a node pinned bottom-left). Fall back to a dark `bgFrom` fill with a small `Gamepad2` icon in `secondary` when there's no cover.

### 3.3 `HexShape` / `HexIcon`
- Hexagon points (percent of size): `(25%,3%) (75%,3%) (100%,50%) (75%,97%) (25%,97%) (0,50%)`.
- `HexIcon` props: `tone: 'primary' | 'secondary'`, `size: 'sm' (28) | 'md' (36)`, `active?`, `shape?: 'hex' | 'diamond'`, `children` (a lucide icon).
  - Hex: outer polygon filled with the tone color (that's the 1px border), inner polygon inset 1px filled with `bgFrom`. When active, fill with `mix(tone, '#000', 0.30)` for primary or `mix(tone, '#000', 0.25)` for secondary. Icon: tone color, `strokeWidth 1.5`, size 14 (sm) / 16 (md). Always glow in the tone color.
  - Diamond: a square rotated 45°, inset 18%, 1px tone border, fill `rgba(0,0,0,0.4)` (active: tone @ 25%), with the icon (14) centered and not rotated.
  - Mark it decorative (`accessibilityElementsHidden` / `importantForAccessibility="no-hide-descendants"`). The parent button carries the label.

### 3.4 `HexPill`
A capsule with pointed ends. Points: `(7,0) (w-7,0) (w,h/2) (w-7,h) (7,h) (0,h/2)`. Same measure-then-SVG approach. Props: `fill`, `stroke?`, `children`. It's used for status badges, filter chips, and the "Offline ready" tag.

### 3.5 `HudBackground`
A full-screen, absolutely positioned, non-interactive (`pointerEvents="none"`) SVG with 4 layers, bottom to top:
1. Vertical `LinearGradient`: `bgVia` (top) → `bgFrom` (bottom).
2. `RadialGradient` centered at (50%, 0%), radius ≈ 120% width × 60% height: `bgTo` at 0% → transparent at 60%.
3. `RadialGradient` centered at (100%, 100%), radius ≈ 90% × 50%: `withAlpha(primary, 0.14)` → transparent at 70%.
4. **Grid**: SVG `<Pattern>` 22×22 with 1px horizontal and vertical lines in `withAlpha(secondary, 0.09)`. Mask it with a radial mask centered at (50%, 30%), radius 80% × 70%: opaque up to 10%, fading to transparent at 85%.
5. **Scanlines**: `<Pattern>` 3px tall containing a 1px-tall rect of `rgba(255,255,255,0.025)`.

Wrap it in `React.memo`. It must not re-render on scroll.

### 3.6 `HudButton`
Two variants. Height 44 (Home) / 40 (List): add a `size` prop.
- `primary`: `BevelFrame` cut `md`, horizontal gradient `primary → secondary`, label in `button` preset colored **black**, icon black (16, strokeWidth 2.25), glow `primary`.
- `secondary`: `BevelFrame` cut `md`, 1px `secondary` border, fill `withAlpha(bgFrom, 0.9)`, label + icon in `secondary`, glow `secondary`.
- Pressed state: `primary` brightens slightly (overlay `rgba(255,255,255,0.1)`). `secondary` fills `withAlpha(secondary, 0.15)`. Disabled: `opacity: 0.4` + `accessibilityState.disabled`.
- Use `Pressable`. Minimum touch target is 44dp (use `hitSlop` when visually smaller).

### 3.7 Small pieces
- `RankChip`: rect with `primary` background, `rankChip` text, `paddingHorizontal: 4`, value zero-padded (`01`, `02`). Accessible label `"Rank 1"`.
- `SagaOrderChip`: same, but `secondary` background, text `#1`, `#2`, or `?` when the order is missing.
- `GradientRule`: a 1px horizontal line that fades from `withAlpha(secondary, 0.5)` to transparent (`flex: 1`). Used after section labels.
- `DiamondDot`: a 4–6px square rotated 45° (`primary` or `currentColor`).
- `TextGlow` helper style: `textShadowColor: withAlpha(primary, 0.8), textShadowRadius: 8, textShadowOffset: {0,0}`.

Add a dev-only screen or Storybook-style test render that shows every primitive in all 3 themes (it can be a hidden `__dev/hud-kit.tsx` route excluded from production, or just a jest render test). Ask me which one I prefer if unsure.


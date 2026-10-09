## PHASE 5 — App shell (`src/app/_layout.tsx`)

- Mount `HudThemeProvider`. Render `HudBackground` once behind the navigator, and set every navigator's `contentStyle` / `sceneStyle` / `cardStyle` background to `transparent` so the HUD background shows through. Hide the default headers (`headerShown: false`). Screens render their own HUD headers.
- Use `SafeAreaView` / `useSafeAreaInsets` (react-native-safe-area-context). **Don't draw a fake status bar.** The real one is light-styled over the background.
- **Bottom navigation (HUD tab bar).** Four items: **Home**, **Lists**, **Search**, **Settings**, with lucide icons `House`, `LayoutList`, `Search`, `Settings`.
  - Container: `borderTopWidth: 1`, `borderTopColor: withAlpha(secondary, 0.25)`, background `rgba(0,0,0,0.5)`, paddingHorizontal 24, paddingTop 8, paddingBottom `8 + insets.bottom`. On top of the top border, add a 1px line that fades transparent → `primary` → transparent, inset 40dp from each side.
  - Item: a `HexIcon` size `sm` (active: tone `primary`, `active`; inactive: tone `secondary`, opacity 0.6) above a `navLabel` (gap 4).
  - `accessibilityRole="tab"` and `accessibilityState={{ selected }}`.
  - If the app already uses `Tabs`, pass this as a custom `tabBar`. If it uses only a `Stack`, **tell me in your Phase 0 report** and propose the smallest change: either convert the top-level routes to a `(tabs)` group, or render this bar as a persistent component in the root layout. Don't restructure routes without my approval. Map "Lists" to whichever route you identified in Phase 0.

---


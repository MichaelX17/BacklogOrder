# 2. Project & Tooling Configuration
> Tags: `Configuration` `Tooling` `Dependencies` `RN`
> **Tags:** `Config` `Tooling` `Dependencies` `Project`

### Dependencies (from `package.json`)
> Tags: `Dependencies` `Package-Json` `Configuration`

| Category | Packages | Purpose |
|---|---|---|
| Framework | `next@16.4.0`, `react@19`, `react-dom@19` | Web framework (not needed for RN target) |
| Styling | `tailwindcss@4.3.3`, `@tailwindcss/postcss@4.3.3`, `clsx@2.1.1`, `tailwind-merge@3.3.1`, `class-variance-authority@0.7.1` | CSS utility framework |
| Animation | `tw-animate-css@1.4.0` | CSS keyframe animations (for `animate-pulse` etc.) |
| shadcn/ui | `shadcn@^4.11.0` | Component library base |
| Icons | `lucide-react@1.16.0` | Icon set — **CRITICAL**: replace with `lucide-react-native` for RN |
| Analytics | `@vercel/analytics@1.6.1` | Web analytics (optional, web-only) |
| Base UI | `@base-ui/react@1.5.0` | Headless UI primitives (Button component) |

### React Native / Expo Equivalents
> Tags: `RN` `Expo` `Translation` `Equivalents`

| Web (example-design) | React Native / Expo |
|---|---|
| `next/image` (`Image` with `fill`) | `react-native` `Image` (uri-based, no layout fill) |
| `lucide-react` | `lucide-react-native` |
| `@base-ui/react` (Button primitive) | `react-native` `Pressable` |
| Tailwind CSS | `tailwind-react-native-classnames` (optional) **or** StyleSheet (recommended) |
| CSS `drop-shadow` filter | `react-native-svg` filter (limited) — **use a shadow approach** or skip |
| CSS `clip-path` polygon | `react-native-svg` `ClipPath` with `Polygon` |
| CSS `backdrop-blur` | `expo-blur` (`BlurView`) |
| CSS `color-mix()` | Compute in JS: `mixColor(primary, amount)` utility |
| CSS `@theme inline` custom properties | JS theme object passed via Context / props |
| CSS `@layer components` | N/A — apply styles via StyleSheet / inline styles |

### TypeScript Configuration
> Tags: `TypeScript` `Configuration` `tsconfig`

- `compilerOptions.jsx: "react-jsx"`
- `compilerOptions.paths: { "@/*": ["./*"] }` (Next.js) — in RN/Expo, use `"@/*": ["./src/*"]`
- `strict: true`
- For RN target, extend `expo/tsconfig.base`

### Build / Dev Commands (web)
> Tags: `Commands` `Build` `Dev` `Configuration`

```json
"dev": "next dev", "build": "next build", "start": "next start"
```

For RN target, use `expo start`.

## PHASE 1 — Dependencies and fonts

Install only what is missing, always with `npx expo install` so versions match the SDK:

```
npx expo install react-native-svg expo-linear-gradient expo-font @expo-google-fonts/orbitron @expo-google-fonts/rajdhani @react-native-masked-view/masked-view
npm install lucide-react-native
```

(`expo-image` is optional; use it for covers only if it's already installed. Skip `expo-blur`; the design works without blur.)

Fonts — exactly two families:

| Role | Family | Weights to load |
|---|---|---|
| **Display** (titles, labels, numbers, buttons, badges) | Orbitron | `Orbitron_500Medium`, `Orbitron_700Bold`, `Orbitron_900Black` |
| **Body** (game names, metadata, paragraphs, inputs) | Rajdhani | `Rajdhani_400Regular`, `Rajdhani_500Medium`, `Rajdhani_600SemiBold`, `Rajdhani_700Bold` |

- Load them in `src/app/_layout.tsx` with `useFonts`. Keep the splash screen up (`expo-splash-screen` `preventAutoHideAsync` / `hideAsync`) until the fonts are ready.
- With custom fonts on Android, **select the weight through `fontFamily`, never with `fontWeight`**.
- Set `<StatusBar style="light" />` (expo-status-bar) and make the root background the theme's `bgFrom`, so there's no white flash.


# 13. Asset Requirements
> **Tags:** `Assets` `Fonts` `Images`

### Fonts
> Tags: `Assets` `Fonts` `Orbitron` `Rajdhani`

Download and install these fonts in `assets/fonts/`:

| Font | Weights Used | Source |
|---|---|---|
| **Orbitron** | 500 (Medium), 700 (Bold), 900 (Black) | Google Fonts |
| **Rajdhani** | 400 (Regular), 500 (Medium), 600 (SemiBold), 700 (Bold) | Google Fonts |

Usage mapping:
- `--font-orbitron` → display header font (Orbitron)
- `--font-rajdhani` → body font (Rajdhani)
- `font-display` class → `fontFamily: 'Orbitron'`
- `font-sans` class → `fontFamily: 'Rajdhani'`

### Images
> Tags: `Assets` `Images` `Covers` `Icons`

| Asset | Path | Dimensions | Purpose |
|---|---|---|---|
| metro-2033.png | public/covers/ | 1024×1024 | Game cover |
| tormented-souls.png | public/covers/ | 1024×1024 | Game cover |
| resident-evil-4.png | public/covers/ | 1024×1024 | Game cover |
| devil-may-cry-5.png | public/covers/ | 1024×1024 | Game cover |
| icon.svg | public/ | — | App icon |
| apple-icon.png | public/ | — | iOS app icon |
| icon-light-32x32.png | public/ | 32×32 | Web favicon |
| icon-dark-32x32.png | public/ | 32×32 | Web favicon |

### Icons
> Tags: `Icons` `Assets` `lucide-react-native`

All icons come from `lucide-react`. In RN, use `lucide-react-native`:

```bash
npx expo install lucide-react-native
```

Import change:
```diff
- import { Search } from 'lucide-react'
+ import { Search } from 'lucide-react-native'
```

Icon rendering differences:
- Web: `<Search className="size-4 stroke-[2.25]" />`
- RN: `<Search size={16} strokeWidth={2.25} color={...} />`

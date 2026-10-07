# 14. Accessibility Specification
> **Tags:** `Accessibility` `A11y`

### ARIA Labels & Roles
> Tags: `Accessibility` `ARIA` `Roles` `Labels`

| Element | aria-* props | Notes |
|---|---|---|
| PhoneFrame root | `role="region"`, `aria-label={label}` | "label" = theme name + "theme preview" |
| Back button | `aria-label="Go back"` | Icon-only button |
| Filter group | `role="group"`, `aria-label="Filter by status"` | Group of 3 pills |
| Filter pills | `aria-pressed={active}` | Toggle button state |
| Sort button | `aria-label="Sort list"` | Icon-only button |
| Entry count divider | `aria-hidden="true"` | Decorative |
| Game cover | `alt={`${game.title} cover art`}` | Image alt text |
| Status badge dot | `aria-hidden="true"` | Decorative indicator |
| Metacritic value | `<span className="sr-only">Metacritic</span>` | Screen reader text |
| Bottom nav | `aria-label="Primary"` (nav) | Navigation landmark |
| Active nav link | `aria-current="page"` | Current page indicator |
| Theme swatches | `title={color}` | Color preview |
| Theme swatch sr-only | `sr-only` | Screen reader color name |

### Focus Management
> Tags: `Accessibility` `Focus` `Keyboard` `Ring`

- **Focus rings:** `outline-none` is used on buttons, but `focus-visible:` variants provide alternative visual feedback (brightness increase, background change). This is intentional — focus is indicated via state change, not a ring.
- **Keyboard navigation:** All interactive elements are semantic `<button>` elements. In RN, `Pressable` with `onAccessibilityTap` for screen readers.

### Screen Reader Text
> Tags: `Accessibility` `Screen-Reader` `sr-only`

- `sr-only` class hides content visually but keeps it available to screen readers
- Used for: "Metacritic" label, color hex values on swatches

## Quick visual reference (what the design looks like)

- A deep near-black background with a colored glow at the top (`bgTo`), a faint secondary-colored 22px grid that fades out toward the edges, and very subtle scanlines.
- Panels and cards use **beveled corners** (top-left and bottom-right cut) and **thin 1px neon outlines**, with a soft glow in the accent color. There are no rounded corners anywhere in the HUD.
- **Hexagons** are used for nav/back icons, **hex pills** (pointed capsules) for badges and chips, and **rotated diamonds** as bullets and status dots.
- **Orbitron**, uppercase with wide letter spacing, for every label, number, and button. **Rajdhani** for game names and readable text.
- Hierarchy: `primary` = the one most important thing (current pick, CTA, selected, score result). `secondary` = structure (borders, icons, grid, secondary buttons). Status colors only for game state.
- Primary CTA = beveled bar with a `primary → secondary` gradient and black uppercase text. Secondary CTA = dark beveled bar with a `secondary` outline and text.

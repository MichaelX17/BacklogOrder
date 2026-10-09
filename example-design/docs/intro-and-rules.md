# PROMPT — Apply the "Dark Fantasy Cyber HUD" design to BacklogOrder (React Native + Expo)

You are working on **BacklogOrder**, an offline-first Android app built with React Native + Expo (managed), Expo Router (`src/app/`), TypeScript strict, Zustand, expo-sqlite + Drizzle, and **StyleSheet** styling (NativeWind is intentionally NOT used). Your job is to apply a new visual design — a dark fantasy / cyber HUD look — to the existing app **without changing any business logic**.

Work in the phases below, **in order**. At the end of every phase:

1. Run the project's quality gates: `npx tsc --noEmit`, `npm run lint`, `npm run format:check`, `npm test`. All must pass with 0 errors and 0 warnings.
2. Write a short report: files created, files changed, anything you were unsure about.
3. **Stop and wait for my approval** before starting the next phase.

---

## 0. NON-NEGOTIABLE RULES (read before anything)

- **Do not change the ranking formula or its behavior.** `Score = NormalizedRating / Playtime`. NormalizedRating = Metacritic (0–100) if present, otherwise RAWG rating (0–5) × 20, otherwise no score (`null`). Playtime 0 → `Infinity`. `src/utils/score.ts`, `recommendation.ts`, `sorting.ts`, `filters.ts` are the source of truth. You may **only add** pure display helpers (e.g. `formatScore`) if they don't exist already.
- **Don't touch** `src/db/**`, `src/services/**`, or store logic, except to read or persist the selected theme through the existing `settings` table / repository pattern (Phase 2).
- Keep TypeScript strict: no `any`, no `as unknown as`, no `@ts-ignore`.
- One component per file, functional components, named exports (follow the existing conventions in `src/components/`).
- Keep `StyleSheet.create`. Do **not** install NativeWind/Tailwind.
- Don't rename routes or move screens. Restyle them in place.
- Every interactive element must keep (or gain) `accessibilityRole`, `accessibilityLabel` where the label isn't visible text, and `accessibilityState` (`selected`, `disabled`).
- Existing tests must keep passing. Update snapshot/text assertions only when the visible copy intentionally changed, and say so in your report.
- Look at the current file before you edit it. If a file or route I mention doesn't exist, **tell me** instead of inventing one.

---

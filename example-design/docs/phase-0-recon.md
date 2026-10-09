## PHASE 0 — Recon (no code changes)

Read and summarize:

- `src/theme/hudTheme.ts` (current tokens and how screens use them)
- `src/app/_layout.tsx` and every screen in `src/app/`. Tell me whether navigation is a `Stack`, `Tabs`, or both, and which route acts as the "Lists" overview (if any).
- `src/components/GameCard.tsx`, `StatusBadge.tsx`, `EmptyState.tsx`, `FranchiseGroup.tsx`
- `src/utils/score.ts`, `recommendation.ts`, `sorting.ts`. Confirm the function names and return shapes for: computing a score, the recommendation candidate list + `skipCount`, and the franchise-grouped list (group name, members sorted by `franchiseOrder`, group score = best member score, missing-order warning).
- `package.json`: check whether these are already installed: `react-native-svg`, `expo-linear-gradient`, `expo-font`, `@expo-google-fonts/orbitron`, `@expo-google-fonts/rajdhani`, `lucide-react-native`, `@react-native-masked-view/masked-view`, `expo-image`, `expo-blur`.

Deliverable: a mapping table "existing file → what changes in it", plus the list of new files you'll create. No edits yet.


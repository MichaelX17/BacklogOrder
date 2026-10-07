import type { ListEntry } from '@/types';
import { computeScore } from '@/utils/score';

interface ScoredEntry {
  entry: ListEntry;
  score: number;
}

export function findRecommendation(entries: ListEntry[], skipCount: number = 0): ListEntry | null {
  const scored: ScoredEntry[] = entries
    .map((entry) => ({
      entry,
      score: computeScore(entry.game),
    }))
    .filter((item): item is ScoredEntry => item.score !== null);

  const playing = scored
    .filter((item) => item.entry.status === 'Playing')
    .sort((a, b) => b.score - a.score);

  if (playing.length > 0) {
    const index = Math.min(skipCount, playing.length - 1);
    return playing[index]?.entry ?? null;
  }

  const backlog = scored
    .filter((item) => item.entry.status === 'Backlog')
    .sort((a, b) => b.score - a.score);

  if (backlog.length > 0) {
    const index = Math.min(skipCount, backlog.length - 1);
    return backlog[index]?.entry ?? null;
  }

  return null;
}

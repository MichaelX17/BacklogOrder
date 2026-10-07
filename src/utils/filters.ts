import type { GameStatus, ListEntry } from '@/types';
import { computeScore, normalizedRating } from '@/utils/score';

export interface FilterState {
  name: string;
  playtimeMin: number | null;
  playtimeMax: number | null;
  ratingMin: number | null;
  ratingMax: number | null;
  scoreMin: number | null;
  scoreMax: number | null;
  statuses: GameStatus[];
}

export const DEFAULT_FILTERS: FilterState = {
  name: '',
  playtimeMin: null,
  playtimeMax: null,
  ratingMin: null,
  ratingMax: null,
  scoreMin: null,
  scoreMax: null,
  statuses: [],
};

export function isFilterActive(filters: FilterState): boolean {
  return (
    filters.name.trim() !== '' ||
    filters.playtimeMin !== null ||
    filters.playtimeMax !== null ||
    filters.ratingMin !== null ||
    filters.ratingMax !== null ||
    filters.scoreMin !== null ||
    filters.scoreMax !== null ||
    filters.statuses.length > 0
  );
}

export function matchesName(gameName: string, filterName: string): boolean {
  if (filterName.trim() === '') {
    return true;
  }
  return gameName.toLowerCase().includes(filterName.toLowerCase());
}

export function matchesPlaytime(
  playtime: number | null,
  min: number | null,
  max: number | null,
): boolean {
  if (min === null && max === null) {
    return true;
  }
  if (playtime === null) {
    return false;
  }
  if (min !== null && playtime < min) {
    return false;
  }
  if (max !== null && playtime > max) {
    return false;
  }
  return true;
}

export function matchesRating(
  metacritic: number | null,
  rating: number | null,
  min: number | null,
  max: number | null,
): boolean {
  if (min === null && max === null) {
    return true;
  }
  const normalized = normalizedRating(metacritic, rating);
  if (normalized === null) {
    return false;
  }
  if (min !== null && normalized < min) {
    return false;
  }
  if (max !== null && normalized > max) {
    return false;
  }
  return true;
}

export function matchesScore(
  score: number | null,
  min: number | null,
  max: number | null,
): boolean {
  if (min === null && max === null) {
    return true;
  }
  if (score === null) {
    return false;
  }
  if (min !== null && score < min) {
    return false;
  }
  if (max !== null && score > max) {
    return false;
  }
  return true;
}

export function matchesStatus(status: GameStatus, statuses: GameStatus[]): boolean {
  if (statuses.length === 0) {
    return true;
  }
  return statuses.includes(status);
}

export function applyFilters(entries: ListEntry[], filters: FilterState): ListEntry[] {
  return entries.filter((entry) => {
    const game = entry.game;
    if (!matchesName(game.name, filters.name)) {
      return false;
    }
    if (!matchesPlaytime(game.playtime, filters.playtimeMin, filters.playtimeMax)) {
      return false;
    }
    if (!matchesRating(game.metacritic, game.rating, filters.ratingMin, filters.ratingMax)) {
      return false;
    }
    if (!matchesScore(computeScore(game), filters.scoreMin, filters.scoreMax)) {
      return false;
    }
    if (!matchesStatus(entry.status, filters.statuses)) {
      return false;
    }
    return true;
  });
}

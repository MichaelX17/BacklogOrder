import type { GameRow } from '@/db/schema';
import type { Game, GameStatus } from '@/types';

export function parseStringArray(value: string | null): string[] {
  if (value === null) {
    return [];
  }
  try {
    const parsed: unknown = JSON.parse(value);
    if (!Array.isArray(parsed)) {
      return [];
    }
    return parsed.filter((item): item is string => typeof item === 'string');
  } catch {
    return [];
  }
}

export function mapGameRow(row: GameRow): Game {
  return {
    id: row.id,
    rawgId: row.rawgId,
    name: row.name,
    cover: row.cover,
    metacritic: row.metacritic,
    rating: row.rating,
    playtime: row.playtime,
    genres: parseStringArray(row.genres),
    platforms: parseStringArray(row.platforms),
    tags: parseStringArray(row.tags),
    isMultiplayer: row.isMultiplayer,
    isManual: row.isManual,
    franchise: row.franchise,
    franchiseOrder: row.franchiseOrder,
    createdAt: row.createdAt,
  };
}

export function toGameStatus(value: string): GameStatus {
  switch (value) {
    case 'Backlog':
    case 'Playing':
    case 'Completed':
    case 'Dropped':
      return value;
    default:
      return 'Backlog';
  }
}

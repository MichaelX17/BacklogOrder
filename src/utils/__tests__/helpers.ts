import type { Game } from '@/types';
import { computeScore } from '../score';

export function makeGame(overrides: Partial<Game> = {}): Game {
  return {
    id: crypto.randomUUID(),
    rawgId: null,
    name: 'Game',
    cover: null,
    metacritic: null,
    rating: null,
    playtime: null,
    genres: [],
    platforms: [],
    tags: [],
    isMultiplayer: false,
    isManual: false,
    franchise: null,
    franchiseOrder: null,
    createdAt: 0,
    ...overrides,
  };
}

export function scoreOf(game: Game): number | null {
  return computeScore(game);
}

describe('test helpers', () => {
  it('exposes the shared game and score helpers', () => {
    expect(typeof makeGame).toBe('function');
    expect(typeof scoreOf).toBe('function');
  });
});

import {
  applyFilters,
  DEFAULT_FILTERS,
  matchesName,
  matchesPlaytime,
  matchesRating,
  matchesScore,
  matchesStatus,
} from '../filters';
import type { Game, ListEntry } from '@/types';

function makeGame(overrides: Partial<Game> = {}): Game {
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

function makeEntry(partial: Partial<ListEntry> = {}): ListEntry {
  return {
    listId: 'list-1',
    game: makeGame(),
    status: 'Backlog',
    addedAt: 0,
    ...partial,
  };
}

describe('filter helpers', () => {
  it('matches names case-insensitively', () => {
    expect(matchesName('Hollow Knight', 'hollow')).toBe(true);
    expect(matchesName('Hollow Knight', 'soul')).toBe(false);
  });

  it('matches playtime ranges and rating ranges', () => {
    expect(matchesPlaytime(12, 10, 20)).toBe(true);
    expect(matchesPlaytime(8, 10, 20)).toBe(false);
    expect(matchesPlaytime(15, null, 10)).toBe(false);

    expect(matchesRating(80, 4.2, 70, 90)).toBe(true);
    expect(matchesRating(30, 2.5, 70, 90)).toBe(false);
  });

  it('matches score and status values', () => {
    expect(matchesScore(8.5, 7, 9)).toBe(true);
    expect(matchesScore(6.5, 7, 9)).toBe(false);
    expect(matchesStatus('Playing', ['Backlog', 'Playing'])).toBe(true);
    expect(matchesStatus('Completed', ['Backlog', 'Playing'])).toBe(false);
  });

  it('applies combined filters without altering unfiltered items', () => {
    const entries = [
      makeEntry({
        game: makeGame({ name: 'Hollow Knight', metacritic: 90, rating: 4.5, playtime: 12 }),
        status: 'Playing',
      }),
      makeEntry({
        game: makeGame({ name: 'Stardew Valley', metacritic: 89, rating: 4.7, playtime: 25 }),
        status: 'Backlog',
      }),
      makeEntry({
        game: makeGame({ name: 'Celeste', metacritic: 90, rating: 4.8, playtime: 7 }),
        status: 'Completed',
      }),
    ];

    const filtered = applyFilters(entries, {
      ...DEFAULT_FILTERS,
      name: 'val',
      playtimeMin: 10,
      ratingMin: 4,
      scoreMin: 3,
      statuses: ['Playing', 'Backlog'],
    });

    expect(filtered).toHaveLength(1);
    expect(filtered[0]?.game.name).toBe('Stardew Valley');
  });
});

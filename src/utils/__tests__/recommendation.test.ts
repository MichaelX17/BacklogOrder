import { findRecommendation } from '../recommendation';
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

describe('findRecommendation', () => {
  it('prefers the highest-scoring Playing game before Backlog', () => {
    const entry = makeEntry({
      game: makeGame({ name: 'Playing game', metacritic: 90, playtime: 8 }),
      status: 'Playing',
    });
    const backlog = makeEntry({
      game: makeGame({ name: 'Backlog game', metacritic: 85, playtime: 10 }),
      status: 'Backlog',
    });

    expect(findRecommendation([backlog, entry])?.game.name).toBe('Playing game');
  });

  it('skips to the next ranked candidate when requested', () => {
    const first = makeEntry({
      game: makeGame({ name: 'First', metacritic: 95, playtime: 10 }),
      status: 'Backlog',
    });
    const second = makeEntry({
      game: makeGame({ name: 'Second', metacritic: 90, playtime: 12 }),
      status: 'Backlog',
    });

    expect(findRecommendation([first, second], 0)?.game.name).toBe('First');
    expect(findRecommendation([first, second], 1)?.game.name).toBe('Second');
  });

  it('returns null when there are no scored entries', () => {
    const entry = makeEntry({
      game: makeGame({ name: 'No score', metacritic: null, rating: null, playtime: null }),
    });

    expect(findRecommendation([entry])).toBeNull();
  });
});

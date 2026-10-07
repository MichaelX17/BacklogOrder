import type { Game } from '@/types';
import { buildFranchiseGroups, sortGames } from '../sorting';
import type { ScoredGame } from '../sorting';

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

function makeScoredGame(score: number | null, overrides: Partial<Game> = {}): ScoredGame {
  return { ...makeGame(overrides), score };
}

describe('sortGames', () => {
  it('orders games without franchise by score descending', () => {
    const groups = sortGames([
      makeGame({ name: 'Low', metacritic: 50, playtime: 10 }),
      makeGame({ name: 'High', metacritic: 90, playtime: 10 }),
      makeGame({ name: 'Mid', metacritic: 70, playtime: 10 }),
    ]);

    const names = groups.map((group) => group.games[0]?.name);
    expect(names).toEqual(['High', 'Mid', 'Low']);
    expect(groups.every((group) => group.franchise === null)).toBe(true);
  });

  it('places games with no score at the end, sorted alphabetically', () => {
    const groups = sortGames([
      makeGame({ name: 'Zeta', metacritic: null, rating: null, playtime: null }),
      makeGame({ name: 'Scored', metacritic: 80, playtime: 10 }),
      makeGame({ name: 'Alpha', metacritic: null, rating: null, playtime: 0 }),
    ]);

    const names = groups.map((group) => group.games[0]?.name);
    expect(names).toEqual(['Scored', 'Alpha', 'Zeta']);
  });

  it('orders franchise groups by their highest-scoring member', () => {
    const groups = sortGames([
      makeGame({
        name: 'Saga A 1',
        franchise: 'Saga A',
        franchiseOrder: 1,
        metacritic: 60,
        playtime: 10,
      }),
      makeGame({
        name: 'Saga B 1',
        franchise: 'Saga B',
        franchiseOrder: 1,
        metacritic: 90,
        playtime: 10,
      }),
    ]);

    expect(groups.map((group) => group.franchise)).toEqual(['Saga B', 'Saga A']);
    expect(groups[0]?.positionScore).toBe(9);
  });
});

describe('franchise grouping', () => {
  it('groups a single franchise and sorts members by franchiseOrder ascending', () => {
    const groups = sortGames([
      makeGame({
        name: 'Second',
        franchise: 'Saga',
        franchiseOrder: 2,
        metacritic: 80,
        playtime: 10,
      }),
      makeGame({
        name: 'First',
        franchise: 'Saga',
        franchiseOrder: 1,
        metacritic: 80,
        playtime: 10,
      }),
    ]);

    expect(groups).toHaveLength(1);
    const group = groups[0];
    expect(group?.franchise).toBe('Saga');
    expect(group?.games.map((game) => game.name)).toEqual(['First', 'Second']);
    expect(group?.hasMissingFranchiseOrder).toBe(false);
  });

  it('handles franchiseOrder gaps while keeping ascending order', () => {
    const groups = sortGames([
      makeGame({
        name: 'Fifth',
        franchise: 'Saga',
        franchiseOrder: 5,
        metacritic: 80,
        playtime: 10,
      }),
      makeGame({
        name: 'First',
        franchise: 'Saga',
        franchiseOrder: 1,
        metacritic: 80,
        playtime: 10,
      }),
      makeGame({
        name: 'Third',
        franchise: 'Saga',
        franchiseOrder: 3,
        metacritic: 80,
        playtime: 10,
      }),
    ]);

    const group = groups[0];
    expect(group?.games.map((game) => game.franchiseOrder)).toEqual([1, 3, 5]);
  });

  it('renders games with missing franchiseOrder last and flags the group', () => {
    const groups = sortGames([
      makeGame({
        name: 'Unknown Order',
        franchise: 'Saga',
        franchiseOrder: null,
        metacritic: 90,
        playtime: 10,
      }),
      makeGame({
        name: 'Second',
        franchise: 'Saga',
        franchiseOrder: 2,
        metacritic: 80,
        playtime: 10,
      }),
      makeGame({
        name: 'Also Unknown',
        franchise: 'Saga',
        franchiseOrder: null,
        metacritic: 70,
        playtime: 10,
      }),
    ]);

    const group = groups[0];
    expect(group?.hasMissingFranchiseOrder).toBe(true);
    const names = group?.games.map((game) => game.name);
    expect(names).toEqual(['Second', 'Unknown Order', 'Also Unknown']);
  });

  it('sorts members without franchiseOrder by score descending then name', () => {
    const groups = sortGames([
      makeGame({
        name: 'Zeta Unknown',
        franchise: 'Saga',
        franchiseOrder: null,
        metacritic: 80,
        playtime: 10,
      }),
      makeGame({
        name: 'Alpha Unknown',
        franchise: 'Saga',
        franchiseOrder: null,
        metacritic: 80,
        playtime: 10,
      }),
    ]);

    const group = groups[0];
    expect(group?.games.map((game) => game.name)).toEqual(['Alpha Unknown', 'Zeta Unknown']);
  });

  it('keeps unscored games as singletons at the end even with a franchise', () => {
    const groups = sortGames([
      makeGame({
        name: 'Unscored Saga',
        franchise: 'Saga',
        metacritic: null,
        rating: null,
        playtime: null,
      }),
      makeGame({ name: 'Scored Solo', metacritic: 80, playtime: 10 }),
    ]);

    expect(groups.map((group) => group.games[0]?.name)).toEqual(['Scored Solo', 'Unscored Saga']);
    expect(groups[1]?.franchise).toBeNull();
  });
});

describe('buildFranchiseGroups', () => {
  it('returns an empty array for no games', () => {
    expect(buildFranchiseGroups([])).toEqual([]);
  });

  it('gives each franchise-less game its own singleton group', () => {
    const groups = buildFranchiseGroups([
      makeScoredGame(8, { name: 'A' }),
      makeScoredGame(6, { name: 'B' }),
    ]);

    expect(groups).toHaveLength(2);
    expect(groups.map((group) => group.key)).toEqual([
      expect.stringContaining('game:'),
      expect.stringContaining('game:'),
    ]);
  });
});

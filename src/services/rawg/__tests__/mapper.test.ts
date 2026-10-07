import { isMultiplayerGame, mapRawgGame } from '../mapper';
import type { RawgGame } from '../types';

function makeRawgGame(overrides: Partial<RawgGame> = {}): RawgGame {
  return {
    id: 1,
    slug: 'game',
    name: 'Game',
    playtime: 20,
    metacritic: 90,
    rating: 4.5,
    background_image: 'https://example.com/cover.jpg',
    genres: [{ name: 'RPG' }],
    platforms: [{ platform: { name: 'PC' } }],
    tags: [{ name: 'Singleplayer' }],
    ...overrides,
  };
}

describe('isMultiplayerGame', () => {
  it('detects the Multiplayer tag', () => {
    const game = makeRawgGame({
      tags: [{ name: 'Multiplayer' }],
    });
    expect(isMultiplayerGame(game)).toBe(true);
  });

  it('detects the Co-op tag', () => {
    const game = makeRawgGame({
      tags: [{ name: 'Co-op' }],
    });
    expect(isMultiplayerGame(game)).toBe(true);
  });

  it('detects the Massively Multiplayer tag', () => {
    const game = makeRawgGame({
      tags: [{ name: 'Massively Multiplayer' }],
    });
    expect(isMultiplayerGame(game)).toBe(true);
  });

  it('matches tags case-insensitively', () => {
    const game = makeRawgGame({
      tags: [{ name: 'multiplayer' }],
    });
    expect(isMultiplayerGame(game)).toBe(true);
  });

  it('returns false for single-player games', () => {
    const game = makeRawgGame({
      tags: [{ name: 'Singleplayer' }, { name: 'Story Rich' }],
    });
    expect(isMultiplayerGame(game)).toBe(false);
  });
});

describe('mapRawgGame', () => {
  it('maps every field', () => {
    expect(mapRawgGame(makeRawgGame())).toEqual({
      rawgId: 1,
      name: 'Game',
      cover: 'https://example.com/cover.jpg',
      metacritic: 90,
      rating: 4.5,
      playtime: 20,
      genres: ['RPG'],
      platforms: ['PC'],
      tags: ['Singleplayer'],
      isMultiplayer: false,
      isManual: false,
    });
  });

  it('returns null for multiplayer games', () => {
    const game = makeRawgGame({
      tags: [{ name: 'Co-op' }],
    });
    expect(mapRawgGame(game)).toBeNull();
  });

  it('maps missing optional fields to null and empty arrays', () => {
    const game = makeRawgGame({
      playtime: null,
      metacritic: null,
      rating: null,
      background_image: null,
      genres: [],
      platforms: [],
      tags: [],
    });

    expect(mapRawgGame(game)).toEqual({
      rawgId: 1,
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
    });
  });
});

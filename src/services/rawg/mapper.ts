import type { NewGame } from '@/types';
import type { RawgGame } from '@/services/rawg/types';

const MULTIPLAYER_TAG_NAMES: ReadonlySet<string> = new Set([
  'multiplayer',
  'co-op',
  'massively multiplayer',
]);

export function isMultiplayerGame(game: RawgGame): boolean {
  return game.tags.some((tag) => MULTIPLAYER_TAG_NAMES.has(tag.name.toLowerCase()));
}

export function mapRawgGame(game: RawgGame): NewGame | null {
  if (isMultiplayerGame(game)) {
    return null;
  }

  return {
    rawgId: game.id,
    name: game.name,
    cover: game.background_image ?? null,
    metacritic: game.metacritic ?? null,
    rating: game.rating ?? null,
    playtime: game.playtime ?? null,
    genres: game.genres.map((genre) => genre.name),
    platforms: game.platforms.map((platform) => platform.platform.name),
    tags: game.tags.map((tag) => tag.name),
    isMultiplayer: false,
    isManual: false,
  };
}

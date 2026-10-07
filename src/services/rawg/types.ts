export interface RawgNamedEntity {
  name: string;
  slug?: string;
}

export interface RawgPlatform {
  platform: RawgNamedEntity;
}

export interface RawgGame {
  id: number;
  slug: string;
  name: string;
  playtime: number | null;
  metacritic: number | null;
  rating: number | null;
  background_image: string | null;
  genres: RawgNamedEntity[];
  platforms: RawgPlatform[];
  tags: RawgNamedEntity[];
}

export interface RawgGamesResponse {
  count: number;
  next: string | null;
  results: RawgGame[];
}

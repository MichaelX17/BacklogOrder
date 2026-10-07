export type GameStatus = 'Backlog' | 'Playing' | 'Completed' | 'Dropped';

export const GAME_STATUSES: readonly GameStatus[] = ['Backlog', 'Playing', 'Completed', 'Dropped'];

export interface Game {
  id: string;
  rawgId: number | null;
  name: string;
  cover: string | null;
  metacritic: number | null;
  rating: number | null;
  playtime: number | null;
  genres: string[];
  platforms: string[];
  tags: string[];
  isMultiplayer: boolean;
  isManual: boolean;
  franchise: string | null;
  franchiseOrder: number | null;
  createdAt: number;
}

export interface NewGame {
  rawgId?: number | null;
  name: string;
  cover?: string | null;
  metacritic?: number | null;
  rating?: number | null;
  playtime?: number | null;
  genres?: string[];
  platforms?: string[];
  tags?: string[];
  isMultiplayer?: boolean;
  isManual?: boolean;
  franchise?: string | null;
  franchiseOrder?: number | null;
}

export interface GameList {
  id: string;
  name: string;
  createdAt: number;
  updatedAt: number;
}

export interface ListEntry {
  game: Game;
  status: GameStatus;
  addedAt: number;
}

export interface ListEntrySummary {
  listId: string;
  status: GameStatus;
}

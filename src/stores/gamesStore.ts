import { create } from 'zustand';

import { initializeDatabase } from '@/db/client';
import { gamesRepo, listGamesRepo } from '@/db/repositories';
import type { GameStatus, ListEntry, NewGame } from '@/types';

interface GamesState {
  entriesByList: Record<string, ListEntry[]>;
  allEntries: ListEntry[];
  loadEntriesForList: (listId: string) => void;
  loadAllEntries: () => void;
  addGameToList: (listId: string, game: NewGame, status: GameStatus) => void;
  removeGameFromList: (listId: string, gameId: string) => void;
  updateStatus: (listId: string, gameId: string, status: GameStatus) => void;
}

export const useGamesStore = create<GamesState>((set, get) => ({
  entriesByList: {},
  allEntries: [],

  loadEntriesForList: (listId) => {
    initializeDatabase();
    set({
      entriesByList: {
        ...get().entriesByList,
        [listId]: listGamesRepo.getEntriesForList(listId),
      },
    });
  },

  loadAllEntries: () => {
    initializeDatabase();
    set({ allEntries: listGamesRepo.getAllEntries() });
  },

  addGameToList: (listId, game, status) => {
    initializeDatabase();
    const existing =
      game.rawgId !== null && game.rawgId !== undefined
        ? gamesRepo.findByRawgId(game.rawgId)
        : null;
    const storedGame = existing ?? gamesRepo.create(game);
    listGamesRepo.addGameToList(listId, storedGame.id, status);
    set({
      entriesByList: {
        ...get().entriesByList,
        [listId]: listGamesRepo.getEntriesForList(listId),
      },
      allEntries: listGamesRepo.getAllEntries(),
    });
  },

  removeGameFromList: (listId, gameId) => {
    listGamesRepo.removeGameFromList(listId, gameId);
    set({
      entriesByList: {
        ...get().entriesByList,
        [listId]: listGamesRepo.getEntriesForList(listId),
      },
      allEntries: listGamesRepo.getAllEntries(),
    });
  },

  updateStatus: (listId, gameId, status) => {
    listGamesRepo.updateStatus(listId, gameId, status);
    set({
      entriesByList: {
        ...get().entriesByList,
        [listId]: listGamesRepo.getEntriesForList(listId),
      },
      allEntries: listGamesRepo.getAllEntries(),
    });
  },
}));

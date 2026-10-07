import { create } from 'zustand';

import { initializeDatabase } from '@/db/client';
import { listsRepo } from '@/db/repositories';
import type { GameList } from '@/types';

interface ListsState {
  lists: GameList[];
  isLoaded: boolean;
  loadLists: () => void;
  createList: (name: string) => GameList;
  renameList: (id: string, name: string) => void;
  deleteList: (id: string) => void;
}

export const useListsStore = create<ListsState>((set, get) => ({
  lists: [],
  isLoaded: false,

  loadLists: () => {
    initializeDatabase();
    set({ lists: listsRepo.getAll(), isLoaded: true });
  },

  createList: (name) => {
    initializeDatabase();
    const created = listsRepo.create(name);
    set({ lists: [...get().lists, created] });
    return created;
  },

  renameList: (id, name) => {
    listsRepo.rename(id, name);
    const updatedAt = Date.now();
    set({
      lists: get().lists.map((list) => (list.id === id ? { ...list, name, updatedAt } : list)),
    });
  },

  deleteList: (id) => {
    listsRepo.remove(id);
    set({ lists: get().lists.filter((list) => list.id !== id) });
  },
}));

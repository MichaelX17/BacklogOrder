import * as FileSystem from 'expo-file-system/legacy';
import * as Sharing from 'expo-sharing';

import { useGamesStore } from '@/stores/gamesStore';
import { useListsStore } from '@/stores/listsStore';

export interface ExportedGame {
  rawgId: number | null;
  name: string;
  cover: string | null;
  metacritic: number | null;
  rating: number | null;
  playtime: number | null;
  genres: string[];
  platforms: string[];
  franchise: string | null;
  franchiseOrder: number | null;
  status: string;
  isManual: boolean;
}

export interface ExportedList {
  id: string;
  name: string;
  createdAt: number;
  games: ExportedGame[];
}

export interface ExportData {
  version: number;
  exportedAt: number;
  lists: ExportedList[];
}

export async function exportList(listId: string): Promise<string> {
  const lists = useListsStore.getState().lists;
  const allEntries = useGamesStore.getState().allEntries;
  
  const list = lists.find((l) => l.id === listId);
  if (!list) {
    throw new Error('List not found');
  }
  
  const listEntries = allEntries.filter((entry) => entry.listId === listId);
  const exportedList: ExportedList = {
    id: list.id,
    name: list.name,
    createdAt: list.createdAt,
    games: listEntries.map((entry) => {
      const game = entry.game;
      return {
        rawgId: game.rawgId,
        name: game.name,
        cover: game.cover,
        metacritic: game.metacritic,
        rating: game.rating,
        playtime: game.playtime,
        genres: game.genres,
        platforms: game.platforms,
        franchise: game.franchise,
        franchiseOrder: game.franchiseOrder,
        status: entry.status,
        isManual: game.isManual,
      };
    }),
  };
  
  const data: ExportData = {
    version: 1,
    exportedAt: Date.now(),
    lists: [exportedList],
  };
  
  return JSON.stringify(data, null, 2);
}

export async function exportAll(): Promise<string> {
  const lists = useListsStore.getState().lists;
  const allEntries = useGamesStore.getState().allEntries;
  
  const exportedLists: ExportedList[] = lists.map((list) => {
    const listEntries = allEntries.filter((entry) => entry.listId === list.id);
    return {
      id: list.id,
      name: list.name,
      createdAt: list.createdAt,
      games: listEntries.map((entry) => {
        const game = entry.game;
        return {
          rawgId: game.rawgId,
          name: game.name,
          cover: game.cover,
          metacritic: game.metacritic,
          rating: game.rating,
          playtime: game.playtime,
          genres: game.genres,
          platforms: game.platforms,
          franchise: game.franchise,
          franchiseOrder: game.franchiseOrder,
          status: entry.status,
          isManual: game.isManual,
        };
      }),
    };
  });
  
  const data: ExportData = {
    version: 1,
    exportedAt: Date.now(),
    lists: exportedLists,
  };
  
  return JSON.stringify(data, null, 2);
}

export async function shareData(data: string, filename: string): Promise<void> {
  const filenameWithExtension = `${filename}.json`;

  const fileUri = `${FileSystem.documentDirectory ?? ''}${filenameWithExtension}`;
  await FileSystem.writeAsStringAsync(fileUri, data, {
    encoding: FileSystem.EncodingType.UTF8,
  });

  if (await Sharing.isAvailableAsync()) {
    await Sharing.shareAsync(fileUri, {
      mimeType: 'application/json',
      dialogTitle: 'Export Game Lists',
      UTI: 'public.json',
    });
  } else {
    throw new Error('Sharing is not available on this device');
  }
}

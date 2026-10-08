import * as DocumentPicker from 'expo-document-picker';
import * as FileSystem from 'expo-file-system';

export interface ImportedGame {
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

export interface ImportedList {
  id: string;
  name: string;
  createdAt: number;
  games: ImportedGame[];
}

export interface ImportData {
  version: number;
  exportedAt: number;
  lists: ImportedList[];
}

function validateImportData(data: unknown): ImportData {
  if (!data || typeof data !== 'object') {
    throw new Error('Invalid import data: expected an object');
  }
  
  const obj = data as Record<string, unknown>;
  
  if (typeof obj.version !== 'number' || obj.version !== 1) {
    throw new Error('Invalid import data: version must be 1');
  }
  
  if (typeof obj.exportedAt !== 'number' || isNaN(obj.exportedAt)) {
    throw new Error('Invalid import data: exportedAt must be a number');
  }
  
  if (!Array.isArray(obj.lists)) {
    throw new Error('Invalid import data: lists must be an array');
  }
  
  for (const list of obj.lists) {
    if (!list || typeof list !== 'object') {
      throw new Error('Invalid list in import data');
    }
    
    if (typeof list.id !== 'string' || typeof list.name !== 'string') {
      throw new Error('Invalid list: id and name must be strings');
    }
    
    if (typeof list.createdAt !== 'number') {
      throw new Error('Invalid list: createdAt must be a number');
    }
    
    if (!Array.isArray(list.games)) {
      throw new Error('Invalid list: games must be an array');
    }
    
    for (const game of list.games) {
      if (!game || typeof game !== 'object') {
        throw new Error('Invalid game in list');
      }
      
      if (typeof game.name !== 'string') {
        throw new Error('Invalid game: name must be a string');
      }
      
      if (game.rawgId !== null && (typeof game.rawgId !== 'number' || !Number.isInteger(game.rawgId) || game.rawgId <= 0)) {
        throw new Error('Invalid game: rawgId must be a positive integer or null');
      }
      
      if (game.metacritic !== null && (typeof game.metacritic !== 'number' || !Number.isInteger(game.metacritic) || game.metacritic < 0 || game.metacritic > 100)) {
        throw new Error('Invalid game: metacritic must be an integer between 0 and 100 or null');
      }
      
      if (game.rating !== null && (typeof game.rating !== 'number' || game.rating < 0 || game.rating > 5)) {
        throw new Error('Invalid game: rating must be a number between 0 and 5 or null');
      }
      
      if (game.playtime !== null && (typeof game.playtime !== 'number' || game.playtime < 0)) {
        throw new Error('Invalid game: playtime must be a non-negative number or null');
      }
      
      if (game.franchiseOrder !== null && (!Number.isInteger(game.franchiseOrder) || game.franchiseOrder < 0)) {
        throw new Error('Invalid game: franchiseOrder must be a non-negative integer or null');
      }
      
      if (typeof game.isManual !== 'boolean') {
        throw new Error('Invalid game: isManual must be a boolean');
      }
      
      if (!['Backlog', 'Playing', 'Completed', 'Dropped'].includes(game.status)) {
        throw new Error(`Invalid game: status must be one of: Backlog, Playing, Completed, Dropped`);
      }
    }
  }
  
  return obj as unknown as ImportData;
}

export async function importData(): Promise<void> {
  try {
    const result = await DocumentPicker.getDocumentAsync({
      type: 'application/json',
      copyToCacheDirectory: true,
    });

    if (result.canceled) {
      throw new Error('Import canceled by user');
    }

    if (!result.assets || result.assets.length === 0) {
      throw new Error('No file selected');
    }

    const fileUri = result.assets[0]?.uri;
    if (fileUri === undefined) {
      throw new Error('No file selected');
    }

    const fileContent = await FileSystem.readAsStringAsync(fileUri, {
      encoding: FileSystem.EncodingType.UTF8,
    });
    
    const parsedData = JSON.parse(fileContent);
    const validatedData = validateImportData(parsedData);
    
    console.log('Import validation successful:', validatedData);
    
    return;
  } catch (error) {
    if (error instanceof Error) {
      throw error;
    }
    throw new Error('Failed to import data');
  }
}

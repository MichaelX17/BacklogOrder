import { Platform } from 'react-native';

type TableName = 'lists' | 'games' | 'list_games' | 'settings';

const STORAGE_KEY = 'backlogorder-web-db-v1';

function isWebStorageAvailable(): boolean {
  return Platform.OS === 'web' && typeof window !== 'undefined' && typeof window.localStorage !== 'undefined';
}

function readStore(): Record<string, unknown[]> {
  if (!isWebStorageAvailable()) {
    return {};
  }

  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return {};
  }

  try {
    const parsed = JSON.parse(raw) as Record<string, unknown>;
    return Object.fromEntries(
      Object.entries(parsed).map(([key, value]) => [key, Array.isArray(value) ? value : []]),
    );
  } catch {
    return {};
  }
}

function writeStore(store: Record<string, unknown[]>): void {
  if (!isWebStorageAvailable()) {
    return;
  }

  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(store));
}

export function readTable<T>(table: TableName): T[] {
  return readStore()[table] as T[] | undefined ?? [];
}

export function writeTable<T>(table: TableName, rows: T[]): void {
  const store = readStore();
  store[table] = rows;
  writeStore(store);
}

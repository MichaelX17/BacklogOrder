import { sql } from 'drizzle-orm';
import { drizzle } from 'drizzle-orm/expo-sqlite';
import * as expoSQLite from 'expo-sqlite';
import { Platform } from 'react-native';

import { runMigrations } from './migrations/runner';
import * as schema from './schema';

export const isWeb = Platform.OS === 'web';

const sqlite = isWeb ? null : expoSQLite.openDatabaseSync('backlogorder.db');

export const db = sqlite === null ? null : drizzle(sqlite, { schema });

let initialized = false;

export function initializeDatabase(): void {
  if (initialized || isWeb) {
    return;
  }
  db?.run(sql`PRAGMA foreign_keys = ON;`);
  runMigrations(db as never);
  initialized = true;
}

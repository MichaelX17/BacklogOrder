import { sql } from 'drizzle-orm';
import { drizzle } from 'drizzle-orm/expo-sqlite';
import * as expoSQLite from 'expo-sqlite';

import { runMigrations } from './migrations/runner';
import * as schema from './schema';

const sqlite = expoSQLite.openDatabaseSync('backlogorder.db');

export const db = drizzle(sqlite, { schema });

let initialized = false;

export function initializeDatabase(): void {
  if (initialized) {
    return;
  }
  db.run(sql`PRAGMA foreign_keys = ON;`);
  runMigrations(db);
  initialized = true;
}

import { sql } from 'drizzle-orm';
import type { ExpoSQLiteDatabase } from 'drizzle-orm/expo-sqlite';

import { migrationV1, type Migration } from './v1';

const migrations: readonly Migration[] = [migrationV1];

interface MigrationRecord {
  version: number;
}

export function runMigrations<TSchema extends Record<string, unknown>>(
  db: ExpoSQLiteDatabase<TSchema>,
): void {
  db.run(
    sql`CREATE TABLE IF NOT EXISTS "migrations" ("version" integer PRIMARY KEY NOT NULL, "appliedAt" integer NOT NULL);`,
  );

  const appliedRows = db.all<MigrationRecord>(sql`SELECT "version" FROM "migrations";`);
  const appliedVersions = new Set(appliedRows.map((row) => row.version));

  for (const migration of migrations) {
    if (appliedVersions.has(migration.version)) {
      continue;
    }
    db.transaction((tx) => {
      for (const statement of migration.statements) {
        tx.run(sql.raw(statement));
      }
      tx.run(
        sql`INSERT INTO "migrations" ("version", "appliedAt") VALUES (${migration.version}, ${Date.now()});`,
      );
    });
  }
}

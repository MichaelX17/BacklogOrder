export interface Migration {
  version: number;
  name: string;
  statements: readonly string[];
}

export const migrationV1: Migration = {
  version: 1,
  name: 'create-tables',
  statements: [
    `CREATE TABLE IF NOT EXISTS "lists" (
      "id" text PRIMARY KEY NOT NULL,
      "name" text NOT NULL,
      "createdAt" integer NOT NULL,
      "updatedAt" integer NOT NULL
    );`,
    `CREATE TABLE IF NOT EXISTS "games" (
      "id" text PRIMARY KEY NOT NULL,
      "rawgId" integer UNIQUE,
      "name" text NOT NULL,
      "cover" text,
      "metacritic" integer,
      "rating" real,
      "playtime" real,
      "genres" text,
      "platforms" text,
      "tags" text,
      "isMultiplayer" integer NOT NULL DEFAULT 0,
      "isManual" integer NOT NULL DEFAULT 0,
      "franchise" text,
      "franchiseOrder" integer,
      "createdAt" integer NOT NULL
    );`,
    `CREATE TABLE IF NOT EXISTS "list_games" (
      "listId" text NOT NULL,
      "gameId" text NOT NULL,
      "status" text NOT NULL,
      "addedAt" integer NOT NULL,
      PRIMARY KEY ("listId", "gameId"),
      FOREIGN KEY ("listId") REFERENCES "lists"("id") ON DELETE CASCADE,
      FOREIGN KEY ("gameId") REFERENCES "games"("id") ON DELETE CASCADE
    );`,
    `CREATE TABLE IF NOT EXISTS "settings" (
      "key" text PRIMARY KEY NOT NULL,
      "value" text NOT NULL
    );`,
  ],
};

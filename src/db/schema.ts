import { integer, primaryKey, real, sqliteTable, text } from 'drizzle-orm/sqlite-core';

export const listsTable = sqliteTable('lists', {
  id: text('id').primaryKey(),
  name: text('name').notNull(),
  createdAt: integer('createdAt', { mode: 'number' }).notNull(),
  updatedAt: integer('updatedAt', { mode: 'number' }).notNull(),
});

export const gamesTable = sqliteTable('games', {
  id: text('id').primaryKey(),
  rawgId: integer('rawgId', { mode: 'number' }).unique(),
  name: text('name').notNull(),
  cover: text('cover'),
  metacritic: integer('metacritic', { mode: 'number' }),
  rating: real('rating'),
  playtime: real('playtime'),
  genres: text('genres'),
  platforms: text('platforms'),
  tags: text('tags'),
  isMultiplayer: integer('isMultiplayer', { mode: 'boolean' }).notNull().default(false),
  isManual: integer('isManual', { mode: 'boolean' }).notNull().default(false),
  franchise: text('franchise'),
  franchiseOrder: integer('franchiseOrder', { mode: 'number' }),
  createdAt: integer('createdAt', { mode: 'number' }).notNull(),
});

export const listGamesTable = sqliteTable(
  'list_games',
  {
    listId: text('listId')
      .notNull()
      .references(() => listsTable.id, { onDelete: 'cascade' }),
    gameId: text('gameId')
      .notNull()
      .references(() => gamesTable.id, { onDelete: 'cascade' }),
    status: text('status').notNull(),
    addedAt: integer('addedAt', { mode: 'number' }).notNull(),
  },
  (table) => [primaryKey({ columns: [table.listId, table.gameId] })],
);

export const settingsTable = sqliteTable('settings', {
  key: text('key').primaryKey(),
  value: text('value').notNull(),
});

export type ListRow = typeof listsTable.$inferSelect;
export type GameRow = typeof gamesTable.$inferSelect;
export type ListGameRow = typeof listGamesTable.$inferSelect;
export type SettingsRow = typeof settingsTable.$inferSelect;

import { and, asc, eq } from 'drizzle-orm';

import { db, isWeb } from '@/db/client';
import { gamesTable, listGamesTable } from '@/db/schema';
import { mapGameRow, toGameStatus } from '@/db/repositories/mappers';
import { readTable, writeTable } from '@/db/webStorage';
import type { GameStatus, ListEntry, ListEntrySummary } from '@/types';

type StoredListGame = {
  listId: string;
  gameId: string;
  status: GameStatus;
  addedAt: number;
};

export const listGamesRepo = {
  addGameToList(listId: string, gameId: string, status: GameStatus): void {
    if (isWeb) {
      const rows = readTable<StoredListGame>('list_games');
      const index = rows.findIndex((row) => row.listId === listId && row.gameId === gameId);
      if (index >= 0) {
        const existing = rows[index];
        if (existing) {
          rows[index] = { ...existing, status, addedAt: existing.addedAt };
        }
      } else {
        rows.push({ listId, gameId, status, addedAt: Date.now() });
      }
      writeTable('list_games', rows);
      return;
    }

    db!.insert(listGamesTable)
      .values({ listId, gameId, status, addedAt: Date.now() })
      .onConflictDoUpdate({
        target: [listGamesTable.listId, listGamesTable.gameId],
        set: { status },
      })
      .run();
  },

  removeGameFromList(listId: string, gameId: string): void {
    if (isWeb) {
      writeTable(
        'list_games',
        readTable<StoredListGame>('list_games').filter(
          (row) => !(row.listId === listId && row.gameId === gameId),
        ),
      );
      return;
    }
    db!.delete(listGamesTable)
      .where(and(eq(listGamesTable.listId, listId), eq(listGamesTable.gameId, gameId)))
      .run();
  },

  updateStatus(listId: string, gameId: string, status: GameStatus): void {
    if (isWeb) {
      const rows = readTable<StoredListGame>('list_games');
      writeTable(
        'list_games',
        rows.map((row) => (row.listId === listId && row.gameId === gameId ? { ...row, status } : row)),
      );
      return;
    }
    db!.update(listGamesTable)
      .set({ status })
      .where(and(eq(listGamesTable.listId, listId), eq(listGamesTable.gameId, gameId)))
      .run();
  },

  getEntriesForList(listId: string): ListEntry[] {
    if (isWeb) {
      return readTable<StoredListGame>('list_games')
        .filter((row) => row.listId === listId)
        .sort((a, b) => a.addedAt - b.addedAt)
        .map((row) => ({
          listId,
          game: mapGameRow(readTable<{ id: string }>('games').find((game) => game.id === row.gameId) as never),
          status: toGameStatus(row.status),
          addedAt: row.addedAt,
        }))
        .filter((entry) => entry.game !== null) as ListEntry[];
    }
    const rows = db!
      .select({
        game: gamesTable,
        status: listGamesTable.status,
        addedAt: listGamesTable.addedAt,
      })
      .from(listGamesTable)
      .innerJoin(gamesTable, eq(listGamesTable.gameId, gamesTable.id))
      .where(eq(listGamesTable.listId, listId))
      .orderBy(asc(listGamesTable.addedAt))
      .all();

    return rows.map((row) => ({
      listId,
      game: mapGameRow(row.game),
      status: toGameStatus(row.status),
      addedAt: row.addedAt,
    }));
  },

  getAllEntries(): ListEntry[] {
    if (isWeb) {
      return readTable<StoredListGame>('list_games')
        .slice()
        .sort((a, b) => a.addedAt - b.addedAt)
        .map((row) => ({
          listId: row.listId,
          game: mapGameRow(readTable<{ id: string }>('games').find((game) => game.id === row.gameId) as never),
          status: toGameStatus(row.status),
          addedAt: row.addedAt,
        }))
        .filter((entry) => entry.game !== null) as ListEntry[];
    }
    const rows = db!
      .select({
        listId: listGamesTable.listId,
        game: gamesTable,
        status: listGamesTable.status,
        addedAt: listGamesTable.addedAt,
      })
      .from(listGamesTable)
      .innerJoin(gamesTable, eq(listGamesTable.gameId, gamesTable.id))
      .orderBy(asc(listGamesTable.addedAt))
      .all();

    return rows.map((row) => ({
      listId: row.listId,
      game: mapGameRow(row.game),
      status: toGameStatus(row.status),
      addedAt: row.addedAt,
    }));
  },

  getEntriesForGame(gameId: string): ListEntrySummary[] {
    if (isWeb) {
      return readTable<StoredListGame>('list_games')
        .filter((row) => row.gameId === gameId)
        .map((row) => ({
          listId: row.listId,
          status: toGameStatus(row.status),
        }));
    }
    const rows = db!
      .select({
        listId: listGamesTable.listId,
        status: listGamesTable.status,
      })
      .from(listGamesTable)
      .where(eq(listGamesTable.gameId, gameId))
      .all();

    return rows.map((row) => ({
      listId: row.listId,
      status: toGameStatus(row.status),
    }));
  },

  countForList(listId: string): number {
    if (isWeb) {
      return readTable<StoredListGame>('list_games').filter((row) => row.listId === listId).length;
    }
    const rows = db!
      .select({ count: listGamesTable.listId })
      .from(listGamesTable)
      .where(eq(listGamesTable.listId, listId))
      .all();
    return rows.length;
  },
};

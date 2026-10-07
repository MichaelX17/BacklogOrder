import { and, asc, eq } from 'drizzle-orm';

import { db } from '@/db/client';
import { gamesTable, listGamesTable } from '@/db/schema';
import { mapGameRow, toGameStatus } from '@/db/repositories/mappers';
import type { GameStatus, ListEntry, ListEntrySummary } from '@/types';

export const listGamesRepo = {
  addGameToList(listId: string, gameId: string, status: GameStatus): void {
    db.insert(listGamesTable)
      .values({ listId, gameId, status, addedAt: Date.now() })
      .onConflictDoUpdate({
        target: [listGamesTable.listId, listGamesTable.gameId],
        set: { status },
      })
      .run();
  },

  removeGameFromList(listId: string, gameId: string): void {
    db.delete(listGamesTable)
      .where(and(eq(listGamesTable.listId, listId), eq(listGamesTable.gameId, gameId)))
      .run();
  },

  updateStatus(listId: string, gameId: string, status: GameStatus): void {
    db.update(listGamesTable)
      .set({ status })
      .where(and(eq(listGamesTable.listId, listId), eq(listGamesTable.gameId, gameId)))
      .run();
  },

  getEntriesForList(listId: string): ListEntry[] {
    const rows = db
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
    const rows = db
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
    const rows = db
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
    const rows = db
      .select({ count: listGamesTable.listId })
      .from(listGamesTable)
      .where(eq(listGamesTable.listId, listId))
      .all();
    return rows.length;
  },
};

import { eq } from 'drizzle-orm';

import { db, isWeb } from '@/db/client';
import { gamesTable } from '@/db/schema';
import { mapGameRow } from '@/db/repositories/mappers';
import { readTable, writeTable } from '@/db/webStorage';
import type { Game, NewGame } from '@/types';

type StoredGame = {
  id: string;
  rawgId: number | null;
  name: string;
  cover: string | null;
  metacritic: number | null;
  rating: number | null;
  playtime: number | null;
  genres: string;
  platforms: string;
  tags: string;
  isMultiplayer: boolean;
  isManual: boolean;
  franchise: string | null;
  franchiseOrder: number | null;
  createdAt: number;
};

export const gamesRepo = {
  create(input: NewGame): Game {
    const id = crypto.randomUUID();

    if (isWeb) {
      const nextRow: StoredGame = {
        id,
        rawgId: input.rawgId ?? null,
        name: input.name,
        cover: input.cover ?? null,
        metacritic: input.metacritic ?? null,
        rating: input.rating ?? null,
        playtime: input.playtime ?? null,
        genres: JSON.stringify(input.genres ?? []),
        platforms: JSON.stringify(input.platforms ?? []),
        tags: JSON.stringify(input.tags ?? []),
        isMultiplayer: input.isMultiplayer ?? false,
        isManual: input.isManual ?? false,
        franchise: input.franchise ?? null,
        franchiseOrder: input.franchiseOrder ?? null,
        createdAt: Date.now(),
      };
      const rows = readTable<StoredGame>('games');
      rows.push(nextRow);
      writeTable('games', rows);
      const created = gamesRepo.getById(id);
      if (created === null) {
        throw new Error('Failed to create game');
      }
      return created;
    }

    db!.insert(gamesTable)
      .values({
        id,
        rawgId: input.rawgId ?? null,
        name: input.name,
        cover: input.cover ?? null,
        metacritic: input.metacritic ?? null,
        rating: input.rating ?? null,
        playtime: input.playtime ?? null,
        genres: JSON.stringify(input.genres ?? []),
        platforms: JSON.stringify(input.platforms ?? []),
        tags: JSON.stringify(input.tags ?? []),
        isMultiplayer: input.isMultiplayer ?? false,
        isManual: input.isManual ?? false,
        franchise: input.franchise ?? null,
        franchiseOrder: input.franchiseOrder ?? null,
        createdAt: Date.now(),
      })
      .run();
    const created = gamesRepo.getById(id);
    if (created === null) {
      throw new Error('Failed to create game');
    }
    return created;
  },

  getById(id: string): Game | null {
    if (isWeb) {
      const row = readTable<StoredGame>('games').find((entry) => entry.id === id);
      return row === undefined ? null : mapGameRow(row as never);
    }
    const rows = db!.select().from(gamesTable).where(eq(gamesTable.id, id)).limit(1).all();
    const row = rows[0];
    return row === undefined ? null : mapGameRow(row);
  },

  findByRawgId(rawgId: number): Game | null {
    if (isWeb) {
      const row = readTable<StoredGame>('games').find((entry) => entry.rawgId === rawgId);
      return row === undefined ? null : mapGameRow(row as never);
    }
    const rows = db!.select().from(gamesTable).where(eq(gamesTable.rawgId, rawgId)).limit(1).all();
    const row = rows[0];
    return row === undefined ? null : mapGameRow(row);
  },

  getAll(): Game[] {
    if (isWeb) {
      return readTable<StoredGame>('games').map((row) => mapGameRow(row as never));
    }
    return db!
      .select()
      .from(gamesTable)
      .all()
      .map((row) => mapGameRow(row));
  },

  updateFranchise(id: string, franchise: string | null, franchiseOrder: number | null): void {
    if (isWeb) {
      const rows = readTable<StoredGame>('games');
      const updated = rows.map((row) =>
        row.id === id ? { ...row, franchise, franchiseOrder } : row,
      );
      writeTable('games', updated);
      return;
    }
    db!.update(gamesTable).set({ franchise, franchiseOrder }).where(eq(gamesTable.id, id)).run();
  },

  remove(id: string): void {
    if (isWeb) {
      writeTable('games', readTable<StoredGame>('games').filter((row) => row.id !== id));
      return;
    }
    db!.delete(gamesTable).where(eq(gamesTable.id, id)).run();
  },
};

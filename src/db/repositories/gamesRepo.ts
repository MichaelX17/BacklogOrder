import { eq } from 'drizzle-orm';

import { db } from '@/db/client';
import { gamesTable } from '@/db/schema';
import { mapGameRow } from '@/db/repositories/mappers';
import type { Game, NewGame } from '@/types';

export const gamesRepo = {
  create(input: NewGame): Game {
    const id = crypto.randomUUID();
    db.insert(gamesTable)
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
    const rows = db.select().from(gamesTable).where(eq(gamesTable.id, id)).limit(1).all();
    const row = rows[0];
    return row === undefined ? null : mapGameRow(row);
  },

  findByRawgId(rawgId: number): Game | null {
    const rows = db.select().from(gamesTable).where(eq(gamesTable.rawgId, rawgId)).limit(1).all();
    const row = rows[0];
    return row === undefined ? null : mapGameRow(row);
  },

  getAll(): Game[] {
    return db
      .select()
      .from(gamesTable)
      .all()
      .map((row) => mapGameRow(row));
  },

  remove(id: string): void {
    db.delete(gamesTable).where(eq(gamesTable.id, id)).run();
  },
};

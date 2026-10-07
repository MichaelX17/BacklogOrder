import { asc, eq } from 'drizzle-orm';

import { db } from '@/db/client';
import { listsTable } from '@/db/schema';
import type { GameList } from '@/types';

export const listsRepo = {
  create(name: string): GameList {
    const now = Date.now();
    const id = crypto.randomUUID();
    db.insert(listsTable).values({ id, name, createdAt: now, updatedAt: now }).run();
    const created = listsRepo.getById(id);
    if (created === null) {
      throw new Error('Failed to create list');
    }
    return created;
  },

  getById(id: string): GameList | null {
    const rows = db.select().from(listsTable).where(eq(listsTable.id, id)).limit(1).all();
    const row = rows[0];
    return row ?? null;
  },

  getAll(): GameList[] {
    return db.select().from(listsTable).orderBy(asc(listsTable.createdAt)).all();
  },

  rename(id: string, name: string): void {
    db.update(listsTable).set({ name, updatedAt: Date.now() }).where(eq(listsTable.id, id)).run();
  },

  remove(id: string): void {
    db.delete(listsTable).where(eq(listsTable.id, id)).run();
  },
};

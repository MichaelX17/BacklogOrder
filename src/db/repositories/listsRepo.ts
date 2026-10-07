import { asc, eq } from 'drizzle-orm';

import { db, isWeb } from '@/db/client';
import { listsTable } from '@/db/schema';
import { readTable, writeTable } from '@/db/webStorage';
import type { GameList } from '@/types';

export const listsRepo = {
  create(name: string): GameList {
    if (isWeb) {
      const now = Date.now();
      const id = crypto.randomUUID();
      const created: GameList = { id, name, createdAt: now, updatedAt: now };
      const rows = readTable<GameList>('lists');
      rows.push(created);
      writeTable('lists', rows);
      return created;
    }

    const now = Date.now();
    const id = crypto.randomUUID();
    db!.insert(listsTable).values({ id, name, createdAt: now, updatedAt: now }).run();
    const created = listsRepo.getById(id);
    if (created === null) {
      throw new Error('Failed to create list');
    }
    return created;
  },

  getById(id: string): GameList | null {
    if (isWeb) {
      return readTable<GameList>('lists').find((list) => list.id === id) ?? null;
    }
    const rows = db!.select().from(listsTable).where(eq(listsTable.id, id)).limit(1).all();
    const row = rows[0];
    return row ?? null;
  },

  getAll(): GameList[] {
    if (isWeb) {
      return [...readTable<GameList>('lists')].sort((a, b) => a.createdAt - b.createdAt);
    }
    return db!.select().from(listsTable).orderBy(asc(listsTable.createdAt)).all();
  },

  rename(id: string, name: string): void {
    if (isWeb) {
      const rows = readTable<GameList>('lists').map((list) =>
        list.id === id ? { ...list, name, updatedAt: Date.now() } : list,
      );
      writeTable('lists', rows);
      return;
    }
    db!.update(listsTable).set({ name, updatedAt: Date.now() }).where(eq(listsTable.id, id)).run();
  },

  remove(id: string): void {
    if (isWeb) {
      writeTable('lists', readTable<GameList>('lists').filter((list) => list.id !== id));
      return;
    }
    db!.delete(listsTable).where(eq(listsTable.id, id)).run();
  },
};

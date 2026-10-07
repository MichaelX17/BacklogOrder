import { eq } from 'drizzle-orm';

import { db } from '@/db/client';
import { settingsTable } from '@/db/schema';

export const settingsRepo = {
  get(key: string): string | null {
    const rows = db.select().from(settingsTable).where(eq(settingsTable.key, key)).limit(1).all();
    const row = rows[0];
    return row?.value ?? null;
  },

  set(key: string, value: string): void {
    db.insert(settingsTable)
      .values({ key, value })
      .onConflictDoUpdate({
        target: settingsTable.key,
        set: { value },
      })
      .run();
  },

  remove(key: string): void {
    db.delete(settingsTable).where(eq(settingsTable.key, key)).run();
  },
};

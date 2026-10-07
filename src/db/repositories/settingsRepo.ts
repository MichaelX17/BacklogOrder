import { eq } from 'drizzle-orm';

import { db, isWeb } from '@/db/client';
import { settingsTable } from '@/db/schema';
import { readTable, writeTable } from '@/db/webStorage';

export const settingsRepo = {
  get(key: string): string | null {
    if (isWeb) {
      return readTable<{ key: string; value: string }>('settings').find((row) => row.key === key)?.value ?? null;
    }
    const rows = db!.select().from(settingsTable).where(eq(settingsTable.key, key)).limit(1).all();
    const row = rows[0];
    return row?.value ?? null;
  },

  set(key: string, value: string): void {
    if (isWeb) {
      const rows = readTable<{ key: string; value: string }>('settings');
      const index = rows.findIndex((row) => row.key === key);
      if (index >= 0) {
        const existing = rows[index];
        if (existing) {
          rows[index] = { ...existing, value };
        }
      } else {
        rows.push({ key, value });
      }
      writeTable('settings', rows);
      return;
    }
    db!.insert(settingsTable)
      .values({ key, value })
      .onConflictDoUpdate({
        target: settingsTable.key,
        set: { value },
      })
      .run();
  },

  remove(key: string): void {
    if (isWeb) {
      writeTable(
        'settings',
        readTable<{ key: string; value: string }>('settings').filter((row) => row.key !== key),
      );
      return;
    }
    db!.delete(settingsTable).where(eq(settingsTable.key, key)).run();
  },
};

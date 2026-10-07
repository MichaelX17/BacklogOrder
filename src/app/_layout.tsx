import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';

import { initializeDatabase } from '@/db/client';

export default function RootLayout() {
  initializeDatabase();

  return (
    <>
      <StatusBar style="auto" />
      <Stack
        screenOptions={{
          headerShown: true,
        }}
      />
    </>
  );
}

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
      >
        <Stack.Screen name="index" options={{ title: 'BacklogOrder' }} />
        <Stack.Screen name="onboarding" options={{ title: 'Welcome' }} />
        <Stack.Screen name="search" options={{ title: 'Search RAWG' }} />
        <Stack.Screen name="manual-game" options={{ title: 'Add Game Manually' }} />
        <Stack.Screen name="list/[id]" options={{ title: 'List' }} />
        <Stack.Screen name="game/[id]" options={{ title: 'Game' }} />
      </Stack>
    </>
  );
}

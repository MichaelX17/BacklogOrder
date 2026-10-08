import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';

import { initializeDatabase } from '@/db/client';
import { HUD_THEME } from '@/theme/hudTheme';

export default function RootLayout() {
  useEffect(() => {
    initializeDatabase();
  }, []);

  return (
    <>
      <StatusBar style="light" />
      <Stack
        screenOptions={{
          headerShown: true,
          headerStyle: {
            backgroundColor: HUD_THEME.bgFrom,
          },
          headerTintColor: HUD_THEME.text,
          headerTitleStyle: {
            fontWeight: '700',
          },
          contentStyle: {
            backgroundColor: HUD_THEME.bgFrom,
          },
        }}
      >
        <Stack.Screen name="index" options={{ title: 'BacklogOrder' }} />
        <Stack.Screen name="onboarding" options={{ title: 'Welcome' }} />
        <Stack.Screen name="search" options={{ title: 'Search RAWG' }} />
        <Stack.Screen name="manual-game" options={{ title: 'Add Game Manually' }} />
        <Stack.Screen name="list/[id]" options={{ title: 'List' }} />
        <Stack.Screen name="game/[id]" options={{ title: 'Game' }} />
        <Stack.Screen name="settings" options={{ title: 'Settings' }} />
      </Stack>
    </>
  );
}

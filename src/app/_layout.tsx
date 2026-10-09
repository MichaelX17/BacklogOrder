import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';
import { View } from 'react-native';

import * as Font from 'expo-font';
import { initializeDatabase } from '@/db/client';
import { HudThemeProvider } from '@/components/HudThemeProvider';

const loadFonts = async () => {
  await Font.loadAsync({
    'Orbitron_Variable': require('@/assets/fonts/Orbitron-VariableFont_wght.ttf'),
    'Orbitron_500Medium': require('@/assets/fonts/Orbitron-VariableFont_wght.ttf'),
    'Orbitron_700Bold': require('@/assets/fonts/Orbitron-VariableFont_wght.ttf'),
    'Orbitron_900Black': require('@/assets/fonts/Orbitron-VariableFont_wght.ttf'),
    'Rajdhani_400Regular': require('@/assets/fonts/Rajdhani_400Regular.ttf'),
    'Rajdhani_500Medium': require('@/assets/fonts/Rajdhani_500Medium.ttf'),
    'Rajdhani_600SemiBold': require('@/assets/fonts/Rajdhani_600SemiBold.ttf'),
    'Rajdhani_700Bold': require('@/assets/fonts/Rajdhani_700Bold.ttf'),
  });
};

export default function RootLayout() {
  useEffect(() => {
    initializeDatabase();
    loadFonts();
  }, []);

  return (
    <HudThemeProvider>
      <StatusBar style="light" />
      <View style={{ flex: 1, backgroundColor: '#07040d' }}>
        <Stack
          screenOptions={{
            headerShown: true,
            headerStyle: {
              backgroundColor: '#07040d',
            },
            headerTintColor: '#f4ecff',
            headerTitleStyle: {
              fontWeight: '700',
            },
            contentStyle: {
              backgroundColor: '#07040d',
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
      </View>
    </HudThemeProvider>
  );
}

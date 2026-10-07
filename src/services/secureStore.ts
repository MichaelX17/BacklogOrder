import { Platform } from 'react-native';
import * as SecureStore from 'expo-secure-store';

const API_KEY_STORAGE_KEY = 'rawg_api_key';

function getWebStorage(): Storage | null {
  if (Platform.OS !== 'web' || typeof window === 'undefined') {
    return null;
  }

  return window.localStorage;
}

export async function getApiKey(): Promise<string | null> {
  if (Platform.OS === 'web') {
    return getWebStorage()?.getItem(API_KEY_STORAGE_KEY) ?? null;
  }

  return SecureStore.getItemAsync(API_KEY_STORAGE_KEY);
}

export async function setApiKey(apiKey: string): Promise<void> {
  if (Platform.OS === 'web') {
    getWebStorage()?.setItem(API_KEY_STORAGE_KEY, apiKey);
    return;
  }

  await SecureStore.setItemAsync(API_KEY_STORAGE_KEY, apiKey);
}

export async function deleteApiKey(): Promise<void> {
  if (Platform.OS === 'web') {
    getWebStorage()?.removeItem(API_KEY_STORAGE_KEY);
    return;
  }

  await SecureStore.deleteItemAsync(API_KEY_STORAGE_KEY);
}

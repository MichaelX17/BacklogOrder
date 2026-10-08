import { router } from 'expo-router';
import { useState } from 'react';
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Linking,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { checkApiKey } from '@/services/rawg/client';
import { rawgErrorMessage } from '@/services/rawg/errors';
import { setApiKey } from '@/services/secureStore';
import { HUD_THEME, rgba } from '@/theme/hudTheme';

export default function OnboardingScreen() {
  const [apiKey, setApiKeyState] = useState('');
  const [isValidating, setIsValidating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleContinue() {
    const trimmed = apiKey.trim();
    if (trimmed === '') {
      setError('Enter your RAWG API key to continue.');
      return;
    }

    setIsValidating(true);
    setError(null);

    const validationError = await checkApiKey(trimmed);
    if (validationError !== null) {
      setError(rawgErrorMessage(validationError));
      setIsValidating(false);
      return;
    }

    await setApiKey(trimmed);
    setIsValidating(false);
    router.replace('/');
  }

  function handleGetKey() {
    Linking.openURL('https://rawg.io/login?forward=developer');
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.glow} pointerEvents="none" />
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <View style={styles.content}>
          <Text style={styles.title}>BacklogOrder</Text>
          <Text style={styles.subtitle}>Rank your backlog by critical rating and playtime.</Text>
          <Text style={styles.label}>RAWG API Key</Text>
          <TextInput
            style={styles.input}
            value={apiKey}
            onChangeText={setApiKeyState}
            placeholder="Paste your RAWG API key"
            placeholderTextColor={HUD_THEME.muted}
            autoCapitalize="none"
            autoCorrect={false}
            autoComplete="off"
            editable={!isValidating}
          />
          {error !== null ? <Text style={styles.error}>{error}</Text> : null}
          <Pressable
            style={({ pressed }) => [
              styles.button,
              (pressed || isValidating) && styles.buttonDisabled,
            ]}
            onPress={handleContinue}
            disabled={isValidating}
          >
            {isValidating ? (
              <ActivityIndicator color={HUD_THEME.text} />
            ) : (
              <Text style={styles.buttonText}>Validate & Continue</Text>
            )}
          </Pressable>
          <Pressable onPress={handleGetKey} style={styles.link}>
            <Text style={styles.linkText}>Get a free key at RAWG</Text>
          </Pressable>
          <Text style={styles.attribution}>Includes data from the RAWG API — https://rawg.io</Text>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: HUD_THEME.bgFrom,
  },
  glow: {
    position: 'absolute',
    inset: 0,
    backgroundColor: rgba(HUD_THEME.primary, 0.13),
  },
  flex: {
    flex: 1,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    padding: 24,
  },
  title: {
    fontSize: 32,
    fontWeight: '800',
    color: HUD_THEME.text,
    textAlign: 'center',
    letterSpacing: 2,
  },
  subtitle: {
    marginTop: 8,
    marginBottom: 32,
    fontSize: 16,
    color: HUD_THEME.muted,
    textAlign: 'center',
  },
  label: {
    fontSize: 14,
    fontWeight: '700',
    color: HUD_THEME.text,
    marginBottom: 8,
    letterSpacing: 1.2,
  },
  input: {
    borderWidth: 1,
    borderColor: rgba(HUD_THEME.secondary, 0.35),
    borderRadius: 12,
    padding: 12,
    fontSize: 16,
    color: HUD_THEME.text,
    backgroundColor: rgba(HUD_THEME.secondary, 0.08),
  },
  error: {
    marginTop: 8,
    fontSize: 14,
    color: '#ff8b8b',
  },
  button: {
    marginTop: 24,
    backgroundColor: HUD_THEME.primary,
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    shadowColor: HUD_THEME.primary,
    shadowOpacity: 0.4,
    shadowRadius: 18,
    shadowOffset: { width: 0, height: 0 },
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  buttonText: {
    color: '#120912',
    fontSize: 16,
    fontWeight: '800',
    letterSpacing: 1,
  },
  link: {
    marginTop: 16,
    alignItems: 'center',
  },
  linkText: {
    color: HUD_THEME.secondary,
    fontSize: 14,
    fontWeight: '600',
  },
  attribution: {
    marginTop: 32,
    fontSize: 12,
    color: HUD_THEME.muted,
    textAlign: 'center',
  },
});

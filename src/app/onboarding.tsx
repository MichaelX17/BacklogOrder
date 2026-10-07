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
    Linking.openURL('https://rawg.io/keys');
  }

  return (
    <SafeAreaView style={styles.container}>
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
            placeholderTextColor="#999999"
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
              <ActivityIndicator color="#ffffff" />
            ) : (
              <Text style={styles.buttonText}>Validate & Continue</Text>
            )}
          </Pressable>
          <Pressable onPress={handleGetKey} style={styles.link}>
            <Text style={styles.linkText}>Get a free key at rawg.io/keys</Text>
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
    backgroundColor: '#ffffff',
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
    fontWeight: 'bold',
    color: '#111111',
    textAlign: 'center',
  },
  subtitle: {
    marginTop: 8,
    marginBottom: 32,
    fontSize: 16,
    color: '#666666',
    textAlign: 'center',
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111111',
    marginBottom: 8,
  },
  input: {
    borderWidth: 1,
    borderColor: '#cccccc',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    color: '#111111',
    backgroundColor: '#ffffff',
  },
  error: {
    marginTop: 8,
    fontSize: 14,
    color: '#d32f2f',
  },
  button: {
    marginTop: 24,
    backgroundColor: '#111111',
    borderRadius: 8,
    padding: 16,
    alignItems: 'center',
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
  link: {
    marginTop: 16,
    alignItems: 'center',
  },
  linkText: {
    color: '#1976d2',
    fontSize: 14,
  },
  attribution: {
    marginTop: 32,
    fontSize: 12,
    color: '#999999',
    textAlign: 'center',
  },
});

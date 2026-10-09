import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, TextInput } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { EmptyState } from '@/components/EmptyState';
import { hudThemes, withAlpha } from '@/theme/hudTheme';
import { useGamesStore } from '@/stores/gamesStore';
import { parseCsv } from '@/utils/parseCsv';
import type { NewGame } from '@/types';

interface FieldErrors {
  name?: string;
  metacritic?: string;
  rating?: string;
  playtime?: string;
  franchiseOrder?: string;
}

export default function ManualGameScreen() {
  const { listId } = useLocalSearchParams<{
    listId: string;
  }>();
  const addGameToList = useGamesStore((state) => state.addGameToList);

  const [name, setName] = useState('');
  const [cover, setCover] = useState('');
  const [metacritic, setMetacritic] = useState('');
  const [rating, setRating] = useState('');
  const [playtime, setPlaytime] = useState('');
  const [genres, setGenres] = useState('');
  const [platforms, setPlatforms] = useState('');
  const [franchise, setFranchise] = useState('');
  const [franchiseOrder, setFranchiseOrder] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);

  function handleCreate() {
    const errors: FieldErrors = {};
    const trimmedName = name.trim();

    if (trimmedName === '') {
      errors.name = 'Name is required.';
    }

    let metacriticValue: number | null = null;
    if (metacritic.trim() !== '') {
      const parsed = Number(metacritic);
      if (!Number.isInteger(parsed) || parsed < 0 || parsed > 100) {
        errors.metacritic = 'Metacritic must be a whole number between 0 and 100.';
      } else {
        metacriticValue = parsed;
      }
    }

    let ratingValue: number | null = null;
    if (rating.trim() !== '') {
      const parsed = Number(rating);
      if (Number.isNaN(parsed) || parsed < 0 || parsed > 5) {
        errors.rating = 'Rating must be a number between 0 and 5.';
      } else {
        ratingValue = parsed;
      }
    }

    let playtimeValue: number | null = null;
    if (playtime.trim() !== '') {
      const parsed = Number(playtime);
      if (Number.isNaN(parsed) || parsed < 0) {
        errors.playtime = 'Playtime must be a non-negative number of hours.';
      } else {
        playtimeValue = parsed;
      }
    }

    let franchiseOrderValue: number | null = null;
    const trimmedFranchiseOrder = franchiseOrder.trim();
    if (trimmedFranchiseOrder !== '') {
      const parsed = Number(trimmedFranchiseOrder);
      if (!Number.isInteger(parsed)) {
        errors.franchiseOrder = 'Franchise order must be a whole number.';
      } else {
        franchiseOrderValue = parsed;
      }
    }

    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      return;
    }

    if (listId === undefined) {
      setFormError('Open this screen from a list to add a game.');
      return;
    }

    const trimmedFranchise = franchise.trim();
    const input: NewGame = {
      name: trimmedName,
      cover: cover.trim() === '' ? null : cover.trim(),
      metacritic: metacriticValue,
      rating: ratingValue,
      playtime: playtimeValue,
      genres: parseCsv(genres),
      platforms: parseCsv(platforms),
      tags: [],
      isManual: true,
      franchise: trimmedFranchise === '' ? null : trimmedFranchise,
      franchiseOrder: trimmedFranchise === '' ? null : franchiseOrderValue,
    };

    addGameToList(listId, input, 'Backlog');
    router.back();
  }

  if (listId === undefined) {
    return (
      <SafeAreaView style={styles.container}>
        <EmptyState
          title="No list selected"
          message="Open this screen from a list to add a game manually."
        />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.label}>
          Name <Text style={styles.required}>*</Text>
        </Text>
        <TextInput
          style={styles.input}
          value={name}
          onChangeText={(text) => {
            setName(text);
            setFieldErrors((prev) => ({ ...prev, name: undefined }));
          }}
          placeholder="Game name"
          placeholderTextColor={hudThemes.violet.colors.muted}
        />
        {fieldErrors.name !== undefined ? (
          <Text style={styles.error}>{fieldErrors.name}</Text>
        ) : null}

        <Text style={styles.label}>Cover URL (optional)</Text>
        <TextInput
          style={styles.input}
          value={cover}
          onChangeText={setCover}
          placeholder="https://…"
          placeholderTextColor={hudThemes.violet.colors.muted}
          autoCapitalize="none"
          autoCorrect={false}
          keyboardType="url"
        />

        <Text style={styles.label}>Metacritic 0–100 (optional)</Text>
        <TextInput
          style={styles.input}
          value={metacritic}
          onChangeText={(text) => {
            setMetacritic(text);
            setFieldErrors((prev) => ({ ...prev, metacritic: undefined }));
          }}
          placeholder="e.g. 92"
          placeholderTextColor={hudThemes.violet.colors.muted}
          keyboardType="number-pad"
        />
        {fieldErrors.metacritic !== undefined ? (
          <Text style={styles.error}>{fieldErrors.metacritic}</Text>
        ) : null}

        <Text style={styles.label}>RAWG rating 0–5 (optional)</Text>
        <TextInput
          style={styles.input}
          value={rating}
          onChangeText={(text) => {
            setRating(text);
            setFieldErrors((prev) => ({ ...prev, rating: undefined }));
          }}
          placeholder="e.g. 4.5"
          placeholderTextColor={hudThemes.violet.colors.muted}
          keyboardType="decimal-pad"
        />
        {fieldErrors.rating !== undefined ? (
          <Text style={styles.error}>{fieldErrors.rating}</Text>
        ) : null}

        <Text style={styles.label}>Playtime in hours (optional)</Text>
        <TextInput
          style={styles.input}
          value={playtime}
          onChangeText={(text) => {
            setPlaytime(text);
            setFieldErrors((prev) => ({ ...prev, playtime: undefined }));
          }}
          placeholder="e.g. 30"
          placeholderTextColor={hudThemes.violet.colors.muted}
          keyboardType="decimal-pad"
        />
        {fieldErrors.playtime !== undefined ? (
          <Text style={styles.error}>{fieldErrors.playtime}</Text>
        ) : null}

        <Text style={styles.label}>Genres, comma-separated (optional)</Text>
        <TextInput
          style={styles.input}
          value={genres}
          onChangeText={setGenres}
          placeholder="RPG, Action"
          placeholderTextColor={hudThemes.violet.colors.muted}
        />

        <Text style={styles.label}>Platforms, comma-separated (optional)</Text>
        <TextInput
          style={styles.input}
          value={platforms}
          onChangeText={setPlatforms}
          placeholder="PC, PlayStation 5"
          placeholderTextColor={hudThemes.violet.colors.muted}
        />

        <Text style={styles.label}>Franchise (optional)</Text>
        <TextInput
          style={styles.input}
          value={franchise}
          onChangeText={setFranchise}
          placeholder="e.g. Hades"
          placeholderTextColor={hudThemes.violet.colors.muted}
        />

        <Text style={styles.label}>Franchise order (optional)</Text>
        <TextInput
          style={styles.input}
          value={franchiseOrder}
          onChangeText={(text) => {
            setFranchiseOrder(text);
            setFieldErrors((prev) => ({ ...prev, franchiseOrder: undefined }));
          }}
          placeholder="e.g. 2"
          placeholderTextColor={hudThemes.violet.colors.muted}
          keyboardType="number-pad"
        />
        {fieldErrors.franchiseOrder !== undefined ? (
          <Text style={styles.error}>{fieldErrors.franchiseOrder}</Text>
        ) : null}

        {formError !== null ? <Text style={styles.error}>{formError}</Text> : null}

        <Pressable
          style={({ pressed }) => [styles.createButton, pressed && styles.buttonPressed]}
          onPress={handleCreate}
        >
          <Text style={styles.createButtonText}>Create game</Text>
        </Pressable>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: hudThemes.violet.colors.bgFrom,
  },
  content: {
    padding: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: '700',
    color: hudThemes.violet.colors.text,
    marginBottom: 6,
    marginTop: 10,
    letterSpacing: 0.8,
  },
  required: {
    color: '#ff8b8b',
  },
  input: {
    borderWidth: 1,
    borderColor: withAlpha(hudThemes.violet.colors.secondary, 0.25),
    borderRadius: 10,
    padding: 12,
    fontSize: 16,
    color: hudThemes.violet.colors.text,
    backgroundColor: withAlpha(hudThemes.violet.colors.secondary, 0.06),
  },
  error: {
    marginTop: 4,
    fontSize: 13,
    color: '#ff8b8b',
  },
  createButton: {
    marginTop: 24,
    backgroundColor: hudThemes.violet.colors.primary,
    borderRadius: 10,
    padding: 16,
    alignItems: 'center',
    shadowColor: hudThemes.violet.colors.primary,
    shadowOpacity: 0.35,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 0 },
  },
  createButtonText: {
    color: '#120912',
    fontSize: 16,
    fontWeight: '800',
  },
  buttonPressed: {
    opacity: 0.7,
  },
});

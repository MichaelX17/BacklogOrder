import { router } from 'expo-router';
import { useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Image,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import AddToListModal from '@/components/AddToListModal';
import { useDebouncedValue } from '@/features/games/useDebouncedValue';
import { searchGames } from '@/services/rawg/client';
import { RawgApiError, rawgErrorMessage } from '@/services/rawg/errors';
import type { RawgGame } from '@/services/rawg/types';
import { getApiKey } from '@/services/secureStore';

export default function SearchScreen() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<RawgGame[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState<RawgApiError | null>(null);
  const [selectedGame, setSelectedGame] = useState<RawgGame | null>(null);
  const debouncedQuery = useDebouncedValue(query, 300);
  const requestIdRef = useRef(0);

  const trimmedQuery = debouncedQuery.trim();
  const isQueryActive = trimmedQuery.length >= 2;
  const displayResults = isQueryActive ? results : [];
  const displayError = isQueryActive ? error : null;

  useEffect(() => {
    getApiKey().then((key) => {
      if (key === null) {
        router.replace('/onboarding');
      }
    });
  }, []);

  useEffect(() => {
    if (!isQueryActive) {
      return;
    }

    const requestId = requestIdRef.current + 1;
    requestIdRef.current = requestId;

    void (async () => {
      setIsSearching(true);
      setError(null);
      try {
        const apiKey = await getApiKey();
        if (apiKey === null) {
          throw new RawgApiError('unauthorized', 'API key not found.');
        }
        const games = await searchGames(trimmedQuery, apiKey);
        if (requestIdRef.current !== requestId) {
          return;
        }
        setResults(games);
      } catch (unknownError) {
        if (requestIdRef.current !== requestId) {
          return;
        }
        setError(
          unknownError instanceof RawgApiError
            ? unknownError
            : new RawgApiError('unknown', 'Search failed.'),
        );
        setResults([]);
      } finally {
        if (requestIdRef.current === requestId) {
          setIsSearching(false);
        }
      }
    })();
  }, [isQueryActive, trimmedQuery]);

  function renderGame({ item }: { item: RawgGame }) {
    return (
      <View style={styles.gameRow}>
        {item.background_image !== null ? (
          <Image source={{ uri: item.background_image }} style={styles.cover} />
        ) : (
          <View style={[styles.cover, styles.coverPlaceholder]} />
        )}
        <View style={styles.gameInfo}>
          <Text style={styles.gameName} numberOfLines={2}>
            {item.name}
          </Text>
          <Text style={styles.gameMeta}>
            {item.metacritic !== null
              ? `Metacritic ${item.metacritic}`
              : item.rating !== null
                ? `RAWG ${item.rating.toFixed(1)}/5`
                : 'No score'}
            {' · '}
            {item.playtime !== null ? `${item.playtime}h` : 'Unknown playtime'}
          </Text>
        </View>
        <Pressable
          style={({ pressed }) => [styles.addButton, pressed && styles.addButtonPressed]}
          onPress={() => setSelectedGame(item)}
        >
          <Text style={styles.addButtonText}>Add</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.searchBar}>
        <TextInput
          style={styles.searchInput}
          value={query}
          onChangeText={setQuery}
          placeholder="Search games on RAWG"
          placeholderTextColor="#999999"
          autoCapitalize="none"
          autoCorrect={false}
          returnKeyType="search"
        />
      </View>
      {displayError !== null ? (
        <Text style={styles.error}>{rawgErrorMessage(displayError)}</Text>
      ) : null}
      {isSearching ? (
        <View style={styles.center}>
          <ActivityIndicator size="large" color="#111111" />
        </View>
      ) : (
        <FlatList
          data={displayResults}
          keyExtractor={(item) => item.id.toString()}
          renderItem={renderGame}
          contentContainerStyle={styles.listContent}
          ListEmptyComponent={
            <Text style={styles.emptyText}>
              {isQueryActive
                ? 'No games found. Try another search.'
                : 'Type at least 2 characters to search.'}
            </Text>
          }
        />
      )}
      <AddToListModal
        visible={selectedGame !== null}
        game={selectedGame}
        onClose={() => setSelectedGame(null)}
        onAdded={() => {
          setSelectedGame(null);
          setQuery('');
          setResults([]);
        }}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
  },
  searchBar: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#eeeeee',
  },
  searchInput: {
    borderWidth: 1,
    borderColor: '#cccccc',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    color: '#111111',
    backgroundColor: '#ffffff',
  },
  error: {
    margin: 16,
    fontSize: 14,
    color: '#d32f2f',
  },
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  listContent: {
    flexGrow: 1,
    padding: 16,
  },
  gameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  cover: {
    width: 64,
    height: 48,
    borderRadius: 6,
    backgroundColor: '#eeeeee',
  },
  coverPlaceholder: {
    backgroundColor: '#dddddd',
  },
  gameInfo: {
    flex: 1,
    marginLeft: 12,
  },
  gameName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111111',
  },
  gameMeta: {
    marginTop: 4,
    fontSize: 13,
    color: '#666666',
  },
  addButton: {
    marginLeft: 12,
    backgroundColor: '#111111',
    borderRadius: 6,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  addButtonPressed: {
    opacity: 0.7,
  },
  addButtonText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: '600',
  },
  emptyText: {
    marginTop: 32,
    fontSize: 15,
    color: '#666666',
    textAlign: 'center',
  },
});

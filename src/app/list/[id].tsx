import { router, useLocalSearchParams } from 'expo-router';
import { useEffect } from 'react';
import { ActivityIndicator, FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import EmptyState from '@/components/EmptyState';
import FranchiseGroup from '@/components/FranchiseGroup';
import { useGamesStore } from '@/stores/gamesStore';
import { useListsStore } from '@/stores/listsStore';
import { sortGames } from '@/utils/sorting';
import type { ListEntry } from '@/types';
import type { ScoredGame } from '@/utils/sorting';

export default function ListDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const lists = useListsStore((state) => state.lists);
  const entriesByList = useGamesStore((state) => state.entriesByList);
  const loadLists = useListsStore((state) => state.loadLists);
  const loadEntriesForList = useGamesStore((state) => state.loadEntriesForList);

  useEffect(() => {
    loadLists();
    if (id !== undefined) {
      loadEntriesForList(id);
    }
  }, [id, loadLists, loadEntriesForList]);

  const list = lists.find((candidate) => candidate.id === id);
  const isEntriesLoaded = id !== undefined && Object.hasOwn(entriesByList, id);
  const listEntries: ListEntry[] = isEntriesLoaded ? (entriesByList[id] ?? []) : [];
  const entryByGameId = new Map(listEntries.map((entry) => [entry.game.id, entry]));
  const groups = sortGames(listEntries.map((entry) => entry.game));

  function handleGamePress(game: ScoredGame) {
    if (list !== undefined) {
      router.push(`/game/${game.id}?listId=${list.id}`);
    }
  }

  if (list === undefined) {
    return (
      <SafeAreaView style={styles.container}>
        <EmptyState title="List not found" message="This list does not exist anymore." />
      </SafeAreaView>
    );
  }

  if (!isEntriesLoaded) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator size="large" color="#111111" />
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.actions}>
        <Pressable
          style={({ pressed }) => [styles.actionButton, pressed && styles.actionButtonPressed]}
          onPress={() => router.push('/search')}
        >
          <Text style={styles.actionButtonText}>Search RAWG</Text>
        </Pressable>
        <Pressable
          style={({ pressed }) => [
            styles.actionButton,
            styles.actionButtonSecondary,
            pressed && styles.actionButtonPressed,
          ]}
          onPress={() => router.push(`/manual-game?listId=${list.id}`)}
        >
          <Text style={styles.actionButtonSecondaryText}>Add manually</Text>
        </Pressable>
      </View>
      {groups.length === 0 ? (
        <EmptyState
          title="No games yet"
          message="Search RAWG or add a game manually to start ranking this list."
        />
      ) : (
        <FlatList
          data={groups}
          keyExtractor={(group) => group.key}
          renderItem={({ item }) => (
            <FranchiseGroup
              group={item}
              entryByGameId={entryByGameId}
              onGamePress={handleGamePress}
            />
          )}
          contentContainerStyle={styles.listContent}
        />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  loading: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f5f5f5',
  },
  actions: {
    flexDirection: 'row',
    gap: 8,
    padding: 16,
    paddingBottom: 8,
  },
  actionButton: {
    flex: 1,
    backgroundColor: '#111111',
    borderRadius: 8,
    padding: 12,
    alignItems: 'center',
  },
  actionButtonSecondary: {
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: '#cccccc',
  },
  actionButtonPressed: {
    opacity: 0.7,
  },
  actionButtonText: {
    color: '#ffffff',
    fontSize: 15,
    fontWeight: '600',
  },
  actionButtonSecondaryText: {
    color: '#111111',
    fontSize: 15,
    fontWeight: '600',
  },
  listContent: {
    padding: 16,
    paddingTop: 8,
    flexGrow: 1,
  },
});

import { router } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
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

import EmptyState from '@/components/EmptyState';
import { getApiKey } from '@/services/secureStore';
import { useGamesStore } from '@/stores/gamesStore';
import { useListsStore } from '@/stores/listsStore';
import { computeScore } from '@/utils/score';
import { findRecommendation } from '@/utils/recommendation';
import type { GameList } from '@/types';

export default function HomeScreen() {
  const lists = useListsStore((state) => state.lists);
  const isListsLoaded = useListsStore((state) => state.isLoaded);
  const allEntries = useGamesStore((state) => state.allEntries);
  const loadLists = useListsStore((state) => state.loadLists);
  const loadAllEntries = useGamesStore((state) => state.loadAllEntries);
  const updateStatus = useGamesStore((state) => state.updateStatus);
  const createList = useListsStore((state) => state.createList);
  const [newListName, setNewListName] = useState('');
  const [skipCount, setSkipCount] = useState(0);

  useEffect(() => {
    loadLists();
    loadAllEntries();
  }, [loadLists, loadAllEntries]);

  useEffect(() => {
    getApiKey().then((key) => {
      if (key === null) {
        router.replace('/onboarding');
      }
    });
  }, []);

  const gameCountByList = new Map<string, number>();
  for (const entry of allEntries) {
    const current = gameCountByList.get(entry.listId) ?? 0;
    gameCountByList.set(entry.listId, current + 1);
  }

  const recommendation = useMemo(
    () => findRecommendation(allEntries, skipCount),
    [allEntries, skipCount],
  );
  const recommendationScore = recommendation !== null ? computeScore(recommendation.game) : null;
  const recommendationScoreText =
    recommendation !== null && recommendation.game.playtime !== null && recommendation.game.playtime <= 0
      ? '∞'
      : recommendationScore !== null
        ? recommendationScore.toFixed(2)
        : '—';

  function handleCreateList() {
    const trimmed = newListName.trim();
    if (trimmed === '') {
      return;
    }
    createList(trimmed);
    setNewListName('');
  }

  function handleStartPlaying() {
    if (recommendation === null) {
      return;
    }

    updateStatus(recommendation.listId, recommendation.game.id, 'Playing');
    setSkipCount(0);
  }

  function handleSkip() {
    setSkipCount((current) => current + 1);
  }

  function renderList({ item }: { item: GameList }) {
    const count = gameCountByList.get(item.id) ?? 0;
    return (
      <Pressable
        style={({ pressed }) => [styles.listRow, pressed && styles.listRowPressed]}
        onPress={() => router.push(`/list/${item.id}`)}
      >
        <View style={styles.listInfo}>
          <Text style={styles.listName}>{item.name}</Text>
          <Text style={styles.listMeta}>
            {count} {count === 1 ? 'game' : 'games'}
          </Text>
        </View>
        <Text style={styles.chevron}>›</Text>
      </Pressable>
    );
  }

  if (!isListsLoaded) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator size="large" color="#111111" />
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={lists}
        keyExtractor={(item) => item.id}
        renderItem={renderList}
        contentContainerStyle={styles.content}
        ListHeaderComponent={
          <View>
            {recommendation !== null ? (
              <View style={styles.pickCard}>
                <Pressable
                  onPress={() =>
                    router.push(`/game/${recommendation.game.id}?listId=${recommendation.listId}`)
                  }
                  style={styles.pickContent}
                >
                  {recommendation.game.cover !== null ? (
                    <Image source={{ uri: recommendation.game.cover }} style={styles.pickCover} />
                  ) : (
                    <View style={[styles.pickCover, styles.pickCoverPlaceholder]} />
                  )}
                  <View style={styles.pickInfo}>
                    <Text style={styles.pickLabel}>RECOMMENDATION</Text>
                    <Text style={styles.pickName} numberOfLines={2}>
                      {recommendation.game.name}
                    </Text>
                    <Text style={styles.pickMeta}>
                      {recommendation.status} · Score {recommendationScoreText}
                      {recommendation.game.playtime !== null
                        ? ` · ${recommendation.game.playtime}h`
                        : ''}
                    </Text>
                  </View>
                </Pressable>
                <View style={styles.actionRow}>
                  <Pressable style={styles.primaryAction} onPress={handleStartPlaying}>
                    <Text style={styles.primaryActionText}>Start playing</Text>
                  </Pressable>
                  <Pressable style={styles.secondaryAction} onPress={handleSkip}>
                    <Text style={styles.secondaryActionText}>Skip</Text>
                  </Pressable>
                </View>
              </View>
            ) : null}
            <View style={styles.createRow}>
              <TextInput
                style={styles.createInput}
                value={newListName}
                onChangeText={setNewListName}
                placeholder="New list name"
                placeholderTextColor="#999999"
                onSubmitEditing={handleCreateList}
                returnKeyType="done"
              />
              <Pressable
                style={({ pressed }) => [styles.createButton, pressed && styles.listRowPressed]}
                onPress={handleCreateList}
              >
                <Text style={styles.createButtonText}>Create</Text>
              </Pressable>
            </View>
            <Text style={styles.sectionTitle}>Your lists</Text>
          </View>
        }
        ListEmptyComponent={
          <EmptyState
            title="No lists yet"
            message="Create a list above to start ranking your backlog."
          />
        }
      />
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
  content: {
    padding: 16,
    flexGrow: 1,
  },
  pickCard: {
    backgroundColor: '#111111',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
  },
  pickContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  pickCover: {
    width: 72,
    height: 96,
    borderRadius: 8,
    backgroundColor: '#2c2c2c',
  },
  pickCoverPlaceholder: {
    backgroundColor: '#3a3a3a',
  },
  pickInfo: {
    flex: 1,
    marginLeft: 12,
  },
  pickLabel: {
    fontSize: 11,
    fontWeight: 'bold',
    color: '#aaaaaa',
    letterSpacing: 1,
  },
  pickName: {
    marginTop: 4,
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  pickMeta: {
    marginTop: 4,
    fontSize: 14,
    color: '#cccccc',
  },
  actionRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 14,
  },
  primaryAction: {
    flex: 1,
    backgroundColor: '#ffffff',
    borderRadius: 8,
    paddingVertical: 10,
    alignItems: 'center',
  },
  primaryActionText: {
    color: '#111111',
    fontWeight: '700',
  },
  secondaryAction: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#ffffff',
    borderRadius: 8,
    paddingVertical: 10,
    alignItems: 'center',
  },
  secondaryActionText: {
    color: '#ffffff',
    fontWeight: '600',
  },
  createRow: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 20,
  },
  createInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#cccccc',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    color: '#111111',
    backgroundColor: '#ffffff',
  },
  createButton: {
    backgroundColor: '#111111',
    borderRadius: 8,
    paddingHorizontal: 20,
    justifyContent: 'center',
  },
  createButtonText: {
    color: '#ffffff',
    fontSize: 15,
    fontWeight: '600',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#111111',
    marginBottom: 8,
  },
  listRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#eeeeee',
    padding: 16,
    marginBottom: 10,
  },
  listRowPressed: {
    opacity: 0.7,
  },
  listInfo: {
    flex: 1,
  },
  listName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111111',
  },
  listMeta: {
    marginTop: 2,
    fontSize: 13,
    color: '#666666',
  },
  chevron: {
    fontSize: 24,
    color: '#999999',
  },
});

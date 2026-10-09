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

import { EmptyState } from '@/components/EmptyState';
import { getApiKey } from '@/services/secureStore';
import { useGamesStore } from '@/stores/gamesStore';
import { useListsStore } from '@/stores/listsStore';
import { hudThemes, withAlpha } from '@/theme/hudTheme';
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
        <ActivityIndicator size="large" color={hudThemes.violet.colors.primary} />
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.gridOverlay} pointerEvents="none" />
      <View style={styles.scanlines} pointerEvents="none" />
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
                placeholderTextColor={hudThemes.violet.colors.muted}
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
    backgroundColor: hudThemes.violet.colors.bgFrom,
  },
  gridOverlay: {
    position: 'absolute',
    inset: 0,
    backgroundColor: 'transparent',
    borderWidth: 0,
    opacity: 0.2,
  },
  scanlines: {
    position: 'absolute',
    inset: 0,
    backgroundColor: withAlpha(hudThemes.violet.colors.primary, 0.04),
  },
  loading: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: hudThemes.violet.colors.bgFrom,
  },
  content: {
    padding: 16,
    flexGrow: 1,
  },
  pickCard: {
    backgroundColor: hudThemes.violet.colors.surface,
    borderWidth: 1,
    borderColor: withAlpha(hudThemes.violet.colors.secondary, 0.28),
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
    shadowColor: hudThemes.violet.colors.surface,
    shadowOpacity: 0.5,
    shadowRadius: 18,
    shadowOffset: { width: 0, height: 0 },
  },
  pickContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  pickCover: {
    width: 76,
    height: 100,
    borderRadius: 10,
    backgroundColor: '#2c2c2c',
    borderWidth: 1,
    borderColor: withAlpha(hudThemes.violet.colors.primary, 0.35),
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
    fontWeight: '700',
    color: hudThemes.violet.colors.muted,
    letterSpacing: 2,
  },
  pickName: {
    marginTop: 4,
    fontSize: 20,
    fontWeight: '800',
    color: hudThemes.violet.colors.text,
  },
  pickMeta: {
    marginTop: 4,
    fontSize: 14,
    color: hudThemes.violet.colors.muted,
  },
  actionRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 14,
  },
  primaryAction: {
    flex: 1,
    backgroundColor: hudThemes.violet.colors.primary,
    borderRadius: 10,
    paddingVertical: 12,
    alignItems: 'center',
    shadowColor: hudThemes.violet.colors.primary,
    shadowOpacity: 0.45,
    shadowRadius: 18,
    shadowOffset: { width: 0, height: 0 },
  },
  primaryActionText: {
    color: '#120912',
    fontWeight: '800',
    letterSpacing: 0.8,
  },
  secondaryAction: {
    flex: 1,
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: withAlpha(hudThemes.violet.colors.secondary, 0.6),
    borderRadius: 10,
    paddingVertical: 12,
    alignItems: 'center',
  },
  secondaryActionText: {
    color: hudThemes.violet.colors.secondary,
    fontWeight: '700',
    letterSpacing: 0.8,
  },
  createRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    marginBottom: 18,
  },
  createInput: {
    flex: 1,
    backgroundColor: withAlpha(hudThemes.violet.colors.secondary, 0.08),
    borderWidth: 1,
    borderColor: withAlpha(hudThemes.violet.colors.secondary, 0.25),
    borderRadius: 10,
    color: hudThemes.violet.colors.text,
    fontSize: 15,
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  createButton: {
    backgroundColor: withAlpha(hudThemes.violet.colors.secondary, 0.12),
    borderWidth: 1,
    borderColor: withAlpha(hudThemes.violet.colors.secondary, 0.35),
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderRadius: 10,
  },
  createButtonText: {
    color: hudThemes.violet.colors.secondary,
    fontWeight: '700',
    letterSpacing: 1,
  },
  listRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 14,
    paddingHorizontal: 14,
    borderRadius: 12,
    backgroundColor: withAlpha(hudThemes.violet.colors.secondary, 0.06),
    borderWidth: 1,
    borderColor: withAlpha(hudThemes.violet.colors.secondary, 0.12),
    marginBottom: 10,
  },
  listRowPressed: {
    opacity: 0.8,
  },
  listInfo: {
    flex: 1,
  },
  listName: {
    fontSize: 18,
    fontWeight: '700',
    color: hudThemes.violet.colors.text,
  },
  listMeta: {
    marginTop: 4,
    fontSize: 12,
    color: hudThemes.violet.colors.muted,
    letterSpacing: 0.75,
  },
  chevron: {
    fontSize: 26,
    color: hudThemes.violet.colors.secondary,
  },
  sectionTitle: {
    marginTop: 10,
    marginBottom: 12,
    color: hudThemes.violet.colors.muted,
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 2,
    textTransform: 'uppercase',
  },
});

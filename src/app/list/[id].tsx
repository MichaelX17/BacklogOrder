import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useRef, useState } from 'react';
import { ActivityIndicator, FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import EmptyState from '@/components/EmptyState';
import FilterSheet from '@/components/FilterSheet';
import FranchiseGroup from '@/components/FranchiseGroup';
import { useGamesStore } from '@/stores/gamesStore';
import { useListsStore } from '@/stores/listsStore';
import { HUD_THEME, rgba } from '@/theme/hudTheme';
import { applyFilters, DEFAULT_FILTERS, isFilterActive, type FilterState } from '@/utils/filters';
import {
  sortGames,
  type FranchiseGroup as FranchiseGroupData,
  type ScoredGame,
} from '@/utils/sorting';
import type { ListEntry } from '@/types';

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
  const [filters, setFilters] = useState<FilterState>(DEFAULT_FILTERS);
  const [filterSheetOpen, setFilterSheetOpen] = useState(false);
  const listRef = useRef<FlatList<FranchiseGroupData>>(null);
  const entryByGameId = new Map(listEntries.map((entry) => [entry.game.id, entry]));
  const filteredEntries = applyFilters(listEntries, filters);
  const groups = sortGames(filteredEntries.map((entry) => entry.game));
  const activeFilterCount = [
    filters.name.trim() !== '',
    filters.playtimeMin !== null,
    filters.playtimeMax !== null,
    filters.ratingMin !== null,
    filters.ratingMax !== null,
    filters.scoreMin !== null,
    filters.scoreMax !== null,
    filters.statuses.length > 0,
  ].filter(Boolean).length;

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
        <ActivityIndicator size="large" color={HUD_THEME.secondary} />
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

      <View style={styles.filterRow}>
        <Pressable
          style={({ pressed }) => [styles.filterButton, pressed && styles.actionButtonPressed]}
          onPress={() => setFilterSheetOpen(true)}
        >
          <Text style={styles.filterButtonText}>
            Filter{activeFilterCount > 0 ? ` (${activeFilterCount})` : ''}
          </Text>
        </Pressable>
        {isFilterActive(filters) ? (
          <Pressable
            style={({ pressed }) => [styles.clearButton, pressed && styles.actionButtonPressed]}
            onPress={() => setFilters(DEFAULT_FILTERS)}
          >
            <Text style={styles.clearButtonText}>Clear</Text>
          </Pressable>
        ) : null}
      </View>

      {groups.length === 0 ? (
        <EmptyState
          title={listEntries.length === 0 ? 'No games yet' : 'No matches'}
          message={
            listEntries.length === 0
              ? 'Search RAWG or add a game manually to start ranking this list.'
              : 'No games match the current filters.'
          }
        />
      ) : (
        <FlatList
          ref={listRef}
          data={groups}
          keyExtractor={(group) => group.key}
          renderItem={({ item }) => (
            <FranchiseGroup
              group={item}
              entryByGameId={entryByGameId}
              onGamePress={handleGamePress}
              onWarningPress={() => {
                const index = groups.findIndex((group) => group.key === item.key);
                if (index >= 0) {
                  listRef.current?.scrollToIndex({ index, animated: true, viewPosition: 0.5 });
                }
              }}
            />
          )}
          contentContainerStyle={styles.listContent}
        />
      )}

      <FilterSheet
        key={`${filterSheetOpen ? 'open' : 'closed'}-${JSON.stringify(filters)}`}
        visible={filterSheetOpen}
        filters={filters}
        onClose={() => setFilterSheetOpen(false)}
        onApply={(nextFilters) => {
          setFilters(nextFilters);
          setFilterSheetOpen(false);
        }}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: HUD_THEME.bgFrom,
  },
  loading: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: HUD_THEME.bgFrom,
  },
  actions: {
    flexDirection: 'row',
    gap: 8,
    padding: 16,
    paddingBottom: 8,
  },
  actionButton: {
    flex: 1,
    backgroundColor: HUD_THEME.primary,
    borderRadius: 10,
    padding: 12,
    alignItems: 'center',
    shadowColor: HUD_THEME.primary,
    shadowOpacity: 0.35,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 0 },
  },
  actionButtonSecondary: {
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: rgba(HUD_THEME.secondary, 0.6),
  },
  actionButtonPressed: {
    opacity: 0.7,
  },
  actionButtonText: {
    color: '#120912',
    fontSize: 15,
    fontWeight: '800',
    letterSpacing: 0.8,
  },
  actionButtonSecondaryText: {
    color: HUD_THEME.secondary,
    fontSize: 15,
    fontWeight: '700',
    letterSpacing: 0.8,
  },
  filterRow: {
    flexDirection: 'row',
    gap: 8,
    paddingHorizontal: 16,
    paddingBottom: 8,
  },
  filterButton: {
    flex: 1,
    backgroundColor: rgba(HUD_THEME.secondary, 0.08),
    borderRadius: 10,
    borderWidth: 1,
    borderColor: rgba(HUD_THEME.secondary, 0.25),
    padding: 12,
    alignItems: 'center',
  },
  filterButtonText: {
    color: HUD_THEME.text,
    fontSize: 15,
    fontWeight: '700',
  },
  clearButton: {
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: rgba(HUD_THEME.secondary, 0.35),
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 12,
    alignItems: 'center',
  },
  clearButtonText: {
    color: HUD_THEME.secondary,
    fontSize: 15,
    fontWeight: '600',
  },
  listContent: {
    padding: 16,
    paddingTop: 8,
    flexGrow: 1,
  },
});

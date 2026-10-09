import { router } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
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
import { hudThemes } from '@/theme/hudTheme';
import { computeScore, normalizedRating } from '@/utils/score';
import { findRecommendation } from '@/utils/recommendation';

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

  const queue = useMemo(() => {
    return [...allEntries]
      .filter((entry) => entry.status !== 'Dropped')
      .sort((a, b) => (computeScore(b.game) ?? 0) - (computeScore(a.game) ?? 0));
  }, [allEntries]);

  const recommendationScore = recommendation !== null ? computeScore(recommendation.game) : null;
  const recommendationScoreText =
    recommendation !== null && recommendation.game.playtime !== null && recommendation.game.playtime <= 0
      ? '∞'
      : recommendationScore !== null
        ? recommendationScore.toFixed(2)
        : '—';

  const currentRecommendation = recommendation ?? queue[0] ?? null;
  const upcoming = currentRecommendation
    ? queue.filter((entry) => entry.game.id !== currentRecommendation.game.id).slice(0, 2)
    : [];

  const counts = {
    playing: allEntries.filter((entry) => entry.status === 'Playing').length,
    backlog: allEntries.filter((entry) => entry.status === 'Backlog').length,
    completed: allEntries.filter((entry) => entry.status === 'Completed').length,
  };

  function handleCreateList() {
    const trimmed = newListName.trim();
    if (trimmed === '') {
      return;
    }
    createList(trimmed);
    setNewListName('');
  }

  function handleStartPlaying() {
    if (currentRecommendation === null) {
      return;
    }

    updateStatus(currentRecommendation.listId, currentRecommendation.game.id, 'Playing');
    setSkipCount(0);
  }

  function handleSkip() {
    setSkipCount((current) => current + 1);
  }

  if (!isListsLoaded) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator size="large" color={hudThemes.violet.colors.primary} />
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.deviceFrame}>
        <View style={styles.screenShell}>
          <View style={styles.gridOverlay} pointerEvents="none" />
          <View style={styles.scanlines} pointerEvents="none" />

          <View style={styles.statusBar}>
            <Text style={styles.statusText}>21:47</Text>
            <View style={styles.statusIcons}>
              <Text style={styles.statusGlyph}>◉</Text>
              <Text style={styles.statusGlyph}>⌁</Text>
              <Text style={styles.statusGlyph}>▣</Text>
            </View>
          </View>

          <View style={styles.headerRow}>
            <View>
              <Text style={styles.kicker}>{'// Next_session'}</Text>
              <Text style={styles.headline}>Up Next</Text>
            </View>
            <View style={styles.offlinePill}>
              <View style={styles.offlineDot} />
              <Text style={styles.offlineText}>Offline ready</Text>
            </View>
          </View>

          <View style={styles.scrollArea}>
            {currentRecommendation ? (
              <>
                <View style={styles.heroCard}>
                  <View style={styles.heroInner}>
                    <Image
                      source={{ uri: currentRecommendation.game.cover ?? '' }}
                      style={styles.heroImage}
                      resizeMode="cover"
                    />
                    <View style={styles.heroOverlay} />
                    <Text style={styles.pickBadge}>Pick 1/{Math.max(queue.length, 1)}</Text>
                    <View style={styles.heroBody}>
                      <StatusBadge status={currentRecommendation.status} />
                      <Text style={styles.heroTitle}>{currentRecommendation.game.name}</Text>
                      <FormulaReadout
                        game={currentRecommendation.game}
                        score={recommendationScore}
                        scoreText={recommendationScoreText}
                      />
                    </View>
                  </View>
                </View>

                <View style={styles.actionRow}>
                  <Pressable style={styles.primaryButton} onPress={handleStartPlaying}>
                    {currentRecommendation.status === 'Playing' ? (
                      <Text style={styles.primaryButtonText}>◉ Continue</Text>
                    ) : (
                      <Text style={styles.primaryButtonText}>▶ Start playing</Text>
                    )}
                  </Pressable>

                  <Pressable style={styles.secondaryButton} onPress={handleSkip} disabled={queue.length < 2}>
                    <Text style={styles.secondaryButtonText}>↷ Skip</Text>
                  </Pressable>
                </View>

                {upcoming.length > 0 ? (
                  <View style={styles.upcomingBlock}>
                    <View style={styles.sectionLabelRow}>
                      <Text style={styles.sectionLabel}>Then</Text>
                      <View style={styles.sectionRule} />
                    </View>

                    <View style={styles.upcomingList}>
                      {upcoming.map((item, index) => {
                        const itemScore = computeScore(item.game);
                        return (
                          <View key={item.game.id} style={styles.upcomingRow}>
                            <Text style={styles.upcomingIndex}>
                              {(index + 2).toString().padStart(2, '0')}
                            </Text>
                            <Text style={styles.upcomingName} numberOfLines={1}>{item.game.name}</Text>
                            <Text style={styles.upcomingScore}>{itemScore !== null ? itemScore.toFixed(2) : '—'}</Text>
                          </View>
                        );
                      })}
                    </View>
                  </View>
                ) : null}

                <View style={styles.statGrid}>
                  <StatCard label="Playing" value={counts.playing} tone="playing" />
                  <StatCard label="Backlog" value={counts.backlog} tone="backlog" />
                  <StatCard label="Done" value={counts.completed} tone="completed" />
                </View>
              </>
            ) : (
              <View style={styles.emptyStateWrap}>
                <Text style={styles.emptyGlyph}>✦</Text>
                <Text style={styles.emptyStateText}>Add games to get a pick</Text>
              </View>
            )}

            <View style={styles.createRow}>
              <TextInput
                style={styles.createInput}
                value={newListName}
                onChangeText={setNewListName}
                placeholder="New list name"
                placeholderTextColor={colors.muted}
                onSubmitEditing={handleCreateList}
                returnKeyType="done"
              />
              <Pressable style={styles.createButton} onPress={handleCreateList}>
                <Text style={styles.createButtonText}>Create</Text>
              </Pressable>
            </View>

            <Text style={styles.sectionTitle}>Your lists</Text>
            {lists.length === 0 ? (
              <EmptyState
                title="No lists yet"
                message="Create a list above to start ranking your backlog."
              />
            ) : (
              lists.map((item) => (
                <Pressable
                  key={item.id}
                  onPress={() => router.push(`/list/${item.id}`)}
                  style={({ pressed }) => [styles.listRow, pressed && styles.listRowPressed]}
                >
                  <View style={styles.listInfo}>
                    <Text style={styles.listName}>{item.name}</Text>
                    <Text style={styles.listMeta}>
                      {(gameCountByList.get(item.id) ?? 0)} {(gameCountByList.get(item.id) ?? 0) === 1 ? 'game' : 'games'}
                    </Text>
                  </View>
                  <Text style={styles.listChevron}>›</Text>
                </Pressable>
              ))
            )}
          </View>

          <View style={styles.bottomNav}>
            <NavItem icon="home" label="Home" active />
            <NavItem icon="list" label="Lists" />
            <NavItem icon="search" label="Search" />
            <NavItem icon="settings" label="Settings" />
          </View>
        </View>
      </View>
    </SafeAreaView>
  );
}

function FormulaReadout({
  game,
  score,
  scoreText,
}: {
  game: { metacritic: number | null; rating: number | null; playtime: number | null; name: string };
  score: number | null;
  scoreText: string;
}) {
  const rating = normalizedRating(game.metacritic, game.rating);
  const source = game.metacritic !== null ? 'Metacritic' : 'RAWG ×20';
  const hours = game.playtime ?? 0;

  return (
    <View style={styles.formulaRow}>
      <FormulaCell label={source} value={rating === null ? '—' : String(rating)} />
      <Text style={styles.formulaDivider}>÷</Text>
      <FormulaCell label="Hours" value={`${hours}h`} />
      <Text style={styles.formulaDivider}>=</Text>
      <View style={styles.scoreBox}>
        <Text style={styles.scoreBoxLabel}>Score</Text>
        <Text style={styles.scoreBoxValue}>{scoreText}</Text>
      </View>
    </View>
  );
}

function FormulaCell({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.formulaCell}>
      <Text style={styles.formulaCellLabel}>{label}</Text>
      <Text style={styles.formulaCellValue}>{value}</Text>
    </View>
  );
}

function StatCard({ label, value, tone }: { label: string; value: number; tone: 'playing' | 'backlog' | 'completed' }) {
  const colors = hudThemes.violet.colors;
  const toneColors = {
    playing: colors.playing,
    backlog: colors.backlog,
    completed: colors.completed,
  };

  return (
    <View style={styles.statCard}>
      <Text style={styles.statLabel}>{label}</Text>
      <Text style={[styles.statValue, { color: toneColors[tone] }]}>{value.toString().padStart(2, '0')}</Text>
    </View>
  );
}

function StatusBadge({ status }: { status: string }) {
  const colors = hudThemes.violet.colors;
  const toneMap: Record<string, string> = {
    Playing: colors.playing,
    Backlog: colors.backlog,
    Completed: colors.completed,
    Dropped: colors.dropped,
  };

  return (
    <View
      style={[
        styles.badge,
        {
          backgroundColor: toneMap[status] ?? colors.surface,
        },
      ]}
    >
      <Text style={styles.badgeText}>{status}</Text>
    </View>
  );
}

function NavItem({ icon, label, active = false }: { icon: 'home' | 'list' | 'search' | 'settings'; label: string; active?: boolean }) {
  const colors = hudThemes.violet.colors;

  const glyphMap = {
    home: '⌂',
    list: '▤',
    search: '⌕',
    settings: '⚙',
  } as const;

  return (
    <View style={styles.navItem}>
      <View style={[styles.navHex, active && { backgroundColor: colors.primary, borderColor: colors.primary }]}> 
        <Text style={[styles.navGlyph, active && { color: '#05070b' }]}>{glyphMap[icon]}</Text>
      </View>
      <Text style={[styles.navLabel, active ? { color: colors.primary } : { color: colors.muted }]}>{label}</Text>
    </View>
  );
}

const colors = hudThemes.violet.colors;

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#07040d',
  },
  deviceFrame: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#07040d',
    paddingVertical: 18,
  },
  screenShell: {
    width: '100%',
    maxWidth: 420,
    flex: 1,
    backgroundColor: colors.bgFrom,
    borderRadius: 40,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.1)',
    shadowColor: '#000',
    shadowOpacity: 0.65,
    shadowRadius: 28,
    shadowOffset: { width: 0, height: 0 },
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
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
  },
  statusBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 22,
    paddingTop: 18,
    paddingBottom: 10,
    zIndex: 2,
  },
  statusText: {
    fontFamily: 'Orbitron_700Bold',
    fontSize: 11,
    letterSpacing: 2,
    color: colors.text,
  },
  statusIcons: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  statusGlyph: {
    color: colors.text,
    fontSize: 12,
    fontWeight: '700',
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
    paddingHorizontal: 18,
    marginTop: 4,
    zIndex: 2,
  },
  kicker: {
    fontFamily: 'Orbitron_500Medium',
    fontSize: 8,
    letterSpacing: 3,
    textTransform: 'uppercase',
    color: colors.muted,
  },
  headline: {
    marginTop: 4,
    fontFamily: 'Orbitron_900Black',
    fontSize: 28,
    letterSpacing: 2,
    textTransform: 'uppercase',
    color: colors.text,
  },
  offlinePill: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 999,
    backgroundColor: 'rgba(255,255,255,0.04)',
    borderWidth: 1,
    borderColor: 'rgba(109,255,176,0.5)',
  },
  offlineDot: {
    width: 7,
    height: 7,
    transform: [{ rotate: '45deg' }],
    backgroundColor: colors.completed,
  },
  offlineText: {
    fontFamily: 'Orbitron_700Bold',
    fontSize: 8,
    letterSpacing: 2,
    color: colors.completed,
    textTransform: 'uppercase',
  },
  loading: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.bgFrom,
  },
  scrollArea: {
    flex: 1,
    paddingHorizontal: 16,
    paddingTop: 14,
    paddingBottom: 10,
    zIndex: 2,
  },
  heroCard: {
    borderRadius: 18,
    overflow: 'hidden',
    backgroundColor: colors.primary,
    padding: 1,
    shadowColor: colors.primary,
    shadowOpacity: 0.35,
    shadowRadius: 18,
    shadowOffset: { width: 0, height: 0 },
  },
  heroInner: {
    position: 'relative',
    overflow: 'hidden',
    borderRadius: 17,
    backgroundColor: '#080914',
  },
  heroImage: {
    width: '100%',
    height: 150,
    backgroundColor: '#26273d',
  },
  heroOverlay: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    backgroundColor: 'rgba(8, 10, 20, 0.2)',
  },
  pickBadge: {
    position: 'absolute',
    top: 10,
    left: 10,
    paddingHorizontal: 7,
    paddingVertical: 4,
    backgroundColor: 'rgba(0,0,0,0.65)',
    color: colors.primary,
    fontFamily: 'Orbitron_700Bold',
    fontSize: 8,
    letterSpacing: 2,
    textTransform: 'uppercase',
  },
  heroBody: {
    position: 'relative',
    marginTop: -14,
    paddingHorizontal: 12,
    paddingBottom: 14,
  },
  heroTitle: {
    marginTop: 8,
    color: '#fff',
    fontFamily: 'Rajdhani_700Bold',
    fontSize: 24,
    fontWeight: '700',
  },
  formulaRow: {
    flexDirection: 'row',
    alignItems: 'stretch',
    marginTop: 12,
    gap: 6,
  },
  formulaCell: {
    flex: 1,
    minHeight: 62,
    paddingHorizontal: 8,
    paddingVertical: 8,
    backgroundColor: 'rgba(34,230,255,0.12)',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.12)',
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  formulaCellLabel: {
    fontFamily: 'Orbitron_700Bold',
    fontSize: 7,
    letterSpacing: 2,
    textTransform: 'uppercase',
    color: colors.muted,
  },
  formulaCellValue: {
    marginTop: 5,
    fontFamily: 'Orbitron_700Bold',
    fontSize: 16,
    color: '#fff',
  },
  formulaDivider: {
    alignSelf: 'center',
    fontFamily: 'Orbitron_700Bold',
    fontSize: 18,
    color: colors.muted,
  },
  scoreBox: {
    flex: 1,
    minHeight: 62,
    paddingHorizontal: 8,
    paddingVertical: 6,
    backgroundColor: 'rgba(255,43,214,0.12)',
    borderWidth: 1,
    borderColor: 'rgba(255,43,214,0.35)',
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  scoreBoxLabel: {
    fontFamily: 'Orbitron_700Bold',
    fontSize: 7,
    letterSpacing: 2,
    textTransform: 'uppercase',
    color: colors.muted,
  },
  scoreBoxValue: {
    marginTop: 5,
    fontFamily: 'Orbitron_900Black',
    fontSize: 18,
    color: colors.primary,
  },
  actionRow: {
    flexDirection: 'row',
    alignItems: 'stretch',
    gap: 10,
    marginTop: 12,
  },
  primaryButton: {
    flex: 1,
    justifyContent: 'center',
    height: 44,
    backgroundColor: colors.primary,
    borderRadius: 10,
    shadowColor: colors.primary,
    shadowOpacity: 0.25,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 0 },
  },
  primaryButtonText: {
    color: '#05070b',
    fontFamily: 'Orbitron_700Bold',
    fontSize: 10,
    letterSpacing: 2,
    textTransform: 'uppercase',
    textAlign: 'center',
  },
  buttonIcon: { marginRight: 6 },
  secondaryButton: {
    width: 94,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: colors.secondary,
    borderRadius: 10,
    backgroundColor: 'rgba(34,230,255,0.04)',
  },
  secondaryButtonText: {
    color: colors.secondary,
    fontFamily: 'Orbitron_700Bold',
    fontSize: 10,
    letterSpacing: 2,
    textTransform: 'uppercase',
    textAlign: 'center',
  },
  upcomingBlock: {
    marginTop: 16,
  },
  sectionLabelRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  sectionLabel: {
    fontFamily: 'Orbitron_700Bold',
    fontSize: 8,
    letterSpacing: 3,
    color: colors.muted,
    textTransform: 'uppercase',
  },
  sectionRule: {
    flex: 1,
    height: 1,
    backgroundColor: 'rgba(34,230,255,0.4)',
  },
  upcomingList: {
    marginTop: 10,
    gap: 8,
  },
  upcomingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    paddingHorizontal: 10,
    paddingVertical: 8,
    borderRadius: 10,
    backgroundColor: 'rgba(255,255,255,0.02)',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.08)',
  },
  upcomingIndex: {
    fontFamily: 'Orbitron_700Bold',
    fontSize: 10,
    color: colors.secondary,
  },
  upcomingName: {
    flex: 1,
    color: '#fff',
    fontFamily: 'Rajdhani_600SemiBold',
    fontSize: 14,
  },
  upcomingScore: {
    fontFamily: 'Orbitron_700Bold',
    fontSize: 11,
    color: colors.primary,
  },
  statGrid: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 16,
  },
  statCard: {
    flex: 1,
    paddingVertical: 12,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 10,
    backgroundColor: 'rgba(255,255,255,0.02)',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.08)',
  },
  statLabel: {
    fontFamily: 'Orbitron_700Bold',
    fontSize: 7,
    letterSpacing: 2,
    textTransform: 'uppercase',
    color: colors.muted,
  },
  statValue: {
    marginTop: 6,
    fontFamily: 'Orbitron_900Black',
    fontSize: 20,
  },
  emptyStateWrap: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 36,
  },
  emptyGlyph: {
    fontSize: 30,
    color: colors.secondary,
    fontWeight: '700',
  },
  emptyStateText: {
    marginTop: 12,
    fontFamily: 'Orbitron_700Bold',
    fontSize: 10,
    letterSpacing: 3,
    textTransform: 'uppercase',
    color: colors.muted,
  },
  createRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 18,
    marginBottom: 10,
  },
  createInput: {
    flex: 1,
    minHeight: 44,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.12)',
    backgroundColor: 'rgba(255,255,255,0.02)',
    paddingHorizontal: 12,
    color: colors.text,
    fontSize: 14,
  },
  createButton: {
    minWidth: 88,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 10,
    backgroundColor: colors.secondary,
  },
  createButtonText: {
    color: '#05070b',
    fontFamily: 'Orbitron_700Bold',
    fontSize: 10,
    letterSpacing: 2,
    textTransform: 'uppercase',
  },
  sectionTitle: {
    marginTop: 8,
    marginBottom: 8,
    fontFamily: 'Orbitron_700Bold',
    fontSize: 8,
    letterSpacing: 3,
    color: colors.muted,
    textTransform: 'uppercase',
  },
  listRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 12,
    paddingVertical: 12,
    marginBottom: 8,
    borderRadius: 10,
    backgroundColor: 'rgba(255,255,255,0.02)',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.08)',
  },
  listRowPressed: {
    backgroundColor: colors.surface,
  },
  listInfo: {
    flex: 1,
  },
  listName: {
    color: colors.text,
    fontFamily: 'Rajdhani_700Bold',
    fontSize: 16,
    fontWeight: '700',
  },
  listMeta: {
    color: colors.muted,
    fontFamily: 'Rajdhani_600SemiBold',
    fontSize: 12,
    marginTop: 2,
  },
  listChevron: {
    color: colors.primary,
    fontSize: 18,
    fontWeight: '700',
  },
  badge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
  },
  badgeText: {
    fontFamily: 'Orbitron_700Bold',
    fontSize: 8,
    letterSpacing: 2,
    color: '#080914',
    textTransform: 'uppercase',
  },
  bottomNav: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: 'rgba(34,230,255,0.25)',
    backgroundColor: 'rgba(0,0,0,0.4)',
    paddingHorizontal: 22,
    paddingTop: 12,
    paddingBottom: 10,
    zIndex: 2,
  },
  navItem: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 54,
    gap: 4,
  },
  navHex: {
    width: 26,
    height: 26,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.5,
    borderRadius: 6,
    backgroundColor: 'rgba(34,230,255,0.04)',
    borderColor: 'rgba(34,230,255,0.4)',
  },
  navGlyph: {
    color: colors.secondary,
    fontSize: 12,
    fontWeight: '700',
  },
  navLabel: {
    fontFamily: 'Orbitron_700Bold',
    fontSize: 7,
    letterSpacing: 2,
    textTransform: 'uppercase',
  },
});

import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { Image, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import EmptyState from '@/components/EmptyState';
import StatusBadge from '@/components/StatusBadge';
import { gamesRepo } from '@/db/repositories';
import { useGamesStore } from '@/stores/gamesStore';
import { toScoredGame } from '@/utils/sorting';
import { GAME_STATUSES } from '@/types';
import type { Game, GameStatus } from '@/types';

type GameDetailScreenParams = {
  id: string;
  listId: string;
};

export default function GameDetailScreen() {
  const { id, listId } = useLocalSearchParams<GameDetailScreenParams>();

  const game = id !== undefined ? gamesRepo.getById(id) : null;

  if (game === null) {
    return (
      <SafeAreaView style={styles.container}>
        <EmptyState title="Game not found" message="This game does not exist anymore." />
      </SafeAreaView>
    );
  }

  return <GameDetailContent key={game.id} game={game} listId={listId} />;
}

interface GameDetailContentProps {
  game: Game;
  listId: string | undefined;
}

function GameDetailContent({ game, listId }: GameDetailContentProps) {
  const [franchise, setFranchise] = useState(game.franchise ?? '');
  const [franchiseOrder, setFranchiseOrder] = useState(
    game.franchiseOrder !== null ? String(game.franchiseOrder) : '',
  );
  const [error, setError] = useState<string | null>(null);

  const entries = useGamesStore((state) =>
    listId !== undefined ? state.entriesByList[listId] : undefined,
  );
  const loadEntriesForList = useGamesStore((state) => state.loadEntriesForList);
  const updateStatus = useGamesStore((state) => state.updateStatus);
  const removeGameFromList = useGamesStore((state) => state.removeGameFromList);

  const entry =
    entries !== undefined ? entries.find((candidate) => candidate.game.id === game.id) : undefined;
  const status: GameStatus | undefined = entry?.status;

  useEffect(() => {
    if (listId !== undefined) {
      loadEntriesForList(listId);
    }
  }, [listId, loadEntriesForList]);

  const scoredGame = toScoredGame(game);
  const hasFranchiseWarning = game.franchise !== null && game.franchiseOrder === null;

  function handleSaveFranchise() {
    const trimmedFranchise = franchise.trim();
    const trimmedOrder = franchiseOrder.trim();
    const newFranchise = trimmedFranchise === '' ? null : trimmedFranchise;

    let newOrder: number | null = null;
    if (newFranchise !== null && trimmedOrder !== '') {
      const parsed = Number(trimmedOrder);
      if (!Number.isInteger(parsed)) {
        setError('Franchise order must be a whole number.');
        return;
      }
      newOrder = parsed;
    }

    gamesRepo.updateFranchise(game.id, newFranchise, newOrder);
    setError(null);
  }

  function handleStatusChange(nextStatus: GameStatus) {
    if (listId === undefined) {
      return;
    }
    updateStatus(listId, game.id, nextStatus);
  }

  function handleRemoveFromList() {
    if (listId === undefined) {
      return;
    }
    removeGameFromList(listId, game.id);
    router.back();
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.header}>
          {game.cover !== null ? (
            <Image source={{ uri: game.cover }} style={styles.cover} />
          ) : (
            <View style={[styles.cover, styles.coverPlaceholder]} />
          )}
          <View style={styles.headerInfo}>
            <Text style={styles.name}>{game.name}</Text>
            {scoredGame.score !== null ? (
              <View style={styles.scoreBadge}>
                <Text style={styles.scoreText}>{scoredGame.score.toFixed(2)}</Text>
              </View>
            ) : (
              <Text style={styles.noScore}>No score</Text>
            )}
          </View>
        </View>

        {hasFranchiseWarning ? (
          <View style={styles.warningBox}>
            <Text style={styles.warningText}>
              This game is part of “{game.franchise}” but has no franchise order, so it renders last
              in the saga group. Set a franchise order below.
            </Text>
          </View>
        ) : null}

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Details</Text>
          <Text style={styles.metaRow}>
            Metacritic: {game.metacritic !== null ? String(game.metacritic) : '—'}
          </Text>
          <Text style={styles.metaRow}>
            RAWG rating: {game.rating !== null ? `${game.rating} / 5` : '—'}
          </Text>
          <Text style={styles.metaRow}>
            Playtime: {game.playtime !== null ? `${game.playtime} hours` : '—'}
          </Text>
          <Text style={styles.metaRow}>
            Genres: {game.genres.length > 0 ? game.genres.join(', ') : '—'}
          </Text>
          <Text style={styles.metaRow}>
            Platforms: {game.platforms.length > 0 ? game.platforms.join(', ') : '—'}
          </Text>
          <Text style={styles.metaRow}>
            Tags: {game.tags.length > 0 ? game.tags.join(', ') : '—'}
          </Text>
          {game.isManual ? <Text style={styles.metaRow}>Manually added</Text> : null}
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Franchise / Saga</Text>
          <TextInput
            style={styles.input}
            value={franchise}
            onChangeText={(text) => {
              setFranchise(text);
              setError(null);
            }}
            placeholder="Franchise name (optional)"
            placeholderTextColor="#999999"
          />
          <TextInput
            style={styles.input}
            value={franchiseOrder}
            onChangeText={(text) => {
              setFranchiseOrder(text);
              setError(null);
            }}
            placeholder="Order in saga (optional)"
            placeholderTextColor="#999999"
            keyboardType="number-pad"
          />
          {error !== null ? <Text style={styles.error}>{error}</Text> : null}
          <Pressable
            style={({ pressed }) => [styles.saveButton, pressed && styles.buttonPressed]}
            onPress={handleSaveFranchise}
          >
            <Text style={styles.saveButtonText}>Save franchise</Text>
          </Pressable>
        </View>

        {listId !== undefined && status !== undefined ? (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>State in this list</Text>
            <View style={styles.statusRow}>
              <StatusBadge status={status} />
            </View>
            <View style={styles.statusChips}>
              {GAME_STATUSES.map((candidate) => (
                <Pressable
                  key={candidate}
                  style={[styles.chip, status === candidate && styles.chipSelected]}
                  onPress={() => handleStatusChange(candidate)}
                >
                  <Text style={[styles.chipText, status === candidate && styles.chipTextSelected]}>
                    {candidate}
                  </Text>
                </Pressable>
              ))}
            </View>
            <Pressable
              style={({ pressed }) => [styles.removeButton, pressed && styles.buttonPressed]}
              onPress={handleRemoveFromList}
            >
              <Text style={styles.removeButtonText}>Remove from list</Text>
            </Pressable>
          </View>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  content: {
    padding: 16,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  cover: {
    width: 96,
    height: 128,
    borderRadius: 8,
    backgroundColor: '#eeeeee',
  },
  coverPlaceholder: {
    backgroundColor: '#dddddd',
  },
  headerInfo: {
    flex: 1,
    marginLeft: 16,
  },
  name: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#111111',
  },
  scoreBadge: {
    marginTop: 8,
    alignSelf: 'flex-start',
    backgroundColor: '#111111',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  scoreText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  noScore: {
    marginTop: 8,
    fontSize: 14,
    color: '#999999',
  },
  warningBox: {
    backgroundColor: '#fff3e0',
    borderLeftWidth: 3,
    borderLeftColor: '#f57c00',
    borderRadius: 6,
    padding: 12,
    marginBottom: 16,
  },
  warningText: {
    fontSize: 13,
    color: '#e65100',
    lineHeight: 19,
  },
  section: {
    backgroundColor: '#ffffff',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#eeeeee',
    padding: 16,
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#111111',
    marginBottom: 12,
  },
  metaRow: {
    fontSize: 14,
    color: '#333333',
    marginBottom: 6,
  },
  input: {
    borderWidth: 1,
    borderColor: '#cccccc',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    color: '#111111',
    backgroundColor: '#ffffff',
    marginBottom: 10,
  },
  error: {
    marginTop: 4,
    fontSize: 13,
    color: '#d32f2f',
  },
  saveButton: {
    backgroundColor: '#111111',
    borderRadius: 8,
    padding: 12,
    alignItems: 'center',
  },
  saveButtonText: {
    color: '#ffffff',
    fontSize: 15,
    fontWeight: '600',
  },
  statusRow: {
    marginBottom: 12,
  },
  statusChips: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 16,
  },
  chip: {
    borderWidth: 1,
    borderColor: '#cccccc',
    borderRadius: 16,
    paddingHorizontal: 14,
    paddingVertical: 8,
  },
  chipSelected: {
    backgroundColor: '#111111',
    borderColor: '#111111',
  },
  chipText: {
    fontSize: 14,
    color: '#111111',
  },
  chipTextSelected: {
    color: '#ffffff',
  },
  removeButton: {
    borderWidth: 1,
    borderColor: '#d32f2f',
    borderRadius: 8,
    padding: 12,
    alignItems: 'center',
  },
  removeButtonText: {
    color: '#d32f2f',
    fontSize: 15,
    fontWeight: '600',
  },
  buttonPressed: {
    opacity: 0.7,
  },
});

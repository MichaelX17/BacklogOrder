import { Image, Pressable, StyleSheet, Text, View } from 'react-native';

import StatusBadge from '@/components/StatusBadge';
import type { GameStatus } from '@/types';
import type { ScoredGame } from '@/utils/sorting';

interface GameCardProps {
  game: ScoredGame;
  status?: GameStatus;
  onPress: () => void;
}

export default function GameCard({ game, status, onPress }: GameCardProps) {
  const scoreText =
    game.score !== null
      ? game.score.toFixed(2)
      : status !== undefined && game.playtime !== null && game.playtime <= 0
        ? '∞'
        : null;

  const scoreDetail =
    game.metacritic !== null
      ? `Metacritic ${game.metacritic}`
      : game.rating !== null
        ? `RAWG ${game.rating.toFixed(1)}/5`
        : 'No score';

  return (
    <Pressable
      style={({ pressed }) => [styles.card, pressed && styles.cardPressed]}
      onPress={onPress}
    >
      {game.cover !== null ? (
        <Image source={{ uri: game.cover }} style={styles.cover} />
      ) : (
        <View style={[styles.cover, styles.coverPlaceholder]} />
      )}
      <View style={styles.info}>
        <Text style={styles.name} numberOfLines={2}>
          {game.name}
        </Text>
        <Text style={styles.meta} numberOfLines={1}>
          {game.playtime !== null ? `${game.playtime}h` : 'Unknown playtime'}
          {' · '}
          {scoreDetail}
        </Text>
        {status !== undefined ? (
          <View style={styles.badgeRow}>
            <StatusBadge status={status} />
          </View>
        ) : null}
      </View>
      {scoreText !== null ? (
        <View style={styles.scoreBadge}>
          <Text style={styles.scoreText}>{scoreText}</Text>
        </View>
      ) : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#eeeeee',
    padding: 10,
    marginBottom: 10,
  },
  cardPressed: {
    opacity: 0.7,
  },
  cover: {
    width: 56,
    height: 72,
    borderRadius: 6,
    backgroundColor: '#eeeeee',
  },
  coverPlaceholder: {
    backgroundColor: '#dddddd',
  },
  info: {
    flex: 1,
    marginLeft: 10,
  },
  name: {
    fontSize: 15,
    fontWeight: '600',
    color: '#111111',
  },
  meta: {
    marginTop: 3,
    fontSize: 12,
    color: '#666666',
  },
  badgeRow: {
    marginTop: 6,
  },
  scoreBadge: {
    marginLeft: 10,
    backgroundColor: '#111111',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 6,
    minWidth: 52,
    alignItems: 'center',
  },
  scoreText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: 'bold',
  },
});

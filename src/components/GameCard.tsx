import { Image, Pressable, StyleSheet, Text, View } from 'react-native';

import { StatusBadge } from '@/components/StatusBadge';
import { BevelFrame } from '@/components/hud/BevelFrame';
import type { GameStatus } from '@/types';
import type { ScoredGame } from '@/utils/sorting';

interface GameCardProps {
  game: ScoredGame;
  status?: GameStatus;
  onPress: () => void;
}

export function GameCard({ game, status, onPress }: GameCardProps) {
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
      <BevelFrame style={styles.bevelContainer}>
        <View style={styles.content}>
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
        </View>
      </BevelFrame>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    marginBottom: 12,
  },
  bevelContainer: {
    padding: 8,
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  cover: {
    width: 58,
    height: 74,
    borderRadius: 8,
  },
  coverPlaceholder: {
    backgroundColor: '#221b2f',
  },
  info: {
    flex: 1,
    marginLeft: 12,
  },
  name: {
    fontSize: 15,
    fontWeight: '700',
    color: '#f4ecff',
  },
  meta: {
    marginTop: 4,
    fontSize: 12,
    color: '#a79bbf',
  },
  badgeRow: {
    marginTop: 8,
  },
  scoreBadge: {
    marginLeft: 12,
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
    backgroundColor: 'rgba(255, 43, 214, 0.15)',
    borderWidth: 1,
    borderColor: 'rgba(255, 43, 214, 0.4)',
    alignItems: 'center',
    minWidth: 56,
  },
  scoreText: {
    color: '#ff2bd6',
    fontSize: 14,
    fontWeight: '800',
  },
  cardPressed: {
    opacity: 0.8,
  },
});
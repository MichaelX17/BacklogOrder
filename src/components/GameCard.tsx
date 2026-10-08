import { Image, Pressable, StyleSheet, Text, View } from 'react-native';

import StatusBadge from '@/components/StatusBadge';
import { HUD_THEME, rgba } from '@/theme/hudTheme';
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
    backgroundColor: rgba(HUD_THEME.bgFrom, 0.75),
    borderRadius: 14,
    borderWidth: 1,
    borderColor: rgba(HUD_THEME.secondary, 0.22),
    padding: 10,
    marginBottom: 10,
    shadowColor: HUD_THEME.shadow,
    shadowOpacity: 0.2,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 0 },
  },
  cardPressed: {
    opacity: 0.8,
  },
  cover: {
    width: 58,
    height: 74,
    borderRadius: 8,
    backgroundColor: '#221b2f',
    borderWidth: 1,
    borderColor: rgba(HUD_THEME.primary, 0.35),
  },
  coverPlaceholder: {
    backgroundColor: '#2d2e3d',
  },
  info: {
    flex: 1,
    marginLeft: 10,
  },
  name: {
    fontSize: 15,
    fontWeight: '700',
    color: HUD_THEME.text,
  },
  meta: {
    marginTop: 3,
    fontSize: 12,
    color: HUD_THEME.muted,
  },
  badgeRow: {
    marginTop: 8,
  },
  scoreBadge: {
    marginLeft: 10,
    backgroundColor: rgba(HUD_THEME.primary, 0.18),
    borderRadius: 10,
    borderWidth: 1,
    borderColor: rgba(HUD_THEME.primary, 0.5),
    paddingHorizontal: 10,
    paddingVertical: 6,
    minWidth: 56,
    alignItems: 'center',
  },
  scoreText: {
    color: HUD_THEME.primary,
    fontSize: 14,
    fontWeight: '800',
  },
});

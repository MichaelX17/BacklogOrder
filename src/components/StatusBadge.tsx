import { StyleSheet, Text, View } from 'react-native';

import type { GameStatus } from '@/types';
import { HUD_THEME, rgba } from '@/theme/hudTheme';

const STATUS_COLORS: Record<GameStatus, string> = {
  Backlog: HUD_THEME.backlog,
  Playing: HUD_THEME.playing,
  Completed: '#4de9a6',
  Dropped: '#8e8ba8',
};

interface StatusBadgeProps {
  status: GameStatus;
}

export default function StatusBadge({ status }: StatusBadgeProps) {
  const color = STATUS_COLORS[status];

  return (
    <View style={[styles.badge, { borderColor: rgba(color, 0.8) }]}>
      <View style={[styles.dot, { backgroundColor: color }]} />
      <Text style={[styles.text, { color }]}>{status}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 4,
    gap: 6,
    backgroundColor: rgba(HUD_THEME.bgFrom, 0.7),
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 2,
    marginRight: 2,
  },
  text: {
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 1.1,
    textTransform: 'uppercase',
  },
});

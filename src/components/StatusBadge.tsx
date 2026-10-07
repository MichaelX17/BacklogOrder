import { StyleSheet, Text, View } from 'react-native';

import type { GameStatus } from '@/types';

const STATUS_COLORS: Record<GameStatus, string> = {
  Backlog: '#1976d2',
  Playing: '#f57c00',
  Completed: '#388e3c',
  Dropped: '#757575',
};

interface StatusBadgeProps {
  status: GameStatus;
}

export default function StatusBadge({ status }: StatusBadgeProps) {
  const color = STATUS_COLORS[status];

  return (
    <View style={[styles.badge, { borderColor: color }]}>
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
    borderRadius: 12,
    paddingHorizontal: 8,
    paddingVertical: 3,
    gap: 4,
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
  },
  text: {
    fontSize: 12,
    fontWeight: '600',
  },
});

import { Text, View, StyleSheet } from 'react-native';

import type { GameStatus } from '@/types';

interface StatusBadgeProps {
  status: GameStatus;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <View style={styles.badge}>
      <Text style={styles.text}>{status}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
    backgroundColor: '#333',
  },
  text: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#fff',
  },
});

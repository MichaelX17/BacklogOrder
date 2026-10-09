import type { ReactNode } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { BevelFrame } from '@/components/hud/BevelFrame';

interface EmptyStateProps {
  title: string;
  message: string;
  children?: ReactNode;
}

export function EmptyState({ title, message, children }: EmptyStateProps) {
  return (
    <View style={styles.container}>
      <BevelFrame style={styles.bevelFrame}>
        <View style={styles.panel}>
          <Text style={styles.title}>{title}</Text>
          <Text style={styles.message}>{message}</Text>
          {children !== undefined ? <View style={styles.actions}>{children}</View> : null}
        </View>
      </BevelFrame>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    padding: 28,
  },
  bevelFrame: {
    width: '100%',
  },
  panel: {
    width: '100%',
    padding: 24,
    borderRadius: 16,
  },
  title: {
    fontSize: 20,
    fontWeight: '800',
    color: '#fff',
    textAlign: 'center',
    letterSpacing: 1.2,
  },
  message: {
    marginTop: 10,
    fontSize: 15,
    color: '#ccc',
    textAlign: 'center',
    lineHeight: 22,
  },
  actions: {
    marginTop: 24,
    alignItems: 'center',
    gap: 12,
  },
});

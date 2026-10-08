import type { ReactNode } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { HUD_THEME, rgba } from '@/theme/hudTheme';

interface EmptyStateProps {
  title: string;
  message: string;
  children?: ReactNode;
}

export default function EmptyState({ title, message, children }: EmptyStateProps) {
  return (
    <View style={styles.container}>
      <View style={styles.panel}>
        <Text style={styles.title}>{title}</Text>
        <Text style={styles.message}>{message}</Text>
        {children !== undefined ? <View style={styles.actions}>{children}</View> : null}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    padding: 28,
  },
  panel: {
    width: '100%',
    padding: 24,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: rgba(HUD_THEME.secondary, 0.25),
    backgroundColor: rgba(HUD_THEME.secondary, 0.05),
  },
  title: {
    fontSize: 20,
    fontWeight: '800',
    color: HUD_THEME.text,
    textAlign: 'center',
    letterSpacing: 1.2,
  },
  message: {
    marginTop: 10,
    fontSize: 15,
    color: HUD_THEME.muted,
    textAlign: 'center',
    lineHeight: 22,
  },
  actions: {
    marginTop: 24,
    alignItems: 'center',
    gap: 12,
  },
});

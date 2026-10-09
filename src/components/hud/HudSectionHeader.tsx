import { StyleSheet, Text, View } from 'react-native';

import { BevelFrame } from '@/components/hud/BevelFrame';
import { HexShape } from '@/components/hud/HexShape';

interface HudSectionHeaderProps {
  title: string;
}

export function HudSectionHeader({ title }: HudSectionHeaderProps) {
  return (
    <BevelFrame style={styles.container}>
      <View style={styles.content}>
        <HexShape style={styles.hexShape}>
          <Text style={styles.title}>{title}</Text>
        </HexShape>
      </View>
    </BevelFrame>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 12,
    marginBottom: 16,
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  hexShape: {
    marginRight: 8,
  },
  title: {
    fontSize: 20,
    fontWeight: '800',
    letterSpacing: 1,
  },
});
import { StyleSheet, Text, View } from 'react-native';

import { BevelFrame } from '@/components/hud/BevelFrame';
import { HexShape } from '@/components/hud/HexShape';

interface HudStatProps {
  label: string;
  value: string | number;
  icon?: string;
}

export function HudStat({ label, value, icon }: HudStatProps) {
  return (
    <BevelFrame style={styles.container}>
      <View style={styles.content}>
        <View style={styles.info}>
          <Text style={styles.label}>{label}</Text>
          <Text style={styles.value}>{value}</Text>
        </View>
        {icon && (
          <HexShape style={styles.iconContainer}>
            <Text style={styles.icon}>{icon}</Text>
          </HexShape>
        )}
      </View>
    </BevelFrame>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 12,
    marginBottom: 8,
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  info: {
    flex: 1,
  },
  label: {
    fontSize: 14,
    color: '#ccc',
    textTransform: 'uppercase',
    letterSpacing: 1,
    marginBottom: 4,
  },
  value: {
    fontSize: 20,
    fontWeight: '700',
    color: '#fff',
  },
  iconContainer: {
    marginLeft: 12,
  },
  icon: {
    fontSize: 16,
  },
});
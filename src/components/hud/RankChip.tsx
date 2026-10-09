import { StyleSheet, View, ViewProps, Text } from 'react-native';
import { useHudTheme } from '@/components/HudThemeProvider';

interface RankChipProps extends ViewProps {
  rank: number;
}

export function RankChip({ rank, style, ...props }: RankChipProps) {
  const { colors } = useHudTheme();
  
  return (
    <View 
      style={[styles.container, { backgroundColor: colors.surface, borderColor: colors.primary, borderRadius: 25 }, style]} 
      {...props}
    >
      <Text style={[styles.text, { color: '#fff' }]}>{rank}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    minWidth: 56,
    height: 56,
    borderWidth: 2,
    position: 'relative',
  },
  text: {
    fontWeight: '800',
    textTransform: 'uppercase',
  },
});
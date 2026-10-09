import { StyleSheet, View, ViewProps, Text } from 'react-native';
import { useHudTheme } from '@/components/HudThemeProvider';

interface SagaOrderChipProps extends ViewProps {
  order: number;
}

export function SagaOrderChip({ order, style, ...props }: SagaOrderChipProps) {
  const { colors } = useHudTheme();
  
  return (
    <View 
      style={[styles.container, { backgroundColor: colors.surface, borderColor: colors.secondary, borderRadius: 8 }, style]} 
      {...props}
    >
      <Text style={[styles.text, { color: '#fff' }]}>{order}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    minWidth: 40,
    height: 40,
    borderWidth: 1,
  },
  text: {
    fontWeight: '700',
    color: '#ffffff',
  },
});
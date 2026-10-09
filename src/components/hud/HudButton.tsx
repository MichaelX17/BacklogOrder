import { StyleSheet, View, ViewProps, Text } from 'react-native';
import { useHudTheme } from '@/components/HudThemeProvider';

interface HudButtonProps extends ViewProps {
  children: React.ReactNode;
}

export function HudButton({ children, style, ...props }: HudButtonProps) {
  const { colors } = useHudTheme();
  
  return (
    <View 
      style={[styles.container, { backgroundColor: colors.primary }, style]} 
      {...props}
    >
      <Text style={[styles.text, { color: '#000' }]}>{children}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 12,
  },
  text: {
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
});
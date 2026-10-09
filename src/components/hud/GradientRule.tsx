import { StyleSheet, View, ViewProps } from 'react-native';
import { useHudTheme } from '@/components/HudThemeProvider';

interface GradientRuleProps extends ViewProps {
  height?: number;
}

export function GradientRule({ height = 1, style, ...props }: GradientRuleProps) {
  const { colors } = useHudTheme();
  
  return (
    <View 
      style={[styles.container, { height }, style]} 
      {...props}
    >
      <View style={[styles.gradient, { backgroundColor: colors.primary, opacity: 0.3 }]} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
  },
  gradient: {
    width: '100%',
    height: '100%',
  },
});
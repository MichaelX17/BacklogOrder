import { StyleSheet, View, ViewProps, Text } from 'react-native';
import { useHudTheme } from '@/components/HudThemeProvider';

interface TextGlowProps extends ViewProps {
  children: React.ReactNode;
}

export function TextGlow({ children, style, ...props }: TextGlowProps) {
  const { colors } = useHudTheme();
  
  return (
    <View 
      style={[styles.container, style]} 
      {...props}
    >
      <Text style={[styles.text, { textShadowColor: colors.primary, textShadowOffset: { width: 0, height: 0 }, textShadowRadius: 8 }]}>
        {children}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'relative',
  },
  text: {
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
});
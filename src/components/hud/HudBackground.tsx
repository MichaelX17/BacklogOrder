import { StyleSheet, View, ViewProps } from 'react-native';
import { useHudTheme } from '@/components/HudThemeProvider';

interface HudBackgroundProps extends ViewProps {
  children: React.ReactNode;
}

export function HudBackground({ children, style, ...props }: HudBackgroundProps) {
  const { colors } = useHudTheme();
  
  return (
    <View 
      style={[styles.container, { backgroundColor: colors.bgFrom }, style]} 
      {...props}
    >
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
});
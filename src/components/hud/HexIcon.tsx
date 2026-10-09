import { StyleSheet, View, ViewProps } from 'react-native';
import { useHudTheme } from '@/components/HudThemeProvider';

interface HexIconProps extends ViewProps {
  children: React.ReactNode;
}

export function HexIcon({ children, style, ...props }: HexIconProps) {
  const { colors } = useHudTheme();
  
  return (
    <View 
      style={[styles.container, { backgroundColor: colors.secondary, borderColor: colors.primary }, style]} 
      {...props}
    >
      <View style={styles.hexInner}>
        {children}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
    borderWidth: 1.5,
    borderRadius: 4,
    overflow: 'hidden',
  },
  hexInner: {
    width: '100%',
    height: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
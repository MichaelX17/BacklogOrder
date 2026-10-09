import { StyleSheet, View, ViewProps } from 'react-native';

interface DiamondDotProps extends ViewProps {
  size?: number;
}

export function DiamondDot({ size = 12, style, ...props }: DiamondDotProps) {
  return (
    <View 
      style={[styles.container, { width: size, height: size }, style]} 
      {...props}
    >
      <View style={[styles.diamond, { borderColor: '#22e6ff' }]} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  diamond: {
    width: '100%',
    height: '100%',
    transform: [{ rotate: '45deg' }],
    borderWidth: 1.5,
    borderRadius: 2,
  },
});
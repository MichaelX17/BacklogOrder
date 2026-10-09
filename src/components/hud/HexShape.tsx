import { StyleSheet, View, ViewProps, Text } from 'react-native';

interface HexShapeProps extends ViewProps {
  size?: number;
}

export function HexShape({ size = 24, style, children, ...props }: HexShapeProps) {
  return (
    <View 
      style={[{ width: size, height: size }, styles.container]} 
      {...props}
    >
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
  },
});
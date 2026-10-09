import { StyleSheet, View, ViewProps, Text } from 'react-native';

interface HexPillProps extends ViewProps {
  children: React.ReactNode;
}

export function HexPill({ children, style, ...props }: HexPillProps) {
  return (
    <View 
      style={[styles.container, { backgroundColor: '#333', borderColor: '#555' }, style]} 
      {...props}
    >
      <Text style={[styles.text, { color: '#fff' }]}>{children}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    borderWidth: 2,
    borderRadius: 4,
  },
  text: {
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
});
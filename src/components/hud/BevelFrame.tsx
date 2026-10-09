import { StyleSheet, View, ViewProps } from 'react-native';

interface BevelFrameProps extends ViewProps {
  children: React.ReactNode;
}

export function BevelFrame({ children, style, ...props }: BevelFrameProps) {
  return (
    <View 
      style={[styles.container, { backgroundColor: '#333' }, style]} 
      {...props}
    >
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    borderRadius: 12,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
    shadowColor: '#000',
    shadowOpacity: 0.3,
    shadowOffset: { width: 2, height: 2 },
    shadowRadius: 8,
    elevation: 5,
  },
});
import { StyleSheet, View, ViewProps, Image, ImageProps } from 'react-native';
import { useHudTheme } from '@/components/HudThemeProvider';

interface BevelImageProps extends ViewProps {
  source: ImageProps['source'];
  size?: number;
}

export function BevelImage({ source, size = 48, style, ...props }: BevelImageProps) {
  const { colors } = useHudTheme();
  
  return (
    <View 
      style={[styles.container, { width: size, height: size }, style]} 
      {...props}
    >
      <Image source={source} style={styles.image} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 8,
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.1)',
    shadowColor: '#000',
    shadowOpacity: 0.3,
    shadowOffset: { width: 2, height: 2 },
    shadowRadius: 6,
    elevation: 4,
  },
  image: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
    borderRadius: 4,
  },
});
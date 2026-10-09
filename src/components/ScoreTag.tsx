import { StyleSheet, Text, View } from 'react-native';

import { BevelFrame } from '@/components/hud/BevelFrame';
import { HexShape } from '@/components/hud/HexShape';

interface ScoreTagProps {
  score: number;
  type?: 'metacritic' | 'rawg' | 'custom';
}

export function ScoreTag({ score, type = 'custom' }: ScoreTagProps) {
  const getColor = () => {
    if (type === 'metacritic') {
      return '#b6ff3b'; // emerald theme primary
    } else if (type === 'rawg') {
      return '#2df5d0'; // secondary color
    } else {
      return '#ff2bd6'; // violet theme primary
    }
  };

  const displayScore = type === 'metacritic' ? `${score}/100` : `${score.toFixed(1)}/5`;

  return (
    <BevelFrame style={styles.container}>
      <HexShape style={styles.hexShape}>
        <Text style={[styles.scoreText, { color: getColor() }]}>{displayScore}</Text>
      </HexShape>
    </BevelFrame>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 8,
  },
  hexShape: {
    minWidth: 60,
    paddingHorizontal: 8,
  },
  scoreText: {
    fontSize: 14,
    fontWeight: '700',
    textAlign: 'center',
  },
});
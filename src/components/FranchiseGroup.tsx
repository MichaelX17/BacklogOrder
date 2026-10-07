import { StyleSheet, Text, View } from 'react-native';

import GameCard from '@/components/GameCard';
import type { ListEntry } from '@/types';
import type { FranchiseGroup as FranchiseGroupData, ScoredGame } from '@/utils/sorting';

interface FranchiseGroupProps {
  group: FranchiseGroupData;
  entryByGameId: Map<string, ListEntry>;
  onGamePress: (game: ScoredGame) => void;
}

export default function FranchiseGroup({ group, entryByGameId, onGamePress }: FranchiseGroupProps) {
  return (
    <View style={styles.group}>
      {group.franchise !== null ? (
        <View style={styles.header}>
          <Text style={styles.headerTitle}>{group.franchise}</Text>
          {group.hasMissingFranchiseOrder ? (
            <Text style={styles.headerWarning}>
              Some games in this saga have no franchise order
            </Text>
          ) : null}
        </View>
      ) : null}
      {group.games.map((game) => (
        <GameCard
          key={game.id}
          game={game}
          status={entryByGameId.get(game.id)?.status}
          onPress={() => onGamePress(game)}
        />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  group: {
    marginBottom: 8,
  },
  header: {
    marginBottom: 8,
    marginTop: 8,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#111111',
  },
  headerWarning: {
    marginTop: 2,
    fontSize: 12,
    color: '#f57c00',
  },
});

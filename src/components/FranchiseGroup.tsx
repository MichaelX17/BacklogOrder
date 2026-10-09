import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { GameCard } from '@/components/GameCard';
import { BevelFrame } from '@/components/hud/BevelFrame';
import { HexShape } from '@/components/hud/HexShape';
import { DiamondDot } from '@/components/hud/DiamondDot';

import type { ListEntry } from '@/types';
import type { FranchiseGroup as FranchiseGroupData, ScoredGame } from '@/utils/sorting';

interface FranchiseGroupProps {
  group: FranchiseGroupData;
  entryByGameId: Map<string, ListEntry>;
  onGamePress: (game: ScoredGame) => void;
  onWarningPress?: () => void;
}

export function FranchiseGroup({
  group,
  entryByGameId,
  onGamePress,
  onWarningPress,
}: FranchiseGroupProps) {
  const [expanded, setExpanded] = useState(true);

  return (
    <View style={styles.group}>
      {group.franchise !== null ? (
        <Pressable style={styles.header} onPress={() => setExpanded((current) => !current)}>
          <View style={styles.headerRow}>
            <HexShape style={styles.hexShape}>
              <Text style={styles.headerTitle}>{group.franchise}</Text>
            </HexShape>
            <Text style={styles.chevron}>{expanded ? '▾' : '▸'}</Text>
          </View>
          {group.hasMissingFranchiseOrder && expanded ? (
            <Pressable
              onPress={(event) => {
                event.stopPropagation();
                onWarningPress?.();
              }}
              style={styles.warningWrap}
            >
              <DiamondDot style={styles.warningDot} />
              <Text style={styles.headerWarning}>
                Some games in this saga have no franchise order
              </Text>
            </Pressable>
          ) : null}
        </Pressable>
      ) : null}
      {expanded &&
        group.games.map((game) => (
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
  hexShape: {
    marginRight: 8,
  },
  header: {
    marginBottom: 8,
    marginTop: 8,
    padding: 8,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '800',
    letterSpacing: 1,
  },
  chevron: {
    fontSize: 18,
    color: '#22e6ff',
  },
  warningWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
    marginLeft: 4,
    gap: 6,
  },
  warningDot: {
    width: 6,
    height: 6,
    borderRadius: 2,
  },
  headerWarning: {
    fontSize: 12,
    color: '#ffb455',
  },
});

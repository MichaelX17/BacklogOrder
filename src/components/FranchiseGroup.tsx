import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import GameCard from '@/components/GameCard';
import { HUD_THEME } from '@/theme/hudTheme';
import type { ListEntry } from '@/types';
import type { FranchiseGroup as FranchiseGroupData, ScoredGame } from '@/utils/sorting';

interface FranchiseGroupProps {
  group: FranchiseGroupData;
  entryByGameId: Map<string, ListEntry>;
  onGamePress: (game: ScoredGame) => void;
  onWarningPress?: () => void;
}

export default function FranchiseGroup({
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
            <Text style={styles.headerTitle}>{group.franchise}</Text>
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
  header: {
    marginBottom: 8,
    marginTop: 8,
    paddingVertical: 2,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 4,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: HUD_THEME.text,
    letterSpacing: 1,
  },
  chevron: {
    fontSize: 18,
    color: HUD_THEME.secondary,
  },
  warningWrap: {
    marginTop: 6,
    paddingHorizontal: 4,
  },
  headerWarning: {
    fontSize: 12,
    color: '#ffb455',
  },
});

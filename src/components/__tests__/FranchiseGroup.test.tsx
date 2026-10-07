import { fireEvent, render, screen, waitFor } from '@testing-library/react-native';

import FranchiseGroup from '../FranchiseGroup';
import type { ListEntry } from '@/types';
import type { ScoredGame } from '@/utils/sorting';

function makeScoredGame(overrides: Partial<ScoredGame> = {}): ScoredGame {
  return {
    id: crypto.randomUUID(),
    rawgId: null,
    name: 'Game',
    cover: null,
    metacritic: 90,
    rating: 4.5,
    playtime: 12,
    genres: [],
    platforms: [],
    tags: [],
    isMultiplayer: false,
    isManual: false,
    franchise: 'Saga',
    franchiseOrder: 1,
    createdAt: 0,
    score: 8.5,
    ...overrides,
  };
}

function makeEntry(game: ScoredGame): ListEntry {
  return {
    listId: 'list-1',
    game,
    status: 'Backlog',
    addedAt: 0,
  };
}

describe('FranchiseGroup', () => {
  it('toggles the missing-order warning when the header is pressed', async () => {
    const group = {
      key: 'franchise:Saga',
      franchise: 'Saga',
      games: [makeScoredGame({ name: 'First', franchiseOrder: null, score: 7.5 })],
      positionScore: 7.5,
      hasMissingFranchiseOrder: true,
    };

    await render(
      <FranchiseGroup
        group={group}
        entryByGameId={
          new Map([[group.games[0]?.id ?? '', makeEntry(group.games[0] ?? makeScoredGame())]])
        }
        onGamePress={() => undefined}
      />,
    );

    expect(screen.getByText('Some games in this saga have no franchise order')).toBeTruthy();
    fireEvent.press(screen.getByText('Saga'));
    await waitFor(() => {
      expect(screen.queryByText('Some games in this saga have no franchise order')).toBeNull();
    });
  });
});

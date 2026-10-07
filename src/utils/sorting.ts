import type { Game } from '@/types';
import { computeScore } from '@/utils/score';

export interface ScoredGame extends Game {
  score: number | null;
}

export interface FranchiseGroup {
  key: string;
  franchise: string | null;
  games: ScoredGame[];
  positionScore: number;
  hasMissingFranchiseOrder: boolean;
}

export function toScoredGame(game: Game): ScoredGame {
  return { ...game, score: computeScore(game) };
}

function compareNamesAscending(a: ScoredGame, b: ScoredGame): number {
  return a.name.localeCompare(b.name);
}

function compareGroupMembers(a: ScoredGame, b: ScoredGame): number {
  const aHasOrder = a.franchiseOrder !== null;
  const bHasOrder = b.franchiseOrder !== null;

  if (aHasOrder !== bHasOrder) {
    return aHasOrder ? -1 : 1;
  }

  if (
    aHasOrder &&
    bHasOrder &&
    a.franchiseOrder !== null &&
    b.franchiseOrder !== null &&
    a.franchiseOrder !== b.franchiseOrder
  ) {
    return a.franchiseOrder - b.franchiseOrder;
  }

  const aScore = a.score ?? 0;
  const bScore = b.score ?? 0;
  if (aScore !== bScore) {
    return bScore - aScore;
  }

  return compareNamesAscending(a, b);
}

function groupLabel(group: FranchiseGroup): string {
  if (group.franchise !== null) {
    return group.franchise;
  }
  return group.games[0]?.name ?? '';
}

function createSingletonGroup(game: ScoredGame): FranchiseGroup {
  return {
    key: `game:${game.id}`,
    franchise: null,
    games: [game],
    positionScore: game.score ?? 0,
    hasMissingFranchiseOrder: false,
  };
}

export function buildFranchiseGroups(games: ScoredGame[]): FranchiseGroup[] {
  const scoredGames = games.filter((game) => game.score !== null);
  const unscoredGames = games.filter((game) => game.score === null).sort(compareNamesAscending);

  const franchiseMembers = new Map<string, ScoredGame[]>();
  const singletons: ScoredGame[] = [];

  for (const game of scoredGames) {
    if (game.franchise === null) {
      singletons.push(game);
      continue;
    }
    const members = franchiseMembers.get(game.franchise);
    if (members === undefined) {
      franchiseMembers.set(game.franchise, [game]);
    } else {
      members.push(game);
    }
  }

  const groups: FranchiseGroup[] = [];

  for (const game of singletons) {
    groups.push(createSingletonGroup(game));
  }

  for (const [franchise, members] of franchiseMembers) {
    const positionScore = members.reduce((max, game) => Math.max(max, game.score ?? 0), 0);
    groups.push({
      key: `franchise:${franchise}`,
      franchise,
      games: [...members].sort(compareGroupMembers),
      positionScore,
      hasMissingFranchiseOrder: members.some((game) => game.franchiseOrder === null),
    });
  }

  groups.sort((a, b) => {
    if (a.positionScore !== b.positionScore) {
      return b.positionScore - a.positionScore;
    }
    return groupLabel(a).localeCompare(groupLabel(b));
  });

  for (const game of unscoredGames) {
    groups.push(createSingletonGroup(game));
  }

  return groups;
}

export function sortGames(games: Game[]): FranchiseGroup[] {
  return buildFranchiseGroups(games.map(toScoredGame));
}

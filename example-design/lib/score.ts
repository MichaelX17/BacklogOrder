import type { Game } from './games'

export type ScoredGame = { game: Game; score: number | null }

export type GameEntry = { kind: 'game'; rank: number } & ScoredGame

export type FranchiseEntry = {
  kind: 'franchise'
  rank: number
  name: string
  members: ScoredGame[]
  score: number | null
  missingOrder: boolean
}

export type RankedEntry = GameEntry | FranchiseEntry

export function normalizedRating(game: Pick<Game, 'metacritic' | 'rating'>): number | null {
  if (game.metacritic != null) return game.metacritic
  if (game.rating != null) return game.rating * 20
  return null
}

export function computeScore(game: Game): number | null {
  const rating = normalizedRating(game)
  if (rating === null) return null
  if (game.playtime === 0) return Number.POSITIVE_INFINITY
  return rating / game.playtime
}

export function formatScore(score: number | null): string {
  if (score === null) return '—'
  if (!Number.isFinite(score)) return '∞'
  return score.toFixed(2)
}

export function compareScores(a: number | null, b: number | null): number {
  if (a === b) return 0
  if (a === null) return 1
  if (b === null) return -1
  return b - a
}

export function scoreGames(list: Game[]): ScoredGame[] {
  return list.map((game) => ({ game, score: computeScore(game) }))
}

export function buildRankedEntries(list: Game[]): RankedEntry[] {
  const solo: Omit<GameEntry, 'rank'>[] = []
  const groups = new Map<string, ScoredGame[]>()

  for (const scored of scoreGames(list)) {
    const franchise = scored.game.franchise
    if (franchise) {
      groups.set(franchise, [...(groups.get(franchise) ?? []), scored])
    } else {
      solo.push({ kind: 'game', ...scored })
    }
  }

  const franchises: Omit<FranchiseEntry, 'rank'>[] = [...groups.entries()].map(([name, members]) => {
    const ordered = [...members].sort(
      (a, b) =>
        (a.game.franchiseOrder ?? Number.MAX_SAFE_INTEGER) - (b.game.franchiseOrder ?? Number.MAX_SAFE_INTEGER) ||
        a.game.name.localeCompare(b.game.name),
    )
    const best = [...members].sort((a, b) => compareScores(a.score, b.score))[0]
    return {
      kind: 'franchise',
      name,
      members: ordered,
      score: best?.score ?? null,
      missingOrder: members.some((m) => m.game.franchiseOrder == null),
    }
  })

  return [...solo, ...franchises]
    .sort((a, b) => compareScores(a.score, b.score))
    .map((entry, index) => ({ ...entry, rank: index + 1 }) as RankedEntry)
}

export function getRecommendationQueue(list: Game[]): ScoredGame[] {
  const byScore = (a: ScoredGame, b: ScoredGame) => compareScores(a.score, b.score)
  const scored = scoreGames(list).filter((s) => s.score !== null)
  const playing = scored.filter((s) => s.game.status === 'playing').sort(byScore)
  const backlog = scored.filter((s) => s.game.status === 'backlog').sort(byScore)
  return [...playing, ...backlog]
}

import { Layers, TriangleAlert } from 'lucide-react'
import type { FranchiseEntry } from '@/lib/score'
import { formatScore } from '@/lib/score'
import { GameCard } from './game-card'

type FranchiseGroupProps = {
  entry: FranchiseEntry
  selectedId: string
  onSelect: (id: string) => void
}

export function FranchiseGroup({ entry, selectedId, onSelect }: FranchiseGroupProps) {
  const headingId = `saga-${entry.name.toLowerCase().replace(/\W+/g, '-')}`

  return (
    <li aria-labelledby={headingId} className="relative">
      <div className="flex items-center gap-2 pb-1.5">
        <span className="bg-hud-primary px-1 font-display text-[9px] font-black tabular-nums leading-tight text-black">
          <span className="sr-only">Rank </span>
          {entry.rank.toString().padStart(2, '0')}
        </span>
        <Layers className="size-3.5 text-hud-secondary glow-current" aria-hidden="true" />
        <h2 id={headingId} className="font-display text-[10px] font-bold uppercase tracking-[0.22em] text-white">
          <span className="text-hud-muted">Saga · </span>
          {entry.name}
        </h2>
        <span className="h-px flex-1 bg-gradient-to-r from-hud-secondary/40 to-transparent" aria-hidden="true" />
        <span className="font-display text-[8px] uppercase tracking-[0.2em] text-hud-muted">
          Best <span className="tabular-nums text-hud-secondary">{formatScore(entry.score)}</span>
        </span>
      </div>

      <div className="relative border-l border-hud-secondary/40 pl-2.5">
        <span aria-hidden="true" className="absolute -left-[3px] top-0 size-1.5 rotate-45 bg-hud-secondary" />
        <span aria-hidden="true" className="absolute -left-[3px] bottom-0 size-1.5 rotate-45 bg-hud-secondary" />
        <ul className="flex flex-col gap-2">
          {entry.members.map(({ game, score }) => (
            <GameCard
              key={game.id}
              game={game}
              score={score}
              sagaOrder={game.franchiseOrder ?? null}
              selected={selectedId === game.id}
              onSelect={() => onSelect(game.id)}
            />
          ))}
        </ul>
        {entry.missingOrder && (
          <p className="mt-1.5 flex items-center gap-1.5 text-[11px] font-medium text-hud-playing">
            <TriangleAlert className="size-3" aria-hidden="true" />
            Missing saga order on some entries
          </p>
        )}
      </div>
    </li>
  )
}

import Image from 'next/image'
import { Clock, Star } from 'lucide-react'
import type { Game } from '@/lib/games'
import { cn } from '@/lib/utils'
import { ScoreTag } from './score-tag'
import { StatusBadge } from './status-badge'

type GameCardProps = {
  game: Game
  score: number | null
  selected: boolean
  onSelect: () => void
  rank?: number
  sagaOrder?: number | null
}

export function GameCard({ game, score, selected, onSelect, rank, sagaOrder }: GameCardProps) {
  const dimmed = game.status === 'dropped' || game.status === 'completed'

  return (
    <li className={cn('transition-[filter]', selected && 'glow-primary')}>
      <button
        type="button"
        onClick={onSelect}
        aria-pressed={selected}
        className={cn(
          'bevel bevel-lg block w-full p-px text-left outline-none transition-colors focus-visible:bg-hud-primary',
          selected ? 'bg-hud-primary' : 'bg-hud-secondary/30 hover:bg-hud-secondary/60',
        )}
      >
        <div className="bevel bevel-lg relative flex items-center gap-3 bg-[color-mix(in_oklab,var(--hud-bg-from)_82%,transparent)] p-2.5 backdrop-blur-md">
          <div
            aria-hidden="true"
            className="pointer-events-none absolute inset-0 bg-gradient-to-r from-white/[0.04] via-transparent to-hud-primary/[0.06]"
          />

          <div className={cn('bevel bevel-sm relative shrink-0 p-px', selected ? 'bg-hud-primary' : 'bg-hud-secondary/70')}>
            <div className="bevel bevel-sm relative size-14 overflow-hidden bg-black">
              <Image
                src={game.cover || '/placeholder.svg'}
                alt={`${game.name} cover art`}
                fill
                sizes="56px"
                className={cn('object-cover', dimmed && 'opacity-60 grayscale-[40%]')}
              />
              {rank !== undefined && (
                <span className="absolute bottom-0 left-0 bg-hud-primary px-1 font-display text-[9px] font-black tabular-nums leading-tight text-black">
                  <span className="sr-only">Rank </span>
                  {rank.toString().padStart(2, '0')}
                </span>
              )}
              {sagaOrder !== undefined && (
                <span className="absolute bottom-0 left-0 bg-hud-secondary px-1 font-display text-[9px] font-black tabular-nums leading-tight text-black">
                  <span className="sr-only">Saga entry </span>
                  {sagaOrder === null ? '?' : `#${sagaOrder}`}
                </span>
              )}
            </div>
          </div>

          <div className="relative min-w-0 flex-1">
            <h3 className={cn('truncate text-[15px] font-bold leading-tight', dimmed ? 'text-white/70' : 'text-white')}>
              {game.name}
            </h3>
            <div className="mt-0.5 flex items-center gap-2.5 text-[11px] font-medium text-hud-muted">
              <span className="inline-flex items-center gap-1">
                <Clock className="size-3" aria-hidden="true" />
                {game.playtime}h
              </span>
              <RatingSource game={game} />
            </div>
            <StatusBadge status={game.status} className="mt-1.5" />
          </div>

          <ScoreTag score={score} highlighted={selected} />
        </div>
      </button>
    </li>
  )
}

function RatingSource({ game }: { game: Game }) {
  if (game.metacritic != null) {
    return (
      <span className="inline-flex items-center gap-1">
        <Star className="size-3" aria-hidden="true" />
        <span className="sr-only">Metacritic</span>
        MC {game.metacritic}
      </span>
    )
  }
  if (game.rating != null) {
    return (
      <span className="inline-flex items-center gap-1">
        <Star className="size-3" aria-hidden="true" />
        <span className="sr-only">RAWG rating</span>
        RAWG {game.rating.toFixed(1)}
      </span>
    )
  }
  return <span className="italic">No rating</span>
}

import Image from 'next/image'
import { Clock, Star } from 'lucide-react'
import type { Game } from '@/lib/games'
import { cn } from '@/lib/utils'
import { StatusBadge } from './status-badge'

type GameCardProps = {
  game: Game
  selected: boolean
  onSelect: () => void
}

export function GameCard({ game, selected, onSelect }: GameCardProps) {
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
              <Image src={game.cover} alt={`${game.title} cover art`} fill sizes="56px" className="object-cover" />
            </div>
          </div>

          <div className="relative min-w-0 flex-1">
            <h3 className="truncate text-[15px] font-bold leading-tight text-white">{game.title}</h3>
            <div className="mt-0.5 flex items-center gap-2.5 text-[11px] font-medium text-hud-muted">
              <span className="inline-flex items-center gap-1">
                <Clock className="size-3" aria-hidden="true" />
                {game.playtimeHours}h
              </span>
              <span className="inline-flex items-center gap-1">
                <Star className="size-3" aria-hidden="true" />
                <span className="sr-only">Metacritic</span>
                MC {game.metacritic}
              </span>
            </div>
            <StatusBadge status={game.status} className="mt-1.5" />
          </div>

          <PriceTag price={game.price} highlighted={selected} />
        </div>
      </button>
    </li>
  )
}

function PriceTag({ price, highlighted }: { price: number; highlighted: boolean }) {
  return (
    <div className={cn('relative shrink-0', highlighted ? 'glow-primary' : 'glow-secondary')}>
      <div className={cn('bevel bevel-sm p-px', highlighted ? 'bg-hud-primary' : 'bg-hud-secondary/80')}>
        <div className="bevel bevel-sm flex min-w-[58px] flex-col items-end bg-black/80 px-2 py-1">
          <span className="font-display text-[7px] font-medium uppercase tracking-[0.2em] text-hud-muted">Price</span>
          <span className={cn('font-display text-[13px] font-bold tabular-nums', highlighted ? 'text-hud-primary' : 'text-hud-secondary')}>
            <span className="text-[9px] opacity-70">$</span>
            {price.toFixed(2)}
          </span>
        </div>
      </div>
    </div>
  )
}

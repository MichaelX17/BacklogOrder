import type { GameStatus } from '@/lib/games'
import { cn } from '@/lib/utils'

export function StatusBadge({ status, className }: { status: GameStatus; className?: string }) {
  const isPlaying = status === 'playing'

  return (
    <span className={cn('inline-flex glow-current', isPlaying ? 'text-hud-playing' : 'text-hud-backlog', className)}>
      <span className="hex-pill bg-current p-px">
        <span className="hex-pill flex items-center gap-1.5 bg-black/85 px-2.5 py-[3px]">
          <span
            aria-hidden="true"
            className={cn('size-1.5 rotate-45 bg-current', isPlaying && 'animate-pulse')}
          />
          <span className="font-display text-[8px] font-bold uppercase tracking-[0.18em]">
            {isPlaying ? 'Playing' : 'Backlog'}
          </span>
        </span>
      </span>
    </span>
  )
}

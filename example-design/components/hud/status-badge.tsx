import { statusLabels, type GameStatus } from '@/lib/games'
import { cn } from '@/lib/utils'

const statusTone: Record<GameStatus, string> = {
  backlog: 'text-hud-backlog',
  playing: 'text-hud-playing',
  completed: 'text-hud-completed',
  dropped: 'text-hud-dropped',
}

export function StatusBadge({ status, className }: { status: GameStatus; className?: string }) {
  return (
    <span className={cn('inline-flex', status !== 'dropped' && 'glow-current', statusTone[status], className)}>
      <span className="hex-pill bg-current p-px">
        <span className="hex-pill flex items-center gap-1.5 bg-black/85 px-2.5 py-[3px]">
          <span
            aria-hidden="true"
            className={cn(
              'size-1.5 rotate-45',
              status === 'dropped' ? 'border border-current' : 'bg-current',
              status === 'playing' && 'animate-pulse',
            )}
          />
          <span
            className={cn(
              'font-display text-[8px] font-bold uppercase tracking-[0.18em]',
              status === 'dropped' && 'line-through decoration-1',
            )}
          >
            {statusLabels[status]}
          </span>
        </span>
      </span>
    </span>
  )
}

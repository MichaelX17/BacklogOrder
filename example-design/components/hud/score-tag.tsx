import { formatScore } from '@/lib/score'
import { cn } from '@/lib/utils'

export function ScoreTag({ score, highlighted }: { score: number | null; highlighted: boolean }) {
  return (
    <div className={cn('relative shrink-0', highlighted ? 'glow-primary' : 'glow-secondary')}>
      <div className={cn('bevel bevel-sm p-px', highlighted ? 'bg-hud-primary' : 'bg-hud-secondary/80')}>
        <div className="bevel bevel-sm flex min-w-[54px] flex-col items-end bg-black/80 px-2 py-1">
          <span className="font-display text-[7px] font-medium uppercase tracking-[0.2em] text-hud-muted">Score</span>
          <span
            className={cn(
              'font-display text-[13px] font-bold tabular-nums',
              highlighted ? 'text-hud-primary' : 'text-hud-secondary',
            )}
          >
            {formatScore(score)}
          </span>
        </div>
      </div>
    </div>
  )
}

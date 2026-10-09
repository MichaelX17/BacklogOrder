'use client'

import { useState } from 'react'
import Image from 'next/image'
import { Gamepad2, ListPlus, Play, SkipForward } from 'lucide-react'
import { games as initialGames, type Game } from '@/lib/games'
import { formatScore, getRecommendationQueue, normalizedRating } from '@/lib/score'
import { StatusBadge } from './status-badge'

export function HomeScreen() {
  const [games, setGames] = useState<Game[]>(initialGames)
  const [skipCount, setSkipCount] = useState(0)

  const queue = getRecommendationQueue(games)
  const index = queue.length ? skipCount % queue.length : 0
  const current = queue[index]
  const upcoming = queue.length > 1 ? [1, 2].map((o) => queue[(index + o) % queue.length]).slice(0, queue.length - 1) : []

  const counts = {
    playing: games.filter((g) => g.status === 'playing').length,
    backlog: games.filter((g) => g.status === 'backlog').length,
    completed: games.filter((g) => g.status === 'completed').length,
  }

  function startPlaying(id: string) {
    setGames((prev) => prev.map((g) => (g.id === id ? { ...g, status: 'playing' } : g)))
    setSkipCount(0)
  }

  return (
    <>
      <header className="relative flex items-end justify-between px-4 pt-1">
        <div>
          <p className="font-display text-[8px] font-medium uppercase tracking-[0.35em] text-hud-secondary/80">
            {'// Next_session'}
          </p>
          <h1 className="text-glow font-display text-2xl font-black uppercase leading-none tracking-[0.12em] text-white">
            Up Next
          </h1>
        </div>
        <span className="hex-pill flex items-center gap-1.5 bg-white/5 px-2.5 py-1 font-display text-[8px] font-bold uppercase tracking-[0.18em] text-hud-completed">
          <span aria-hidden="true" className="size-1.5 rotate-45 bg-current" />
          Offline ready
        </span>
      </header>

      <div className="no-scrollbar relative flex flex-1 flex-col gap-3 overflow-y-auto px-4 pb-4 pt-3">
        {current ? (
          <>
            <article aria-live="polite" className="glow-primary">
              <div className="bevel bevel-lg bg-hud-primary p-px">
                <div className="bevel bevel-lg relative overflow-hidden bg-hud-bg">
                  <div className="relative h-36 w-full">
                    <Image
                      src={current.game.cover || '/placeholder.svg'}
                      alt={`${current.game.name} cover art`}
                      fill
                      sizes="360px"
                      className="object-cover"
                    />
                    <div
                      aria-hidden="true"
                      className="absolute inset-0 bg-gradient-to-t from-hud-bg via-hud-bg/30 to-transparent"
                    />
                    <span className="absolute left-2 top-2 bg-black/70 px-1.5 py-0.5 font-display text-[8px] font-bold uppercase tracking-[0.2em] text-hud-primary backdrop-blur-sm">
                      Pick {index + 1}/{queue.length}
                    </span>
                  </div>
                  <div className="relative -mt-8 px-3 pb-3">
                    <StatusBadge status={current.game.status} />
                    <h2 className="mt-1.5 text-balance text-xl font-bold leading-tight text-white">
                      {current.game.name}
                    </h2>
                    <FormulaReadout game={current.game} score={current.score} />
                  </div>
                </div>
              </div>
            </article>

            <div className="grid grid-cols-[1fr_auto] gap-2.5">
              {current.game.status === 'playing' ? (
                <button type="button" className="glow-primary group outline-none">
                  <span className="bevel flex h-11 items-center justify-center gap-2 bg-gradient-to-r from-hud-primary to-hud-secondary font-display text-[10px] font-bold uppercase tracking-[0.14em] text-black transition-[filter] group-hover:brightness-110 group-focus-visible:brightness-125">
                    <Gamepad2 className="size-4 stroke-[2.25]" aria-hidden="true" />
                    Continue
                  </span>
                </button>
              ) : (
                <button type="button" onClick={() => startPlaying(current.game.id)} className="glow-primary group outline-none">
                  <span className="bevel flex h-11 items-center justify-center gap-2 bg-gradient-to-r from-hud-primary to-hud-secondary font-display text-[10px] font-bold uppercase tracking-[0.14em] text-black transition-[filter] group-hover:brightness-110 group-focus-visible:brightness-125">
                    <Play className="size-4 fill-current stroke-[2.25]" aria-hidden="true" />
                    Start playing
                  </span>
                </button>
              )}
              <button
                type="button"
                onClick={() => setSkipCount((c) => c + 1)}
                disabled={queue.length < 2}
                className="glow-secondary group outline-none disabled:opacity-40"
              >
                <span className="bevel block bg-hud-secondary p-px">
                  <span className="bevel flex h-[42px] items-center justify-center gap-2 bg-hud-bg/90 px-4 font-display text-[10px] font-bold uppercase tracking-[0.14em] text-hud-secondary transition-colors group-hover:bg-hud-secondary/15 group-focus-visible:bg-hud-secondary/25">
                    <SkipForward className="size-4 stroke-[2.25]" aria-hidden="true" />
                    Skip
                  </span>
                </span>
              </button>
            </div>

            {upcoming.length > 0 && (
              <section aria-labelledby="queue-heading">
                <div className="flex items-center gap-2">
                  <h3
                    id="queue-heading"
                    className="font-display text-[8px] font-bold uppercase tracking-[0.3em] text-hud-muted"
                  >
                    Then
                  </h3>
                  <span aria-hidden="true" className="h-px flex-1 bg-gradient-to-r from-hud-secondary/50 to-transparent" />
                </div>
                <ol className="mt-1.5 flex flex-col gap-1.5">
                  {upcoming.map(({ game, score }, i) => (
                    <li
                      key={game.id}
                      className="bevel bevel-sm flex items-center gap-2.5 bg-hud-surface px-2.5 py-1.5 backdrop-blur-md"
                    >
                      <span className="font-display text-[10px] font-black tabular-nums text-hud-secondary">
                        {(index + i + 2).toString().padStart(2, '0')}
                      </span>
                      <span className="min-w-0 flex-1 truncate text-sm font-semibold text-white">{game.name}</span>
                      <span className="font-display text-[11px] font-bold tabular-nums text-hud-primary">
                        {formatScore(score)}
                      </span>
                    </li>
                  ))}
                </ol>
              </section>
            )}

            <dl className="grid grid-cols-3 gap-2">
              <Stat label="Playing" value={counts.playing} className="text-hud-playing" />
              <Stat label="Backlog" value={counts.backlog} className="text-hud-backlog" />
              <Stat label="Done" value={counts.completed} className="text-hud-completed" />
            </dl>
          </>
        ) : (
          <div className="flex flex-1 flex-col items-center justify-center gap-3 text-center">
            <ListPlus className="size-8 text-hud-secondary glow-current" aria-hidden="true" />
            <p className="font-display text-[10px] uppercase tracking-[0.25em] text-hud-muted">
              Add games to get a pick
            </p>
          </div>
        )}
      </div>
    </>
  )
}

function FormulaReadout({ game, score }: { game: Game; score: number | null }) {
  const rating = normalizedRating(game)
  const source = game.metacritic != null ? 'Metacritic' : 'RAWG ×20'

  return (
    <div className="mt-2.5 flex items-stretch gap-1.5" aria-label={`Score ${formatScore(score)}: ${source} ${rating} divided by ${game.playtime} hours`}>
      <FormulaCell label={source} value={rating === null ? '—' : String(rating)} />
      <span aria-hidden="true" className="self-center font-display text-sm text-hud-muted">
        ÷
      </span>
      <FormulaCell label="Hours" value={`${game.playtime}h`} />
      <span aria-hidden="true" className="self-center font-display text-sm text-hud-muted">
        =
      </span>
      <div className="bevel bevel-sm flex-1 bg-hud-primary p-px">
        <div className="bevel bevel-sm flex h-full flex-col items-center justify-center bg-black/85 px-2 py-1">
          <span className="font-display text-[7px] uppercase tracking-[0.2em] text-hud-muted">Score</span>
          <span className="font-display text-base font-black tabular-nums text-hud-primary">{formatScore(score)}</span>
        </div>
      </div>
    </div>
  )
}

function FormulaCell({ label, value }: { label: string; value: string }) {
  return (
    <div aria-hidden="true" className="bevel bevel-sm flex-1 bg-hud-secondary/40 p-px">
      <div className="bevel bevel-sm flex h-full flex-col items-center justify-center bg-black/70 px-2 py-1">
        <span className="font-display text-[7px] uppercase tracking-[0.2em] text-hud-muted">{label}</span>
        <span className="font-display text-sm font-bold tabular-nums text-white">{value}</span>
      </div>
    </div>
  )
}

function Stat({ label, value, className }: { label: string; value: number; className: string }) {
  return (
    <div className="bevel bevel-sm flex flex-col items-center bg-hud-surface py-1.5 backdrop-blur-md">
      <dt className="font-display text-[7px] uppercase tracking-[0.22em] text-hud-muted">{label}</dt>
      <dd className={`font-display text-base font-black tabular-nums ${className}`}>
        {value.toString().padStart(2, '0')}
      </dd>
    </div>
  )
}

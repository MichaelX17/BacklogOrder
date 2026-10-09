'use client'

import { useState } from 'react'
import { ArrowDownUp, ArrowLeft, Plus, Search, SlidersHorizontal } from 'lucide-react'
import { games, statusLabels, type GameStatus } from '@/lib/games'
import { buildRankedEntries } from '@/lib/score'
import { cn } from '@/lib/utils'
import { FranchiseGroup } from './franchise-group'
import { GameCard } from './game-card'
import { HexIcon } from './hex-icon'

type Filter = 'all' | GameStatus

const filters: Filter[] = ['all', 'backlog', 'playing', 'completed', 'dropped']

export function ListScreen() {
  const [filter, setFilter] = useState<Filter>('all')
  const [selectedId, setSelectedId] = useState('devil-may-cry-5')

  const visibleGames = filter === 'all' ? games : games.filter((g) => g.status === filter)
  const entries = buildRankedEntries(visibleGames)
  const queueHours = visibleGames
    .filter((g) => g.status === 'backlog' || g.status === 'playing')
    .reduce((sum, g) => sum + g.playtime, 0)

  return (
    <>
      <header className="relative px-4 pt-1">
        <div className="flex items-center gap-3">
          <button type="button" aria-label="Go back" className="outline-none focus-visible:scale-110">
            <HexIcon tone="secondary">
              <ArrowLeft />
            </HexIcon>
          </button>
          <div className="min-w-0 flex-1">
            <p className="font-display text-[8px] font-medium uppercase tracking-[0.35em] text-hud-secondary/80">
              {'// List_01'}
            </p>
            <h1 className="text-glow truncate font-display text-xl font-black uppercase leading-none tracking-[0.1em] text-white">
              Main Backlog
            </h1>
          </div>
          <div className="text-right">
            <p className="font-display text-[8px] uppercase tracking-[0.25em] text-hud-muted">Queue</p>
            <p className="font-display text-sm font-bold tabular-nums text-hud-primary">{queueHours}h</p>
          </div>
        </div>

        <div className="mt-3 grid grid-cols-2 gap-2.5">
          <button type="button" className="glow-primary group outline-none">
            <span className="bevel flex h-10 items-center justify-center gap-2 bg-gradient-to-r from-hud-primary to-hud-secondary font-display text-[10px] font-bold uppercase tracking-[0.14em] text-black transition-[filter] group-hover:brightness-110 group-focus-visible:brightness-125">
              <Search className="size-4 stroke-[2.25]" aria-hidden="true" />
              Search RAWG
            </span>
          </button>
          <button type="button" className="glow-secondary group outline-none">
            <span className="bevel block bg-hud-secondary p-px">
              <span className="bevel flex h-[38px] items-center justify-center gap-2 bg-hud-bg/90 font-display text-[10px] font-bold uppercase tracking-[0.14em] text-hud-secondary transition-colors group-hover:bg-hud-secondary/15 group-focus-visible:bg-hud-secondary/25">
                <Plus className="size-4 stroke-[2.25]" aria-hidden="true" />
                Add manually
              </span>
            </span>
          </button>
        </div>

        <div className="bevel bevel-sm mt-3 bg-hud-secondary/25 p-px">
          <div className="bevel bevel-sm bg-hud-surface px-2 py-1.5 backdrop-blur-md">
            <div className="flex items-center gap-2">
              <HexIcon shape="diamond" size="sm" tone="secondary">
                <SlidersHorizontal />
              </HexIcon>
              <span className="font-display text-[9px] font-bold uppercase tracking-[0.2em] text-hud-muted">
                Status
              </span>
              <span className="ml-auto font-display text-[8px] uppercase tracking-[0.18em] text-hud-muted">
                Score ↓
              </span>
              <button
                type="button"
                aria-label="Change sort"
                className="text-hud-secondary outline-none hover:text-white focus-visible:text-white"
              >
                <ArrowDownUp className="size-3.5 glow-current" aria-hidden="true" />
              </button>
            </div>
            <div role="group" aria-label="Filter by status" className="no-scrollbar mt-1.5 flex gap-1 overflow-x-auto">
              {filters.map((f) => {
                const active = filter === f
                return (
                  <button
                    key={f}
                    type="button"
                    aria-pressed={active}
                    onClick={() => setFilter(f)}
                    className={cn(
                      'hex-pill shrink-0 px-2.5 py-1 font-display text-[8px] font-bold uppercase tracking-[0.12em] outline-none transition-colors',
                      active
                        ? 'bg-hud-primary text-black'
                        : 'bg-white/5 text-hud-muted hover:bg-white/10 hover:text-white focus-visible:text-white',
                    )}
                  >
                    {f === 'all' ? 'All' : statusLabels[f]}
                  </button>
                )
              })}
            </div>
          </div>
        </div>

        <div className="mt-3 flex items-center gap-2" aria-hidden="true">
          <span className="font-display text-[8px] uppercase tracking-[0.3em] text-hud-muted">
            {visibleGames.length.toString().padStart(2, '0')} games · Rating ÷ Hours
          </span>
          <span className="h-px flex-1 bg-gradient-to-r from-hud-secondary/50 to-transparent" />
          <span className="size-1 rotate-45 bg-hud-primary" />
        </div>
      </header>

      <ul className="no-scrollbar relative mt-2 flex flex-1 flex-col gap-3 overflow-y-auto px-4 pb-4 pt-1">
        {entries.length === 0 && (
          <li className="py-10 text-center font-display text-[10px] uppercase tracking-[0.25em] text-hud-muted">
            No games in this state
          </li>
        )}
        {entries.map((entry) =>
          entry.kind === 'franchise' ? (
            <FranchiseGroup key={entry.name} entry={entry} selectedId={selectedId} onSelect={setSelectedId} />
          ) : (
            <GameCard
              key={entry.game.id}
              game={entry.game}
              score={entry.score}
              rank={entry.rank}
              selected={selectedId === entry.game.id}
              onSelect={() => setSelectedId(entry.game.id)}
            />
          ),
        )}
      </ul>
    </>
  )
}

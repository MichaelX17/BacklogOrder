'use client'

import { useState } from 'react'
import {
  ArrowDownUp,
  ArrowLeft,
  BatteryFull,
  Gamepad2,
  House,
  LayoutList,
  Plus,
  Search,
  SignalHigh,
  SlidersHorizontal,
  UserRound,
  Wifi,
} from 'lucide-react'
import { games, type GameStatus } from '@/lib/games'
import { cn } from '@/lib/utils'
import { GameCard } from './game-card'
import { HexIcon } from './hex-icon'

type Filter = 'all' | GameStatus

const filters: { id: Filter; label: string }[] = [
  { id: 'all', label: 'All' },
  { id: 'playing', label: 'Playing' },
  { id: 'backlog', label: 'Backlog' },
]

export function BacklogScreen({ themeClassName }: { themeClassName: string }) {
  const [filter, setFilter] = useState<Filter>('all')
  const [selectedId, setSelectedId] = useState(games[0].id)

  const visibleGames = filter === 'all' ? games : games.filter((g) => g.status === filter)
  const total = visibleGames.reduce((sum, g) => sum + g.price, 0)

  return (
    <div className={cn(themeClassName, 'hud-screen relative flex h-full flex-col overflow-hidden font-sans')}>
      <div aria-hidden="true" className="hud-grid pointer-events-none absolute inset-0" />
      <div aria-hidden="true" className="hud-scanlines pointer-events-none absolute inset-0" />

      <StatusBar />

      <header className="relative px-4 pt-1">
        <div className="flex items-center gap-3">
          <button type="button" aria-label="Go back" className="rounded-none outline-none focus-visible:scale-110">
            <HexIcon tone="secondary">
              <ArrowLeft />
            </HexIcon>
          </button>
          <div className="min-w-0 flex-1">
            <p className="font-display text-[8px] font-medium uppercase tracking-[0.35em] text-hud-secondary/80">
              {'// Archive_01'}
            </p>
            <h1 className="text-glow font-display text-2xl font-black uppercase leading-none tracking-[0.12em] text-white">
              List
            </h1>
          </div>
          <div className="text-right">
            <p className="font-display text-[8px] uppercase tracking-[0.25em] text-hud-muted">Total</p>
            <p className="font-display text-sm font-bold tabular-nums text-hud-primary">${total.toFixed(2)}</p>
          </div>
        </div>

        <div className="mt-4 grid grid-cols-2 gap-2.5">
          <button type="button" className="glow-primary group outline-none">
            <span className="bevel flex h-11 items-center justify-center gap-2 bg-gradient-to-r from-hud-primary to-hud-secondary font-display text-[10px] font-bold uppercase tracking-[0.14em] text-black transition-[filter] group-hover:brightness-110 group-focus-visible:brightness-125">
              <Search className="size-4 stroke-[2.25]" aria-hidden="true" />
              Search RAWG
            </span>
          </button>
          <button type="button" className="glow-secondary group outline-none">
            <span className="bevel block bg-hud-secondary p-px">
              <span className="bevel flex h-[42px] items-center justify-center gap-2 bg-hud-bg/90 font-display text-[10px] font-bold uppercase tracking-[0.14em] text-hud-secondary transition-colors group-hover:bg-hud-secondary/15 group-focus-visible:bg-hud-secondary/25">
                <Plus className="size-4 stroke-[2.25]" aria-hidden="true" />
                Add manually
              </span>
            </span>
          </button>
        </div>

        <div className="bevel bevel-sm mt-3 bg-hud-secondary/25 p-px">
          <div className="bevel bevel-sm flex items-center gap-2 bg-hud-surface px-2 py-1.5 backdrop-blur-md">
            <HexIcon shape="diamond" size="sm" tone="secondary">
              <SlidersHorizontal />
            </HexIcon>
            <span className="font-display text-[9px] font-bold uppercase tracking-[0.2em] text-hud-muted">Filter</span>
            <div role="group" aria-label="Filter by status" className="ml-auto flex gap-1">
              {filters.map((f) => {
                const active = filter === f.id
                return (
                  <button
                    key={f.id}
                    type="button"
                    aria-pressed={active}
                    onClick={() => setFilter(f.id)}
                    className={cn(
                      'hex-pill px-2.5 py-1 font-display text-[8px] font-bold uppercase tracking-[0.14em] outline-none transition-colors',
                      active
                        ? 'bg-hud-primary text-black'
                        : 'bg-white/5 text-hud-muted hover:bg-white/10 hover:text-white focus-visible:text-white',
                    )}
                  >
                    {f.label}
                  </button>
                )
              })}
            </div>
            <button type="button" aria-label="Sort list" className="text-hud-secondary outline-none hover:text-white focus-visible:text-white">
              <ArrowDownUp className="size-3.5 glow-current" aria-hidden="true" />
            </button>
          </div>
        </div>

        <div className="mt-3 flex items-center gap-2" aria-hidden="true">
          <span className="font-display text-[8px] uppercase tracking-[0.3em] text-hud-muted">
            {visibleGames.length.toString().padStart(2, '0')} entries
          </span>
          <span className="h-px flex-1 bg-gradient-to-r from-hud-secondary/50 to-transparent" />
          <span className="size-1 rotate-45 bg-hud-primary" />
        </div>
      </header>

      <ul className="no-scrollbar relative mt-2 flex flex-1 flex-col gap-2.5 overflow-y-auto px-4 pb-4 pt-1">
        {visibleGames.map((game) => (
          <GameCard
            key={game.id}
            game={game}
            selected={selectedId === game.id}
            onSelect={() => setSelectedId(game.id)}
          />
        ))}
      </ul>

      <BottomNav />
    </div>
  )
}

function StatusBar() {
  return (
    <div className="relative flex items-center justify-between px-6 pb-2 pt-3 text-hud-text" aria-hidden="true">
      <span className="font-display text-[11px] font-bold tracking-wider">21:47</span>
      <div className="flex items-center gap-1.5">
        <SignalHigh className="size-3.5" />
        <Wifi className="size-3.5" />
        <BatteryFull className="size-4" />
      </div>
    </div>
  )
}

function BottomNav() {
  const items = [
    { label: 'Home', icon: House, active: false },
    { label: 'List', icon: LayoutList, active: true },
    { label: 'Playing', icon: Gamepad2, active: false },
    { label: 'Profile', icon: UserRound, active: false },
  ]

  return (
    <nav aria-label="Primary" className="relative border-t border-hud-secondary/25 bg-black/50 px-6 pb-4 pt-2 backdrop-blur-md">
      <span
        aria-hidden="true"
        className="absolute inset-x-10 -top-px h-px bg-gradient-to-r from-transparent via-hud-primary to-transparent"
      />
      <ul className="flex items-center justify-between">
        {items.map(({ label, icon: Icon, active }) => (
          <li key={label}>
            <a
              href="#"
              aria-current={active ? 'page' : undefined}
              className="flex flex-col items-center gap-1 outline-none"
            >
              <HexIcon size="sm" tone={active ? 'primary' : 'secondary'} active={active} className={active ? '' : 'opacity-60'}>
                <Icon />
              </HexIcon>
              <span
                className={cn(
                  'font-display text-[7px] font-bold uppercase tracking-[0.2em]',
                  active ? 'text-hud-primary' : 'text-hud-muted',
                )}
              >
                {label}
              </span>
            </a>
          </li>
        ))}
      </ul>
    </nav>
  )
}

import { BatteryFull, House, LayoutList, Search, Settings, SignalHigh, Wifi } from 'lucide-react'
import { cn } from '@/lib/utils'
import { HexIcon } from './hex-icon'

export type ScreenId = 'home' | 'list'

type ScreenShellProps = {
  themeClassName: string
  active: ScreenId
  onNavigate: (screen: ScreenId) => void
  children: React.ReactNode
}

export function ScreenShell({ themeClassName, active, onNavigate, children }: ScreenShellProps) {
  return (
    <div className={cn(themeClassName, 'hud-screen relative flex h-full flex-col overflow-hidden font-sans')}>
      <div aria-hidden="true" className="hud-grid pointer-events-none absolute inset-0" />
      <div aria-hidden="true" className="hud-scanlines pointer-events-none absolute inset-0" />
      <StatusBar />
      {children}
      <BottomNav active={active} onNavigate={onNavigate} />
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

const navItems = [
  { label: 'Home', icon: House, screen: 'home' },
  { label: 'Lists', icon: LayoutList, screen: 'list' },
  { label: 'Search', icon: Search, screen: null },
  { label: 'Settings', icon: Settings, screen: null },
] as const

function BottomNav({ active, onNavigate }: { active: ScreenId; onNavigate: (screen: ScreenId) => void }) {
  return (
    <nav
      aria-label="Primary"
      className="relative border-t border-hud-secondary/25 bg-black/50 px-6 pb-4 pt-2 backdrop-blur-md"
    >
      <span
        aria-hidden="true"
        className="absolute inset-x-10 -top-px h-px bg-gradient-to-r from-transparent via-hud-primary to-transparent"
      />
      <ul className="flex items-center justify-between">
        {navItems.map(({ label, icon: Icon, screen }) => {
          const isActive = screen === active
          return (
            <li key={label}>
              <button
                type="button"
                onClick={() => screen && onNavigate(screen)}
                aria-current={isActive ? 'page' : undefined}
                className="flex flex-col items-center gap-1 outline-none"
              >
                <HexIcon
                  size="sm"
                  tone={isActive ? 'primary' : 'secondary'}
                  active={isActive}
                  className={isActive ? '' : 'opacity-60'}
                >
                  <Icon />
                </HexIcon>
                <span
                  className={cn(
                    'font-display text-[7px] font-bold uppercase tracking-[0.2em]',
                    isActive ? 'text-hud-primary' : 'text-hud-muted',
                  )}
                >
                  {label}
                </span>
              </button>
            </li>
          )
        })}
      </ul>
    </nav>
  )
}

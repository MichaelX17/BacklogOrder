'use client'

import { useState } from 'react'
import { hudThemes } from '@/lib/games'
import { cn } from '@/lib/utils'
import { HomeScreen } from './home-screen'
import { ListScreen } from './list-screen'
import { PhoneFrame } from './phone-frame'
import { ScreenShell, type ScreenId } from './screen-shell'

const screens: { id: ScreenId; label: string }[] = [
  { id: 'home', label: 'Home · index.tsx' },
  { id: 'list', label: 'List · list/[id].tsx' },
]

export function ThemeShowcase() {
  const [screen, setScreen] = useState<ScreenId>('home')

  return (
    <>
      <div role="group" aria-label="Screen" className="mb-10 flex flex-wrap gap-2">
        {screens.map((s) => (
          <button
            key={s.id}
            type="button"
            aria-pressed={screen === s.id}
            onClick={() => setScreen(s.id)}
            className={cn(
              'border px-4 py-2 font-display text-[10px] font-bold uppercase tracking-[0.2em] outline-none transition-colors focus-visible:ring-2 focus-visible:ring-white/40',
              screen === s.id
                ? 'border-white bg-white text-black'
                : 'border-white/15 text-neutral-400 hover:border-white/40 hover:text-white',
            )}
          >
            {s.label}
          </button>
        ))}
      </div>

      <section aria-label="Theme variants" className="grid grid-cols-1 justify-items-center gap-12 md:grid-cols-2 xl:grid-cols-3">
        {hudThemes.map((theme) => (
          <figure key={theme.id} className="flex w-full flex-col items-center gap-5">
            <PhoneFrame label={`${theme.name} theme preview`}>
              <ScreenShell themeClassName={theme.className} active={screen} onNavigate={setScreen}>
                {screen === 'home' ? <HomeScreen /> : <ListScreen />}
              </ScreenShell>
            </PhoneFrame>
            <figcaption className="flex w-full max-w-[380px] items-center justify-between">
              <div>
                <p className="font-display text-[9px] uppercase tracking-[0.3em] text-neutral-500">{theme.version}</p>
                <p className="font-display text-sm font-bold uppercase tracking-[0.12em]">{theme.name}</p>
              </div>
              <ul className="flex gap-1.5" aria-label={`${theme.name} palette`}>
                {theme.swatches.map((color) => (
                  <li
                    key={color}
                    title={color}
                    className="size-5 rotate-45 border border-white/15"
                    style={{ backgroundColor: color }}
                  >
                    <span className="sr-only">{color}</span>
                  </li>
                ))}
              </ul>
            </figcaption>
          </figure>
        ))}
      </section>
    </>
  )
}

import { BacklogScreen } from '@/components/hud/backlog-screen'
import { PhoneFrame } from '@/components/hud/phone-frame'
import { hudThemes } from '@/lib/games'

export default function Page() {
  return (
    <main className="min-h-screen bg-[#07060a] text-white">
      <div className="mx-auto max-w-7xl px-6 py-12 lg:py-16">
        <header className="mb-12 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="font-display text-[10px] font-medium uppercase tracking-[0.4em] text-neutral-500">
              Backlog HUD · Mobile UI concept
            </p>
            <h1 className="mt-2 text-balance font-display text-3xl font-black uppercase tracking-[0.08em] md:text-4xl">
              Dark Fantasy Cyber
            </h1>
          </div>
          <p className="max-w-md text-pretty text-base leading-relaxed text-neutral-400">
            One layout, one primary accent variable. Every glow, bevel and badge is driven by theme tokens, so swapping a
            single class reskins the whole screen.
          </p>
        </header>

        <section
          aria-label="Theme variants"
          className="grid grid-cols-1 justify-items-center gap-12 md:grid-cols-2 xl:grid-cols-3"
        >
          {hudThemes.map((theme) => (
            <figure key={theme.id} className="flex w-full flex-col items-center gap-5">
              <PhoneFrame label={`${theme.name} theme preview`}>
                <BacklogScreen themeClassName={theme.className} />
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
      </div>
    </main>
  )
}

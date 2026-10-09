import { ThemeShowcase } from '@/components/hud/theme-showcase'

export default function Page() {
  return (
    <main className="min-h-screen bg-[#07060a] text-white">
      <div className="mx-auto max-w-7xl px-6 py-12 lg:py-16">
        <header className="mb-8 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="font-display text-[10px] font-medium uppercase tracking-[0.4em] text-neutral-500">
              BacklogOrder · HUD theme concept
            </p>
            <h1 className="mt-2 text-balance font-display text-3xl font-black uppercase tracking-[0.08em] md:text-4xl">
              Dark Fantasy Cyber
            </h1>
          </div>
          <p className="max-w-md text-pretty text-base leading-relaxed text-neutral-400">
            {'Score = NormalizedRating ÷ Playtime. Every screen is driven by the same theme tokens, so a single class swap reskins the whole app.'}
          </p>
        </header>

        <ThemeShowcase />
      </div>
    </main>
  )
}

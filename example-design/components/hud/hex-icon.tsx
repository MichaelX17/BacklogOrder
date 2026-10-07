import { cn } from '@/lib/utils'

type HexIconProps = {
  children: React.ReactNode
  shape?: 'hex' | 'diamond'
  tone?: 'primary' | 'secondary'
  size?: 'sm' | 'md'
  active?: boolean
  className?: string
}

export function HexIcon({
  children,
  shape = 'hex',
  tone = 'secondary',
  size = 'md',
  active = false,
  className,
}: HexIconProps) {
  const toneBorder = tone === 'primary' ? 'bg-hud-primary' : 'bg-hud-secondary'
  const toneText = tone === 'primary' ? 'text-hud-primary' : 'text-hud-secondary'
  const dims = size === 'sm' ? 'size-7' : 'size-9'

  if (shape === 'diamond') {
    return (
      <span
        aria-hidden="true"
        className={cn(
          'relative inline-flex shrink-0 items-center justify-center',
          dims,
          tone === 'primary' ? 'glow-primary' : 'glow-secondary',
          className,
        )}
      >
        <span
          className={cn(
            'absolute inset-[18%] rotate-45 border',
            tone === 'primary' ? 'border-hud-primary' : 'border-hud-secondary',
            active ? (tone === 'primary' ? 'bg-hud-primary/25' : 'bg-hud-secondary/25') : 'bg-black/40',
          )}
        />
        <span className={cn('relative [&_svg]:size-3.5 [&_svg]:stroke-[1.5]', toneText)}>{children}</span>
      </span>
    )
  }

  return (
    <span
      aria-hidden="true"
      className={cn(
        'relative inline-flex shrink-0',
        dims,
        tone === 'primary' ? 'glow-primary' : 'glow-secondary',
        className,
      )}
    >
      <span className={cn('hex absolute inset-0', toneBorder)} />
      <span
        className={cn(
          'hex absolute inset-px flex items-center justify-center',
          active ? (tone === 'primary' ? 'bg-[color-mix(in_oklab,var(--hud-primary)_30%,#000)]' : 'bg-[color-mix(in_oklab,var(--hud-secondary)_25%,#000)]') : 'bg-hud-bg',
          toneText,
          size === 'sm' ? '[&_svg]:size-3.5' : '[&_svg]:size-4',
          '[&_svg]:stroke-[1.5]',
        )}
      >
        {children}
      </span>
    </span>
  )
}

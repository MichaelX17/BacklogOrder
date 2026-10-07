export function PhoneFrame({ children, label }: { children: React.ReactNode; label: string }) {
  return (
    <div
      role="region"
      aria-label={label}
      className="relative w-full max-w-[380px] rounded-[44px] bg-gradient-to-b from-neutral-700 via-neutral-900 to-neutral-800 p-[10px] shadow-[0_40px_80px_-20px_rgb(0_0_0/0.8),0_0_0_1px_rgb(255_255_255/0.06)]"
    >
      <div className="relative aspect-[9/16] w-full overflow-hidden rounded-[34px] bg-black">
        <div
          aria-hidden="true"
          className="absolute left-1/2 top-2 z-20 h-5 w-24 -translate-x-1/2 rounded-full bg-black"
        />
        {children}
      </div>
    </div>
  )
}

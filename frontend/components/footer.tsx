export function Footer() {
  return (
    <footer className="relative overflow-hidden border-t border-white/10 bg-[#0A0A0A] px-4 pb-8 pt-16 text-white sm:px-6">
      <div className="relative z-10 mx-auto max-w-7xl">
        <div className="grid gap-10 border-b border-white/10 pb-12 lg:grid-cols-12">
          <div className="space-y-4 lg:col-span-5">
            <a href="/" className="flex items-center gap-2">
              <div className="flex size-8 items-center justify-center rounded-lg bg-[#CCFF00] text-lg font-black text-[#0D0D0D]">
                A
              </div>
              <span className="text-2xl font-black tracking-tighter">
                AdFix<span className="ml-1 font-light text-[#CCFF00]">Studio</span>
              </span>
            </a>
            <p className="max-w-md text-xs font-light leading-relaxed text-white/60 sm:text-sm">
              Turn your average ad into a high-performing story. We help creators and brands make their ads scroll-stopping, engaging, and profitable.
            </p>
          </div>

          <div className="space-y-3 lg:col-span-4">
            <h4 className="font-mono text-xs font-bold uppercase tracking-widest text-[#CCFF00]">
              Navigation
            </h4>
            <ul className="grid grid-cols-2 gap-2 font-mono text-xs">
              <li><a href="#vsl" className="py-1 text-white/60 transition-colors hover:text-[#CCFF00]">Overview</a></li>
              <li><a href="#problem" className="py-1 text-white/60 transition-colors hover:text-[#CCFF00]">The Problem</a></li>
              <li><a href="#services" className="py-1 text-white/60 transition-colors hover:text-[#CCFF00]">What You&apos;ll Learn</a></li>
              <li><a href="#curriculum" className="py-1 text-white/60 transition-colors hover:text-[#CCFF00]">Curriculum</a></li>
              <li><a href="#who-its-for" className="py-1 text-white/60 transition-colors hover:text-[#CCFF00]">Who It&apos;s For</a></li>
              <li><a href="#pricing" className="py-1 text-white/60 transition-colors hover:text-[#CCFF00]">Pricing</a></li>
              <li><a href="#faq" className="py-1 text-white/60 transition-colors hover:text-[#CCFF00]">FAQ</a></li>
            </ul>
          </div>

          <div className="space-y-3 lg:col-span-3">
            <h4 className="font-mono text-xs font-bold uppercase tracking-widest text-[#CCFF00]">
              Start here
            </h4>
            <ul className="flex flex-col gap-2 font-mono text-xs">
              <li><a href="#pricing" className="text-white/60 transition-colors hover:text-[#CCFF00]">Course offer</a></li>
              <li><a href="#faq" className="text-white/60 transition-colors hover:text-[#CCFF00]">Common questions</a></li>
              <li><a href="/free-resources" className="text-white/60 transition-colors hover:text-[#CCFF00]">Free resources</a></li>
            </ul>
          </div>
        </div>

        <div className="flex flex-col gap-5 pt-6 font-mono text-xs text-white/40">
          <div className="flex flex-wrap gap-x-4 gap-y-2">
            {[["Terms & Conditions", "/terms"], ["Privacy Policy", "/privacy"], ["Refund Policy", "/refund-policy"], ["Cookie Policy", "/cookie-policy"], ["Course Usage", "/course-usage"], ["Disclaimer", "/disclaimer"], ["Contact / Support", "/contact"]].map(([label, href]) => <a key={href} href={href} className="transition-colors hover:text-[#CCFF00]">{label}</a>)}
          </div>
          <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between"><div>© 2026 AdFix Studio. All rights reserved.</div><a href="mailto:support@adfixstudio.com" className="text-[#CCFF00]/80 hover:text-[#CCFF00]">support@adfixstudio.com</a></div>
        </div>
      </div>

      <div className="pointer-events-none absolute -bottom-10 left-1/2 -translate-x-1/2 select-none text-[12rem] font-black leading-none text-white/[0.015] sm:text-[18rem]">
        ADFIX STUDIO
      </div>
    </footer>
  )
}

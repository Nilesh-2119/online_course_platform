"use client"

import { motion } from "framer-motion"
import { MobileNavigationMenu } from "@/components/mobile-navigation-menu"

const links = [["Why Us", "#problems"], ["What You'll Learn", "#learn"], ["Course Map", "#curriculum"], ["Who It's For", "#for-you"], ["FAQ", "#faq"]] as const

export function Navigation() {
  return (
    <motion.header initial={{ y: -80 }} animate={{ y: 0 }} className="fixed left-0 right-0 top-0 z-50 px-4 pt-4 md:px-6">
      <nav className="mx-auto flex max-w-7xl items-center justify-between rounded-full border border-foreground/10 bg-background/85 px-4 py-3 shadow-sm backdrop-blur-md md:px-6">
        <a href="#hero" className="text-xl font-black tracking-[-.07em] text-foreground">Ad<span className="text-accent">Fix</span><span className="ml-1 font-mono text-[9px] tracking-normal text-muted-foreground">STUDIO</span></a>
        <div className="hidden items-center gap-6 md:flex">
          {links.map(([label, href]) => <a key={href} href={href} className="group relative font-mono text-xs font-semibold text-muted-foreground transition-colors hover:text-foreground">{label}<span className="absolute -bottom-1 left-0 h-px w-0 bg-accent transition-[width] duration-300 group-hover:w-full" /></a>)}
        </div>
        <div className="hidden items-center gap-2 md:flex">
          <a href="/login" className="rounded-full border border-foreground/20 px-4 py-2.5 text-xs font-bold text-foreground transition-colors hover:bg-foreground hover:text-background">LOGIN</a>
          <a href="/create-account" className="rounded-full bg-accent px-5 py-2.5 text-xs font-bold text-accent-foreground transition-transform hover:scale-[1.03]">CREATE ACCOUNT</a>
        </div>
        <MobileNavigationMenu />
      </nav>
    </motion.header>
  )
}

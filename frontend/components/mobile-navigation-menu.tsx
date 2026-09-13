"use client"

import { AnimatePresence, motion } from "framer-motion"
import { Menu, X } from "lucide-react"
import { useState } from "react"

const links = [["Why Us", "#vsl"], ["Learn", "#learn"], ["Course", "#curriculum"], ["For You", "#for-you"], ["FAQ", "#faq"]] as const

export function MobileNavigationMenu() {
  const [open, setOpen] = useState(false)

  return (
    <>
      <button type="button" onClick={() => setOpen((value) => !value)} className="p-1 md:hidden" aria-label={open ? "Close menu" : "Open menu"} aria-expanded={open}>
        {open ? <X className="size-5" /> : <Menu className="size-5" />}
      </button>
      <AnimatePresence>
        {open && (
          <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: "auto" }} exit={{ opacity: 0, height: 0 }} className="mx-4 mt-2 overflow-hidden rounded-3xl bg-foreground p-5 md:hidden">
            <div className="flex flex-col gap-4">
              {links.map(([label, href]) => <a key={href} href={href} onClick={() => setOpen(false)} className="font-mono text-sm font-semibold text-background/75">{label}</a>)}
              <a href="/login" onClick={() => setOpen(false)} className="font-mono text-sm font-semibold text-background/75">LOGIN</a>
              <a href="/create-account" onClick={() => setOpen(false)} className="rounded-full bg-accent px-4 py-3 text-center text-xs font-bold text-accent-foreground">CREATE ACCOUNT</a>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  )
}

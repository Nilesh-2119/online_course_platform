"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { useState, type ReactNode } from "react"
import { BarChart3, Bell, FileText, Grid2X2, LogOut, Menu, Settings, Tag, Users, X } from "lucide-react"
import { adminNavItems, type AdminNavIcon } from "@/mocks/seed-data"
import { cn } from "@/lib/utils"
import { useAuth } from "@/lib/auth"
import { AdminGuard } from "./admin-guard"

const icons: Record<AdminNavIcon, typeof Grid2X2> = { grid: Grid2X2, users: Users, file: FileText, tag: Tag, chart: BarChart3, bell: Bell }

// ---------------------------------------------------------------------------
// Sidebar
// ---------------------------------------------------------------------------

function Sidebar({ mobile = false, onClose }: { mobile?: boolean; onClose?: () => void }) {
  const path = usePathname()
  const { logout } = useAuth()

  return (
    <aside className={cn(mobile ? "fixed inset-y-0 left-0 z-50 w-72" : "fixed inset-y-0 left-0 z-40 hidden w-64 lg:block", "border-r border-border bg-background p-6")}>
      <div className="flex h-full flex-col">
        <div className="mb-10">
          <div className="text-lg font-black tracking-tight">ADFIX <span className="font-normal text-accent">STUDIO</span></div>
          <div className="mt-1 font-mono text-[10px] tracking-[.22em] text-muted-foreground">ADMIN PORTAL</div>
        </div>
        <nav className="flex flex-col gap-1" aria-label="Admin navigation">
          {adminNavItems.map((item) => {
            const Icon = icons[item.icon]
            return (
              <Link onClick={onClose} key={item.href} href={item.href} className={cn("flex items-center gap-3 rounded-md px-3 py-3 font-mono text-[11px] tracking-[.12em] transition-colors", path === item.href ? "bg-foreground text-background" : "text-muted-foreground hover:bg-muted hover:text-foreground")}>
                <Icon size={16} />{item.label}
              </Link>
            )
          })}
        </nav>
        <div className="mt-auto flex flex-col gap-1 border-t border-border pt-5">
          <Link onClick={onClose} href="/admin/settings" className="flex items-center gap-3 rounded-md px-3 py-3 font-mono text-[11px] tracking-[.12em] text-muted-foreground hover:bg-muted hover:text-foreground">
            <Settings size={16} />SETTINGS
          </Link>
          <button
            onClick={() => {
              logout()
              window.location.href = "/login"
            }}
            className="flex items-center gap-3 rounded-md px-3 py-3 font-mono text-[11px] tracking-[.12em] text-muted-foreground transition-colors hover:bg-muted hover:text-foreground text-left"
          >
            <LogOut size={16} />LOG OUT
          </button>
        </div>
      </div>
    </aside>
  )
}

// ---------------------------------------------------------------------------
// TopBar
// ---------------------------------------------------------------------------

function TopBar({ title, onMenu }: { title: string; onMenu?: () => void }) {
  const { user } = useAuth()
  const displayName = user?.name || "Admin"
  const displayEmail = user?.email || "adfixstudio25@gmail.com"
  const initial = (displayName[0] || "A").toUpperCase()

  return (
    <header className="sticky top-0 z-30 flex h-20 items-center justify-between border-b border-border bg-background/95 px-5 backdrop-blur lg:px-10">
      <div className="flex items-center gap-3">
        <button aria-label="Open menu" onClick={onMenu} className="rounded-md border border-border p-2 lg:hidden">
          <Menu size={18} />
        </button>
        <div>
          <h1 className="text-xl font-bold tracking-tight">{title}</h1>
          <p className="font-mono text-[10px] tracking-[.12em] text-muted-foreground">ADFIX STUDIO / ADMIN</p>
        </div>
      </div>
      <div className="hidden items-center gap-3 sm:flex">
        <div className="flex size-9 items-center justify-center rounded-full bg-foreground text-xs font-bold text-background">{initial}</div>
        <div>
          <div className="font-mono text-[10px] font-bold tracking-widest">{displayName.toUpperCase()}</div>
          <div className="text-xs text-muted-foreground">{displayEmail}</div>
        </div>
      </div>
    </header>
  )
}

// ---------------------------------------------------------------------------
// Confirm Dialog
// ---------------------------------------------------------------------------

export type ConfirmAction = { title: string; body: string; action: () => void; label: string } | null

export function ConfirmDialog({ confirm, close }: { confirm: ConfirmAction; close: () => void }) {
  if (!confirm) return null
  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center bg-foreground/30 p-5">
      <div role="dialog" aria-modal="true" aria-label={confirm.title} className="w-full max-w-md rounded-xl border border-border bg-background p-6 shadow-xl">
        <h2 className="text-lg font-bold">{confirm.title}</h2>
        <p className="mt-3 text-sm leading-6 text-muted-foreground">{confirm.body}</p>
        <div className="mt-6 flex justify-end gap-3">
          <button onClick={close} className="rounded-md border border-border px-4 py-3 font-mono text-[10px] tracking-widest">CANCEL</button>
          <button onClick={() => { confirm.action(); close() }} className="rounded-md bg-foreground px-4 py-3 font-mono text-[10px] tracking-widest text-background">{confirm.label}</button>
        </div>
      </div>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Shared UI Pieces
// ---------------------------------------------------------------------------

export function Badge({ children, tone = "default" }: { children: ReactNode; tone?: "default" | "good" | "bad" }) {
  return (
    <span className={cn("inline-flex rounded-full border px-2 py-1 font-mono text-[9px] tracking-wider", tone === "good" ? "border-accent/60 text-foreground" : tone === "bad" ? "border-destructive/50 text-destructive" : "border-border text-muted-foreground")}>
      {children}
    </span>
  )
}

export function Progress({ value }: { value: number }) {
  return (
    <div className="flex items-center gap-3">
      <div className="h-1.5 flex-1 rounded-full bg-muted">
        <div className="h-full rounded-full bg-accent" style={{ width: `${value}%` }} />
      </div>
      <span className="font-mono text-[10px] text-muted-foreground">{value}%</span>
    </div>
  )
}

export function Card({ title, children, action }: { title: string; children: ReactNode; action?: ReactNode }) {
  return (
    <section className="rounded-lg border border-border bg-card">
      <div className="flex items-center justify-between border-b border-border p-5">
        <h2 className="font-mono text-xs font-bold tracking-widest">{title}</h2>
        {action}
      </div>
      <div className="p-5">{children}</div>
    </section>
  )
}

export function SearchBox({ value, onChange, placeholder }: { value: string; onChange: (v: string) => void; placeholder: string }) {
  return (
    <div className="flex items-center gap-3 rounded-md border border-border bg-card px-3 py-2 sm:max-w-sm">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-muted-foreground"><circle cx="11" cy="11" r="8" /><path d="m21 21-4.3-4.3" /></svg>
      <input aria-label={placeholder} value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder} className="w-full bg-transparent text-sm outline-none" />
    </div>
  )
}

// ---------------------------------------------------------------------------
// Admin Shell (layout wrapper)
// ---------------------------------------------------------------------------

export function AdminShell({ title, children }: { title: string; children: ReactNode }) {
  const [open, setOpen] = useState(false)

  return (
    <AdminGuard>
      <div className="min-h-screen bg-background text-foreground">
        <Sidebar />
        <div className="lg:pl-64">
          <TopBar title={title} onMenu={() => setOpen(true)} />
          <main className="mx-auto max-w-[1600px] p-5 lg:p-10">
            {children}
          </main>
        </div>
        {open && (
          <>
            <div onClick={() => setOpen(false)} className="fixed inset-0 z-40 bg-foreground/20 lg:hidden" role="presentation" />
            <Sidebar mobile onClose={() => setOpen(false)} />
            <button aria-label="Close menu" onClick={() => setOpen(false)} className="fixed right-4 top-5 z-[60] rounded-md bg-background p-2 lg:hidden">
              <X size={18} />
            </button>
          </>
        )}
      </div>
    </AdminGuard>
  )
}


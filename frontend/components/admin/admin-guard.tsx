"use client"

import { useEffect, useState, type ReactNode } from "react"
import Link from "next/link"
import { useAuth } from "@/lib/auth"
import { ShieldAlert, LogOut, ArrowRight, Loader2 } from "lucide-react"

interface AdminGuardProps {
  children: ReactNode
}

export function AdminGuard({ children }: AdminGuardProps) {
  const { user, isAuthenticated, isLoading, logout } = useAuth()
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  const configuredAdminEmail = (
    process.env.NEXT_PUBLIC_ADMIN_EMAIL || "adfixstudio25@gmail.com"
  ).toLowerCase().trim()

  const currentEmail = user?.email?.toLowerCase().trim() || ""
  const isAuthorized =
    isAuthenticated &&
    Boolean(currentEmail) &&
    (currentEmail === "adfixstudio25@gmail.com" || currentEmail === configuredAdminEmail)

  if (!mounted || isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background text-foreground">
        <div className="flex flex-col items-center gap-3 font-mono text-xs text-muted-foreground">
          <Loader2 className="size-6 animate-spin text-accent" />
          <span>VERIFYING ADMIN PRIVILEGES...</span>
        </div>
      </div>
    )
  }

  // Not authenticated -> Prompt to log in with admin account
  if (!isAuthenticated || !user) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background p-6 text-foreground">
        <div className="w-full max-w-md rounded-2xl border border-border bg-card p-8 text-center shadow-xl">
          <div className="mx-auto flex size-14 items-center justify-center rounded-2xl border border-accent/40 bg-accent/10 text-accent">
            <ShieldAlert className="size-7" />
          </div>
          <p className="mt-4 font-mono text-[10px] tracking-[.22em] text-accent">ADFIX STUDIO</p>
          <h1 className="mt-2 text-2xl font-black tracking-tight">Admin Portal</h1>
          <p className="mt-3 text-sm text-muted-foreground">
            Please log in with your authorized admin account to access this portal.
          </p>
          <div className="mt-6 flex flex-col gap-3">
            <Link
              href="/login?returnUrl=/admin"
              className="flex items-center justify-center gap-2 rounded-xl bg-foreground py-3.5 font-mono text-xs font-bold text-background transition hover:opacity-90"
            >
              LOG IN WITH ADMIN ACCOUNT <ArrowRight className="size-4" />
            </Link>
            <Link
              href="/"
              className="rounded-xl border border-border py-3 font-mono text-xs text-muted-foreground transition hover:text-foreground"
            >
              RETURN TO HOME
            </Link>
          </div>
        </div>
      </div>
    )
  }

  // Authenticated, but email does NOT match the single authorized admin email
  if (!isAuthorized) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background p-6 text-foreground">
        <div className="w-full max-w-lg rounded-3xl border border-destructive/30 bg-card p-8 text-center shadow-2xl">
          <div className="mx-auto flex size-14 items-center justify-center rounded-2xl border border-destructive/40 bg-destructive/10 text-destructive">
            <ShieldAlert className="size-7" />
          </div>
          <p className="mt-4 font-mono text-[10px] tracking-[.22em] text-destructive">RESTRICTED ACCESS</p>
          <h1 className="mt-2 text-2xl font-black tracking-tight">Access Denied</h1>
          <p className="mt-3 text-sm text-muted-foreground">
            You are signed in as <strong className="text-foreground">{user.email}</strong>, which does not have administrator privileges. Only the designated administrator account is permitted to view this dashboard.
          </p>

          <div className="mt-6 rounded-2xl border border-border bg-muted/40 p-4 text-left font-mono text-xs text-muted-foreground">
            <div><strong>Status:</strong> 403 Forbidden</div>
            <div className="mt-1"><strong>Action:</strong> Switch to authorized admin account</div>
          </div>

          <div className="mt-6 flex flex-col gap-3">
            <button
              onClick={() => {
                logout()
                window.location.href = "/login?returnUrl=/admin"
              }}
              className="flex items-center justify-center gap-2 rounded-xl bg-foreground py-3.5 font-mono text-xs font-bold text-background transition hover:opacity-90"
            >
              <LogOut className="size-4" /> SWITCH ACCOUNT / LOG IN AS ADMIN
            </button>
            <Link
              href="/dashboard"
              className="rounded-xl border border-border py-3 font-mono text-xs text-muted-foreground transition hover:text-foreground"
            >
              RETURN TO STUDENT DASHBOARD
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return <>{children}</>
}

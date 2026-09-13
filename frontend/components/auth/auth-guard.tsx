"use client"

import { useEffect, useState, type ReactNode } from "react"
import { useRouter, usePathname } from "next/navigation"
import Link from "next/link"
import { Lock, Loader2, ArrowRight } from "lucide-react"
import { useAuth } from "@/lib/auth"

interface AuthGuardProps {
  children: ReactNode
}

export function AuthGuard({ children }: AuthGuardProps) {
  const { isAuthenticated, isLoading } = useAuth()
  const router = useRouter()
  const pathname = usePathname()
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  useEffect(() => {
    if (mounted && !isLoading && !isAuthenticated) {
      router.replace(`/login?redirect=${encodeURIComponent(pathname || "/dashboard")}`)
    }
  }, [mounted, isLoading, isAuthenticated, router, pathname])

  // While mounting on client or validating session
  if (!mounted || isLoading) {
    return (
      <div suppressHydrationWarning className="flex min-h-[80vh] flex-col items-center justify-center px-4 py-16 text-foreground">
        <div className="flex flex-col items-center gap-4 rounded-3xl border border-border bg-card p-10 backdrop-blur-xl shadow-2xl">
          <div className="relative">
            <div className="size-12 rounded-full border-2 border-primary/20 border-t-primary animate-spin" />
            <Loader2 className="absolute inset-0 m-auto size-5 text-primary animate-pulse" />
          </div>
          <div className="text-center">
            <p className="font-mono text-xs uppercase tracking-widest text-primary font-bold">Security Verification</p>
            <p className="mt-1 text-sm text-muted-foreground">Verifying session credentials...</p>
          </div>
        </div>
      </div>
    )
  }

  // If not authenticated after mounting
  if (!isAuthenticated) {
    return (
      <div suppressHydrationWarning className="flex min-h-[80vh] flex-col items-center justify-center px-4 py-16 text-foreground">
        <div className="w-full max-w-md rounded-3xl border border-border bg-card p-8 text-center backdrop-blur-xl shadow-2xl">
          <div className="mx-auto flex size-14 items-center justify-center rounded-2xl border border-destructive/20 bg-destructive/10 text-destructive">
            <Lock className="size-6" />
          </div>
          <h2 className="mt-5 font-serif text-2xl font-bold tracking-tight text-foreground">
            Authentication Required
          </h2>
          <p className="mt-2 text-sm text-muted-foreground">
            This section contains private student data. Please log in to your account to continue.
          </p>
          <div className="mt-6 flex flex-col gap-3">
            <Link
              href={`/login?redirect=${encodeURIComponent(pathname || "/dashboard")}`}
              className="inline-flex items-center justify-center gap-2 rounded-xl bg-primary px-5 py-3 font-mono text-xs font-bold uppercase tracking-wider text-primary-foreground transition-transform hover:scale-[1.02] active:scale-[0.98]"
            >
              Log In to Access <ArrowRight className="size-4" />
            </Link>
            <Link
              href="/register"
              className="inline-flex items-center justify-center gap-2 rounded-xl border border-border bg-secondary px-5 py-3 font-mono text-xs font-semibold uppercase tracking-wider text-foreground hover:bg-accent hover:text-accent-foreground"
            >
              Create New Account
            </Link>
          </div>
        </div>
      </div>
    )
  }

  // Authenticated: Render protected contents
  return <>{children}</>
}

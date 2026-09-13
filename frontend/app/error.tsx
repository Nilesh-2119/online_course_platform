"use client"

import { useEffect } from "react"
import Link from "next/link"
import { AlertTriangle, RotateCcw } from "lucide-react"

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  useEffect(() => {
    // In production, log error to an error reporting service (e.g. Sentry)
    // console.error(error)
  }, [error])

  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-5 py-16 text-foreground">
      <div className="mx-auto max-w-md text-center">
        <div className="mx-auto flex size-12 items-center justify-center rounded-full border border-destructive/30 bg-destructive/10 text-destructive">
          <AlertTriangle className="size-6" />
        </div>
        <p className="mt-6 font-mono text-xs tracking-[.25em] text-destructive">APPLICATION ERROR</p>
        <h1 className="mt-3 text-4xl font-black tracking-[-.07em]">Something went wrong.</h1>
        <p className="mt-3 font-mono text-sm leading-6 text-muted-foreground">
          An unexpected error occurred. Please try reloading the page or contact support if the issue persists.
        </p>
        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <button
            onClick={() => reset()}
            className="inline-flex items-center justify-center gap-2 rounded-full bg-foreground px-6 py-3.5 font-mono text-xs font-bold text-background"
          >
            <RotateCcw className="size-4" /> TRY AGAIN
          </button>
          <Link
            href="/"
            className="inline-flex items-center justify-center rounded-full border border-border px-6 py-3.5 font-mono text-xs font-semibold"
          >
            GO TO HOMEPAGE
          </Link>
        </div>
      </div>
    </main>
  )
}

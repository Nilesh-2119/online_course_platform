"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { FlowShell } from "@/components/shared/flow-shell"
import { useAuth } from "@/lib/auth"
import { login as apiLogin } from "@/lib/api/auth-service"
import { getApiBaseUrl } from "@/lib/api/client"

const inputClass = "w-full rounded-xl border border-border bg-transparent px-4 py-3 text-sm font-sans outline-none focus:border-accent"

export function LoginView() {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [message, setMessage] = useState("")
  const [noAccount, setNoAccount] = useState(false)
  const [loading, setLoading] = useState(false)
  const { user, isAuthenticated, isLoading, refreshUser } = useAuth()

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      const params = typeof window !== "undefined" ? new URLSearchParams(window.location.search) : null
      const returnUrl = params?.get("returnUrl")
      if (returnUrl) {
        window.location.href = returnUrl
        return
      }

      const isAdmin = user?.role === "admin" || user?.email?.toLowerCase().trim() === "adfixstudio25@gmail.com"
      window.location.href = isAdmin ? "/admin" : "/dashboard"
    }
  }, [isLoading, isAuthenticated, user])

  useEffect(() => {
    if (typeof window !== "undefined") {
      const params = new URLSearchParams(window.location.search)
      const err = params.get("error")
      const msg = params.get("message")
      if (err || msg) {
        setMessage(msg ? decodeURIComponent(msg) : "Google authentication failed. Please try again.")
      }
    }
  }, [])

  const handleLogin = async () => {
    if (!email || !password) {
      setMessage("Enter your email and password to continue.")
      return
    }
    setLoading(true)
    setMessage("")
    setNoAccount(false)

    const res = await apiLogin({ email: email.trim().toLowerCase(), password })
    setLoading(false)

    if (res.success && res.data) {
      await refreshUser()
      const params = typeof window !== "undefined" ? new URLSearchParams(window.location.search) : null
      const returnUrl = params?.get("returnUrl")
      if (returnUrl) {
        window.location.href = returnUrl
        return
      }
      const isAdmin = res.data.role === "admin" || res.data.email?.toLowerCase().trim() === "adfixstudio25@gmail.com"
      window.location.href = isAdmin ? "/admin" : "/dashboard"
    } else {
      const errMsg = res.error || "Invalid email or password."
      setMessage(errMsg)
      if (errMsg.toLowerCase().includes("no account found") || errMsg.toLowerCase().includes("not found")) {
        setNoAccount(true)
      }
    }
  }

  const handleGoogleLogin = () => {
    window.location.href = `${getApiBaseUrl()}/oauth2/authorization/google`
  }

  return (
    <FlowShell back>
      <div className="mx-auto max-w-md pt-4">
        <p className="font-mono text-xs tracking-[.2em] text-accent">ADFIX STUDIO ACCOUNT</p>
        <h1 className="mt-3 text-5xl font-black tracking-[-.07em]">Welcome back.</h1>
        <p className="mt-3 font-mono text-sm text-muted-foreground">Log in to your AdFix Studio account.</p>

        {/* No Account Found Notice */}
        {noAccount && (
          <div className="mt-6 rounded-2xl border border-amber-500/40 bg-amber-500/10 p-5 text-left">
            <h3 className="font-bold text-amber-500">⚠️ No Account Found</h3>
            <p className="mt-1 font-mono text-xs text-muted-foreground">
              We couldn&apos;t find an account matching <strong>{email}</strong>. Please create an account first to access the platform.
            </p>
            <Link
              href="/create-account"
              className="mt-3 inline-block rounded-xl bg-accent px-4 py-2.5 font-mono text-xs font-bold text-accent-foreground"
            >
              CREATE AN ACCOUNT &rarr;
            </Link>
          </div>
        )}

        {/* Google OAuth Login */}
        <button
          type="button"
          onClick={handleGoogleLogin}
          className="mt-6 flex w-full items-center justify-center gap-3 rounded-2xl border border-border bg-card px-5 py-3.5 font-mono text-sm font-semibold transition hover:bg-muted"
        >
          <svg width="18" height="18" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
          </svg>
          CONTINUE WITH GOOGLE
        </button>

        <div className="my-6 flex items-center gap-3">
          <div className="h-px flex-1 bg-border" />
          <span className="font-mono text-xs text-muted-foreground">OR LOGIN WITH EMAIL</span>
          <div className="h-px flex-1 bg-border" />
        </div>

        <div className="grid gap-3">
          <label className="grid gap-1.5 font-mono text-xs">
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} className={inputClass} placeholder="your@email.com" />
          </label>
          <label className="grid gap-1.5 font-mono text-xs">
            Password
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} className={inputClass} placeholder="••••••••" />
          </label>
          <button
            onClick={handleLogin}
            disabled={loading}
            className="rounded-2xl bg-foreground px-5 py-4 font-bold text-background transition disabled:opacity-50"
          >
            {loading ? "LOGGING IN..." : "LOGIN"}
          </button>
          {message && !noAccount && <p className="font-mono text-xs text-destructive">{message}</p>}
          <Link href="/forgot-password" className="font-mono text-xs text-muted-foreground hover:text-foreground">
            Forgot password?
          </Link>
          <p className="pt-2 font-mono text-xs text-muted-foreground">
            Don&apos;t have an account? <Link href="/create-account" className="text-accent font-bold">CREATE ACCOUNT</Link>
          </p>
          <Link href="/free-resources" className="font-mono text-xs text-accent">
            EXPLORE FREE RESOURCES
          </Link>
        </div>
      </div>
    </FlowShell>
  )
}

"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { FlowShell } from "@/components/shared/flow-shell"
import { useAuth } from "@/lib/auth"
import { createAccount as apiCreateAccount, verifyOtp as apiVerifyOtp, resendOtp as apiResendOtp } from "@/lib/api/auth-service"
import { getApiBaseUrl } from "@/lib/api/client"

const inputClass = "w-full rounded-xl border border-border bg-transparent px-4 py-3 text-sm font-sans outline-none focus:border-accent"

export function CreateAccountView() {
  const [step, setStep] = useState<"FORM" | "VERIFY">("FORM")
  const [form, setForm] = useState({
    name: "",
    email: "",
    phone: "",
    password: "",
    confirm: "",
  })
  const [acceptedTerms, setAcceptedTerms] = useState(false)
  const [marketingConsent, setMarketingConsent] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  // OTP Verification state
  const [emailOtp, setEmailOtp] = useState("")
  const [phoneOtp, setPhoneOtp] = useState("")
  const [emailVerified, setEmailVerified] = useState(false)
  const [phoneVerified, setPhoneVerified] = useState(false)
  const [otpMessage, setOtpMessage] = useState("")
  const [resendMessage, setResendMessage] = useState("")

  const { isAuthenticated, isLoading, refreshUser } = useAuth()

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      window.location.href = "/dashboard"
    }
  }, [isLoading, isAuthenticated])

  useEffect(() => {
    if (typeof window !== "undefined") {
      const params = new URLSearchParams(window.location.search)
      const err = params.get("error")
      const msg = params.get("message")
      if (err || msg) {
        setError(msg ? decodeURIComponent(msg) : "Google authentication failed. Please try again.")
      }
    }
  }, [])

  const update = (key: string, value: string) => setForm((v) => ({ ...v, [key]: value }))
  
  const valid = Boolean(
    form.email.trim() &&
    form.name.trim() &&
    form.phone.trim() &&
    form.password.length >= 8 &&
    form.password === form.confirm &&
    acceptedTerms
  )

  const handleRegister = async () => {
    if (!valid) {
      if (form.password !== form.confirm) {
        setError("Passwords do not match.")
        return
      }
      if (form.password.length < 8) {
        setError("Password must be at least 8 characters.")
        return
      }
      setError("Please complete all required fields and accept the terms.")
      return
    }

    setLoading(true)
    setError("")

    // Format phone if user typed local 10 digits
    let formattedPhone = form.phone.trim().replace(/\s+/g, "")
    if (!formattedPhone.startsWith("+")) {
      formattedPhone = `+91${formattedPhone}`
    }

    const res = await apiCreateAccount({
      name: form.name.trim(),
      email: form.email.trim().toLowerCase(),
      phone: formattedPhone,
      password: form.password,
    })
    setLoading(false)

    if (res.success) {
      setStep("VERIFY")
    } else {
      setError(res.error || "Account creation failed.")
    }
  }

  const handleVerifyEmail = async () => {
    setOtpMessage("")
    const res = await apiVerifyOtp({
      identifier: form.email.trim().toLowerCase(),
      channel: "EMAIL",
      otp: emailOtp.trim(),
    })
    if (res.success) {
      setEmailVerified(true)
      await refreshUser()
      window.location.href = "/dashboard"
    } else {
      setOtpMessage(res.error || "Invalid email OTP")
    }
  }

  const handleResend = async (channel: "EMAIL" | "PHONE") => {
    setResendMessage("")
    const identifier = form.email.trim().toLowerCase()
    const res = await apiResendOtp({
      identifier,
      channel: "EMAIL",
    })
    if (res.success) {
      setResendMessage("A new 6-digit verification code has been dispatched to your email.")
    } else {
      setResendMessage(res.error || "Resend failed")
    }
  }

  const handleGoogleLogin = () => {
    window.location.href = `${getApiBaseUrl()}/oauth2/authorization/google`
  }

  if (step === "VERIFY") {
    return (
      <FlowShell back>
        <div className="mx-auto max-w-md pt-4">
          <p className="font-mono text-xs tracking-[.2em] text-accent">STEP 2 OF 2 · SECURITY VERIFICATION</p>
          <h1 className="mt-3 text-4xl font-black tracking-[-.07em]">Verify your email.</h1>
          <p className="mt-3 font-mono text-sm leading-6 text-muted-foreground">
            We sent a 6-digit verification code to <strong>{form.email}</strong>.
            <br />
            <span className="text-xs text-muted-foreground">Please check your inbox (and spam folder) to activate your account.</span>
          </p>

          <div className="mt-6 space-y-6">
            {/* Email OTP Only */}
            <div className="rounded-2xl border border-border p-5 bg-card">
              <div className="flex items-center justify-between">
                <span className="font-mono text-xs font-bold uppercase tracking-wider text-accent">EMAIL VERIFICATION CODE</span>
                {emailVerified ? (
                  <span className="font-mono text-xs text-green-500 font-bold">✓ VERIFIED</span>
                ) : (
                  <button
                    type="button"
                    onClick={() => handleResend("EMAIL")}
                    className="font-mono text-xs text-accent underline hover:opacity-80 transition"
                  >
                    Resend Code
                  </button>
                )}
              </div>
              {!emailVerified && (
                <div className="mt-4 flex gap-2">
                  <input
                    type="text"
                    maxLength={6}
                    placeholder="Enter 6-digit code"
                    value={emailOtp}
                    onChange={(e) => setEmailOtp(e.target.value)}
                    className={inputClass}
                    autoFocus
                  />
                  <button
                    onClick={handleVerifyEmail}
                    className="rounded-xl bg-accent px-5 font-mono text-xs font-bold text-accent-foreground hover:opacity-90 transition active:scale-95"
                  >
                    VERIFY
                  </button>
                </div>
              )}
            </div>

            {otpMessage && <p className="font-mono text-xs text-destructive">{otpMessage}</p>}
            {resendMessage && <p className="font-mono text-xs text-accent">{resendMessage}</p>}
          </div>
        </div>
      </FlowShell>
    )
  }

  return (
    <FlowShell back>
      <div className="mx-auto max-w-md">
        <p className="font-mono text-xs tracking-[.2em] text-accent">ADFIX STUDIO ACCOUNT</p>
        <h1 className="mt-3 text-5xl font-black tracking-[-.07em]">Create your account.</h1>
        <p className="mt-3 font-mono text-sm leading-6 text-muted-foreground">
          Free account creation. Access free resources now; purchase the course whenever you are ready.
        </p>

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
          <span className="font-mono text-xs text-muted-foreground">OR FILL DETAILS</span>
          <div className="h-px flex-1 bg-border" />
        </div>

        <div className="grid gap-3">
          <label className="grid gap-1.5 font-mono text-xs">
            Full Name
            <input
              type="text"
              autoComplete="name"
              placeholder="e.g. Jane Doe"
              value={form.name}
              onChange={(e) => update("name", e.target.value)}
              className={inputClass}
            />
          </label>

          <label className="grid gap-1.5 font-mono text-xs">
            Email Address
            <input
              type="email"
              autoComplete="email"
              placeholder="e.g. jane@example.com"
              value={form.email}
              onChange={(e) => update("email", e.target.value)}
              className={inputClass}
            />
          </label>

          <label className="grid gap-1.5 font-mono text-xs">
            Phone Number
            <input
              type="tel"
              autoComplete="tel"
              placeholder="e.g. +91 9876543210"
              value={form.phone}
              onChange={(e) => update("phone", e.target.value)}
              className={inputClass}
            />
          </label>

          <label className="grid gap-1.5 font-mono text-xs">
            Password (Min 8 characters)
            <input
              type="password"
              autoComplete="new-password"
              placeholder="••••••••"
              value={form.password}
              onChange={(e) => update("password", e.target.value)}
              className={inputClass}
            />
          </label>

          <label className="grid gap-1.5 font-mono text-xs">
            Confirm Password
            <input
              type="password"
              autoComplete="new-password"
              placeholder="••••••••"
              value={form.confirm}
              onChange={(e) => update("confirm", e.target.value)}
              className={inputClass}
            />
          </label>

          {/* Mandatory Legal Acceptance */}
          <label className="mt-2 flex items-start gap-2.5 font-mono text-xs leading-5 text-muted-foreground">
            <input
              type="checkbox"
              checked={acceptedTerms}
              onChange={(e) => setAcceptedTerms(e.target.checked)}
              className="mt-1 accent-[hsl(var(--accent))]"
              required
            />
            <span>
              I agree to the <a href="/terms" className="text-foreground underline">Terms &amp; Conditions</a> and <a href="/privacy" className="text-foreground underline">Privacy Policy</a> <span className="text-destructive">*</span>
            </span>
          </label>

          {/* Optional Marketing Consent */}
          <label className="flex items-start gap-2.5 font-mono text-xs leading-5 text-muted-foreground">
            <input
              type="checkbox"
              checked={marketingConsent}
              onChange={(e) => setMarketingConsent(e.target.checked)}
              className="mt-1 accent-[hsl(var(--accent))]"
            />
            <span>I want to receive occasional educational resources, ad breakdowns, and special offers from AdFix Studio (optional, unsubscribe anytime).</span>
          </label>

          {error && <p className="font-mono text-xs text-destructive">{error}</p>}

          <button
            disabled={!valid || loading}
            onClick={handleRegister}
            className="mt-2 rounded-2xl bg-foreground px-5 py-4 font-bold text-background disabled:opacity-40"
          >
            {loading ? "CREATING ACCOUNT..." : "CREATE ACCOUNT"}
          </button>
        </div>
      </div>
    </FlowShell>
  )
}

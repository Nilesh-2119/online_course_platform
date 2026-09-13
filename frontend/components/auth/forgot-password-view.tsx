"use client"

import { useState } from "react"
import Link from "next/link"
import { ArrowRight, CheckCircle2, Eye, EyeOff, KeyRound, Loader2, Mail, ShieldAlert, Sparkles } from "lucide-react"
import { FlowShell } from "@/components/shared/flow-shell"
import { apiForgotPassword, apiResetPassword } from "@/lib/api/auth-service"

const inputClass = "w-full rounded-xl border border-border bg-transparent px-4 py-3 text-sm font-sans outline-none focus:border-accent"

export function ForgotPasswordView() {
  const [step, setStep] = useState<"EMAIL" | "RESET" | "SUCCESS">("EMAIL")
  const [email, setEmail] = useState("")
  const [otp, setOtp] = useState("")
  const [newPassword, setNewPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")
  const [resendStatus, setResendStatus] = useState("")

  const handleRequestOtp = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!email || !email.includes("@")) {
      setError("Please enter a valid email address.")
      return
    }

    setLoading(true)
    setError("")
    setResendStatus("")

    try {
      const res = await apiForgotPassword(email)
      if (res.success) {
        setStep("RESET")
      } else {
        setError(res.error || "Failed to dispatch reset code. Please try again.")
      }
    } catch {
      setError("A network error occurred. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")

    if (!otp || otp.trim().length !== 6) {
      setError("Please enter the 6-digit verification code.")
      return
    }
    if (newPassword.length < 8) {
      setError("Password must be at least 8 characters.")
      return
    }
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match. Please re-enter.")
      return
    }

    setLoading(true)

    try {
      const res = await apiResetPassword({
        email,
        otp: otp.trim(),
        newPassword,
      })

      if (res.success) {
        setStep("SUCCESS")
      } else {
        setError(res.error || "Failed to reset password. Please check your OTP code.")
      }
    } catch {
      setError("An unexpected error occurred while resetting password.")
    } finally {
      setLoading(false)
    }
  }

  const handleResendOtp = async () => {
    setResendStatus("")
    setError("")
    try {
      const res = await apiForgotPassword(email)
      if (res.success) {
        setResendStatus("New 6-digit code dispatched! (Check backend terminal in dev mode)")
      } else {
        setError(res.error || "Resend rate limit active. Please wait.")
      }
    } catch {
      setError("Failed to resend code.")
    }
  }

  return (
    <FlowShell back>
      <div className="mx-auto max-w-md pt-4">
        <p className="font-mono text-xs tracking-[.2em] text-accent">ACCOUNT RECOVERY</p>
        <h1 className="mt-3 text-4xl font-black tracking-[-.07em] md:text-5xl">Forgot password?</h1>
        <p className="mt-3 font-mono text-sm text-muted-foreground">
          {step === "EMAIL" && "Enter your registered email address to receive a secure 6-digit reset code."}
          {step === "RESET" && `Enter the 6-digit code sent to ${email} and choose a new password.`}
          {step === "SUCCESS" && "Your password has been successfully updated."}
        </p>

        {error && (
          <div className="mt-6 flex items-start gap-2.5 rounded-2xl border border-red-500/30 bg-red-500/10 p-4 text-xs font-mono text-red-600">
            <ShieldAlert className="size-4 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {/* STEP 1: REQUEST CODE */}
        {step === "EMAIL" && (
          <form onSubmit={handleRequestOtp} className="mt-6 grid gap-4">
            <label className="grid gap-1.5 font-mono text-xs">
              Registered Email Address
              <div className="relative">
                <input
                  required
                  type="email"
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value)
                    setError("")
                  }}
                  className={inputClass}
                  placeholder="your@email.com"
                />
                <Mail className="absolute right-3.5 top-3.5 size-4 text-muted-foreground pointer-events-none" />
              </div>
            </label>

            <button
              type="submit"
              disabled={loading || !email}
              className="mt-2 flex items-center justify-center gap-2 rounded-2xl bg-foreground px-5 py-4 font-bold text-background disabled:opacity-40 transition-transform hover:scale-[1.01] active:scale-95"
            >
              {loading ? (
                <>
                  <Loader2 className="size-4 animate-spin" /> DISPATCHING CODE...
                </>
              ) : (
                <>
                  DISPATCH 6-DIGIT OTP <ArrowRight className="size-4 text-accent" />
                </>
              )}
            </button>

            <div className="mt-4 text-center font-mono text-xs text-muted-foreground">
              Remember your password?{" "}
              <Link href="/login" className="text-foreground font-bold hover:text-accent underline underline-offset-4">
                LOGIN
              </Link>
            </div>
          </form>
        )}

        {/* STEP 2: ENTER OTP & NEW PASSWORD */}
        {step === "RESET" && (
          <form onSubmit={handleResetPassword} className="mt-6 grid gap-4">
            {resendStatus && (
              <div className="flex items-center gap-2 rounded-xl border border-emerald-500/30 bg-emerald-500/10 p-3 font-mono text-xs text-emerald-700">
                <CheckCircle2 className="size-4 shrink-0" />
                <span>{resendStatus}</span>
              </div>
            )}

            <label className="grid gap-1.5 font-mono text-xs">
              6-Digit Verification Code
              <div className="relative">
                <input
                  required
                  type="text"
                  maxLength={6}
                  value={otp}
                  onChange={(e) => {
                    setOtp(e.target.value.replace(/\D/g, ""))
                    setError("")
                  }}
                  className={`${inputClass} font-mono tracking-[0.3em] text-center text-lg font-bold`}
                  placeholder="000000"
                />
                <KeyRound className="absolute right-3.5 top-3.5 size-4 text-muted-foreground pointer-events-none" />
              </div>
            </label>

            <label className="grid gap-1.5 font-mono text-xs">
              New Password (Min. 8 characters)
              <div className="relative">
                <input
                  required
                  type={showPassword ? "text" : "password"}
                  value={newPassword}
                  onChange={(e) => {
                    setNewPassword(e.target.value)
                    setError("")
                  }}
                  className={inputClass}
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-3.5 text-muted-foreground hover:text-foreground"
                >
                  {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
                </button>
              </div>
            </label>

            <label className="grid gap-1.5 font-mono text-xs">
              Confirm New Password
              <input
                required
                type={showPassword ? "text" : "password"}
                value={confirmPassword}
                onChange={(e) => {
                  setConfirmPassword(e.target.value)
                  setError("")
                }}
                className={inputClass}
                placeholder="••••••••"
              />
            </label>

            <button
              type="submit"
              disabled={loading || otp.length !== 6 || newPassword.length < 8}
              className="mt-2 flex items-center justify-center gap-2 rounded-2xl bg-foreground px-5 py-4 font-bold text-background disabled:opacity-40 transition-transform hover:scale-[1.01] active:scale-95"
            >
              {loading ? (
                <>
                  <Loader2 className="size-4 animate-spin" /> RESETTING PASSWORD...
                </>
              ) : (
                <>
                  UPDATE PASSWORD & LOGIN <ArrowRight className="size-4 text-accent" />
                </>
              )}
            </button>

            <div className="mt-2 flex items-center justify-between font-mono text-xs">
              <button
                type="button"
                onClick={handleResendOtp}
                className="text-muted-foreground hover:text-foreground underline underline-offset-4"
              >
                Resend 6-Digit Code
              </button>
              <button
                type="button"
                onClick={() => {
                  setStep("EMAIL")
                  setOtp("")
                  setError("")
                }}
                className="text-muted-foreground hover:text-foreground underline underline-offset-4"
              >
                Change Email
              </button>
            </div>
          </form>
        )}

        {/* STEP 3: SUCCESS STATE */}
        {step === "SUCCESS" && (
          <div className="mt-6 rounded-3xl border border-emerald-500/30 bg-emerald-500/10 p-6 md:p-8 text-foreground">
            <div className="flex items-center gap-3">
              <div className="grid size-10 place-items-center rounded-2xl bg-emerald-600 text-white shadow-sm">
                <CheckCircle2 className="size-6" />
              </div>
              <div>
                <h2 className="font-bold text-lg">Password Reset Complete!</h2>
                <p className="font-mono text-xs text-muted-foreground">Your account is secured with your new password.</p>
              </div>
            </div>

            <p className="mt-5 font-mono text-xs leading-relaxed text-muted-foreground">
              All previous active login sessions have been invalidated for your safety. You can now log in with your updated credentials.
            </p>

            <div className="mt-6">
              <Link
                href="/login"
                className="flex w-full items-center justify-center gap-2 rounded-2xl bg-foreground px-5 py-4 font-mono text-xs font-bold text-background transition-transform hover:scale-[1.01]"
              >
                RETURN TO LOGIN <ArrowRight className="size-4 text-accent" />
              </Link>
            </div>
          </div>
        )}
      </div>
    </FlowShell>
  )
}

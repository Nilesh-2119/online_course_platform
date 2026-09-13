"use client"

import { useState } from "react"
import { ArrowRight, Phone, ShieldCheck } from "lucide-react"
import { FlowShell } from "@/components/shared/flow-shell"
import { useAuth } from "@/lib/auth"
import { updateUserProfile } from "@/lib/api/auth-service"

const inputClass = "w-full rounded-xl border border-border bg-transparent px-4 py-3 text-sm font-sans outline-none focus:border-accent"

export function AccountSetupView() {
  const { user, refreshUser } = useAuth()
  const [phone, setPhone] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  const formatPhone = (val: string) => {
    let clean = val.replace(/[^\d+]/g, "")
    if (!clean.startsWith("+")) {
      if (clean.startsWith("91") && clean.length > 2) {
        clean = "+" + clean
      } else if (clean.length > 0) {
        clean = "+91" + clean.replace(/^0+/, "")
      }
    }
    return clean
  }

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPhone(formatPhone(e.target.value))
    setError("")
  }

  const isValidPhone = phone.length >= 10

  const handleCompleteSetup = async () => {
    if (!isValidPhone) {
      setError("Please enter a valid mobile phone number.")
      return
    }

    setLoading(true)
    setError("")

    try {
      const res = await updateUserProfile({
        name: user?.name || "Student",
        phone: phone.trim(),
      })

      if (res.success) {
        await refreshUser()
        window.location.href = "/dashboard"
      } else {
        setError(res.error || "Failed to update phone number. Please try again.")
      }
    } catch {
      setError("An unexpected error occurred. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <FlowShell>
      <div className="mx-auto max-w-lg pt-4">
        <p className="font-mono text-xs tracking-[.2em] text-accent">WELCOME TO ADFIX STUDIO</p>
        <h1 className="mt-3 text-4xl font-black tracking-[-.07em]">One Last Step.</h1>
        <p className="mt-3 font-mono text-sm text-muted-foreground">
          Enter your mobile number to complete your profile and access your courses & resources.
        </p>

        <div className="mt-6 rounded-2xl border border-border bg-card p-6 shadow-sm">
          <div className="grid gap-4">
            <div>
              <label className="font-mono text-xs text-muted-foreground">Registered Email</label>
              <div className="mt-1 flex items-center justify-between rounded-xl border border-border bg-muted/40 px-4 py-3 text-sm font-mono">
                <span>{user?.email || "Google Account"}</span>
                <span className="inline-flex items-center gap-1 text-[11px] font-bold text-accent">
                  <ShieldCheck className="size-3.5" /> Verified
                </span>
              </div>
            </div>

            <div>
              <label className="font-mono text-xs text-muted-foreground">Full Name</label>
              <input
                value={user?.name ?? ""}
                readOnly
                className="mt-1 w-full rounded-xl border border-border bg-muted/40 px-4 py-3 text-sm font-sans"
              />
            </div>

            <div>
              <label className="font-mono text-xs font-semibold text-foreground">
                Mobile Number <span className="text-accent">*</span>
              </label>
              <div className="relative mt-1">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-muted-foreground">
                  <Phone className="size-4" />
                </div>
                <input
                  type="tel"
                  value={phone}
                  onChange={handlePhoneChange}
                  className={`${inputClass} pl-10`}
                  placeholder="+91 98765 43210"
                  autoFocus
                />
              </div>
              <p className="mt-1.5 font-mono text-[11px] text-muted-foreground">
                We will use this to send you important course updates and purchase receipts.
              </p>
            </div>

            {error && (
              <p className="font-mono text-xs text-destructive">{error}</p>
            )}

            <button
              disabled={!isValidPhone || loading}
              onClick={handleCompleteSetup}
              className="mt-3 flex w-full items-center justify-center gap-2 rounded-2xl bg-foreground px-5 py-4 font-bold text-background transition hover:opacity-90 disabled:opacity-40"
            >
              {loading ? "SAVING..." : "COMPLETE SETUP & GO TO DASHBOARD"}
              {!loading && <ArrowRight className="size-4" />}
            </button>
          </div>
        </div>
      </div>
    </FlowShell>
  )
}

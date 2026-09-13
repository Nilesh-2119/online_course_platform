"use client"

import { useState } from "react"
import { ArrowRight } from "lucide-react"
import { FlowShell } from "@/components/shared/flow-shell"
import { useAuth } from "@/lib/auth"

export function OnboardingView() {
  const [step, setStep] = useState(1)
  const { user, updateProfile } = useAuth()
  const [role, setRole] = useState(user?.onboardingRole ?? "")
  const [goals, setGoals] = useState<string[]>(user?.onboardingGoals ?? [])
  const [experience, setExperience] = useState(user?.onboardingExperience ?? "")

  const options =
    step === 1
      ? ["Creator", "Marketer", "Founder", "Business", "Agency", "Freelancer"]
      : step === 2
      ? ["Hooks", "Scripts", "CTAs", "Full Ad Strategy"]
      : ["Beginner", "Intermediate", "Advanced"]

  const selected = step === 1 ? role : step === 2 ? goals : experience

  const choose = (value: string) => {
    if (step === 1) setRole(value)
    else if (step === 2) setGoals((v) => (v.includes(value) ? v.filter((x) => x !== value) : [...v, value]))
    else setExperience(value)
  }

  const valid = Array.isArray(selected) ? selected.length > 0 : Boolean(selected)

  const next = () => {
    if (!valid) return
    if (step < 3) {
      setStep(step + 1)
    } else {
      // BACKEND_INTEGRATION_POINT: Save onboarding preferences to user profile API.
      updateProfile({
        onboardingComplete: true,
        onboardingRole: role,
        onboardingGoals: goals,
        onboardingExperience: experience,
      })
      window.location.href = "/dashboard"
    }
  }

  return (
    <FlowShell>
      <div className="mx-auto flex min-h-[calc(100vh-3rem)] max-w-2xl flex-col justify-center">
        <div className="flex items-center justify-between font-mono text-xs text-muted-foreground">
          <span>STEP {step} OF 3</span>
          <span className="text-accent">{step * 33 === 99 ? "100%" : `${step * 33}%`}</span>
        </div>
        <div className="mt-3 h-1 rounded-full bg-muted">
          <div className="h-full rounded-full bg-accent transition-all" style={{ width: `${step * 33.333}%` }} />
        </div>
        <p className="mt-7 font-mono text-xs tracking-[.2em] text-accent">ADFIX ONBOARDING</p>
        <h1 className="mt-3 text-4xl font-black tracking-[-.07em] md:text-5xl">
          {step === 1 ? "Tell us about yourself" : step === 2 ? "What do you want to improve?" : "Your experience"}
        </h1>
        <p className="mt-2 font-mono text-sm text-muted-foreground">
          {step === 1 ? "What best describes you?" : step === 2 ? "Choose all that apply." : "Where are you starting from?"}
        </p>
        <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-3">
          {options.map((option) => {
            const active = Array.isArray(selected) ? selected.includes(option) : selected === option
            return (
              <button
                key={option}
                onClick={() => choose(option)}
                className={`rounded-2xl border px-4 py-4 text-left font-mono text-sm transition-colors ${
                  active ? "border-accent bg-accent text-accent-foreground" : "border-border hover:border-accent"
                }`}
              >
                {option}
              </button>
            )
          })}
        </div>
        <button
          disabled={!valid}
          onClick={next}
          className="mt-6 flex w-full items-center justify-between rounded-2xl bg-foreground px-5 py-4 font-bold text-background disabled:opacity-40"
        >
          {step === 3 ? "GO TO MY COURSE" : "CONTINUE"}
          <ArrowRight className="size-5" />
        </button>
      </div>
    </FlowShell>
  )
}

"use client"

import Link from "next/link"
import { ArrowRight, Check, CheckCircle2 } from "lucide-react"
import { FlowShell } from "@/components/shared/flow-shell"
import { useAuth } from "@/lib/auth"
import { formatCurrency } from "@/lib/utils"
import { mockCourses } from "@/mocks/seed-data"

const benefits = [
  "Recorded video course",
  "Hook Engineering",
  "Script Flow Rewrite",
  "High-Conversion CTAs",
  "Ad Strategy",
  "Lifetime access",
]

export function BuyView() {
  const { user } = useAuth()
  const course = mockCourses[0]
  const isPaid = user?.type === "paid"

  return (
    <FlowShell back>
      <div className="mx-auto max-w-xl">
        <p className="font-mono text-xs tracking-[.2em] text-muted-foreground">ADFIX STUDIO</p>
        <h1 className="mt-4 text-5xl font-black leading-[.9] tracking-[-.07em] md:text-7xl">
          {isPaid ? "YOUR MEMBERSHIP" : "GET INSTANT ACCESS"}
        </h1>
        <p className="mt-5 font-mono text-sm leading-6 text-muted-foreground">
          AdFix High-Performing Ads Course — a practical recorded course for creators, marketers, founders, freelancers and businesses who want to create better-performing ads.
        </p>

        <div className="mt-7 rounded-3xl bg-foreground p-6 text-background md:p-8">
          <h2 className="text-3xl font-black tracking-[-.06em]">{course.title}</h2>
          <div className="mt-5 grid gap-2 font-mono text-xs text-background/75">
            {benefits.map((item) => (
              <span key={item} className="flex gap-2">
                <Check className="size-4 text-accent" />
                {item}
              </span>
            ))}
          </div>

          {isPaid ? (
            <div className="mt-8 rounded-2xl border border-accent/40 bg-accent/10 p-5">
              <div className="flex items-center gap-2 text-accent font-bold text-sm">
                <CheckCircle2 className="size-4" /> YOU ALREADY HAVE ACCESS
              </div>
              <p className="mt-2 font-mono text-xs text-background/80">
                You are enrolled with lifetime access to all lessons and materials.
              </p>
              <div className="mt-6 flex flex-col gap-3">
                <Link
                  href="/dashboard/course/lesson-1"
                  className="flex items-center justify-between rounded-2xl bg-accent px-5 py-4 font-bold text-accent-foreground"
                >
                  CONTINUE COURSE <ArrowRight className="size-5" />
                </Link>
                <Link
                  href="/dashboard"
                  className="flex items-center justify-between rounded-2xl border border-background/20 px-5 py-3 font-mono text-xs font-bold text-background hover:bg-background/10"
                >
                  GO TO DASHBOARD <ArrowRight className="size-4" />
                </Link>
              </div>
            </div>
          ) : (
            <>
              <p className="mt-6 font-mono text-xs tracking-[.18em] text-accent">PRICE</p>
              <p className="mt-1 text-4xl font-black">{formatCurrency(course.price, course.currency)}</p>

              {user ? (
                <div className="mt-6 flex flex-col gap-3">
                  <div className="rounded-xl border border-accent/20 bg-accent/5 p-3 text-center font-mono text-xs text-background/80">
                    Logged in as <span className="font-bold text-accent">{user.email}</span>
                  </div>
                  <Link
                    href="/checkout"
                    className="flex items-center justify-between rounded-2xl bg-accent px-5 py-4 font-bold text-accent-foreground transition-transform hover:scale-[1.02] active:scale-[0.98]"
                  >
                    CONTINUE TO CHECKOUT <ArrowRight className="size-5" />
                  </Link>
                </div>
              ) : (
                <div className="mt-6 flex flex-col gap-3">
                  <div className="rounded-xl border border-background/20 bg-background/5 p-3 text-center font-mono text-xs text-background/70">
                    An account is required to activate and save your course enrollment.
                  </div>
                  <Link
                    href="/register?redirect=/checkout"
                    className="flex items-center justify-between rounded-2xl bg-accent px-5 py-4 font-bold text-accent-foreground transition-transform hover:scale-[1.02] active:scale-[0.98]"
                  >
                    CREATE ACCOUNT & ENROLL <ArrowRight className="size-5" />
                  </Link>
                  <Link
                    href="/login?redirect=/checkout"
                    className="block text-center font-mono text-xs text-background/70 hover:text-accent"
                  >
                    Already have an account? <span className="font-bold underline text-accent">LOG IN</span>
                  </Link>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </FlowShell>
  )
}

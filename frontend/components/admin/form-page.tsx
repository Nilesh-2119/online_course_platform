"use client"

import Link from "next/link"
import { useRouter } from "next/navigation"
import { useState } from "react"
import { Loader2, CheckCircle2, Tag } from "lucide-react"
import { couponService } from "@/lib/api"
import type { CreateCouponDto } from "@/lib/types"

export function AdminFormPage({ kind }: { kind: "USER" | "RESOURCE" | "COUPON" }) {
  const router = useRouter()
  const isCoupon = kind === "COUPON"
  const isUser = kind === "USER"

  // Coupon form state
  const defaultExpiry = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
    .toISOString()
    .slice(0, 16)

  const [couponForm, setCouponForm] = useState({
    code: "",
    discountType: "PERCENTAGE" as "PERCENTAGE" | "FIXED",
    discountValue: "20",
    usageLimit: "100",
    perUserLimit: "1",
    minimumPurchase: "0",
    startDate: "",
    expiryDate: defaultExpiry,
    active: true,
  })

  const [submitting, setSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  // Demo state for USER / RESOURCE fallbacks
  const [file, setFile] = useState("")

  const handleCouponSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setErrorMessage(null)
    setSuccessMessage(null)

    const code = couponForm.code.trim().toUpperCase()
    if (!code) {
      setErrorMessage("Coupon code is required")
      setSubmitting(false)
      return
    }

    const discountVal = parseFloat(couponForm.discountValue)
    if (isNaN(discountVal) || discountVal <= 0) {
      setErrorMessage("Discount value must be greater than 0")
      setSubmitting(false)
      return
    }

    if (couponForm.discountType === "PERCENTAGE" && discountVal > 100) {
      setErrorMessage("Percentage discount cannot exceed 100%")
      setSubmitting(false)
      return
    }

    if (!couponForm.expiryDate) {
      setErrorMessage("Expiry date is required")
      setSubmitting(false)
      return
    }

    const payload: CreateCouponDto = {
      code,
      discountType: couponForm.discountType,
      discountValue: discountVal,
      currency: "INR",
      usageLimit: parseInt(couponForm.usageLimit) || 100,
      perUserLimit: parseInt(couponForm.perUserLimit) || 1,
      minimumPurchase: parseFloat(couponForm.minimumPurchase) || 0,
      startDate: couponForm.startDate ? new Date(couponForm.startDate).toISOString() : null,
      expiryDate: new Date(couponForm.expiryDate).toISOString(),
      active: couponForm.active,
    }

    try {
      const res = await couponService.createCoupon(payload)
      if (res.success && res.data) {
        setSuccessMessage(`Coupon '${code}' created successfully! Redirecting...`)
        setTimeout(() => {
          router.push("/admin/coupons")
        }, 1000)
      } else {
        setErrorMessage(res.error || "Failed to create coupon")
      }
    } catch (err: any) {
      setErrorMessage(err.message || "An unexpected error occurred")
    } finally {
      setSubmitting(false)
    }
  }

  if (isCoupon) {
    return (
      <div className="max-w-2xl">
        <div className="mb-6">
          <Link href="/admin/coupons" className="font-mono text-[10px] text-accent hover:underline">
            ← BACK TO COUPONS
          </Link>
          <h2 className="mt-2 text-2xl font-bold">Create Discount Coupon</h2>
          <p className="text-sm text-muted-foreground">
            Generate promotional coupons with percentage or flat discounts for course checkout.
          </p>
        </div>

        {errorMessage && (
          <div className="mb-5 rounded-lg border border-destructive/40 bg-destructive/10 p-4 text-xs font-mono text-destructive">
            {errorMessage}
          </div>
        )}

        {successMessage && (
          <div className="mb-5 rounded-lg border border-accent/40 bg-accent/15 p-4 text-xs font-mono text-accent flex items-center gap-2">
            <CheckCircle2 className="size-4 shrink-0" />
            {successMessage}
          </div>
        )}

        <form
          onSubmit={handleCouponSubmit}
          className="flex flex-col gap-5 rounded-lg border border-border bg-card p-6"
        >
          <div>
            <label className="block font-mono text-[10px] tracking-widest text-muted-foreground uppercase">
              COUPON CODE *
            </label>
            <div className="relative mt-2">
              <input
                required
                type="text"
                disabled={submitting}
                value={couponForm.code}
                onChange={(e) => setCouponForm({ ...couponForm, code: e.target.value.toUpperCase() })}
                className="w-full rounded-md border border-border bg-background p-3 font-mono text-sm uppercase tracking-wider outline-none focus:border-accent"
                placeholder="e.g. ADFIX20, SPECIAL50"
              />
            </div>
            <small className="mt-1 block text-[11px] text-muted-foreground font-mono">
              Codes are automatically capitalized.
            </small>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="block font-mono text-[10px] tracking-widest text-muted-foreground uppercase">
                DISCOUNT TYPE *
              </label>
              <select
                disabled={submitting}
                value={couponForm.discountType}
                onChange={(e) =>
                  setCouponForm({
                    ...couponForm,
                    discountType: e.target.value as "PERCENTAGE" | "FIXED",
                  })
                }
                className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm outline-none focus:border-accent"
              >
                <option value="PERCENTAGE">Percentage (%)</option>
                <option value="FIXED">Flat Amount (₹ INR)</option>
              </select>
            </div>

            <div>
              <label className="block font-mono text-[10px] tracking-widest text-muted-foreground uppercase">
                DISCOUNT VALUE * {couponForm.discountType === "PERCENTAGE" ? "(%)" : "(₹)"}
              </label>
              <input
                required
                type="number"
                step="any"
                min="1"
                max={couponForm.discountType === "PERCENTAGE" ? "100" : undefined}
                disabled={submitting}
                value={couponForm.discountValue}
                onChange={(e) => setCouponForm({ ...couponForm, discountValue: e.target.value })}
                className="mt-2 w-full rounded-md border border-border bg-background p-3 font-mono text-sm outline-none focus:border-accent"
                placeholder={couponForm.discountType === "PERCENTAGE" ? "20" : "500"}
              />
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-3">
            <div>
              <label className="block font-mono text-[10px] tracking-widest text-muted-foreground uppercase">
                TOTAL USAGE LIMIT
              </label>
              <input
                type="number"
                min="1"
                disabled={submitting}
                value={couponForm.usageLimit}
                onChange={(e) => setCouponForm({ ...couponForm, usageLimit: e.target.value })}
                className="mt-2 w-full rounded-md border border-border bg-background p-3 font-mono text-sm outline-none focus:border-accent"
                placeholder="100"
              />
            </div>

            <div>
              <label className="block font-mono text-[10px] tracking-widest text-muted-foreground uppercase">
                PER-USER LIMIT
              </label>
              <input
                type="number"
                min="1"
                disabled={submitting}
                value={couponForm.perUserLimit}
                onChange={(e) => setCouponForm({ ...couponForm, perUserLimit: e.target.value })}
                className="mt-2 w-full rounded-md border border-border bg-background p-3 font-mono text-sm outline-none focus:border-accent"
                placeholder="1"
              />
            </div>

            <div>
              <label className="block font-mono text-[10px] tracking-widest text-muted-foreground uppercase">
                MIN. PURCHASE (₹)
              </label>
              <input
                type="number"
                min="0"
                step="any"
                disabled={submitting}
                value={couponForm.minimumPurchase}
                onChange={(e) => setCouponForm({ ...couponForm, minimumPurchase: e.target.value })}
                className="mt-2 w-full rounded-md border border-border bg-background p-3 font-mono text-sm outline-none focus:border-accent"
                placeholder="0"
              />
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="block font-mono text-[10px] tracking-widest text-muted-foreground uppercase">
                START DATE (OPTIONAL)
              </label>
              <input
                type="datetime-local"
                disabled={submitting}
                value={couponForm.startDate}
                onChange={(e) => setCouponForm({ ...couponForm, startDate: e.target.value })}
                className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm outline-none focus:border-accent"
              />
            </div>

            <div>
              <label className="block font-mono text-[10px] tracking-widest text-muted-foreground uppercase">
                EXPIRY DATE *
              </label>
              <input
                required
                type="datetime-local"
                disabled={submitting}
                value={couponForm.expiryDate}
                onChange={(e) => setCouponForm({ ...couponForm, expiryDate: e.target.value })}
                className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm outline-none focus:border-accent"
              />
            </div>
          </div>

          <label className="flex items-center gap-3 font-mono text-xs cursor-pointer select-none">
            <input
              type="checkbox"
              disabled={submitting}
              checked={couponForm.active}
              onChange={(e) => setCouponForm({ ...couponForm, active: e.target.checked })}
              className="size-4 rounded border-border accent-[hsl(var(--accent))]"
            />
            <span>Active immediately upon creation</span>
          </label>

          <div className="mt-2 flex flex-wrap gap-3">
            <button
              type="submit"
              disabled={submitting}
              className="flex items-center gap-2 rounded-md bg-foreground px-5 py-3 font-mono text-[10px] tracking-widest text-background disabled:opacity-50 hover:opacity-90 transition-opacity"
            >
              {submitting ? (
                <>
                  <Loader2 className="size-3.5 animate-spin" /> CREATING...
                </>
              ) : (
                "CREATE COUPON"
              )}
            </button>
            <Link
              href="/admin/coupons"
              className="rounded-md border border-border px-5 py-3 font-mono text-[10px] tracking-widest text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
            >
              CANCEL
            </Link>
          </div>
        </form>
      </div>
    )
  }

  // Fallback for other kinds if invoked
  return (
    <div className="max-w-2xl">
      <form
        onSubmit={(e) => { e.preventDefault(); router.push(isUser ? "/admin/users" : "/admin/resources") }}
        className="mt-6 flex flex-col gap-5 rounded-lg border border-border bg-card p-6"
      >
        <label className="font-mono text-[10px] tracking-widest">
          {isUser ? "NAME" : "RESOURCE TITLE"}
          <input required className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm" placeholder={isUser ? "Rahul Sharma" : "Hook Writing Checklist"} />
        </label>

        {isUser && (
          <>
            <label className="font-mono text-[10px] tracking-widest">
              EMAIL
              <input required type="email" className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm" placeholder="rahul@example.com" />
            </label>
            <label className="font-mono text-[10px] tracking-widest">
              PHONE
              <input className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm" placeholder="+91 98765 43210" />
            </label>
          </>
        )}

        <div className="flex flex-wrap gap-3">
          <button type="submit" className="rounded-md bg-foreground px-4 py-3 font-mono text-[10px] tracking-widest text-background">
            {isUser ? "CREATE USER" : "PUBLISH RESOURCE"}
          </button>
          <Link href={isUser ? "/admin/users" : "/admin/resources"} className="rounded-md border border-border px-4 py-3 font-mono text-[10px] tracking-widest">
            CANCEL
          </Link>
        </div>
      </form>
    </div>
  )
}

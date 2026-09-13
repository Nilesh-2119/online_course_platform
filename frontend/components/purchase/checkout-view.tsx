"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { ArrowRight, CheckCircle2, Loader2, ShieldCheck, Lock, UserCheck, Tag, X } from "lucide-react"
import { FlowShell } from "@/components/shared/flow-shell"
import { useAuth } from "@/lib/auth"
import { formatCurrency } from "@/lib/utils"
import { mockCourses } from "@/mocks/seed-data"
import { paymentService, couponService } from "@/lib/api"
import type { PaymentStatus } from "@/lib/types"

const inputClass = "w-full rounded-xl border border-border bg-transparent px-4 py-3 text-sm font-sans outline-none focus:border-accent"

function loadRazorpayScript(): Promise<boolean> {
  return new Promise((resolve) => {
    if (typeof window === "undefined") {
      resolve(false)
      return
    }
    if ((window as any).Razorpay) {
      resolve(true)
      return
    }
    const script = document.createElement("script")
    script.src = "https://checkout.razorpay.com/v1/checkout.js"
    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)
    document.body.appendChild(script)
  })
}

export function CheckoutView() {
  const { user, refreshUser, updateProfile } = useAuth()
  const course = mockCourses[0]

  const [form, setForm] = useState({
    email: user?.email || "",
    name: user?.name || "",
    phone: user?.phone || "",
  })
  const [accepted, setAccepted] = useState(false)
  const [status, setStatus] = useState<PaymentStatus>("idle")
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  // Coupon state
  const [couponInput, setCouponInput] = useState("")
  const [validatingCoupon, setValidatingCoupon] = useState(false)
  const [appliedCoupon, setAppliedCoupon] = useState<{
    code: string
    discountAmount: number
    finalPrice: number
    discountType?: string
    discountValue?: number
  } | null>(null)
  const [couponError, setCouponError] = useState<string | null>(null)

  useEffect(() => {
    if (user) {
      setForm({
        email: user.email || "",
        name: user.name || "",
        phone: user.phone || "",
      })
    }
  }, [user])

  const update = (key: string, value: string) => setForm((v) => ({ ...v, [key]: value }))
  const isProcessing = status === "processing"
  const isSuccess = status === "success"
  const isAlreadyPaid = user?.type === "paid"

  const finalPrice = appliedCoupon ? appliedCoupon.finalPrice : course.price
  const isFreeEnrollment = finalPrice === 0

  const valid = Boolean(form.email && form.name && accepted && !isProcessing && !isSuccess && !isAlreadyPaid)

  const handleApplyCoupon = async () => {
    if (!couponInput.trim()) return
    setValidatingCoupon(true)
    setCouponError(null)

    try {
      const res = await couponService.validateCoupon(couponInput.trim(), Number(course.id) || 1)
      if (res.success && res.data && res.data.valid) {
        setAppliedCoupon({
          code: res.data.couponCode || couponInput.trim().toUpperCase(),
          discountAmount: res.data.discountAmount ?? 0,
          finalPrice: res.data.finalPrice ?? 0,
          discountType: res.data.discountType,
          discountValue: res.data.discountValue,
        })
        setCouponError(null)
      } else {
        setAppliedCoupon(null)
        setCouponError(res.data?.message || res.error || "Invalid coupon code")
      }
    } catch (err: any) {
      setAppliedCoupon(null)
      setCouponError(err.message || "Failed to validate coupon")
    } finally {
      setValidatingCoupon(false)
    }
  }

  const handleRemoveCoupon = () => {
    setCouponInput("")
    setAppliedCoupon(null)
    setCouponError(null)
  }

  const handlePay = async () => {
    if (!valid) return
    setStatus("processing")
    setErrorMessage(null)

    try {
      // Step 1: Create Order on Spring Boot Backend
      const orderRes = await paymentService.createCheckout({
        email: form.email,
        name: form.name,
        phone: form.phone || undefined,
        courseId: course.id,
        couponCode: appliedCoupon?.code || undefined,
      })

      if (!orderRes.success || !orderRes.data) {
        setStatus("failed")
        setErrorMessage(orderRes.error || "Order creation failed.")
        return
      }

      const { razorpayOrderId, amount, currency, keyId } = orderRes.data

      // Step 1a: If 100% free coupon applied, access is granted immediately without payment modal
      if (amount === 0 || (razorpayOrderId && razorpayOrderId.startsWith("FREE_COUPON_"))) {
        setStatus("success")
        updateProfile({ type: "paid", coursePurchased: true })
        try {
          await refreshUser()
        } catch (refreshErr) {
          console.warn("User refresh non-blocking warning:", refreshErr)
        }
        setTimeout(() => {
          window.location.href = "/dashboard"
        }, 800)
        return
      }

      // Step 2: Load SDK and open Razorpay Checkout Modal
      const scriptLoaded = await loadRazorpayScript()
      if (!scriptLoaded) {
        setStatus("failed")
        setErrorMessage("Could not load payment gateway SDK. Please check your internet connection.")
        return
      }

      const activeKey =
        (keyId && !keyId.includes("placeholder"))
          ? keyId
          : (process.env.NEXT_PUBLIC_RAZORPAY_KEY_ID || "").trim().replace(/['"]/g, "")

      if (!activeKey) {
        setStatus("failed")
        setErrorMessage("Payment gateway is not configured. Please configure RAZORPAY_KEY_ID.")
        return
      }

      const options = {
        key: activeKey,
        amount: Math.round(amount * 100), // in paise
        currency: currency || "INR",
        name: "Creative Ads Masterclass",
        description: course.title,
        order_id: razorpayOrderId,
        prefill: {
          name: form.name,
          email: form.email,
          contact: form.phone || "",
        },
        theme: {
          color: "#0f172a",
        },
        handler: async (response: any) => {
          try {
            console.log("Razorpay checkout success response:", response)
            // Step 3: Cryptographically verify signature on backend
            const verifyRes = await paymentService.verifyPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })

            if (verifyRes.success) {
              setStatus("success")
              updateProfile({ type: "paid", coursePurchased: true })
              try {
                await refreshUser()
              } catch (refreshErr) {
                console.warn("User refresh non-blocking warning:", refreshErr)
              }
              setTimeout(() => {
                window.location.href = "/dashboard"
              }, 800)
            } else {
              setStatus("failed")
              setErrorMessage(verifyRes.error || "Payment verification failed.")
            }
          } catch (handlerErr: any) {
            console.error("Error in payment handler:", handlerErr)
            setStatus("failed")
            setErrorMessage(handlerErr.message || "Payment verification could not be completed.")
          }
        },
        modal: {
          ondismiss: () => {
            setStatus("idle")
            setErrorMessage("Payment checkout window was closed.")
          },
        },
      }

      const rzp = new (window as any).Razorpay(options)

      // Handle payment.failed event as required by Razorpay standard integration
      rzp.on("payment.failed", (response: any) => {
        setStatus("failed")
        setErrorMessage(
          response?.error?.description ||
            response?.error?.reason ||
            "Payment failed. Please try again or use another payment method."
        )
      })

      rzp.open()
    } catch (err: any) {
      setStatus("failed")
      setErrorMessage(err.message || "An unexpected error occurred during checkout.")
    }
  }

  return (
    <FlowShell back>
      <div className="mx-auto max-w-2xl">
        <p className="font-mono text-xs tracking-[.2em] text-muted-foreground">SECURE ENROLLMENT / ADFIX STUDIO</p>
        <h1 className="mt-3 text-4xl font-black tracking-[-.07em] md:text-5xl">Complete your purchase.</h1>

        <div className="mt-6 rounded-3xl border border-border bg-background p-5 md:p-7 shadow-lg">
          <div className="flex items-end justify-between gap-4 border-b border-border pb-4">
            <div>
              <p className="font-bold">{course.title.toUpperCase()}</p>
              <p className="mt-1 font-mono text-xs text-muted-foreground">Recorded masterclass · Lifetime Access</p>
            </div>
            <div className="text-right">
              {appliedCoupon ? (
                <>
                  <span className="text-sm font-mono line-through text-muted-foreground block">
                    {formatCurrency(course.price, course.currency)}
                  </span>
                  <strong className="text-2xl font-black text-accent">
                    {isFreeEnrollment ? "FREE" : formatCurrency(finalPrice, course.currency)}
                  </strong>
                </>
              ) : (
                <strong className="text-2xl font-black">{formatCurrency(course.price, course.currency)}</strong>
              )}
            </div>
          </div>

          {/* Already Enrolled Banner */}
          {isAlreadyPaid ? (
            <div className="my-8 rounded-2xl border border-accent/40 bg-accent/10 p-6 text-center">
              <CheckCircle2 className="mx-auto size-12 text-accent" />
              <h2 className="mt-3 text-2xl font-black">You Already Own Lifetime Access!</h2>
              <p className="mt-2 font-mono text-xs text-muted-foreground">
                Your account ({user?.email}) is already enrolled in this course.
              </p>
              <Link
                href="/dashboard"
                className="mt-6 inline-flex items-center justify-center gap-2 rounded-2xl bg-accent px-6 py-4 font-bold text-accent-foreground transition-transform hover:scale-[1.02]"
              >
                GO TO DASHBOARD <ArrowRight className="size-5" />
              </Link>
            </div>
          ) : isSuccess ? (
            <div className="my-8 rounded-2xl border border-accent/40 bg-accent/10 p-6 text-center">
              <CheckCircle2 className="mx-auto size-12 text-accent" />
              <h2 className="mt-3 text-2xl font-black">Payment Confirmed!</h2>
              <p className="mt-2 font-mono text-xs text-muted-foreground">
                Your lifetime access has been activated. Redirecting you to your dashboard...
              </p>
            </div>
          ) : (
            <>
              {/* Authenticated User Badge */}
              <div className="mt-5 flex items-center justify-between rounded-xl border border-accent/20 bg-accent/5 p-3 font-mono text-xs">
                <div className="flex items-center gap-2 text-accent font-semibold">
                  <UserCheck className="size-4" /> Enrolling as: <span className="text-foreground">{user?.email}</span>
                </div>
                <span className="rounded bg-accent/20 px-2 py-0.5 text-[10px] font-bold text-accent">VERIFIED</span>
              </div>

              <div className="mt-5 grid gap-3">
                <label className="grid gap-1.5 font-mono text-xs">
                  Full Name
                  <input
                    disabled={isProcessing}
                    required
                    type="text"
                    value={form.name}
                    onChange={(e) => update("name", e.target.value)}
                    className={inputClass}
                    placeholder="Enter your name"
                  />
                </label>

                <label className="grid gap-1.5 font-mono text-xs">
                  Account Email (Linked)
                  <input
                    disabled
                    readOnly
                    type="email"
                    value={form.email}
                    className={`${inputClass} opacity-70 cursor-not-allowed bg-muted/30`}
                  />
                </label>

                <label className="grid gap-1.5 font-mono text-xs">
                  Phone Number
                  <input
                    disabled={isProcessing}
                    type="tel"
                    value={form.phone}
                    onChange={(e) => update("phone", e.target.value)}
                    className={inputClass}
                    placeholder="+91 9876543210"
                  />
                </label>
              </div>

              {/* Coupon Code Section */}
              <div className="mt-5 rounded-2xl border border-border/80 bg-muted/20 p-4">
                <div className="flex items-center gap-2 text-xs font-mono font-semibold text-muted-foreground mb-2">
                  <Tag className="size-3.5" /> HAVE A COUPON CODE?
                </div>
                {appliedCoupon ? (
                  <div className="flex items-center justify-between rounded-xl bg-accent/15 border border-accent/30 p-3">
                    <div className="flex items-center gap-2">
                      <CheckCircle2 className="size-4 text-accent" />
                      <span className="font-mono text-xs font-bold text-accent">
                        {appliedCoupon.code} APPLIED
                      </span>
                      <span className="text-xs text-muted-foreground">
                        (-{formatCurrency(appliedCoupon.discountAmount, course.currency)} OFF)
                      </span>
                    </div>
                    <button
                      type="button"
                      onClick={handleRemoveCoupon}
                      className="text-muted-foreground hover:text-foreground text-xs font-mono p-1 rounded-lg hover:bg-background transition-colors"
                      title="Remove coupon"
                    >
                      <X className="size-4" />
                    </button>
                  </div>
                ) : (
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={couponInput}
                      onChange={(e) => {
                        setCouponInput(e.target.value.toUpperCase())
                        if (couponError) setCouponError(null)
                      }}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") {
                          e.preventDefault()
                          handleApplyCoupon()
                        }
                      }}
                      placeholder="ENTER CODE (e.g. ADFIX20)"
                      disabled={isProcessing || validatingCoupon}
                      className="flex-1 rounded-xl border border-border bg-background px-3 py-2 text-xs font-mono uppercase tracking-wider outline-none focus:border-accent"
                    />
                    <button
                      type="button"
                      disabled={!couponInput.trim() || isProcessing || validatingCoupon}
                      onClick={handleApplyCoupon}
                      className="rounded-xl bg-foreground px-4 py-2 text-xs font-bold text-background disabled:opacity-40 transition-colors"
                    >
                      {validatingCoupon ? <Loader2 className="size-3.5 animate-spin" /> : "APPLY"}
                    </button>
                  </div>
                )}
                {couponError && (
                  <p className="mt-2 text-[11px] font-mono text-destructive">{couponError}</p>
                )}
              </div>

              {/* Price Breakdown Summary */}
              {appliedCoupon && (
                <div className="mt-4 rounded-xl border border-border bg-muted/10 p-3.5 text-xs font-mono space-y-1.5">
                  <div className="flex justify-between text-muted-foreground">
                    <span>Original Price</span>
                    <span>{formatCurrency(course.price, course.currency)}</span>
                  </div>
                  <div className="flex justify-between text-accent font-semibold">
                    <span>Coupon Discount ({appliedCoupon.code})</span>
                    <span>-{formatCurrency(appliedCoupon.discountAmount, course.currency)}</span>
                  </div>
                  <div className="border-t border-border pt-1.5 flex justify-between font-bold text-foreground text-sm">
                    <span>Total Payable</span>
                    <span>{isFreeEnrollment ? "FREE" : formatCurrency(finalPrice, course.currency)}</span>
                  </div>
                </div>
              )}

              <label className="mt-5 flex items-start gap-3 font-mono text-xs leading-5 text-muted-foreground">
                <input
                  disabled={isProcessing}
                  type="checkbox"
                  checked={accepted}
                  onChange={(e) => setAccepted(e.target.checked)}
                  className="mt-1 accent-[hsl(var(--accent))]"
                />
                <span>
                  I agree to the{" "}
                  <Link href="/terms" className="text-foreground underline">
                    Terms &amp; Conditions
                  </Link>{" "}
                  and{" "}
                  <Link href="/privacy" className="text-foreground underline">
                    Privacy Policy
                  </Link>
                </span>
              </label>

              <div className="mt-4 flex items-center gap-3 rounded-xl bg-muted p-3">
                <ShieldCheck className="size-5 text-accent shrink-0" />
                <div>
                  <p className="font-mono text-[10px] font-bold tracking-[.15em]">SECURE PAYMENT ENCRYPTION</p>
                  <p className="font-mono text-xs text-muted-foreground">
                    Direct HMAC-SHA256 authenticated checkout. Instant activation after payment.
                  </p>
                </div>
              </div>

              {errorMessage && (
                <p className="mt-3 font-mono text-xs text-destructive">{errorMessage}</p>
              )}

              <button
                disabled={!valid}
                onClick={handlePay}
                className="mt-5 flex w-full items-center justify-between rounded-2xl bg-foreground px-5 py-4 font-bold text-background disabled:opacity-40 transition-transform hover:scale-[1.01] active:scale-[0.99]"
              >
                {isProcessing ? (
                  <>
                    <span className="flex items-center gap-2">
                      <Loader2 className="size-4 animate-spin" /> {isFreeEnrollment ? "ENROLLING..." : "OPENING SECURE CHECKOUT..."}
                    </span>
                    <span className="font-mono text-xs">
                      {isFreeEnrollment ? "FREE" : formatCurrency(finalPrice, course.currency)}
                    </span>
                  </>
                ) : (
                  <>
                    <span>
                      {isFreeEnrollment
                        ? "ENROLL FOR FREE (100% OFF)"
                        : `PAY ${formatCurrency(finalPrice, course.currency)}`}
                    </span>
                    <ArrowRight className="size-5" />
                  </>
                )}
              </button>
            </>
          )}
        </div>
      </div>
    </FlowShell>
  )
}

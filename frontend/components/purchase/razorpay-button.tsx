"use client"

import { useState } from "react"
import { Loader2 } from "lucide-react"

interface RazorpayButtonProps {
  amount?: number // Amount in paise (minimum 100 paise)
  currency?: string
  name?: string
  description?: string
  receipt?: string
  notes?: Record<string, string>
  prefill?: {
    name?: string
    email?: string
    contact?: string
  }
  onSuccess?: (data: { orderId: string; paymentId: string; signature: string }) => void
  onFailure?: (errorMessage: string) => void
  className?: string
  children?: React.ReactNode
}

function loadRazorpayCheckoutScript(): Promise<boolean> {
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
    script.async = true
    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)
    document.body.appendChild(script)
  })
}

export function RazorpayCheckoutButton({
  amount = 499900,
  currency = "INR",
  name = "AdFix Studio Masterclass",
  description = "Lifetime access to Creative Ads Course",
  receipt,
  notes,
  prefill,
  onSuccess,
  onFailure,
  className = "w-full rounded-2xl bg-accent px-6 py-4 font-bold text-accent-foreground transition-all hover:scale-[1.01] active:scale-[0.99] disabled:opacity-50",
  children,
}: RazorpayButtonProps) {
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const handleCheckout = async () => {
    setLoading(true)
    setErrorMessage(null)

    try {
      // Step 2a: Ensure Razorpay SDK script is loaded
      const isLoaded = await loadRazorpayCheckoutScript()
      if (!isLoaded) {
        throw new Error("Failed to load Razorpay SDK. Please check your internet connection.")
      }

      // Step 1: Create Order on Backend (/api/create-order)
      const orderRes = await fetch("/api/create-order", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          amount,
          currency,
          receipt: receipt || `rcpt_${Date.now()}`,
          notes: notes || {},
        }),
      })

      if (!orderRes.ok) {
        const errData = await orderRes.json().catch(() => ({}))
        throw new Error(errData.error || `Failed to create order (${orderRes.status})`)
      }

      const orderData = await orderRes.json()
      const { order_id, amount: orderAmount, currency: orderCurrency, key_id } = orderData

      // Step 2: Configure Razorpay Checkout modal
      const options = {
        key: key_id || process.env.NEXT_PUBLIC_RAZORPAY_KEY_ID || "",
        amount: orderAmount,
        currency: orderCurrency,
        name,
        description,
        order_id,
        prefill: {
          name: prefill?.name || "",
          email: prefill?.email || "",
          contact: prefill?.contact || "",
        },
        theme: {
          color: "#0f172a",
        },
        handler: async (response: {
          razorpay_order_id: string
          razorpay_payment_id: string
          razorpay_signature: string
        }) => {
          try {
            // Step 3: Cryptographically verify signature on Backend (/api/verify-payment)
            const verifyRes = await fetch("/api/verify-payment", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                order_id: response.razorpay_order_id,
                payment_id: response.razorpay_payment_id,
                signature: response.razorpay_signature,
              }),
            })

            const verifyData = await verifyRes.json().catch(() => ({}))

            if (!verifyRes.ok || !verifyData.success) {
              const err = verifyData.error || "Payment signature verification failed"
              setErrorMessage(err)
              onFailure?.(err)
              return
            }

            setErrorMessage(null)
            onSuccess?.({
              orderId: response.razorpay_order_id,
              paymentId: response.razorpay_payment_id,
              signature: response.razorpay_signature,
            })
          } catch (verifyErr: any) {
            const err = verifyErr.message || "Payment verification network error"
            setErrorMessage(err)
            onFailure?.(err)
            console.error("Verification failed:", verifyErr)
          } finally {
            setLoading(false)
          }
        },
        modal: {
          ondismiss: () => {
            setLoading(false)
            setErrorMessage("Checkout dismissed by user.")
            onFailure?.("Checkout dismissed by user")
          },
        },
      }

      const rzp = new (window as any).Razorpay(options)

      // Handle payment.failed event
      rzp.on("payment.failed", (response: any) => {
        setLoading(false)
        const desc =
          response?.error?.description ||
          response?.error?.reason ||
          "Payment failed. Please try again with another method."
        setErrorMessage(desc)
        onFailure?.(desc)
      })

      rzp.open()
    } catch (err: any) {
      setLoading(false)
      const msg = err.message || "Failed to initialize checkout"
      setErrorMessage(msg)
      onFailure?.(msg)
    }
  }

  return (
    <div className="flex flex-col gap-2">
      <button
        type="button"
        onClick={handleCheckout}
        disabled={loading}
        className={className}
      >
        {loading ? (
          <span className="flex items-center justify-center gap-2">
            <Loader2 className="size-4 animate-spin" />
            Initializing Payment...
          </span>
        ) : (
          children || "Pay with Razorpay"
        )}
      </button>

      {errorMessage && (
        <p className="font-mono text-xs text-destructive text-center">{errorMessage}</p>
      )}
    </div>
  )
}

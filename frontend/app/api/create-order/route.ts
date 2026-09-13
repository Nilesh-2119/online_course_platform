import { NextResponse } from "next/server"
import Razorpay from "razorpay"

export async function POST(request: Request) {
  try {
    const body = await request.json().catch(() => ({}))
    const rawAmount = body.amount
    const currency = (body.currency || "INR").toUpperCase()
    const receipt = body.receipt || `rcpt_${Date.now()}`

    // Ensure amount is in paise (minimum 100 paise = 1 INR)
    const amount = Number(rawAmount)

    if (isNaN(amount) || amount < 100) {
      return NextResponse.json(
        { error: "Minimum order amount is 100 paise (1 INR)" },
        { status: 400 }
      )
    }

    const keyId = (process.env.RAZORPAY_KEY_ID || process.env.NEXT_PUBLIC_RAZORPAY_KEY_ID || "").trim().replace(/['"]/g, "")
    const keySecret = (process.env.RAZORPAY_KEY_SECRET || "").trim().replace(/['"]/g, "")

    if (!keyId || !keySecret || keyId.includes("placeholder") || keySecret.includes("placeholder")) {
      return NextResponse.json(
        { error: "Razorpay credentials are not configured on server. Please set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET in environment variables." },
        { status: 500 }
      )
    }

    const razorpay = new Razorpay({
      key_id: keyId,
      key_secret: keySecret,
    })

    const options = {
      amount: Math.round(amount),
      currency,
      receipt,
      notes: body.notes || {},
    }

    const order = await razorpay.orders.create(options)

    return NextResponse.json({
      order_id: order.id,
      amount: order.amount,
      currency: order.currency,
      key_id: keyId,
    })
  } catch (err: any) {
    console.error("Razorpay order creation error:", err)
    let status = 500
    if (err.statusCode === 401 || err.status === 401 || err.error?.code === "AUTHENTICATION_FAILED") {
      status = 401
    } else if (err.statusCode) {
      status = err.statusCode
    } else if (err.error?.code === "BAD_REQUEST_ERROR") {
      status = 400
    }

    return NextResponse.json(
      { error: err.error?.description || err.message || "Failed to create Razorpay order" },
      { status }
    )
  }
}

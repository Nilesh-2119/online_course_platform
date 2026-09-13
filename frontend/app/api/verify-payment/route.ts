import { NextResponse } from "next/server"
import crypto from "crypto"

export async function POST(request: Request) {
  try {
    const body = await request.json().catch(() => ({}))

    const orderId = body.razorpay_order_id || body.order_id
    const paymentId = body.razorpay_payment_id || body.payment_id
    const signature = body.razorpay_signature || body.signature

    // Validate all 3 required fields
    if (!orderId || !paymentId || !signature) {
      return NextResponse.json(
        {
          success: false,
          error: "Missing required fields: order_id, payment_id, and signature are all required",
        },
        { status: 400 }
      )
    }

    const keySecret = (process.env.RAZORPAY_KEY_SECRET || "").trim().replace(/['"]/g, "")

    if (!keySecret || keySecret.includes("placeholder")) {
      return NextResponse.json(
        { success: false, error: "Razorpay secret key is not configured on server. Please set RAZORPAY_KEY_SECRET in environment variables." },
        { status: 500 }
      )
    }

    // Algorithm: HMAC-SHA256(order_id + "|" + payment_id, KEY_SECRET)
    const computeSignature = (secret: string) =>
      crypto
        .createHmac("sha256", secret)
        .update(`${orderId}|${paymentId}`)
        .digest("hex")

    const expectedSignature = computeSignature(keySecret)

    // Constant-time comparison to prevent timing attacks
    const isSignatureValid =
      expectedSignature.length === signature.length &&
      crypto.timingSafeEqual(
        Buffer.from(expectedSignature, "utf8"),
        Buffer.from(signature, "utf8")
      )

    if (!isSignatureValid) {
      return NextResponse.json(
        {
          success: false,
          error: "Invalid payment signature: verification failed",
        },
        { status: 400 }
      )
    }

    return NextResponse.json({
      success: true,
      message: "Payment verified successfully",
      order_id: orderId,
      payment_id: paymentId,
    })
  } catch (err: any) {
    console.error("Razorpay signature verification error:", err)
    return NextResponse.json(
      { success: false, error: err.message || "Payment verification error" },
      { status: 500 }
    )
  }
}

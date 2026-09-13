/** Order & payment domain types. */

export type OrderStatus = "paid" | "pending" | "failed" | "refunded" | "cancelled"
export type PaymentProvider = "razorpay" | "stripe" | "manual" | "mock"
export type PaymentStatus = "idle" | "loading" | "processing" | "success" | "failed" | "cancelled" | "pending" | "already_purchased"

export interface Order {
  id: string
  userId: string
  customerName: string
  customerEmail: string
  productId: string
  productTitle: string
  amount: number // raw numeric value
  currency: string
  discountAmount: number
  finalAmount: number
  couponCode?: string
  status: OrderStatus
  paymentProvider?: PaymentProvider
  transactionId?: string
  createdAt: string // ISO 8601
  refundedAt?: string // ISO 8601
}

export interface CheckoutRequest {
  email: string
  name: string
  phone?: string
  courseId: string
  couponCode?: string
}

export interface PaymentResult {
  orderId: string
  status: PaymentStatus
  transactionId?: string
  message?: string
}

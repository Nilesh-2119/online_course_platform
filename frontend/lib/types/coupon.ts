/** Coupon domain types. */

export type CouponStatus = "ACTIVE" | "INACTIVE" | "EXPIRED" | "EXHAUSTED" | "active" | "inactive" | "expired"
export type DiscountType = "PERCENTAGE" | "FIXED" | "percentage" | "fixed"

export interface Coupon {
  id: string | number
  code: string
  discountType: DiscountType
  discountValue: number // percentage (0-100) or fixed amount in INR
  currency?: string
  usageCount: number
  usageLimit: number
  perUserLimit?: number
  minimumPurchase?: number
  applicableCourseId?: string
  startDate?: string // ISO 8601
  expiryDate: string // ISO 8601
  active?: boolean
  status: CouponStatus
  createdAt?: string
}

export interface CreateCouponDto {
  code: string
  discountType: "PERCENTAGE" | "FIXED"
  discountValue: number
  currency?: string
  usageLimit?: number
  perUserLimit?: number
  minimumPurchase?: number
  startDate?: string | null
  expiryDate: string
  active?: boolean
}

export interface CouponValidationResult {
  valid: boolean
  message: string
  couponId?: number
  couponCode?: string
  discountType?: "PERCENTAGE" | "FIXED"
  discountValue?: number
  originalPrice?: number
  discountAmount?: number
  finalPrice?: number
}

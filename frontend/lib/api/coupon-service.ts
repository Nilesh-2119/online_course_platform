/**
 * Coupon service connected to Spring Boot backend.
 */

import type { ApiResponse, Coupon, CreateCouponDto, CouponValidationResult } from "@/lib/types"
import { apiClient } from "./client"

/**
 * Fetch all coupons (admin).
 */
export async function getCoupons(): Promise<ApiResponse<Coupon[]>> {
  const res = await apiClient<Coupon[]>("/api/v1/admin/coupons", {
    method: "GET",
    requiresAuth: true,
  })
  if (!res.success || !res.data) {
    return { data: [], success: false, error: res.error }
  }
  return res
}

/**
 * Fetch a single coupon by ID (admin).
 */
export async function getCoupon(id: string | number): Promise<ApiResponse<Coupon>> {
  return apiClient<Coupon>(`/api/v1/admin/coupons/${id}`, {
    method: "GET",
    requiresAuth: true,
  })
}

/**
 * Create a new coupon (admin).
 */
export async function createCoupon(data: CreateCouponDto): Promise<ApiResponse<Coupon>> {
  return apiClient<Coupon>("/api/v1/admin/coupons", {
    method: "POST",
    body: JSON.stringify(data),
    requiresAuth: true,
  })
}

/**
 * Update an existing coupon (admin).
 */
export async function updateCoupon(id: string | number, data: CreateCouponDto): Promise<ApiResponse<Coupon>> {
  return apiClient<Coupon>(`/api/v1/admin/coupons/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
    requiresAuth: true,
  })
}

/**
 * Delete a coupon (admin).
 */
export async function deleteCoupon(id: string | number): Promise<ApiResponse<void>> {
  return apiClient<void>(`/api/v1/admin/coupons/${id}`, {
    method: "DELETE",
    requiresAuth: true,
  })
}

/**
 * Toggle coupon active/inactive status (admin).
 */
export async function toggleCouponActive(id: string | number): Promise<ApiResponse<Coupon>> {
  return apiClient<Coupon>(`/api/v1/admin/coupons/${id}/toggle`, {
    method: "PATCH",
    requiresAuth: true,
  })
}

/**
 * Validate a coupon code at checkout (authenticated user).
 */
export async function validateCoupon(code: string, courseId: number = 1): Promise<ApiResponse<CouponValidationResult>> {
  const res = await apiClient<CouponValidationResult>("/api/v1/coupons/validate", {
    method: "POST",
    body: JSON.stringify({ code: code.trim().toUpperCase(), courseId }),
    requiresAuth: true,
  })
  return res
}

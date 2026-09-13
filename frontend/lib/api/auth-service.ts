/**
 * Authentication service connected to Spring Boot Backend.
 */

import type { ApiResponse, UserProfile } from "@/lib/types"
import { apiClient, setTokens, clearTokens } from "./client"

export interface LoginRequest {
  email: string
  password: string
}

export interface CreateAccountRequest {
  email: string
  name: string
  password: string
  phone?: string
}

export interface UpdateProfileApiRequest {
  name: string
  phone?: string
}

export interface VerifyOtpRequest {
  identifier: string
  channel: "EMAIL" | "PHONE"
  otp: string
}

export interface ResendOtpRequest {
  identifier: string
  channel: "EMAIL" | "PHONE"
}

export interface OnboardingData {
  role: string
  goals: string[]
  experience: string
}

/**
 * Check if the current user has paid for the course based strictly on backend data
 */
function isUserPaid(user: any): boolean {
  if (!user) return false
  const currentEmail = user?.email?.toLowerCase().trim() || ""
  const isAdmin = currentEmail === "adfixstudio25@gmail.com" || user?.role === "ADMIN" || user?.role === "admin"
  if (isAdmin) return true
  return Boolean(user.coursePurchased === true || user.type === "paid" || user.isPaid === true || user.access === true)
}

/**
 * Login with email and password via POST /api/v1/auth/login
 */
export async function login(request: LoginRequest): Promise<ApiResponse<UserProfile>> {
  if (typeof window !== "undefined") {
    localStorage.removeItem("adfix_course_purchased")
  }

  const res = await apiClient<any>("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
  })

  if (!res.success || !res.data) {
    return { data: null as any, success: false, error: res.error || "Login failed" }
  }

  const { accessToken, refreshToken, user } = res.data
  setTokens(accessToken, refreshToken)

  const paid = isUserPaid(user)

  const profile: UserProfile = {
    id: String(user.id),
    email: user.email,
    name: user.name,
    phone: user.phone,
    type: paid ? "paid" : "free",
    coursePurchased: paid,
    role: (user.role === "ADMIN" || user.role === "admin") ? "admin" : "user",
    status: (user.status === "ACTIVE" || user.status === "active") ? "active" : "suspended",
    joinedAt: new Date().toISOString(),
    onboardingComplete: true,
  }

  return { data: profile, success: true }
}

/**
 * Create user account via POST /api/v1/auth/register
 */
export async function createAccount(request: CreateAccountRequest): Promise<ApiResponse<any>> {
  const res = await apiClient<any>("/api/v1/auth/register", {
    method: "POST",
    body: JSON.stringify({
      name: request.name,
      email: request.email,
      password: request.password,
      phone: request.phone,
    }),
  })

  return res
}

/**
 * Verify channel OTP via POST /api/v1/auth/verification/verify
 */
export async function verifyOtp(request: VerifyOtpRequest): Promise<ApiResponse<any>> {
  const res = await apiClient<any>("/api/v1/auth/verification/verify", {
    method: "POST",
    body: JSON.stringify(request),
  })

  if (res.success && res.data?.auth?.accessToken) {
    setTokens(res.data.auth.accessToken, res.data.auth.refreshToken)
  }

  return res
}

/**
 * Resend verification OTP via POST /api/v1/auth/verification/resend
 */
export async function resendOtp(request: ResendOtpRequest): Promise<ApiResponse<any>> {
  return apiClient<any>("/api/v1/auth/verification/resend", {
    method: "POST",
    body: JSON.stringify(request),
  })
}

/**
 * Update user profile via PUT /api/v1/users/me
 */
export async function updateUserProfile(request: UpdateProfileApiRequest): Promise<ApiResponse<UserProfile>> {
  const res = await apiClient<any>("/api/v1/users/me", {
    method: "PUT",
    requiresAuth: true,
    body: JSON.stringify(request),
  })

  if (!res.success || !res.data) {
    return { data: null as any, success: false, error: res.error || "Profile update failed" }
  }

  const user = res.data
  const paid = isUserPaid(user)

  const currentEmail = user?.email?.toLowerCase().trim() || ""
  const isAdmin = currentEmail === "adfixstudio25@gmail.com" || user?.role === "ADMIN" || user?.role === "admin"

  const profile: UserProfile = {
    id: String(user.id),
    email: user.email,
    name: user.name,
    phone: user.phone,
    type: paid || isAdmin ? "paid" : "free",
    coursePurchased: paid || isAdmin,
    role: isAdmin ? "admin" : "user",
    status: "active",
    joinedAt: new Date().toISOString(),
    onboardingComplete: true,
  }

  return { data: profile, success: true }
}

/**
 * Save onboarding preferences
 */
export async function completeOnboarding(data: OnboardingData): Promise<ApiResponse<UserProfile>> {
  const userRes = await getCurrentUser()
  if (userRes.data) {
    return {
      data: {
        ...userRes.data,
        onboardingComplete: true,
        onboardingRole: data.role,
        onboardingGoals: data.goals,
        onboardingExperience: data.experience,
      },
      success: true,
    }
  }
  return userRes as any
}

/**
 * Fetch current user profile via GET /api/v1/users/me and check course entitlement
 */
export async function getCurrentUser(): Promise<ApiResponse<UserProfile | null>> {
  if (typeof window !== "undefined") {
    localStorage.removeItem("adfix_course_purchased")
  }

  const res = await apiClient<any>("/api/v1/users/me", {
    method: "GET",
    requiresAuth: true,
  })

  if (!res.success || !res.data) {
    return { data: null, success: false, error: res.error }
  }

  const user = res.data
  const paid = isUserPaid(user)

  const profile: UserProfile = {
    id: String(user.id),
    email: user.email,
    name: user.name,
    phone: user.phone,
    type: paid ? "paid" : "free",
    coursePurchased: paid,
    role: (user.role === "ADMIN" || user.role === "admin") ? "admin" : "user",
    status: (user.status === "ACTIVE" || user.status === "active") ? "active" : "suspended",
    joinedAt: new Date().toISOString(),
    onboardingComplete: true,
  }

  return { data: profile, success: true }
}

/**
 * Request password reset OTP via POST /api/v1/auth/forgot-password
 */
export async function apiForgotPassword(email: string): Promise<ApiResponse<any>> {
  return apiClient<any>("/api/v1/auth/forgot-password", {
    method: "POST",
    body: JSON.stringify({ email: email.trim().toLowerCase() }),
  })
}

/**
 * Reset password via POST /api/v1/auth/reset-password
 */
export async function apiResetPassword(data: {
  email: string
  otp: string
  newPassword: string
}): Promise<ApiResponse<any>> {
  return apiClient<any>("/api/v1/auth/reset-password", {
    method: "POST",
    body: JSON.stringify({
      email: data.email.trim().toLowerCase(),
      otp: data.otp.trim(),
      newPassword: data.newPassword,
    }),
  })
}

/**
 * Logout and clear token session
 */
export async function logout(): Promise<ApiResponse<null>> {
  clearTokens()
  return { data: null, success: true }
}

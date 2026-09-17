/**
 * Admin API service connected to Spring Boot Backend.
 */

import { apiClient } from "./client"
import type { ApiResponse } from "@/lib/types"

export interface RecentEnrolledStudent {
  id: string
  name: string
  email: string
  enrolledAt: string
  courseTitle: string
}

export interface AdminOverviewData {
  totalUsers: number
  freeUsers: number
  enrolledStudents: number
  resourcesDownloaded: number
  recentEnrolledStudents: RecentEnrolledStudent[]
}

export interface AdminUserItem {
  id: string
  name: string
  email: string
  phone: string | null
  role: string
  status: string
  type: "paid" | "free"
  access: boolean
  joinedAt: string
  lastLoginAt: string | null
}

export interface AdminUserDetailData {
  id: string
  name: string
  email: string
  phone: string | null
  role: string
  status: string
  type: "paid" | "free"
  access: boolean
  joinedAt: string
  lastLoginAt: string | null
  courseTitle: string
  purchases: Array<{
    id: number
    courseTitle: string
    amount: number
    currency: string
    status: string
    razorpayOrderId?: string
    razorpayPaymentId?: string
    paidAt?: string
    createdAt: string
  }>
}

/**
 * Fetch real-time live database metrics for the Admin Overview tab.
 */
export async function getAdminOverview(): Promise<ApiResponse<AdminOverviewData>> {
  return apiClient<AdminOverviewData>("/api/v1/admin/overview", {
    method: "GET",
    requiresAuth: true,
  })
}

/**
 * Fetch list of all real users from the database.
 */
export async function getAdminUsers(): Promise<ApiResponse<AdminUserItem[]>> {
  return apiClient<AdminUserItem[]>("/api/v1/admin/users", {
    method: "GET",
    requiresAuth: true,
  })
}

/**
 * Fetch real user detail from database.
 */
export async function getAdminUserDetail(id: string): Promise<ApiResponse<AdminUserDetailData>> {
  return apiClient<AdminUserDetailData>(`/api/v1/admin/users/${id}`, {
    method: "GET",
    requiresAuth: true,
  })
}

/**
 * Revoke course access for a user.
 */
export async function revokeUserAccess(id: string): Promise<ApiResponse<{ access: boolean }>> {
  return apiClient<{ access: boolean }>(`/api/v1/admin/users/${id}/revoke-access`, {
    method: "POST",
    requiresAuth: true,
  })
}

/**
 * Grant course access for a user.
 */
export async function grantUserAccess(id: string): Promise<ApiResponse<{ access: boolean }>> {
  return apiClient<{ access: boolean }>(`/api/v1/admin/users/${id}/grant-access`, {
    method: "POST",
    requiresAuth: true,
  })
}

/**
 * Update user status (ACTIVE, SUSPENDED, DISABLED).
 */
export async function updateUserStatus(id: string, status: string): Promise<ApiResponse<{ status: string }>> {
  return apiClient<{ status: string }>(`/api/v1/admin/users/${id}/status`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
    requiresAuth: true,
  })
}

/**
 * Permanently delete a user and related database records.
 */
export async function deleteAdminUser(id: string): Promise<ApiResponse<{ message: string }>> {
  return apiClient<{ message: string }>(`/api/v1/admin/users/${id}`, {
    method: "DELETE",
    requiresAuth: true,
  })
}

export interface CreateAdminUserInput {
  name: string
  email: string
  phone?: string
  password: string
  grantCourseAccess?: boolean
  role?: string
}

/**
 * Admin creates a user directly with active status and optional course access (no OTP required).
 */
export async function createAdminUser(input: CreateAdminUserInput): Promise<ApiResponse<AdminUserItem>> {
  return apiClient<AdminUserItem>("/api/v1/admin/users", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
    requiresAuth: true,
  })
}

export interface MonthMetricPoint {
  month: string
  label: string
  value: number
}

export interface FreeVsEnrolledPoint {
  month: string
  label: string
  freeUsers: number
  enrolledUsers: number
  total: number
}

export interface AdminAnalyticsData {
  activeLearners: MonthMetricPoint[]
  freeVsEnrolled: FreeVsEnrolledPoint[]
  courseCompletion: MonthMetricPoint[]
  resourceDownloads: MonthMetricPoint[]
  couponEngagement: MonthMetricPoint[]
  vslVideoViews: MonthMetricPoint[]
  welcomeVideoViews: MonthMetricPoint[]
  totalActiveLearners: number
  totalFreeUsers: number
  totalEnrolledStudents: number
  totalCompletions: number
  totalResourceDownloads: number
  totalCouponUsages: number
  totalVslViews: number
  totalWelcomeVideoViews: number
}

/**
 * Fetch month-wise live metrics for the 5 analytics charts.
 */
export async function getAdminAnalytics(): Promise<ApiResponse<AdminAnalyticsData>> {
  return apiClient<AdminAnalyticsData>("/api/v1/admin/analytics", {
    method: "GET",
    requiresAuth: true,
  })
}


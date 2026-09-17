/**
 * Notification service connected to Spring Boot backend.
 */

import type { ApiResponse, AppNotification, CreateNotificationDto, UpdateNotificationDto } from "@/lib/types"
import { apiClient } from "./client"

/**
 * Fetch active notifications for student dashboard (public/student).
 */
export async function getActiveNotifications(): Promise<ApiResponse<AppNotification[]>> {
  const res = await apiClient<AppNotification[]>("/api/v1/notifications", {
    method: "GET",
  })
  if (!res.success || !res.data) {
    return { data: [], success: false, error: res.error }
  }
  return res
}

/**
 * Fetch all notifications for admin dashboard (includes active & inactive).
 */
export async function getAdminNotifications(): Promise<ApiResponse<AppNotification[]>> {
  const res = await apiClient<AppNotification[]>("/api/v1/admin/notifications", {
    method: "GET",
    requiresAuth: true,
  })
  if (!res.success || !res.data) {
    return { data: [], success: false, error: res.error }
  }
  return res
}

/**
 * Create a new notification (Admin).
 */
export async function createAdminNotification(data: CreateNotificationDto): Promise<ApiResponse<AppNotification>> {
  return apiClient<AppNotification>("/api/v1/admin/notifications", {
    method: "POST",
    body: JSON.stringify(data),
    requiresAuth: true,
  })
}

/**
 * Update an existing notification (Admin).
 */
export async function updateAdminNotification(
  id: string | number,
  data: UpdateNotificationDto
): Promise<ApiResponse<AppNotification>> {
  return apiClient<AppNotification>(`/api/v1/admin/notifications/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
    requiresAuth: true,
  })
}

/**
 * Toggle active state of a notification (Admin).
 */
export async function toggleAdminNotification(id: string | number): Promise<ApiResponse<AppNotification>> {
  return apiClient<AppNotification>(`/api/v1/admin/notifications/${id}/toggle`, {
    method: "PATCH",
    requiresAuth: true,
  })
}

/**
 * Delete a notification (Admin).
 */
export async function deleteAdminNotification(id: string | number): Promise<ApiResponse<void>> {
  return apiClient<void>(`/api/v1/admin/notifications/${id}`, {
    method: "DELETE",
    requiresAuth: true,
  })
}

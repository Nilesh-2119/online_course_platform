/**
 * Free resources service connected to Spring Boot Backend.
 */

import type { ApiResponse, FreeResource, ResourceType } from "@/lib/types"
import { apiClient, getApiBaseUrl } from "./client"

export interface AdminResourceItem {
  id: string
  title: string
  description: string
  resourceType: string
  resourceUrl: string
  status: string
  fileName?: string
  fileSize?: number
  downloadCount: number
  createdAt: string
}

function resolveResourceUrl(url?: string): string {
  if (!url) return ""
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url
  }
  const base = getApiBaseUrl()
  const cleanUrl = url.startsWith("/") ? url : `/${url}`
  return `${base}${cleanUrl}`
}

function mapToResourceType(typeStr?: string, fileName?: string): ResourceType {
  const t = (typeStr || "").toLowerCase()
  if (t === "pdf") return "pdf"
  if (t === "excel" || t === "spreadsheet") return "spreadsheet"
  if (t === "document" || t === "doc") return "doc"
  if (t === "template" || t === "archive") return "archive"

  // Check filename extension as fallback
  if (fileName) {
    const fn = fileName.toLowerCase()
    if (fn.endsWith(".pdf")) return "pdf"
    if (fn.endsWith(".xlsx") || fn.endsWith(".xls") || fn.endsWith(".csv")) return "spreadsheet"
    if (fn.endsWith(".doc") || fn.endsWith(".docx")) return "doc"
    if (fn.endsWith(".zip") || fn.endsWith(".rar")) return "archive"
  }

  return "other"
}

/**
 * Fetch all published free resources from GET /api/v1/resources
 */
export async function getResources(): Promise<ApiResponse<FreeResource[]>> {
  const res = await apiClient<any[]>("/api/v1/resources", { method: "GET" })
  if (!res.success || !res.data) {
    return { data: [], success: false, error: res.error }
  }

  const resources: FreeResource[] = res.data.map((r) => ({
    id: String(r.id),
    title: r.title,
    description: r.description || "",
    type: mapToResourceType(r.resourceType, r.fileName),
    fileName: r.fileName || `${r.title.toLowerCase().replace(/\s+/g, "_")}`,
    fileSize: typeof r.fileSize === "number" ? r.fileSize : undefined,
    status: (r.status?.toLowerCase() || "published") as any,
    downloadCount: r.downloadCount || 0,
    createdAt: r.createdAt || new Date().toISOString(),
    downloadUrl: resolveResourceUrl(r.resourceUrl),
  }))

  return { data: resources, success: true }
}

/**
 * Fetch all resources (alias for getResources)
 */
export async function getAllResources(): Promise<ApiResponse<FreeResource[]>> {
  return getResources()
}

/**
 * Request download URL for a resource via GET /api/v1/resources/{id}/download
 */
export async function getResourceDownloadUrl(resourceId: string): Promise<ApiResponse<string>> {
  const res = await apiClient<any>(`/api/v1/resources/${resourceId}/download`, {
    method: "GET",
  })

  if (!res.success || !res.data) {
    return { data: "", success: false, error: res.error }
  }

  const rawUrl = res.data.downloadUrl || res.data.url
  return { data: resolveResourceUrl(rawUrl), success: true }
}

// ---------------------------------------------------------------------------
// ADMIN OPERATIONS
// ---------------------------------------------------------------------------

/**
 * Fetch all resources for admin (includes draft, published, archived)
 */
export async function getAdminResources(): Promise<ApiResponse<AdminResourceItem[]>> {
  const res = await apiClient<any[]>("/api/v1/admin/resources", {
    method: "GET",
    requiresAuth: true,
  })

  if (!res.success || !res.data) {
    return { data: [], success: false, error: res.error }
  }

  const items: AdminResourceItem[] = res.data.map((r) => ({
    id: String(r.id),
    title: r.title,
    description: r.description || "",
    resourceType: r.resourceType || "OTHER",
    resourceUrl: resolveResourceUrl(r.resourceUrl),
    status: r.status || "PUBLISHED",
    fileName: r.fileName,
    fileSize: r.fileSize,
    downloadCount: r.downloadCount || 0,
    createdAt: r.createdAt || new Date().toISOString(),
  }))

  return { data: items, success: true }
}

/**
 * Admin creates a new resource with a file upload or external URL
 */
export async function createAdminResource(formData: FormData): Promise<ApiResponse<AdminResourceItem>> {
  return apiClient<AdminResourceItem>("/api/v1/admin/resources", {
    method: "POST",
    body: formData,
    requiresAuth: true,
  })
}

/**
 * Update resource status (PUBLISHED, DRAFT, ARCHIVED)
 */
export async function updateAdminResourceStatus(resourceId: string, status: string): Promise<ApiResponse<AdminResourceItem>> {
  return apiClient<AdminResourceItem>(`/api/v1/admin/resources/${resourceId}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status }),
    requiresAuth: true,
  })
}

/**
 * Delete a resource
 */
export async function deleteAdminResource(resourceId: string): Promise<ApiResponse<{ message: string }>> {
  return apiClient<{ message: string }>(`/api/v1/admin/resources/${resourceId}`, {
    method: "DELETE",
    requiresAuth: true,
  })
}

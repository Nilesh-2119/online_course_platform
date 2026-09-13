/**
 * HTTP Client for connecting Next.js frontend to Spring Boot backend.
 */

export function getApiBaseUrl(): string {
  let raw = process.env.NEXT_PUBLIC_API_BASE_URL

  // If in browser and not running on localhost, ensure we NEVER attempt to hit localhost
  if (typeof window !== "undefined") {
    const hostname = window.location.hostname
    const isLocal = hostname === "localhost" || hostname === "127.0.0.1"
    if (!isLocal && (!raw || raw.includes("localhost") || raw.includes("127.0.0.1"))) {
      raw = "https://lavish-education-production.up.railway.app"
    }
  }

  let url = (raw || "https://lavish-education-production.up.railway.app").trim()
  if (!url.startsWith("http://") && !url.startsWith("https://")) {
    url = `https://${url}`
  }
  return url.replace(/\/+$/, "")
}

export const BASE_URL = getApiBaseUrl()
const TOKEN_KEY = "adfix_access_token"
const REFRESH_TOKEN_KEY = "adfix_refresh_token"

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem(TOKEN_KEY)
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setTokens(accessToken: string, refreshToken?: string) {
  if (typeof window === "undefined") return
  localStorage.setItem(TOKEN_KEY, accessToken)
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }
}

export function clearTokens() {
  if (typeof window === "undefined") return
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem("adfix_course_purchased")
}

interface RequestOptions extends RequestInit {
  requiresAuth?: boolean
}

export async function apiClient<T>(endpoint: string, options: RequestOptions = {}): Promise<{ data: T; success: boolean; error?: string }> {
  const { requiresAuth = false, headers = {}, ...rest } = options

  const reqHeaders: Record<string, string> = {
    ...(headers as Record<string, string>),
  }

  if (!(rest.body instanceof FormData)) {
    if (!reqHeaders["Content-Type"]) {
      reqHeaders["Content-Type"] = "application/json"
    }
  }

  const token = getAccessToken()
  if (requiresAuth && token) {
    reqHeaders["Authorization"] = `Bearer ${token}`
  }

  const base = getApiBaseUrl()
  const url = endpoint.startsWith("http") ? endpoint : `${base}${endpoint}`

  try {
    const res = await fetch(url, {
      ...rest,
      headers: reqHeaders,
    })

    // Handle token refresh on 401
    if (res.status === 401 && requiresAuth) {
      const refreshToken = getRefreshToken()
      if (refreshToken) {
        try {
          const refreshRes = await fetch(`${BASE_URL}/api/v1/auth/refresh`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ refreshToken }),
          })

          if (refreshRes.ok) {
            const refreshData = await refreshRes.json()
            if (refreshData.data?.accessToken) {
              setTokens(refreshData.data.accessToken, refreshData.data.refreshToken)
              reqHeaders["Authorization"] = `Bearer ${refreshData.data.accessToken}`
              const retryRes = await fetch(url, { ...rest, headers: reqHeaders })
              const retryJson = await retryRes.json()
              return { data: retryJson.data, success: retryRes.ok, error: retryJson.message }
            }
          }
        } catch {
          clearTokens()
        }
      }
    }

    const contentType = res.headers.get("content-type")
    let json: any = null
    if (contentType && contentType.includes("application/json")) {
      json = await res.json()
    }

    if (!res.ok) {
      const errorMessage = json?.message || json?.error || `Request failed with status ${res.status}`
      return {
        data: json?.data ?? null,
        success: false,
        error: errorMessage,
      }
    }

    return {
      data: (json?.data !== undefined ? json.data : json) as T,
      success: true,
    }
  } catch (err: any) {
    return {
      data: null as any,
      success: false,
      error: err.message || "Network connection error. Ensure backend is running.",
    }
  }
}

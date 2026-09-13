"use client"

/**
 * Authentication context connected to Spring Boot Backend JWT Session.
 */

import { createContext, useContext, useEffect, useState, useCallback, type ReactNode } from "react"
import type { UserProfile, UserRole } from "@/lib/types"
import { getCurrentUser, logout as apiLogout } from "@/lib/api/auth-service"
import { setTokens, clearTokens, getAccessToken } from "@/lib/api/client"

interface AuthState {
  user: UserProfile | null
  isAuthenticated: boolean
  isLoading: boolean
  role: UserRole | null
}

interface AuthContextValue extends AuthState {
  login: (email: string, name: string, purchased?: boolean) => void
  logout: () => void
  updateProfile: (patch: Partial<UserProfile>) => void
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const refreshUser = useCallback(async () => {
    // Check if OAuth callback query params exist in URL
    if (typeof window !== "undefined") {
      localStorage.removeItem("adfix_course_purchased")
      const urlParams = new URLSearchParams(window.location.search)
      const accessToken = urlParams.get("accessToken")
      const refreshToken = urlParams.get("refreshToken")
      if (accessToken) {
        setTokens(accessToken, refreshToken || undefined)
        const userEmail = urlParams.get("userEmail")
        const userName = urlParams.get("userName")
        const userRole = urlParams.get("userRole")
        const coursePurchased = urlParams.get("coursePurchased") === "true"
        if (userEmail) {
          setUser({
            id: "oauth",
            email: decodeURIComponent(userEmail),
            name: userName ? decodeURIComponent(userName) : "Student",
            phone: "",
            type: coursePurchased ? "paid" : "free",
            coursePurchased: coursePurchased,
            role: userRole === "ADMIN" ? "admin" : "user",
            status: "active",
            joinedAt: new Date().toISOString(),
            onboardingComplete: true,
          })
        }
        window.history.replaceState({}, document.title, window.location.pathname)
      }
    }

    const token = getAccessToken()
    if (!token) {
      setUser(null)
      setIsLoading(false)
      return
    }

    try {
      const res = await getCurrentUser()
      if (res.success && res.data) {
        setUser(res.data)
      } else {
        clearTokens()
        setUser(null)
      }
    } catch {
      setUser(null)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    refreshUser()
  }, [refreshUser])

  const login = useCallback(async (email: string, name: string, purchased = false) => {
    await refreshUser()
  }, [refreshUser])

  const logout = useCallback(() => {
    apiLogout()
    clearTokens()
    if (typeof window !== "undefined") {
      localStorage.removeItem("adfix_course_purchased")
    }
    setUser(null)
  }, [])

  const updateProfile = useCallback((patch: Partial<UserProfile>) => {
    setUser((prev) => {
      if (!prev) return prev
      return { ...prev, ...patch }
    })
  }, [])

  const value: AuthContextValue = {
    user,
    isAuthenticated: Boolean(user),
    isLoading,
    role: user?.role || null,
    login,
    logout,
    updateProfile,
    refreshUser,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}

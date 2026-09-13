/** User domain types. Values are raw/numeric; format at the UI layer. */

export type UserRole = "user" | "admin"
export type UserType = "free" | "paid"
export type AccountStatus = "active" | "suspended" | "deleted"

export interface User {
  id: string
  name: string
  email: string
  phone?: string
  type: UserType
  role: UserRole
  status: AccountStatus
  coursePurchased?: boolean
  joinedAt: string // ISO 8601
  lastLoginAt?: string // ISO 8601
}

export interface UserProfile extends User {
  onboardingComplete: boolean
  onboardingRole?: string
  onboardingGoals?: string[]
  onboardingExperience?: string
}

export interface AdminUser {
  id: string
  name: string
  email: string
  role: "admin" | "superadmin"
  twoFactorEnabled: boolean
}

export interface AdminSession {
  id: string
  device: string
  lastActiveAt: string // ISO 8601
  isCurrent: boolean
}

export interface AuditLogEntry {
  id: string
  action: string
  actor: string
  createdAt: string // ISO 8601
}

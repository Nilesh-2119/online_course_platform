/**
 * Mock seed data for frontend prototype.
 *
 * This file is the ONLY place hardcoded demo data should live.
 * All service functions read from here. When a real backend is connected,
 * the service layer replaces these imports — this file can be deleted.
 */

import type {
  User,
  UserProfile,
  AdminUser,
  AdminSession,
  AuditLogEntry,
  Course,
  CourseLesson,
  CourseAccess,
  CourseProgress,
  FreeResource,
  Order,
  Coupon,
} from "@/lib/types"

// ---------------------------------------------------------------------------
// Users
// ---------------------------------------------------------------------------

export const mockUsers: User[] = [
  { id: "u1", name: "Rahul Sharma", email: "rahul@example.com", phone: "+91 98765 43210", type: "paid", role: "user", status: "active", joinedAt: "2026-08-25T04:30:00Z", lastLoginAt: "2026-08-26T05:12:00Z" },
  { id: "u2", name: "Priya Mehta", email: "priya@example.com", type: "paid", role: "user", status: "active", joinedAt: "2026-08-24T06:00:00Z", lastLoginAt: "2026-08-26T03:00:00Z" },
  { id: "u3", name: "Aarav Mehta", email: "aarav@example.com", type: "free", role: "user", status: "active", joinedAt: "2026-08-24T08:00:00Z", lastLoginAt: "2026-08-25T10:00:00Z" },
  { id: "u4", name: "Kavya Iyer", email: "kavya@example.com", type: "free", role: "user", status: "active", joinedAt: "2026-08-22T09:00:00Z", lastLoginAt: "2026-08-24T14:00:00Z" },
  { id: "u5", name: "Arjun Rao", email: "arjun@example.com", type: "paid", role: "user", status: "suspended", joinedAt: "2026-08-20T07:00:00Z", lastLoginAt: "2026-08-25T16:00:00Z" },
]

export const mockUserProfile: UserProfile = {
  id: "u1",
  name: "Rahul Sharma",
  email: "rahul@example.com",
  phone: "+91 98765 43210",
  type: "paid",
  role: "user",
  status: "active",
  joinedAt: "2026-08-25T04:30:00Z",
  lastLoginAt: "2026-08-26T05:12:00Z",
  onboardingComplete: true,
  onboardingRole: "Creator",
  onboardingGoals: ["Hooks", "Scripts"],
  onboardingExperience: "Intermediate",
}

// ---------------------------------------------------------------------------
// Admin
// ---------------------------------------------------------------------------

export const mockAdminUser: AdminUser = {
  id: "admin-1",
  name: "AdFix Admin",
  email: "adfixstudio25@gmail.com",
  role: "superadmin",
  twoFactorEnabled: true,
}

export const mockAdminSessions: AdminSession[] = [
  { id: "s1", device: "Chrome · Windows", lastActiveAt: "2026-08-26T04:30:00Z", isCurrent: true },
  { id: "s2", device: "Chrome · Mac", lastActiveAt: "2026-08-26T02:30:00Z", isCurrent: false },
  { id: "s3", device: "Mobile", lastActiveAt: "2026-08-25T10:00:00Z", isCurrent: false },
]

export const mockAuditLog: AuditLogEntry[] = [
  { id: "a1", action: "Admin created coupon ADFIX20", actor: "adfixstudio25@gmail.com", createdAt: "2026-08-25T04:10:00Z" },
  { id: "a2", action: "Admin granted course access to Rahul Sharma", actor: "adfixstudio25@gmail.com", createdAt: "2026-08-25T04:42:00Z" },
  { id: "a3", action: "Admin published Hook Writing Checklist", actor: "adfixstudio25@gmail.com", createdAt: "2026-08-24T10:48:00Z" },
  { id: "a4", action: "Admin edited course information", actor: "adfixstudio25@gmail.com", createdAt: "2026-08-23T06:00:00Z" },
]

// ---------------------------------------------------------------------------
// Courses & Lessons
// ---------------------------------------------------------------------------

export const mockCourses: Course[] = [
  {
    id: "adfix-course",
    title: "AdFix High-Performing Ads Course",
    description: "A practical recorded course for creators and brands building high-performing ads. Video lessons are hosted and managed through an external video platform.",
    price: 4000,
    currency: "INR",
    status: "published",
    lessonCount: 12,
    studentCount: 187,
    completionRate: 64,
    accessType: "Lifetime Access",
  },
]

export const mockLessons: CourseLesson[] = [
  { id: "lesson-1", courseId: "adfix-course", title: "Hook Engineering", description: "Build openings that earn attention.", order: 1, status: "published", hasPreview: true },
  { id: "lesson-2", courseId: "adfix-course", title: "Script Flow Rewrite", description: "Turn rough scripts into clear stories.", order: 2, status: "published", hasPreview: true },
  { id: "lesson-3", courseId: "adfix-course", title: "High-Conversion CTAs", description: "Give people a reason to act.", order: 3, status: "published", hasPreview: true },
  { id: "lesson-4", courseId: "adfix-course", title: "Ad Strategy", description: "Connect creative choices to performance.", order: 4, status: "published", hasPreview: true },
]

export const mockCourseProgress: CourseProgress = {
  courseId: "adfix-course",
  userId: "u1",
  status: "in_progress",
  completedLessons: 8,
  totalLessons: 12,
  percentage: 72,
  currentLessonId: "lesson-1",
  lastAccessedAt: "2026-08-26T05:12:00Z",
}

export const mockCourseAccess: CourseAccess = {
  courseId: "adfix-course",
  userId: "u1",
  status: "unlocked",
  grantedAt: "2026-08-25T04:30:00Z",
}

// ---------------------------------------------------------------------------
// Free Resources
// ---------------------------------------------------------------------------

export const mockResources: FreeResource[] = [
  { id: "r1", title: "Hook Writing Checklist", description: "A practical checklist for sharper opening lines.", type: "pdf", fileName: "hook-checklist.pdf", fileSize: 245760, status: "published", downloadCount: 324, createdAt: "2026-08-21T05:00:00Z" },
  { id: "r2", title: "Ad Script Framework", description: "A simple structure for writing clearer ad scripts.", type: "pdf", fileName: "script-framework.pdf", fileSize: 184320, status: "published", downloadCount: 218, createdAt: "2026-08-18T05:00:00Z" },
  { id: "r3", title: "CTA Swipe File", description: "A collection of prompts for stronger calls to action.", type: "pdf", fileName: "cta-swipe.pdf", fileSize: 122880, status: "draft", downloadCount: 0, createdAt: "2026-08-16T05:00:00Z" },
]

// ---------------------------------------------------------------------------
// Orders
// ---------------------------------------------------------------------------

export const mockOrders: Order[] = [
  { id: "AFX-0187", userId: "u1", customerName: "Rahul Sharma", customerEmail: "rahul@example.com", productId: "adfix-course", productTitle: "AdFix High-Performing Ads Course", amount: 4000, currency: "INR", discountAmount: 800, finalAmount: 3200, couponCode: "ADFIX20", status: "paid", createdAt: "2026-08-25T04:30:00Z" },
  { id: "AFX-0186", userId: "u2", customerName: "Priya Mehta", customerEmail: "priya@example.com", productId: "adfix-course", productTitle: "AdFix High-Performing Ads Course", amount: 4000, currency: "INR", discountAmount: 0, finalAmount: 4000, status: "paid", createdAt: "2026-08-24T06:00:00Z" },
  { id: "AFX-0185", userId: "u3", customerName: "Aarav Mehta", customerEmail: "aarav@example.com", productId: "adfix-course", productTitle: "AdFix High-Performing Ads Course", amount: 4000, currency: "INR", discountAmount: 500, finalAmount: 3500, couponCode: "WELCOME500", status: "pending", createdAt: "2026-08-23T08:00:00Z" },
]

// ---------------------------------------------------------------------------
// Coupons
// ---------------------------------------------------------------------------

export const mockCoupons: Coupon[] = [
  { id: "c1", code: "ADFIX20", discountType: "percentage", discountValue: 20, usageCount: 34, usageLimit: 100, expiryDate: "2026-09-30T18:30:00Z", status: "active" },
  { id: "c2", code: "WELCOME500", discountType: "fixed", discountValue: 500, currency: "INR", usageCount: 12, usageLimit: 50, expiryDate: "2026-10-15T18:30:00Z", status: "active" },
]

// ---------------------------------------------------------------------------
// Admin navigation (static, not backend-dependent)
// ---------------------------------------------------------------------------

export const adminNavItems = [
  { label: "OVERVIEW", href: "/admin", icon: "grid" as const },
  { label: "USERS", href: "/admin/users", icon: "users" as const },
  { label: "FREE RESOURCES", href: "/admin/resources", icon: "file" as const },
  { label: "COUPONS", href: "/admin/coupons", icon: "tag" as const },
  { label: "ANALYTICS", href: "/admin/analytics", icon: "chart" as const },
  { label: "NOTIFICATIONS", href: "/admin/notifications", icon: "bell" as const },
]

export type AdminNavIcon = typeof adminNavItems[number]["icon"]

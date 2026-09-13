/** Course domain types. */

export type CourseStatus = "draft" | "published" | "archived"
export type LessonStatus = "draft" | "published"
export type AccessStatus = "locked" | "unlocked" | "revoked"
export type ProgressStatus = "not_started" | "in_progress" | "completed"

export interface Course {
  id: string
  title: string
  description: string
  price: number
  currency: string
  status: CourseStatus
  lessonCount: number
  studentCount: number
  completionRate: number // 0-100
  accessType: string // e.g. "lifetime"
}

export interface CourseLesson {
  id: string
  courseId: string
  title: string
  description: string
  order: number
  status: LessonStatus
  hasPreview: boolean
  videoProvider?: string // e.g. "vdocipher", "mux"
  videoId?: string
  duration?: number // seconds
}

export interface CourseAccess {
  courseId: string
  userId: string
  status: AccessStatus
  grantedAt?: string // ISO 8601
  revokedAt?: string // ISO 8601
}

export interface CourseProgress {
  courseId: string
  userId: string
  status: ProgressStatus
  completedLessons: number
  totalLessons: number
  percentage: number // 0-100
  currentLessonId?: string
  lastAccessedAt?: string // ISO 8601
}

export interface VideoPlayerConfig {
  videoId: string
  playbackUrl?: string
  signedToken?: string
  provider: string
  duration?: number
}

export type VideoState = "loading" | "ready" | "playing" | "paused" | "completed" | "error" | "locked"

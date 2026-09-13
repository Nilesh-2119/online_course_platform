/**
 * Course service connected to Spring Boot Backend.
 */

import type { ApiResponse, Course, CourseLesson, CourseAccess, CourseProgress } from "@/lib/types"
import { apiClient } from "./client"

/**
 * Fetch all published courses from GET /api/v1/courses
 */
export async function getCourses(): Promise<ApiResponse<Course[]>> {
  const res = await apiClient<any[]>("/api/v1/courses", { method: "GET" })
  if (!res.success || !res.data) {
    return { data: [], success: false, error: res.error }
  }

  const courses: Course[] = res.data.map((c) => ({
    id: String(c.id),
    title: c.title,
    description: c.description,
    price: c.price,
    currency: c.currency,
    status: c.status === "PUBLISHED" ? "published" : "draft",
    lessonCount: c.videoCount || 6,
    studentCount: 124,
    completionRate: 85,
    accessType: "lifetime",
  }))

  return { data: courses, success: true }
}

/**
 * Fetch a single course by ID from GET /api/v1/courses/{id}
 */
export async function getCourse(id: string): Promise<ApiResponse<Course | null>> {
  const res = await apiClient<any>(`/api/v1/courses/${id}`, { method: "GET" })
  if (!res.success || !res.data) {
    return { data: null, success: false, error: res.error }
  }

  const c = res.data
  const course: Course = {
    id: String(c.id),
    title: c.title,
    description: c.description,
    price: c.price,
    currency: c.currency,
    status: c.status === "PUBLISHED" ? "published" : "draft",
    lessonCount: c.videoCount || (c.sections ? c.sections.reduce((acc: number, s: any) => acc + (s.videos?.length || 0), 0) : 6),
    studentCount: 124,
    completionRate: 85,
    accessType: "lifetime",
  }

  return { data: course, success: true }
}

/**
 * Fetch lessons for a course from GET /api/v1/courses/{id}
 */
export async function getCourseLessons(courseId: string): Promise<ApiResponse<CourseLesson[]>> {
  const res = await apiClient<any>(`/api/v1/courses/${courseId}`, {
    method: "GET",
    requiresAuth: true,
  })

  if (!res.success || !res.data) {
    return { data: [], success: false, error: res.error }
  }

  const lessons: CourseLesson[] = []
  if (res.data.sections) {
    for (const sec of res.data.sections) {
      if (sec.videos) {
        for (const v of sec.videos) {
          lessons.push({
            id: String(v.id),
            courseId,
            title: v.title,
            description: v.description || "",
            order: v.displayOrder || 1,
            status: "published",
            hasPreview: Boolean(v.isFree),
            videoProvider: "vdocipher",
            videoId: v.vdocipherVideoId,
            duration: v.durationSeconds,
          })
        }
      }
    }
  }

  return { data: lessons, success: true }
}

/**
 * Check if the current user has access to a course via GET /api/v1/courses/{courseId}/progress
 */
export async function getCourseAccess(courseId: string): Promise<ApiResponse<CourseAccess | null>> {
  const res = await apiClient<any>(`/api/v1/courses/${courseId}/progress`, {
    method: "GET",
    requiresAuth: true,
  })

  if (res.success && res.data) {
    return {
      data: {
        courseId,
        userId: "current",
        status: "unlocked",
      },
      success: true,
    }
  }

  return { data: null, success: true }
}

/**
 * Fetch course progress for the current user from GET /api/v1/courses/{courseId}/progress
 */
export async function getCourseProgress(courseId: string): Promise<ApiResponse<CourseProgress | null>> {
  const res = await apiClient<any>(`/api/v1/courses/${courseId}/progress`, {
    method: "GET",
    requiresAuth: true,
  })

  if (!res.success || !res.data) {
    return { data: null, success: false, error: res.error }
  }

  const p = res.data
  return {
    data: {
      courseId,
      userId: "current",
      status: p.completionPercentage >= 100 ? "completed" : p.completedVideoCount > 0 ? "in_progress" : "not_started",
      completedLessons: p.completedVideoCount || 0,
      totalLessons: p.totalVideoCount || 6,
      percentage: p.completionPercentage || 0,
      currentLessonId: p.lastWatchedVideoId ? String(p.lastWatchedVideoId) : undefined,
    },
    success: true,
  }
}

/**
 * Fetch secure VdoCipher playback credentials from GET /api/v1/videos/{id}/playback
 */
export async function getVideoPlayback(videoId: string | number): Promise<ApiResponse<{ videoId: number; otp: string; playbackInfo: string }>> {
  return apiClient<any>(`/api/v1/videos/${videoId}/playback`, {
    method: "GET",
    requiresAuth: true,
  })
}

/**
 * Update video watch progress via PUT /api/v1/videos/{id}/progress
 */
export async function updateVideoProgress(videoId: string | number, currentPositionSeconds: number, durationSeconds?: number, completed?: boolean): Promise<ApiResponse<any>> {
  return apiClient<any>(`/api/v1/videos/${videoId}/progress`, {
    method: "PUT",
    requiresAuth: true,
    body: JSON.stringify({
      currentPositionSeconds,
      durationSeconds: durationSeconds || 0,
      completed: Boolean(completed),
    }),
  })
}

/**
 * Trigger on-demand instant sync with VdoCipher
 */
export async function syncVdoCipherVideos(): Promise<ApiResponse<any>> {
  return apiClient<any>("/api/v1/videos/sync", {
    method: "POST",
  })
}

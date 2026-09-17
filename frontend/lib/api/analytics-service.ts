/**
 * Analytics service connected directly to live backend API.
 */

import { apiClient } from "./client"
import { getAdminAnalytics, type AdminAnalyticsData, type MonthMetricPoint, type FreeVsEnrolledPoint } from "./admin-service"

export { getAdminAnalytics }
export type { AdminAnalyticsData, MonthMetricPoint, FreeVsEnrolledPoint }

/**
 * Record a video watch/view event in the database (VSL on landing page or Welcome on dashboard)
 */
export async function recordVideoView(
  videoType: "VSL" | "WELCOME",
  videoId?: string
): Promise<{ success: boolean; data?: any; error?: string }> {
  return apiClient<any>("/api/v1/analytics/video-view", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ videoType, videoId }),
  })
}

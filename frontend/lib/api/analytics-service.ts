/**
 * Analytics service connected directly to live backend API.
 */

import { getAdminAnalytics, type AdminAnalyticsData, type MonthMetricPoint, type FreeVsEnrolledPoint } from "./admin-service"

export { getAdminAnalytics }
export type { AdminAnalyticsData, MonthMetricPoint, FreeVsEnrolledPoint }

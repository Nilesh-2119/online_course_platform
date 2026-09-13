/** Analytics domain types. */

export interface KPI {
  label: string
  value: number
  formattedValue: string
  changePercent?: number
}

export interface ChartDataPoint {
  label: string
  value: number
}

export interface AnalyticsSummary {
  totalUsers: number
  freeUsers: number
  paidUsers: number
  totalSales: number
  totalRevenue: number
  currency: string
  conversionRate: number // 0-100
  completionRate: number // 0-100
  resourceDownloads: number
  couponUsage: number
}

export interface AnalyticsChartData {
  userGrowth: ChartDataPoint[]
  revenueOverTime?: ChartDataPoint[]
  freeVsPaid: ChartDataPoint[]
  courseCompletion: ChartDataPoint[]
  resourceDownloads: ChartDataPoint[]
  couponUsage: ChartDataPoint[]
}

export interface ActivityItem {
  id: string
  message: string
  createdAt: string // ISO 8601
}

"use client"

import { useState, useEffect } from "react"
import {
  Users,
  UserCheck,
  UserX,
  GraduationCap,
  Download,
  Tag,
  RotateCw,
  AlertCircle,
  Video,
  Play,
} from "lucide-react"
import {
  getAdminAnalytics,
  type AdminAnalyticsData,
  type MonthMetricPoint,
  type FreeVsEnrolledPoint,
} from "@/lib/api/admin-service"

// ---------------------------------------------------------------------------
// Single Metric Monthly Bar Chart Component (100% Real DB Data)
// ---------------------------------------------------------------------------
interface MetricBarChartProps {
  title: string
  subtitle: string
  data: MonthMetricPoint[]
  badgeText: string
  accentColor?: "accent" | "emerald" | "sky" | "amber" | "violet"
  valueUnit?: string
}

function MetricBarChart({
  title,
  subtitle,
  data,
  badgeText,
  accentColor = "accent",
  valueUnit = "",
}: MetricBarChartProps) {
  const [hoveredIdx, setHoveredIdx] = useState<number | null>(null)
  const maxValue = Math.max(...data.map((d) => d.value), 1)

  const colorStyles = {
    accent: {
      bar: "bg-accent/80 hover:bg-accent",
      barActive: "bg-accent shadow-[0_0_12px_rgba(245,158,11,0.5)]",
      text: "text-accent",
      border: "border-accent/30",
    },
    emerald: {
      bar: "bg-emerald-500/80 hover:bg-emerald-400",
      barActive: "bg-emerald-400 shadow-[0_0_12px_rgba(52,211,153,0.5)]",
      text: "text-emerald-400",
      border: "border-emerald-500/30",
    },
    sky: {
      bar: "bg-sky-500/80 hover:bg-sky-400",
      barActive: "bg-sky-400 shadow-[0_0_12px_rgba(56,189,248,0.5)]",
      text: "text-sky-400",
      border: "border-sky-500/30",
    },
    amber: {
      bar: "bg-amber-500/80 hover:bg-amber-400",
      barActive: "bg-amber-400 shadow-[0_0_12px_rgba(251,191,36,0.5)]",
      text: "text-amber-400",
      border: "border-amber-500/30",
    },
    violet: {
      bar: "bg-violet-500/80 hover:bg-violet-400",
      barActive: "bg-violet-400 shadow-[0_0_12px_rgba(167,139,250,0.5)]",
      text: "text-violet-400",
      border: "border-violet-500/30",
    },
  }[accentColor]

  const latestValue = data.length > 0 ? data[data.length - 1].value : 0

  return (
    <section className="flex flex-col rounded-2xl border border-border bg-card p-6 shadow-sm transition hover:border-border/80">
      {/* Header */}
      <div className="flex flex-wrap items-start justify-between gap-2 border-b border-border/60 pb-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="font-mono text-sm font-bold tracking-wider text-foreground">{title}</h2>
            <span
              className={`rounded-full border px-2 py-0.5 font-mono text-[9px] font-semibold uppercase tracking-widest ${colorStyles.border} ${colorStyles.text} bg-background/50`}
            >
              {badgeText}
            </span>
          </div>
          <p className="mt-1 text-xs text-muted-foreground">{subtitle}</p>
        </div>
        <div className="text-right">
          <div className="font-mono text-lg font-bold tracking-tight text-foreground">
            {latestValue.toLocaleString()} {valueUnit}
          </div>
          <div className="font-mono text-[10px] text-muted-foreground">CURRENT MONTH (LIVE)</div>
        </div>
      </div>

      {/* Interactive Tooltip Area */}
      <div className="flex h-7 items-center justify-between px-2 pt-3 font-mono text-xs">
        {hoveredIdx !== null ? (
          <>
            <span className="text-muted-foreground">
              Month: <strong className="text-foreground">{data[hoveredIdx].label}</strong>
            </span>
            <span className={`font-bold ${colorStyles.text}`}>
              {data[hoveredIdx].value.toLocaleString()} {valueUnit}
            </span>
          </>
        ) : (
          <span className="text-[11px] text-muted-foreground/60 italic">
            Hover over any monthly bar for database figures
          </span>
        )}
      </div>

      {/* Chart Canvas */}
      <div className="relative mt-2 flex h-52 items-end gap-3 border-b border-l border-border/80 px-3 pb-2 pt-8">
        {/* Horizontal grid guide lines */}
        <div className="pointer-events-none absolute inset-x-0 top-0 border-t border-dashed border-border/40" />
        <div className="pointer-events-none absolute inset-x-0 top-1/2 border-t border-dashed border-border/25" />

        {data.map((item, idx) => {
          const heightPercent = item.value === 0 ? 3 : Math.max((item.value / maxValue) * 100, 6)
          const isHovered = hoveredIdx === idx

          return (
            <div
              key={item.month}
              className="group relative flex flex-1 cursor-pointer flex-col items-center justify-end h-full"
              onMouseEnter={() => setHoveredIdx(idx)}
              onMouseLeave={() => setHoveredIdx(null)}
            >
              {/* Value pill over bar */}
              <div
                className={`absolute -top-7 font-mono text-[10px] font-bold transition-all duration-200 ${
                  isHovered ? "opacity-100 scale-110 text-foreground" : "opacity-0 text-muted-foreground"
                }`}
              >
                {item.value}
              </div>

              {/* Bar */}
              <div
                className={`w-full rounded-t-md transition-all duration-300 ${
                  item.value === 0
                    ? "bg-muted-foreground/20 hover:bg-muted-foreground/30"
                    : isHovered
                    ? colorStyles.barActive
                    : colorStyles.bar
                }`}
                style={{ height: `${heightPercent}%` }}
              />
            </div>
          )
        })}
      </div>

      {/* Month Labels on X-axis */}
      <div className="mt-3 flex justify-between px-2 font-mono text-[10px] text-muted-foreground">
        {data.map((item, idx) => (
          <span
            key={item.month}
            className={`transition-colors ${hoveredIdx === idx ? "font-bold text-foreground" : ""}`}
          >
            {item.label.split(" ")[0].toUpperCase()}
          </span>
        ))}
      </div>
    </section>
  )
}

// ---------------------------------------------------------------------------
// Dual Bar Chart: Free vs Enrolled Students (100% Real DB Data)
// ---------------------------------------------------------------------------
interface FreeVsEnrolledChartProps {
  data: FreeVsEnrolledPoint[]
}

function FreeVsEnrolledChart({ data }: FreeVsEnrolledChartProps) {
  const [hoveredIdx, setHoveredIdx] = useState<number | null>(null)
  const maxVal = Math.max(
    ...data.map((d) => Math.max(d.freeUsers, d.enrolledUsers)),
    1
  )

  return (
    <section className="flex flex-col rounded-2xl border border-border bg-card p-6 shadow-sm transition hover:border-border/80">
      {/* Header */}
      <div className="flex flex-wrap items-start justify-between gap-2 border-b border-border/60 pb-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="font-mono text-sm font-bold tracking-wider text-foreground">
              FREE VS ENROLLED STUDENTS
            </h2>
            <span className="rounded-full border border-border bg-background/50 px-2 py-0.5 font-mono text-[9px] font-semibold uppercase tracking-widest text-muted-foreground">
              ACCOUNT SIGNUPS
            </span>
          </div>
          <p className="mt-1 text-xs text-muted-foreground">
            Monthly student account registrations: Paid / Enrolled vs Free Preview users.
          </p>
        </div>

        {/* Legend */}
        <div className="flex items-center gap-4 font-mono text-xs">
          <div className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-sm bg-accent" />
            <span className="text-foreground font-semibold">Enrolled (Paid)</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-sm bg-muted-foreground/60" />
            <span className="text-muted-foreground">Free Preview</span>
          </div>
        </div>
      </div>

      {/* Tooltip & Current Snapshot */}
      <div className="flex h-7 items-center justify-between px-2 pt-3 font-mono text-xs">
        {hoveredIdx !== null ? (
          <>
            <span className="text-muted-foreground">
              Month: <strong className="text-foreground">{data[hoveredIdx].label}</strong>
            </span>
            <div className="flex gap-4">
              <span className="text-accent font-bold">
                Enrolled: {data[hoveredIdx].enrolledUsers.toLocaleString()}
              </span>
              <span className="text-muted-foreground font-semibold">
                Free: {data[hoveredIdx].freeUsers.toLocaleString()}
              </span>
              <span className="text-foreground font-bold">
                Total: {data[hoveredIdx].total.toLocaleString()}
              </span>
            </div>
          </>
        ) : (
          <span className="text-[11px] text-muted-foreground/60 italic">
            Hover over any dual bar to compare Free vs Paid user acquisition
          </span>
        )}
      </div>

      {/* Chart Canvas with Dual Bars */}
      <div className="relative mt-2 flex h-52 items-end gap-3 border-b border-l border-border/80 px-3 pb-2 pt-8">
        <div className="pointer-events-none absolute inset-x-0 top-0 border-t border-dashed border-border/40" />
        <div className="pointer-events-none absolute inset-x-0 top-1/2 border-t border-dashed border-border/25" />

        {data.map((item, idx) => {
          const enrolledHeight = item.enrolledUsers === 0 ? 3 : Math.max((item.enrolledUsers / maxVal) * 100, 6)
          const freeHeight = item.freeUsers === 0 ? 3 : Math.max((item.freeUsers / maxVal) * 100, 6)
          const isHovered = hoveredIdx === idx

          return (
            <div
              key={item.month}
              className="group relative flex flex-1 cursor-pointer items-end justify-center gap-1 h-full"
              onMouseEnter={() => setHoveredIdx(idx)}
              onMouseLeave={() => setHoveredIdx(null)}
            >
              {/* Enrolled Bar */}
              <div
                className={`w-1/2 rounded-t-md transition-all duration-300 ${
                  item.enrolledUsers === 0
                    ? "bg-muted-foreground/20 hover:bg-muted-foreground/30"
                    : isHovered
                    ? "bg-accent shadow-[0_0_12px_rgba(245,158,11,0.5)]"
                    : "bg-accent/80 hover:bg-accent"
                }`}
                style={{ height: `${enrolledHeight}%` }}
              />

              {/* Free Bar */}
              <div
                className={`w-1/2 rounded-t-md transition-all duration-300 ${
                  item.freeUsers === 0
                    ? "bg-muted-foreground/20 hover:bg-muted-foreground/30"
                    : isHovered
                    ? "bg-foreground/80"
                    : "bg-muted-foreground/40 hover:bg-muted-foreground/60"
                }`}
                style={{ height: `${freeHeight}%` }}
              />
            </div>
          )
        })}
      </div>

      {/* Month Labels */}
      <div className="mt-3 flex justify-between px-2 font-mono text-[10px] text-muted-foreground">
        {data.map((item, idx) => (
          <span
            key={item.month}
            className={`transition-colors ${hoveredIdx === idx ? "font-bold text-foreground" : ""}`}
          >
            {item.label.split(" ")[0].toUpperCase()}
          </span>
        ))}
      </div>
    </section>
  )
}

// ---------------------------------------------------------------------------
// Main Admin Analytics Page (100% Real Database Data)
// ---------------------------------------------------------------------------
export function AdminAnalyticsPage() {
  const [data, setData] = useState<AdminAnalyticsData | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false)
  const [error, setError] = useState<string | null>(null)

  const fetchAnalytics = async () => {
    try {
      setIsRefreshing(true)
      setError(null)
      const res = await getAdminAnalytics()
      if (res.success && res.data) {
        setData(res.data)
      } else {
        setError(res.error || "Failed to load live analytics from database.")
      }
    } catch (err: any) {
      setError(err?.message || "Failed to connect to backend analytics service.")
    } finally {
      setLoading(false)
      setIsRefreshing(false)
    }
  }

  useEffect(() => {
    fetchAnalytics()
  }, [])

  if (loading) {
    return (
      <div className="flex flex-col gap-8">
        <div className="flex flex-col justify-between gap-4 border-b border-border pb-6 sm:flex-row sm:items-center">
          <div>
            <div className="h-6 w-48 animate-pulse rounded bg-muted/60" />
            <div className="mt-2 h-4 w-96 animate-pulse rounded bg-muted/40" />
          </div>
        </div>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="h-24 animate-pulse rounded-xl border border-border bg-card p-4" />
          ))}
        </div>
        <div className="grid gap-6 lg:grid-cols-2">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-80 animate-pulse rounded-2xl border border-border bg-card p-6" />
          ))}
        </div>
      </div>
    )
  }

  if (error || !data) {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-border bg-card p-12 text-center">
        <AlertCircle className="size-10 text-destructive" />
        <h2 className="mt-4 font-mono text-base font-bold text-foreground">FAILED TO LOAD REAL ANALYTICS</h2>
        <p className="mt-2 max-w-md text-sm text-muted-foreground">
          {error || "Unable to reach the live database metrics endpoint."}
        </p>
        <button
          onClick={fetchAnalytics}
          className="mt-6 flex items-center gap-2 rounded-lg border border-border bg-foreground px-4 py-2 font-mono text-xs font-semibold text-background hover:opacity-90"
        >
          <RotateCw className="size-3.5" /> RETRY DATABASE CONNECTION
        </button>
      </div>
    )
  }

  const kpiItems = [
    {
      label: "ACTIVE LEARNERS",
      value: data.totalActiveLearners,
      icon: Users,
      description: "Enrolled in-progress",
      badge: "In Progress",
      color: "text-accent",
    },
    {
      label: "ENROLLED STUDENTS",
      value: data.totalEnrolledStudents,
      icon: UserCheck,
      description: "Paid course buyers",
      badge: "Paid Access",
      color: "text-accent",
    },
    {
      label: "FREE ACCOUNTS",
      value: data.totalFreeUsers,
      icon: UserX,
      description: "Registered free users",
      badge: "Free Preview",
      color: "text-muted-foreground",
    },
    {
      label: "COURSE COMPLETIONS",
      value: data.totalCompletions,
      icon: GraduationCap,
      description: "Finished 100% lessons",
      badge: "Graduated",
      color: "text-emerald-400",
    },
    {
      label: "VSL VIDEO WATCHES",
      value: data.totalVslViews || 0,
      icon: Video,
      description: "Landing page VSL views",
      badge: "Top Funnel",
      color: "text-amber-400",
    },
    {
      label: "WELCOME VIDEO WATCHES",
      value: data.totalWelcomeVideoViews || 0,
      icon: Play,
      description: "Dashboard overview views",
      badge: "Orientation",
      color: "text-violet-400",
    },
    {
      label: "RESOURCE DOWNLOADS",
      value: data.totalResourceDownloads,
      icon: Download,
      description: "Total material downloads",
      badge: "Free Materials",
      color: "text-sky-400",
    },
    {
      label: "COUPON ORDERS",
      value: data.totalCouponUsages,
      icon: Tag,
      description: "Coupon-applied purchases",
      badge: "Discounts Used",
      color: "text-amber-400",
    },
  ]

  return (
    <div className="flex flex-col gap-8">
      {/* Top Banner & Refresh Action */}
      <div className="flex flex-col justify-between gap-4 border-b border-border pb-6 sm:flex-row sm:items-center">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-bold tracking-tight">PLATFORM ANALYTICS</h1>
            <span className="inline-flex items-center gap-1.5 rounded-full border border-emerald-500/30 bg-emerald-500/10 px-2.5 py-0.5 font-mono text-[10px] font-semibold text-emerald-400">
              <span className="size-1.5 rounded-full bg-emerald-400 animate-pulse" />
              100% REAL DATABASE SYNC
            </span>
          </div>
          <p className="mt-1 text-sm text-muted-foreground">
            Live database metrics for active learning, video watch engagement, course progress, account acquisition, and promotional performance.
          </p>
        </div>

        <button
          onClick={fetchAnalytics}
          disabled={isRefreshing}
          className="flex items-center gap-2 self-start rounded-lg border border-border bg-card px-4 py-2 font-mono text-xs font-semibold tracking-wider text-muted-foreground transition hover:border-accent/40 hover:text-foreground disabled:opacity-50"
        >
          <RotateCw className={`size-3.5 ${isRefreshing ? "animate-spin text-accent" : ""}`} />
          REFRESH LIVE DATA
        </button>
      </div>

      {/* KPI Highlights Bar */}
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {kpiItems.map((kpi) => {
          const Icon = kpi.icon
          return (
            <div
              key={kpi.label}
              className="group rounded-xl border border-border bg-card p-4 shadow-sm transition hover:border-border/80"
            >
              <div className="flex items-center justify-between">
                <span className="font-mono text-[9px] uppercase tracking-widest text-muted-foreground">
                  {kpi.label}
                </span>
                <Icon className={`size-3.5 ${kpi.color}`} />
              </div>
              <div className="mt-3 font-mono text-2xl font-black tracking-tight text-foreground">
                {kpi.value.toLocaleString()}
              </div>
              <div className="mt-1 text-[11px] text-muted-foreground truncate">{kpi.description}</div>
            </div>
          )
        })}
      </div>

      {/* The Charts Grid */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* 1. VSL VIDEO WATCHES */}
        <MetricBarChart
          title="VSL VIDEO WATCHES"
          subtitle="Month-wise watch count of the homepage Video Sales Letter by prospective visitors."
          data={data.vslVideoViews || []}
          badgeText="Landing Page · Top Funnel"
          accentColor="amber"
          valueUnit="views"
        />

        {/* 2. WELCOME VIDEO WATCHES */}
        <MetricBarChart
          title="WELCOME VIDEO WATCHES"
          subtitle="Month-wise watch count of the Student Dashboard Course Overview & Welcome video."
          data={data.welcomeVideoViews || []}
          badgeText="Dashboard · Onboarding"
          accentColor="violet"
          valueUnit="views"
        />

        {/* 3. ACTIVE LEARNERS */}
        <MetricBarChart
          title="ACTIVE LEARNERS"
          subtitle="Enrolled students who purchased the course and are actively learning (course not completed yet)."
          data={data.activeLearners}
          badgeText="In Progress · Learning"
          accentColor="accent"
          valueUnit="students"
        />

        {/* 4. FREE VS ENROLLED STUDENTS */}
        <FreeVsEnrolledChart data={data.freeVsEnrolled} />

        {/* 5. COURSE COMPLETION */}
        <MetricBarChart
          title="COURSE COMPLETION"
          subtitle="Total students who completed 100% of all lessons in the course curriculum."
          data={data.courseCompletion}
          badgeText="100% Curriculum Finished"
          accentColor="emerald"
          valueUnit="completions"
        />

        {/* 6. RESOURCE DOWNLOADS */}
        <MetricBarChart
          title="RESOURCE DOWNLOADS"
          subtitle="Month-wise download frequency of free PDF guides, toolkits, and creative assets."
          data={data.resourceDownloads}
          badgeText="Free Materials"
          accentColor="sky"
          valueUnit="downloads"
        />

        {/* 7. COUPON ENGAGEMENT */}
        <div className="lg:col-span-2">
          <MetricBarChart
            title="COUPON ENGAGEMENT"
            subtitle="Month-wise number of students who redeemed discount coupon codes during course checkout."
            data={data.couponEngagement}
            badgeText="Coupon-Assisted Purchases"
            accentColor="amber"
            valueUnit="orders"
          />
        </div>
      </div>
    </div>
  )
}

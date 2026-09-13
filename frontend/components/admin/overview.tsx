"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { Card } from "@/components/admin/shell"
import { getAdminOverview, type AdminOverviewData } from "@/lib/api/admin-service"
import { formatDate } from "@/lib/utils"
import { Users, UserCheck, UserX, Download, Loader2, ArrowRight } from "lucide-react"

export function AdminOverview() {
  const [data, setData] = useState<AdminOverviewData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadOverviewData = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await getAdminOverview()
      if (res.success && res.data) {
        setData(res.data)
      } else {
        setError(res.error || "Failed to load live metrics from database")
      }
    } catch (err: any) {
      setError(err?.message || "Failed to connect to backend")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadOverviewData()
  }, [])

  const kpiCards = [
    {
      label: "TOTAL USERS",
      value: data?.totalUsers ?? 0,
      icon: Users,
      description: "Total registered accounts",
    },
    {
      label: "FREE USERS",
      value: data?.freeUsers ?? 0,
      icon: UserX,
      description: "Free preview / non-enrolled accounts",
    },
    {
      label: "ENROLLED STUDENTS",
      value: data?.enrolledStudents ?? 0,
      icon: UserCheck,
      description: "Active paid course enrollments",
    },
    {
      label: "RESOURCES DOWNLOADED",
      value: data?.resourcesDownloaded ?? 0,
      icon: Download,
      description: "Total published materials",
    },
  ]

  const recentStudents = (data?.recentEnrolledStudents || []).slice(0, 4)

  return (
    <div className="flex flex-col gap-8">
      {/* Top 4 Operational KPIs */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {kpiCards.map((kpi) => {
          const Icon = kpi.icon
          return (
            <div
              key={kpi.label}
              className="group relative overflow-hidden rounded-2xl border border-border bg-card p-6 shadow-sm transition hover:border-accent/40"
            >
              <div className="flex items-center justify-between">
                <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                  {kpi.label}
                </span>
                <div className="flex size-8 items-center justify-center rounded-lg border border-border/60 bg-muted/30 text-muted-foreground transition group-hover:text-accent group-hover:border-accent/30">
                  <Icon className="size-4" />
                </div>
              </div>
              <div className="mt-4 flex items-baseline gap-2">
                {loading ? (
                  <div className="h-8 w-16 animate-pulse rounded bg-muted/60" />
                ) : (
                  <span className="text-3xl font-black tracking-tight">
                    {Number(kpi.value).toLocaleString()}
                  </span>
                )}
              </div>
              <p className="mt-2 text-xs text-muted-foreground">{kpi.description}</p>
            </div>
          )
        })}
      </div>

      {/* Enrolled Students (Recent 4) */}
      <div className="w-full">
        <Card
          title="ENROLLED STUDENTS"
          action={
            <Link
              className="flex items-center gap-1 font-mono text-[11px] font-semibold text-accent transition hover:opacity-80"
              href="/admin/users"
            >
              VIEW ALL <ArrowRight className="size-3" />
            </Link>
          }
        >
          {loading ? (
            <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
              <Loader2 className="size-6 animate-spin text-accent" />
              <span className="mt-2 font-mono text-xs">LOADING REAL DATABASE DATA...</span>
            </div>
          ) : recentStudents.length === 0 ? (
            <div className="rounded-xl border border-dashed border-border p-8 text-center">
              <UserCheck className="mx-auto size-8 text-muted-foreground/50" />
              <p className="mt-2 font-mono text-xs font-semibold text-muted-foreground">
                NO ENROLLED STUDENTS FOUND
              </p>
              <p className="mt-1 text-xs text-muted-foreground/80">
                When students complete a course purchase via Razorpay, they will appear here automatically.
              </p>
            </div>
          ) : (
            <div className="divide-y divide-border">
              {recentStudents.map((student) => (
                <div
                  key={student.id || student.email}
                  className="flex flex-col gap-2 py-4 first:pt-0 last:pb-0 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div className="flex items-center gap-3">
                    <div className="flex size-9 items-center justify-center rounded-full border border-border bg-muted/50 font-mono text-xs font-bold text-foreground">
                      {(student.name?.[0] || "S").toUpperCase()}
                    </div>
                    <div>
                      <div className="font-semibold text-foreground">{student.name}</div>
                      <div className="font-mono text-xs text-muted-foreground">{student.email}</div>
                    </div>
                  </div>
                  <div className="flex items-center justify-between gap-6 sm:justify-end">
                    <span className="inline-flex items-center rounded-md border border-accent/30 bg-accent/10 px-2.5 py-0.5 font-mono text-[11px] font-medium text-accent">
                      Full Course Access
                    </span>
                    <span className="font-mono text-xs text-muted-foreground">
                      {student.enrolledAt ? formatDate(student.enrolledAt) : "Recently"}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  )
}

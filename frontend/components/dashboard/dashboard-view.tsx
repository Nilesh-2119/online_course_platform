"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import {
  ArrowRight,
  Bell,
  BookOpen,
  CheckCircle,
  CheckCircle2,
  Download,
  Flame,
  GraduationCap,
  Info,
  Loader2,
  Lock,
  LogOut,
  Play,
  PlayCircle,
  RefreshCw,
  ShieldAlert,
  Sparkles,
  UserRound,
  Zap,
  FileSpreadsheet,
  FileText,
  File,
  ExternalLink,
} from "lucide-react"
import { useAuth } from "@/lib/auth"
import { formatCurrency, cn } from "@/lib/utils"
import { mockCourses, mockLessons } from "@/mocks/seed-data"
import { resourceService } from "@/lib/api"
import { getCourseProgress, getCourseLessons, syncVdoCipherVideos } from "@/lib/api/course-service"
import type { CourseLesson, CourseProgress, FreeResource } from "@/lib/types"

interface NotificationItem {
  id: string
  title: string
  message: string
  date: string
  tag: string
  isNew?: boolean
  icon: typeof Bell
}

const NOTIFICATIONS: NotificationItem[] = [
  {
    id: "notif-1",
    title: "Live Creative Workshop",
    message: "Join the live breakdown session this Saturday at 6:00 PM IST with Q&A.",
    date: "Today, 11:30 AM",
    tag: "LIVE EVENT",
    isNew: true,
    icon: Flame,
  },
  {
    id: "notif-2",
    title: "New Resource Added",
    message: "The 2026 Ad Hook Swipe File & Script Template has been added to Free Resources.",
    date: "Yesterday",
    tag: "RESOURCE",
    isNew: false,
    icon: Sparkles,
  },
  {
    id: "notif-3",
    title: "Course System Updated",
    message: "Fast 1080p adaptive bitrate streaming is now enabled for all course videos.",
    date: "2 days ago",
    tag: "UPDATE",
    isNew: false,
    icon: Zap,
  },
]

export function DashboardView() {
  const { user, logout } = useAuth()
  const [section, setSection] = useState<"overview" | "courses" | "resources" | "account">("overview")
  const [downloadingId, setDownloadingId] = useState<string | null>(null)
  const [downloadedIds, setDownloadedIds] = useState<string[]>([])
  const [lessons, setLessons] = useState<CourseLesson[]>([])
  const [progress, setProgress] = useState<CourseProgress>({
    courseId: "1",
    userId: user?.id || "current",
    status: "not_started",
    completedLessons: 0,
    totalLessons: 0,
    percentage: 0,
    currentLessonId: "1",
  })
  const [loadingLessons, setLoadingLessons] = useState(true)
  const [loadingResources, setLoadingResources] = useState(true)
  const [isSyncing, setIsSyncing] = useState(false)
  const [dbResources, setDbResources] = useState<FreeResource[]>([])

  const isPurchased = user?.type === "paid" || Boolean((user as any)?.coursePurchased)
  const course = mockCourses[0]

  const loadData = async () => {
    try {
      const [progressRes, lessonsRes, resourcesRes] = await Promise.all([
        getCourseProgress("1"),
        getCourseLessons("1"),
        resourceService.getResources(),
      ])

      if (progressRes.success && progressRes.data) {
        setProgress(progressRes.data)
      }
      if (lessonsRes.success && Array.isArray(lessonsRes.data)) {
        setLessons(lessonsRes.data)
      }
      if (resourcesRes.success && Array.isArray(resourcesRes.data)) {
        setDbResources(resourcesRes.data)
      }
    } catch (err) {
      console.error("Failed to load dashboard course data:", err)
    } finally {
      setLoadingLessons(false)
      setLoadingResources(false)
    }
  }

  useEffect(() => {
    if (typeof window !== "undefined") {
      const host = window.location.hostname || ""
      if (host.includes("lighthousedashboard") || host.startsWith("admin.")) {
        window.location.href = "/admin"
        return
      }
    }
    const currentEmail = user?.email?.toLowerCase().trim() || ""
    if (user && (user.role === "admin" || currentEmail === "adfixstudio25@gmail.com")) {
      window.location.href = "/admin"
      return
    }
    loadData()
  }, [user])

  const handleSyncVideos = async () => {
    setIsSyncing(true)
    try {
      await syncVdoCipherVideos()
      await loadData()
    } catch (err) {
      console.error("Failed to sync videos:", err)
    } finally {
      setIsSyncing(false)
    }
  }

  const handleLogout = () => {
    logout()
    window.location.href = "/"
  }

  const [resourceFilter, setResourceFilter] = useState<string>("ALL")

  const handleDownloadResource = async (resourceId: string, directUrl?: string) => {
    setDownloadingId(resourceId)
    try {
      const res = await resourceService.getResourceDownloadUrl(resourceId)
      const targetUrl = (res.success && res.data) ? res.data : directUrl
      if (targetUrl) {
        setDownloadedIds((prev) => [...prev, resourceId])
        window.open(targetUrl, "_blank", "noopener,noreferrer")
      }
    } catch (err) {
      console.error("Download error:", err)
      if (directUrl) {
        window.open(directUrl, "_blank", "noopener,noreferrer")
      }
    } finally {
      setDownloadingId(null)
    }
  }

  const formatFileSize = (bytes?: number) => {
    if (!bytes || bytes <= 0) return ""
    if (bytes < 1024) return `${bytes} B`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  }

  // Determine welcome / overview lesson or first lesson
  const welcomeLesson = lessons.find(
    (l) =>
      l.title?.toLowerCase().includes("welcome") ||
      l.title?.toLowerCase().includes("overview") ||
      l.title?.toLowerCase().includes("intro")
  ) || lessons[0]

  const overviewHref = `/dashboard/course/${welcomeLesson?.id || lessons[0]?.id || "1"}`
  const activeLesson = lessons.find((l) => String(l.id) === String(progress.currentLessonId)) || lessons[0]
  const completedCount = progress.completedLessons || (isPurchased ? 1 : 0)
  const totalCount = lessons.length || progress.totalLessons || 4

  const resumeHref = `/dashboard/course/${activeLesson?.id || "1"}`

  return (
    <main className="min-h-screen bg-[#f5f4ef] text-foreground font-sans antialiased selection:bg-accent selection:text-accent-foreground">
      {/* Desktop Sidebar */}
      <aside className="fixed inset-y-0 left-0 hidden w-72 flex-col border-r border-border bg-background lg:flex z-30">
        <div className="border-b border-border px-7 py-7">
          <Link href="/" className="text-xl font-black tracking-[-.06em]">
            AdFix <span className="text-accent">Studio</span>
          </Link>
        </div>

        {/* User Mini Profile Card in Sidebar */}
        <div className="mx-4 mt-6 rounded-2xl border border-border bg-[#f5f4ef] p-4">
          <div className="flex items-center gap-3">
            <div className="grid size-10 place-items-center rounded-xl bg-foreground text-background font-bold text-sm">
              {(user?.name || "Student").charAt(0).toUpperCase()}
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-black tracking-tight">{user?.name || "Student"}</p>
              <p className="truncate font-mono text-[10px] text-muted-foreground">{user?.email || "student@example.com"}</p>
            </div>
          </div>
          <div className="mt-3 flex items-center justify-between border-t border-border/70 pt-2.5">
            <span className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">TIER</span>
            <span
              className={cn(
                "rounded-full px-2 py-0.5 font-mono text-[9px] font-bold tracking-wider",
                isPurchased ? "bg-emerald-500/15 text-emerald-700 border border-emerald-500/30" : "bg-amber-500/15 text-amber-700 border border-amber-500/30"
              )}
            >
              {isPurchased ? "PURCHASED" : "FREE TIER"}
            </span>
          </div>
        </div>

        <nav className="flex flex-col gap-2 px-5 py-6 font-mono text-xs" aria-label="Dashboard navigation">
          {([
            ["overview", "OVERVIEW", Flame],
            ["courses", "ALL LESSONS", BookOpen],
            ["resources", "FREE RESOURCES", Download],
            ["account", "ACCOUNT", UserRound],
          ] as const).map(([key, label, Icon]) => (
            <button
              key={key}
              onClick={() => setSection(key)}
              className={cn(
                "flex items-center gap-2.5 rounded-xl px-3 py-3 text-left transition-all",
                section === key
                  ? "bg-foreground font-bold text-background shadow-sm"
                  : "text-muted-foreground hover:bg-muted hover:text-foreground"
              )}
            >
              <Icon className="size-4" />
              {label}
            </button>
          ))}
        </nav>

        <div className="mt-auto border-t border-border px-5 py-5">
          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-2.5 rounded-xl px-3 py-3 text-left font-mono text-xs text-muted-foreground transition-colors hover:bg-red-500/10 hover:text-red-600"
          >
            <LogOut className="size-4" /> LOG OUT
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="min-h-screen lg:pl-72">
        {/* Mobile Header */}
        <header className="sticky top-0 z-20 flex items-center justify-between border-b border-border bg-background/95 backdrop-blur px-5 py-4 lg:hidden">
          <Link href="/" className="text-lg font-black tracking-[-.06em]">
            AdFix <span className="text-accent">Studio</span>
          </Link>
          <div className="flex items-center gap-2">
            <span
              className={cn(
                "rounded-full px-2.5 py-1 font-mono text-[9px] font-bold tracking-wider",
                isPurchased ? "bg-emerald-500/15 text-emerald-700 border border-emerald-500/30" : "bg-amber-500/15 text-amber-700 border border-amber-500/30"
              )}
            >
              {isPurchased ? "PURCHASED" : "FREE"}
            </span>
            <button onClick={handleLogout} className="rounded-lg p-1.5 text-muted-foreground hover:bg-muted">
              <LogOut className="size-4" />
            </button>
          </div>
        </header>

        <div className="mx-auto max-w-5xl px-5 py-8 pb-32 md:px-10 md:py-12 md:pb-20">
          {/* ========================================================================= */}
          {/* TOP WELCOME & STATUS BANNER (Displayed Across Dashboard) */}
          {/* ========================================================================= */}
          <div className="relative overflow-hidden rounded-3xl border border-border bg-background p-6 md:p-8 shadow-sm">
            <div className="relative z-10 flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
              <div className="space-y-2">
                <div className="flex flex-wrap items-center gap-2.5">
                  <span className="font-mono text-xs font-semibold tracking-wider text-muted-foreground uppercase">
                    STUDENT DASHBOARD
                  </span>
                  <span className="text-muted-foreground">·</span>
                  <span
                    className={cn(
                      "inline-flex items-center gap-1.5 rounded-full px-3 py-1 font-mono text-[10px] font-bold uppercase tracking-wider",
                      isPurchased
                        ? "bg-emerald-500/15 text-emerald-700 border border-emerald-500/30 shadow-[0_0_12px_rgba(16,185,129,0.15)]"
                        : "bg-amber-500/15 text-amber-700 border border-amber-500/30"
                    )}
                  >
                    {isPurchased ? (
                      <>
                        <Sparkles className="size-3 text-emerald-600 animate-pulse" />
                        PURCHASED · LIFETIME ACCESS
                      </>
                    ) : (
                      <>
                        <Zap className="size-3 text-amber-600" />
                        FREE TIER · PREVIEW ONLY
                      </>
                    )}
                  </span>
                </div>

                <h1 className="text-3xl font-black tracking-[-0.05em] sm:text-4xl md:text-5xl">
                  Welcome back, {user?.name || "Student"} 👋
                </h1>

                <div className="flex flex-wrap items-center gap-x-4 gap-y-1 font-mono text-xs text-muted-foreground">
                  <span>{user?.email || "student@example.com"}</span>
                  {user?.phone && (
                    <>
                      <span>·</span>
                      <span>{user.phone}</span>
                    </>
                  )}
                </div>
              </div>

              {/* Top CTA for free students only */}
              {!isPurchased && (
                <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
                  <Link
                    href="/buy"
                    className="inline-flex items-center justify-center gap-2 rounded-full bg-foreground px-6 py-3.5 font-mono text-xs font-bold text-background shadow-md transition-transform hover:scale-[1.02] active:scale-95"
                  >
                    UNLOCK FULL COURSE <ArrowRight className="size-4 text-accent" />
                  </Link>
                </div>
              )}
            </div>

            {/* Subtle Gradient Accent background glow */}
            <div className="pointer-events-none absolute -right-20 -top-20 size-72 rounded-full bg-accent/15 blur-3xl" />
          </div>

          {/* ========================================================================= */}
          {/* TAB 1: OVERVIEW (Recent Video + Notifications + Modules) */}
          {/* ========================================================================= */}
          {section === "overview" && (
            <div className="mt-8 space-y-8">
              {/* 2-Column Grid: Recent Video Played & Notifications */}
              <div className="grid gap-6 lg:grid-cols-12">
                {/* 1. Welcome Video / Course Overview Card (7 Cols) */}
                <div className="lg:col-span-7 flex flex-col justify-between rounded-3xl border border-border bg-background p-6 md:p-7 shadow-sm">
                  <div>
                    <div className="flex items-center justify-between">
                      <span className="inline-flex items-center gap-1.5 font-mono text-[11px] font-bold tracking-wider text-muted-foreground uppercase">
                        <PlayCircle className="size-4 text-accent" /> WELCOME VIDEO / COURSE OVERVIEW
                      </span>
                      <span className="rounded-full bg-accent/20 px-2.5 py-0.5 font-mono text-[10px] font-bold text-accent-foreground">
                        START HERE
                      </span>
                    </div>

                    <div className="mt-5 group relative aspect-video overflow-hidden rounded-2xl border border-border bg-foreground">
                      <div className="absolute inset-0 bg-[linear-gradient(135deg,rgba(204,255,0,.25),transparent_70%)]" />
                      <div className="absolute left-4 top-4 font-mono text-xs font-bold text-accent">
                        COURSE OVERVIEW
                      </div>

                      {/* Play Button Overlay */}
                      <Link
                        href={overviewHref}
                        className="absolute inset-0 m-auto grid size-14 place-items-center rounded-full border border-accent bg-accent text-accent-foreground shadow-lg transition-transform group-hover:scale-110 active:scale-95"
                      >
                        <Play className="size-6 fill-current ml-1" />
                      </Link>

                      <div className="absolute bottom-4 left-4 right-4 flex items-center justify-between text-background font-mono text-[11px]">
                        <span className="rounded-md bg-black/60 px-2 py-1 backdrop-blur font-bold">
                          {welcomeLesson?.title || "Welcome to AdFix Masterclass"}
                        </span>
                        <span className="rounded-md bg-black/60 px-2 py-1 backdrop-blur">
                          {welcomeLesson?.duration ? `${Math.floor(welcomeLesson.duration / 60)} mins` : "Overview"}
                        </span>
                      </div>
                    </div>

                    <h3 className="mt-4 text-xl font-black tracking-tight">
                      {welcomeLesson?.title ? `Course Overview: ${welcomeLesson.title}` : "Welcome to AdFix Studio – Course Overview"}
                    </h3>
                    <p className="mt-1 font-mono text-xs text-muted-foreground line-clamp-2">
                      {welcomeLesson?.description || "Watch this quick orientation before starting your lessons to understand how to get the most out of the curriculum, resources, and frameworks."}
                    </p>
                  </div>

                  <div className="mt-6 pt-4 border-t border-border flex items-center justify-between">
                    <Link
                      href={overviewHref}
                      className="inline-flex items-center gap-2 rounded-full bg-foreground px-5 py-2.5 font-mono text-xs font-bold text-background transition-transform hover:scale-[1.02] active:scale-95"
                    >
                      <Play className="size-3.5 fill-current text-accent" /> WATCH COURSE OVERVIEW
                    </Link>
                    <button
                      onClick={() => setSection("courses")}
                      className="font-mono text-xs text-muted-foreground hover:text-foreground underline underline-offset-4"
                    >
                      View All Lessons →
                    </button>
                  </div>
                </div>

                {/* 2. Notifications & Announcements Card (5 Cols) */}
                <div className="lg:col-span-5 flex flex-col rounded-3xl border border-border bg-background p-6 md:p-7 shadow-sm">
                  <div className="flex items-center justify-between">
                    <span className="inline-flex items-center gap-1.5 font-mono text-[11px] font-bold tracking-wider text-muted-foreground uppercase">
                      <Bell className="size-4 text-accent" /> NOTIFICATIONS
                    </span>
                    <span className="flex size-2 rounded-full bg-accent animate-ping" />
                  </div>

                  <div className="mt-4 flex-1 divide-y divide-border/60">
                    {NOTIFICATIONS.map((notif) => {
                      const Icon = notif.icon
                      return (
                        <div key={notif.id} className="py-3.5 first:pt-0 last:pb-0">
                          <div className="flex items-start gap-3">
                            <div className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-xl bg-[#f5f4ef] border border-border">
                              <Icon className="size-4 text-foreground" />
                            </div>
                            <div className="min-w-0 flex-1 space-y-1">
                              <div className="flex items-center justify-between gap-2">
                                <p className="truncate text-xs font-black tracking-tight">{notif.title}</p>
                                {notif.isNew && (
                                  <span className="rounded-full bg-accent px-1.5 py-0.5 font-mono text-[8px] font-black text-accent-foreground">
                                    NEW
                                  </span>
                                )}
                              </div>
                              <p className="font-mono text-[11px] leading-relaxed text-muted-foreground">
                                {notif.message}
                              </p>
                              <p className="font-mono text-[9px] text-muted-foreground/80">{notif.date}</p>
                            </div>
                          </div>
                        </div>
                      )
                    })}
                  </div>

                  <div className="mt-4 pt-3 border-t border-border">
                    <div className="flex items-center justify-between text-muted-foreground font-mono text-[10px]">
                      <span>All updates synced</span>
                      <span className="text-foreground font-semibold">3 Total</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Course Curriculum & Modules Breakdown */}
              <div className="rounded-3xl border border-border bg-background p-6 md:p-8 shadow-sm">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <p className="font-mono text-xs font-bold tracking-wider text-accent uppercase">
                      COURSE CURRICULUM
                    </p>
                    <h2 className="mt-1 text-2xl font-black tracking-tight md:text-3xl">All Modules & Lessons</h2>
                  </div>
                  <div className="flex items-center gap-3">
                    <button
                      onClick={handleSyncVideos}
                      disabled={isSyncing}
                      className="inline-flex items-center gap-1.5 rounded-full border border-border bg-card px-3.5 py-1.5 font-mono text-[11px] font-bold text-foreground hover:border-accent transition-all hover:scale-105 active:scale-95 disabled:opacity-50"
                      title="Refresh latest lessons"
                    >
                      <RefreshCw className={cn("size-3 text-accent", isSyncing && "animate-spin")} />
                      {isSyncing ? "Refreshing..." : "Refresh"}
                    </button>
                    <span className="font-mono text-xs text-muted-foreground">
                      {lessons.length} Total Lessons
                    </span>
                  </div>
                </div>

                {loadingLessons ? (
                  <div className="mt-8 flex flex-col items-center justify-center py-12 text-center">
                    <Loader2 className="size-8 animate-spin text-accent" />
                    <p className="mt-3 font-mono text-xs text-muted-foreground uppercase tracking-wider">Loading Synced Lessons...</p>
                  </div>
                ) : lessons.length === 0 ? (
                  <div className="mt-8 rounded-2xl border border-dashed border-border p-8 text-center">
                    <p className="font-mono text-sm font-bold text-foreground">No videos found</p>
                    <p className="mt-1 font-mono text-xs text-muted-foreground">
                      Upload your course videos directly to VdoCipher to automatically populate this curriculum.
                    </p>
                  </div>
                ) : (
                  <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {lessons.map((lesson, idx) => {
                      const isLessonUnlocked = isPurchased || lesson.hasPreview
                      const isCompleted = idx < completedCount

                      return (
                        <article
                          key={lesson.id}
                          className="group flex flex-col justify-between overflow-hidden rounded-2xl border border-border bg-[#f5f4ef] transition-all hover:border-foreground/30 hover:shadow-md"
                        >
                          <div className="relative aspect-video bg-foreground">
                            <div className="absolute inset-0 bg-[linear-gradient(135deg,rgba(204,255,0,.15),transparent_60%)]" />
                            <div className="absolute left-3 top-3 flex items-center gap-1.5">
                              <span className="rounded-md bg-black/60 px-2 py-0.5 font-mono text-[10px] text-accent backdrop-blur font-bold">
                                0{lesson.order || idx + 1}
                              </span>
                              <span className="rounded-md bg-black/60 px-2 py-0.5 font-mono text-[9px] text-background backdrop-blur">
                                {isPurchased ? "FULL LESSON" : lesson.hasPreview ? "FREE PREVIEW" : "LOCKED"}
                              </span>
                            </div>

                            {isLessonUnlocked ? (
                              <Link
                                href={`/dashboard/course/${lesson.id}`}
                                className="absolute inset-0 m-auto grid size-11 place-items-center rounded-full border border-accent bg-accent text-accent-foreground transition-transform group-hover:scale-110 active:scale-95"
                              >
                                <Play className="size-5 fill-current ml-0.5" />
                              </Link>
                            ) : (
                              <div className="absolute inset-0 m-auto grid size-11 place-items-center rounded-full bg-black/60 border border-white/20 text-white">
                                <Lock className="size-4" />
                              </div>
                            )}
                          </div>

                          <div className="flex flex-1 flex-col justify-between p-4">
                            <div className="space-y-1">
                              <div className="flex items-center justify-between">
                                <p className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">
                                  LESSON 0{lesson.order || idx + 1}
                                </p>
                                {isCompleted && (
                                  <span className="inline-flex items-center gap-1 text-[10px] font-mono font-bold text-emerald-600">
                                    <CheckCircle className="size-3" /> DONE
                                  </span>
                                )}
                              </div>
                              <h3 className="font-bold text-sm leading-tight text-foreground">{lesson.title}</h3>
                              <p className="font-mono text-[11px] leading-relaxed text-muted-foreground line-clamp-2">
                                {lesson.description}
                              </p>
                            </div>

                            <div className="mt-4 pt-3 border-t border-border/70">
                              {isLessonUnlocked ? (
                                <Link
                                  href={`/dashboard/course/${lesson.id}`}
                                  className="inline-flex items-center gap-1.5 font-mono text-[11px] font-bold text-foreground group-hover:text-accent transition-colors"
                                >
                                  {isCompleted ? "REPLAY LESSON →" : "START LESSON →"}
                                </Link>
                              ) : (
                                <Link
                                  href="/buy"
                                  className="inline-flex items-center gap-1.5 font-mono text-[11px] font-bold text-muted-foreground hover:text-foreground"
                                >
                                  <Lock className="size-3" /> UNLOCK WITH PRO →
                                </Link>
                              )}
                            </div>
                          </div>
                        </article>
                      )
                    })}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* ========================================================================= */}
          {/* TAB 2: COURSES / LESSONS FULL VIEW */}
          {/* ========================================================================= */}
          {section === "courses" && (
            <section className="mt-8 space-y-6">
              <div className="flex flex-col gap-2">
                <p className="font-mono text-xs font-bold tracking-wider text-accent uppercase">
                  MASTERCLASS CURRICULUM
                </p>
                <h2 className="text-4xl font-black tracking-[-0.05em] md:text-5xl">Your Course Lessons</h2>
                <p className="font-mono text-sm text-muted-foreground">
                  Access every module, playback session, and step-by-step breakdown.
                </p>
              </div>

              {loadingLessons ? (
                <div className="mt-8 flex flex-col items-center justify-center py-16 text-center">
                  <Loader2 className="size-8 animate-spin text-accent" />
                  <p className="mt-3 font-mono text-xs text-muted-foreground uppercase tracking-wider">Loading Course Lessons...</p>
                </div>
              ) : lessons.length === 0 ? (
                <div className="mt-8 rounded-3xl border border-dashed border-border p-12 text-center">
                  <p className="font-mono text-base font-bold text-foreground">No videos found</p>
                  <p className="mt-1 font-mono text-xs text-muted-foreground">
                    Upload your course videos directly to VdoCipher to automatically populate this curriculum.
                  </p>
                </div>
              ) : (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  {lessons.map((lesson, idx) => {
                    const isLessonUnlocked = isPurchased || lesson.hasPreview
                    const isCompleted = idx < completedCount

                    return (
                      <article
                        key={lesson.id}
                        className="group flex flex-col justify-between overflow-hidden rounded-3xl border border-border bg-background p-5 shadow-sm transition-all hover:border-foreground/40 hover:shadow-md"
                      >
                        <div className="relative aspect-video overflow-hidden rounded-2xl bg-foreground">
                          <div className="absolute inset-0 bg-[linear-gradient(135deg,rgba(204,255,0,.15),transparent_60%)]" />
                          <div className="absolute left-3 top-3 flex items-center gap-1.5">
                            <span className="rounded-md bg-black/60 px-2 py-0.5 font-mono text-[10px] text-accent backdrop-blur font-bold">
                              0{lesson.order || idx + 1}
                            </span>
                          </div>

                          {isLessonUnlocked ? (
                            <Link
                              href={`/dashboard/course/${lesson.id}`}
                              className="absolute inset-0 m-auto grid size-12 place-items-center rounded-full border border-accent bg-accent text-accent-foreground transition-transform group-hover:scale-110 active:scale-95"
                            >
                              <Play className="size-5 fill-current ml-0.5" />
                            </Link>
                          ) : (
                            <div className="absolute inset-0 m-auto grid size-12 place-items-center rounded-full bg-black/60 border border-white/20 text-white">
                              <Lock className="size-5" />
                            </div>
                          )}
                        </div>

                        <div className="mt-4 flex flex-1 flex-col justify-between">
                          <div className="space-y-1">
                            <div className="flex items-center justify-between">
                              <p className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">
                                LESSON 0{lesson.order || idx + 1}
                              </p>
                              {isCompleted && (
                                <span className="inline-flex items-center gap-1 text-[10px] font-mono font-bold text-emerald-600">
                                  <CheckCircle className="size-3" /> COMPLETED
                                </span>
                              )}
                            </div>
                            <h3 className="font-bold text-base text-foreground">{lesson.title}</h3>
                            <p className="font-mono text-xs text-muted-foreground">{lesson.description}</p>
                          </div>

                          <div className="mt-5 pt-4 border-t border-border">
                            {isLessonUnlocked ? (
                              <Link
                                href={`/dashboard/course/${lesson.id}`}
                                className="inline-flex w-full items-center justify-center gap-2 rounded-full bg-foreground px-4 py-2.5 font-mono text-xs font-bold text-background transition-transform hover:scale-[1.02]"
                              >
                                <Play className="size-3 fill-current text-accent" /> PLAY NOW
                              </Link>
                            ) : (
                              <Link
                                href="/buy"
                                className="inline-flex w-full items-center justify-center gap-2 rounded-full border border-foreground/30 px-4 py-2.5 font-mono text-xs font-bold text-foreground hover:bg-foreground hover:text-background"
                              >
                                <Lock className="size-3" /> BUY FULL COURSE
                              </Link>
                            )}
                          </div>
                        </div>
                      </article>
                    )
                  })}
                </div>
              )}
            </section>
          )}

          {/* ========================================================================= */}
          {/* ========================================================================= */}
          {/* TAB 3: FREE RESOURCES */}
          {/* ========================================================================= */}
          {section === "resources" && (() => {
            const publishedList = dbResources.filter((r) => r.status === "published")

            const filteredList = publishedList.filter((r) => {
              if (resourceFilter === "ALL") return true
              if (resourceFilter === "EXCEL") {
                const t = (r.type || "").toLowerCase()
                const fn = (r.fileName || "").toLowerCase()
                return t.includes("excel") || t.includes("spreadsheet") || fn.endsWith(".xlsx") || fn.endsWith(".xls") || fn.endsWith(".csv")
              }
              if (resourceFilter === "PDF") {
                const t = (r.type || "").toLowerCase()
                const fn = (r.fileName || "").toLowerCase()
                return t.includes("pdf") || fn.endsWith(".pdf")
              }
              if (resourceFilter === "TEMPLATE") {
                const t = (r.type || "").toLowerCase()
                return t.includes("archive") || t.includes("template") || t.includes("doc")
              }
              return true
            })

            return (
              <section className="mt-8 space-y-6">
                <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
                  <div className="space-y-2">
                    <p className="font-mono text-xs font-bold tracking-wider text-accent uppercase">
                      FREE TOOLKIT & ASSETS
                    </p>
                    <h2 className="text-4xl font-black tracking-[-0.05em] md:text-5xl">Free Resources</h2>
                    <p className="max-w-2xl font-mono text-sm text-muted-foreground">
                      High-converting swipe files, Excel trackers, budget calculators, and plug-and-play templates.
                    </p>
                  </div>

                  {/* Filter chips - only show if there are resources */}
                  {publishedList.length > 0 && (
                    <div className="flex flex-wrap gap-2">
                      {[
                        { key: "ALL", label: "All Assets" },
                        { key: "EXCEL", label: "Excel & Sheets" },
                        { key: "PDF", label: "PDF Guides" },
                        { key: "TEMPLATE", label: "Templates" },
                      ].map((tab) => (
                        <button
                          key={tab.key}
                          onClick={() => setResourceFilter(tab.key)}
                          className={cn(
                            "rounded-full px-3.5 py-1.5 font-mono text-xs font-bold transition-all",
                            resourceFilter === tab.key
                              ? "bg-foreground text-background shadow-sm"
                              : "border border-border bg-background text-muted-foreground hover:border-foreground/40 hover:text-foreground"
                          )}
                        >
                          {tab.label}
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                {/* Resource List / Empty State */}
                {loadingResources ? (
                  <div className="flex flex-col items-center justify-center py-20 gap-3">
                    <Loader2 className="size-8 animate-spin text-accent" />
                    <p className="font-mono text-xs text-muted-foreground">Checking for available resources...</p>
                  </div>
                ) : filteredList.length === 0 ? (
                  <div className="rounded-3xl border border-dashed border-border bg-background p-12 text-center flex flex-col items-center justify-center max-w-lg mx-auto shadow-sm">
                    <div className="grid size-14 place-items-center rounded-2xl bg-muted text-muted-foreground mb-4">
                      <FileText className="size-7 text-muted-foreground/60" />
                    </div>
                    <h3 className="text-xl font-black tracking-tight text-foreground">
                      No Free Resources Available Yet
                    </h3>
                    <p className="mt-2 max-w-md font-mono text-xs text-muted-foreground leading-relaxed">
                      {publishedList.length === 0
                        ? "Our team is actively preparing verified swipe files, spreadsheets, and guides. Please check back soon—new resources will appear here automatically!"
                        : `No resources found under the "${resourceFilter}" filter. Try selecting "All Assets" to view other available files.`}
                    </p>
                    {publishedList.length > 0 && resourceFilter !== "ALL" && (
                      <button
                        onClick={() => setResourceFilter("ALL")}
                        className="mt-5 inline-flex items-center gap-2 rounded-full bg-foreground px-5 py-2 font-mono text-xs font-bold text-background transition-transform hover:scale-105"
                      >
                        VIEW ALL RESOURCES
                      </button>
                    )}
                  </div>
                ) : (
                  <div className="grid gap-4 md:grid-cols-2">
                    {filteredList.map((item) => {
                      const isDownloading = downloadingId === item.id
                      const isDownloaded = downloadedIds.includes(item.id)
                      const isExcel =
                        item.type === "spreadsheet" ||
                        item.fileName?.toLowerCase().endsWith(".xlsx") ||
                        item.fileName?.toLowerCase().endsWith(".xls") ||
                        item.fileName?.toLowerCase().endsWith(".csv")
                      const isPdf = item.type === "pdf" || item.fileName?.toLowerCase().endsWith(".pdf")
                      const formattedSize = formatFileSize(item.fileSize)

                      return (
                        <article
                          key={item.id}
                          className="flex flex-col justify-between gap-5 rounded-3xl border border-border bg-background p-6 shadow-sm transition-all hover:border-foreground/30 hover:shadow-md"
                        >
                          <div className="space-y-3">
                            <div className="flex items-center justify-between gap-2">
                              <div className="flex items-center gap-2">
                                <div
                                  className={cn(
                                    "grid size-8 place-items-center rounded-lg text-xs font-bold",
                                    isExcel
                                      ? "bg-emerald-500/15 text-emerald-600 dark:text-emerald-400"
                                      : isPdf
                                      ? "bg-rose-500/15 text-rose-600 dark:text-rose-400"
                                      : "bg-accent/20 text-accent-foreground"
                                  )}
                                >
                                  {isExcel ? (
                                    <FileSpreadsheet className="size-4" />
                                  ) : isPdf ? (
                                    <FileText className="size-4" />
                                  ) : (
                                    <File className="size-4" />
                                  )}
                                </div>
                                <span
                                  className={cn(
                                    "rounded-md px-2 py-0.5 font-mono text-[10px] font-bold uppercase tracking-wider",
                                    isExcel
                                      ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                                      : isPdf
                                      ? "bg-rose-50 text-rose-700 border border-rose-200"
                                      : "bg-muted text-muted-foreground border border-border"
                                  )}
                                >
                                  {isExcel ? "EXCEL SHEET" : isPdf ? "PDF GUIDE" : item.type.toUpperCase()}
                                </span>
                              </div>

                              {formattedSize && (
                                <span className="font-mono text-[11px] font-bold text-muted-foreground">
                                  {formattedSize}
                                </span>
                              )}
                            </div>

                            <div>
                              <h3 className="text-xl font-black tracking-tight text-foreground">{item.title}</h3>
                              <p className="mt-1.5 font-mono text-xs text-muted-foreground leading-relaxed">
                                {item.description}
                              </p>
                            </div>

                            {item.fileName && (
                              <p className="truncate font-mono text-[11px] text-muted-foreground/80">
                                File: <span className="text-foreground font-semibold">{item.fileName}</span>
                              </p>
                            )}
                          </div>

                          <div className="flex items-center gap-2 pt-3 border-t border-border">
                            <button
                              onClick={() => handleDownloadResource(item.id, item.downloadUrl)}
                              disabled={isDownloading}
                              className={cn(
                                "flex-1 inline-flex items-center justify-center gap-2 rounded-full py-2.5 px-4 font-mono text-xs font-bold transition-all",
                                isDownloaded
                                  ? "bg-emerald-600 text-white"
                                  : "bg-foreground text-background hover:opacity-90 active:scale-[0.98]"
                              )}
                            >
                              {isDownloading ? (
                                <>
                                  <Loader2 className="size-3.5 animate-spin" /> DOWNLOADING...
                                </>
                              ) : isDownloaded ? (
                                <>
                                  <CheckCircle className="size-3.5" /> DOWNLOADED
                                </>
                              ) : (
                                <>
                                  <Download className="size-3.5" /> DOWNLOAD
                                </>
                              )}
                            </button>

                            {item.downloadUrl && (
                              <a
                                href={item.downloadUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-flex items-center justify-center gap-1.5 rounded-full border border-border bg-background px-3.5 py-2.5 font-mono text-xs font-bold text-foreground hover:bg-muted transition-colors"
                                title="Open or preview in new tab"
                              >
                                <ExternalLink className="size-3.5" />
                                VIEW
                              </a>
                            )}
                          </div>
                        </article>
                      )
                    })}
                  </div>
                )}
              </section>
            )
          })()}

          {/* ========================================================================= */}
          {/* TAB 4: ACCOUNT SETTINGS & PROFILE */}
          {/* ========================================================================= */}
          {section === "account" && (
            <section className="mt-8 max-w-xl space-y-6">
              <div>
                <p className="font-mono text-xs font-bold tracking-wider text-accent uppercase">
                  PROFILE & SECURITY
                </p>
                <h2 className="text-4xl font-black tracking-[-0.05em] md:text-5xl">Your Account</h2>
              </div>

              <div className="grid gap-5 rounded-3xl border border-border bg-background p-6 md:p-8 shadow-sm">
                <div>
                  <p className="font-mono text-[10px] tracking-wider text-muted-foreground uppercase">FULL NAME</p>
                  <p className="mt-1.5 font-bold text-lg">{user?.name || "Student"}</p>
                </div>
                <div>
                  <p className="font-mono text-[10px] tracking-wider text-muted-foreground uppercase">EMAIL ADDRESS</p>
                  <p className="mt-1.5 font-bold text-lg">{user?.email || "student@example.com"}</p>
                </div>
                <div>
                  <p className="font-mono text-[10px] tracking-wider text-muted-foreground uppercase">PHONE NUMBER</p>
                  <p className="mt-1.5 font-bold text-lg">{user?.phone || "Not Set"}</p>
                </div>
                <div>
                  <p className="font-mono text-[10px] tracking-wider text-muted-foreground uppercase">ACCOUNT STATUS</p>
                  <div className="mt-2">
                    <span
                      className={cn(
                        "rounded-full px-3 py-1 font-mono text-[10px] font-bold uppercase",
                        isPurchased ? "bg-emerald-500/15 text-emerald-700 border border-emerald-500/30" : "bg-amber-500/15 text-amber-700 border border-amber-500/30"
                      )}
                    >
                      {isPurchased ? "PURCHASED (LIFETIME ACCESS)" : "FREE TIER (PREVIEW)"}
                    </span>
                  </div>
                </div>

                {!isPurchased && (
                  <div className="pt-2">
                    <Link
                      href="/buy"
                      className="inline-flex w-full items-center justify-center gap-2 rounded-full bg-foreground px-6 py-3.5 font-mono text-xs font-bold text-background transition-transform hover:scale-[1.01]"
                    >
                      UPGRADE TO PRO <ArrowRight className="size-4 text-accent" />
                    </Link>
                  </div>
                )}

                <div className="pt-4 border-t border-border">
                  <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 font-mono text-xs text-red-600 hover:underline"
                  >
                    <LogOut className="size-3.5" /> Log Out of Platform
                  </button>
                </div>
              </div>
            </section>
          )}
        </div>
      </div>

      {/* Mobile Bottom Navigation Bar */}
      <nav
        className="fixed inset-x-0 bottom-0 z-40 grid grid-cols-4 border-t border-border bg-background px-2 pb-[calc(.5rem+env(safe-area-inset-bottom))] pt-2 shadow-[0_-4px_18px_rgba(0,0,0,.08)] lg:hidden"
        aria-label="Mobile dashboard navigation"
      >
        {([
          ["overview", "HOME", Flame],
          ["courses", "LESSONS", BookOpen],
          ["resources", "FREE", Download],
          ["account", "ACCOUNT", UserRound],
        ] as const).map(([key, label, Icon]) => (
          <button
            key={key}
            onClick={() => setSection(key)}
            className={cn(
              "flex min-h-12 flex-col items-center justify-center gap-1 rounded-xl px-1 py-1 font-mono text-[9px] transition-colors",
              section === key
                ? "bg-foreground font-bold text-background"
                : "text-muted-foreground hover:bg-muted hover:text-foreground"
            )}
          >
            <Icon className="size-4" />
            {label}
          </button>
        ))}
      </nav>
    </main>
  )
}

"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { ArrowLeft, ArrowRight, CheckCircle, Lock, Play, Loader2, AlertCircle, RefreshCw } from "lucide-react"
import { useAuth } from "@/lib/auth"
import { mockLessons, mockCourseProgress } from "@/mocks/seed-data"
import { getCourseLessons, getVideoPlayback, updateVideoProgress, getCourseProgress } from "@/lib/api/course-service"
import type { CourseLesson, CourseProgress } from "@/lib/types"

interface PlaybackCredentials {
  otp: string
  playbackInfo: string
}

export function CoursePlayerView({ lessonId }: { lessonId: string }) {
  const { user } = useAuth()
  const isPaid = user?.type === "paid" || Boolean((user as any)?.coursePurchased)

  const [lessons, setLessons] = useState<CourseLesson[]>(mockLessons)
  const [loadingLessons, setLoadingLessons] = useState(true)
  const [playback, setPlayback] = useState<PlaybackCredentials | null>(null)
  const [playbackLoading, setPlaybackLoading] = useState(true)
  const [playbackError, setPlaybackError] = useState<string | null>(null)
  const [progress, setProgress] = useState<CourseProgress>(mockCourseProgress)

  // 1. Fetch real course lessons and user course progress
  useEffect(() => {
    async function loadData() {
      setLoadingLessons(true)
      try {
        const [lessonsRes, progressRes] = await Promise.all([
          getCourseLessons("1"),
          getCourseProgress("1"),
        ])

        if (lessonsRes.success && lessonsRes.data && lessonsRes.data.length > 0) {
          setLessons(lessonsRes.data)
        }
        if (progressRes.success && progressRes.data) {
          setProgress(progressRes.data)
        }
      } catch (err) {
        console.error("Failed to load lessons:", err)
      } finally {
        setLoadingLessons(false)
      }
    }

    loadData()
  }, [])

  // Find active, previous, and next lesson
  const currentIndex = lessons.findIndex((l) => String(l.id) === String(lessonId))
  const currentLesson = currentIndex >= 0 ? lessons[currentIndex] : lessons[0]
  const prevLesson = currentIndex > 0 ? lessons[currentIndex - 1] : null
  const nextLesson = currentIndex < lessons.length - 1 ? lessons[currentIndex + 1] : null

  const isUnlocked = isPaid || Boolean(currentLesson?.hasPreview)

  // 2. Fetch VdoCipher OTP credentials for the current lesson
  const fetchPlaybackCredentials = async () => {
    if (!currentLesson?.id) return

    setPlaybackLoading(true)
    setPlaybackError(null)

    try {
      const res = await getVideoPlayback(currentLesson.id)
      if (res.success && res.data?.otp && res.data?.playbackInfo) {
        setPlayback({
          otp: res.data.otp,
          playbackInfo: res.data.playbackInfo,
        })
      } else {
        setPlaybackError(res.error || "Failed to load secure video playback.")
      }
    } catch (err: any) {
      setPlaybackError(err?.message || "An unexpected error occurred while loading the video stream.")
    } finally {
      setPlaybackLoading(false)
    }
  }

  useEffect(() => {
    if (isUnlocked && currentLesson?.id) {
      fetchPlaybackCredentials()
    } else {
      setPlayback(null)
      setPlaybackLoading(false)
    }
  }, [currentLesson?.id, isUnlocked])

  const handleMarkComplete = async () => {
    if (!currentLesson?.id) return
    try {
      await updateVideoProgress(currentLesson.id, currentLesson.duration || 600, currentLesson.duration || 600, true)
    } catch (err) {
      console.error("Failed to update progress:", err)
    }
  }

  return (
    <main className="min-h-screen bg-[#f5f4ef] text-foreground">
      {/* Top Header */}
      <header className="border-b border-border bg-background px-5 py-4 md:px-8">
        <div className="mx-auto flex max-w-7xl items-center justify-between">
          <Link
            href="/dashboard"
            className="inline-flex items-center gap-2 font-mono text-xs text-muted-foreground hover:text-foreground transition-colors"
          >
            <ArrowLeft className="size-4" /> BACK TO DASHBOARD
          </Link>
          <div className="flex items-center gap-3">
            <span className="font-mono text-xs tracking-wider text-muted-foreground">
              LESSON {String(currentLesson?.order || 1).padStart(2, "0")} / {String(lessons.length).padStart(2, "0")}
            </span>
            <span className={`rounded-full px-3 py-1 font-mono text-[10px] font-bold ${
              isUnlocked ? "bg-accent text-accent-foreground" : "bg-muted text-muted-foreground"
            }`}>
              {isPaid ? "UNLOCKED · FULL ACCESS" : currentLesson?.hasPreview ? "FREE PREVIEW" : "LOCKED"}
            </span>
          </div>
        </div>
      </header>

      {/* Main Player Area */}
      <div className="mx-auto max-w-7xl px-5 py-8 md:px-8">
        <div className="grid gap-8 lg:grid-cols-[1fr_360px]">
          {/* Video & Current Lesson */}
          <div>
            {/* Video Player Frame */}
            <div className="relative aspect-video w-full overflow-hidden rounded-3xl border border-border bg-black shadow-2xl">
              {isUnlocked ? (
                playbackLoading ? (
                  <div className="absolute inset-0 flex flex-col items-center justify-center gap-3 bg-black text-white">
                    <Loader2 className="size-8 animate-spin text-accent" />
                    <p className="font-mono text-xs uppercase tracking-widest text-accent">Initializing Secure Stream</p>
                    <p className="text-xs text-white/60">Generating dynamic encrypted DRM OTP...</p>
                  </div>
                ) : playbackError ? (
                  <div className="absolute inset-0 flex flex-col items-center justify-center gap-4 bg-black p-6 text-center text-white">
                    <AlertCircle className="size-10 text-destructive" />
                    <h3 className="text-lg font-bold">Playback Error</h3>
                    <p className="max-w-md font-mono text-xs text-white/70">{playbackError}</p>
                    <button
                      onClick={fetchPlaybackCredentials}
                      className="inline-flex items-center gap-2 rounded-xl bg-accent px-4 py-2 font-mono text-xs font-bold text-accent-foreground"
                    >
                      <RefreshCw className="size-3.5" /> Retry Playback
                    </button>
                  </div>
                ) : playback ? (
                  /* Official VdoCipher Secure Player Embed */
                  <iframe
                    src={`https://player.vdocipher.com/v2/?otp=${encodeURIComponent(playback.otp)}&playbackInfo=${encodeURIComponent(playback.playbackInfo)}`}
                    className="absolute inset-0 size-full border-0"
                    allow="encrypted-media; autoplay; fullscreen; picture-in-picture"
                    allowFullScreen
                    title={currentLesson?.title || "Video Lesson"}
                  />
                ) : (
                  <div className="absolute inset-0 flex flex-col items-center justify-center bg-black p-6 text-center text-white">
                    <Play className="size-10 text-accent" />
                    <p className="mt-3 font-mono text-xs text-white/70">Click to start lesson playback.</p>
                  </div>
                )
              ) : (
                /* Locked Lesson Overlay */
                <div className="absolute inset-0 flex flex-col items-center justify-center bg-black/90 p-6 text-center text-white">
                  <div className="grid size-16 place-items-center rounded-full bg-muted/20 text-muted-foreground border border-white/10">
                    <Lock className="size-7 text-accent" />
                  </div>
                  <h3 className="mt-4 text-2xl font-black text-white">Full Lesson Locked</h3>
                  <p className="mt-2 max-w-sm font-mono text-xs text-white/70">
                    Enroll in the course to unlock this lesson, high-res downloads, and all premium masterclass modules.
                  </p>
                  <Link
                    href="/buy"
                    className="mt-5 rounded-full bg-accent px-6 py-3 font-mono text-xs font-bold text-accent-foreground transition-transform hover:scale-105"
                  >
                    UNLOCK FULL COURSE
                  </Link>
                </div>
              )}
            </div>

            {/* Lesson Details */}
            <div className="mt-6 rounded-3xl border border-border bg-background p-6 md:p-8">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <span className="font-mono text-xs tracking-[.2em] text-accent font-bold">
                  MODULE 0{currentLesson?.order || 1}
                </span>
                {isUnlocked && (
                  <span className="inline-flex items-center gap-1.5 font-mono text-xs text-accent">
                    <CheckCircle className="size-4" /> Lesson Available
                  </span>
                )}
              </div>
              <h1 className="mt-2 text-3xl font-black tracking-[-.05em] md:text-4xl">
                {currentLesson?.title}
              </h1>
              <p className="mt-4 font-mono text-sm leading-6 text-muted-foreground">
                {currentLesson?.description || "Master the practical framework for creating high-converting creative ad variations and hook hooks."}
              </p>

              {/* Navigation Actions */}
              <div className="mt-8 flex flex-wrap items-center justify-between gap-4 border-t border-border pt-6">
                {prevLesson ? (
                  <Link
                    href={`/dashboard/course/${prevLesson.id}`}
                    className="inline-flex items-center gap-2 font-mono text-xs font-bold text-muted-foreground hover:text-foreground transition-colors"
                  >
                    <ArrowLeft className="size-4" /> PREVIOUS: {prevLesson.title}
                  </Link>
                ) : (
                  <span className="font-mono text-xs text-muted-foreground/50">START OF COURSE</span>
                )}

                {nextLesson ? (
                  <Link
                    href={`/dashboard/course/${nextLesson.id}`}
                    onClick={handleMarkComplete}
                    className="inline-flex items-center gap-2 rounded-full bg-foreground px-5 py-3 font-mono text-xs font-bold text-background transition-transform hover:scale-[1.02] active:scale-[0.98]"
                  >
                    NEXT: {nextLesson.title} <ArrowRight className="size-4" />
                  </Link>
                ) : (
                  <Link
                    href="/dashboard"
                    onClick={handleMarkComplete}
                    className="inline-flex items-center gap-2 rounded-full bg-accent px-5 py-3 font-mono text-xs font-bold text-accent-foreground transition-transform hover:scale-[1.02]"
                  >
                    COMPLETE COURSE <CheckCircle className="size-4" />
                  </Link>
                )}
              </div>
            </div>
          </div>

          {/* Sidebar Lesson List */}
          <aside className="rounded-3xl border border-border bg-background p-6">
            <h2 className="font-mono text-xs font-bold tracking-widest text-muted-foreground">
              COURSE CURRICULUM
            </h2>
            <div className="mt-4 flex items-center justify-between text-xs">
              <span>Overall Progress</span>
              <span className="font-mono font-bold text-accent">{isPaid ? `${progress.percentage || 33}%` : "0%"}</span>
            </div>
            <div className="mt-2 h-1.5 rounded-full bg-muted">
              <div
                className="h-full rounded-full bg-accent"
                style={{ width: isPaid ? `${progress.percentage || 33}%` : "0%" }}
              />
            </div>

            <div className="mt-6 flex flex-col gap-2">
              {lessons.map((lesson) => {
                const isActive = String(lesson.id) === String(currentLesson?.id)
                const canAccess = isPaid || Boolean(lesson.hasPreview)

                return (
                  <Link
                    key={lesson.id}
                    href={`/dashboard/course/${lesson.id}`}
                    className={`flex items-center justify-between rounded-2xl border p-4 transition-colors ${
                      isActive
                        ? "border-accent bg-accent/10 font-bold"
                        : "border-border hover:border-foreground/40"
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <span className="font-mono text-xs text-muted-foreground">
                        0{lesson.order}
                      </span>
                      <div>
                        <p className="text-sm font-semibold">{lesson.title}</p>
                        <p className="font-mono text-[10px] text-muted-foreground">
                          {canAccess ? (lesson.hasPreview && !isPaid ? "Free Preview" : "Full Lesson") : "Locked"}
                        </p>
                      </div>
                    </div>
                    {canAccess ? (
                      <Play className="size-4 text-accent fill-current" />
                    ) : (
                      <Lock className="size-3.5 text-muted-foreground" />
                    )}
                  </Link>
                )
              })}
            </div>
          </aside>
        </div>
      </div>
    </main>
  )
}

"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import {
  ArrowLeft,
  ArrowRight,
  CheckCircle,
  Download,
  ExternalLink,
  File,
  FileSpreadsheet,
  FileText,
  Loader2,
  Lock,
} from "lucide-react"
import { useAuth } from "@/lib/auth"
import { resourceService } from "@/lib/api"
import type { FreeResource } from "@/lib/types"
import { cn } from "@/lib/utils"

export default function FreeResourcesPage() {
  const { user } = useAuth()
  const hasAccount = Boolean(user?.email && user?.name)

  const [resources, setResources] = useState<FreeResource[]>([])
  const [loading, setLoading] = useState(true)
  const [downloadingId, setDownloadingId] = useState<string | null>(null)
  const [downloadedIds, setDownloadedIds] = useState<string[]>([])

  useEffect(() => {
    async function fetchResources() {
      try {
        const res = await resourceService.getResources()
        if (res.success && Array.isArray(res.data)) {
          setResources(res.data)
        } else {
          setResources([])
        }
      } catch (err) {
        console.error("Failed to fetch resources:", err)
        setResources([])
      } finally {
        setLoading(false)
      }
    }
    fetchResources()
  }, [])

  const handleDownload = async (item: FreeResource) => {
    setDownloadingId(item.id)
    try {
      const res = await resourceService.getResourceDownloadUrl(item.id)
      const targetUrl = (res.success && res.data) ? res.data : item.downloadUrl
      if (targetUrl) {
        setDownloadedIds((prev) => [...prev, item.id])
        window.open(targetUrl, "_blank", "noopener,noreferrer")
      }
    } catch (err) {
      console.error("Failed to download resource:", err)
      if (item.downloadUrl) {
        window.open(item.downloadUrl, "_blank", "noopener,noreferrer")
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

  if (!hasAccount) {
    return (
      <main className="min-h-screen bg-background px-5 py-6 md:px-8 md:py-8">
        <div className="mx-auto flex min-h-[calc(100vh-3rem)] max-w-md flex-col justify-center">
          <Link
            href="/"
            className="mb-8 inline-flex items-center gap-2 font-mono text-xs text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="size-4" /> BACK TO ADFIX STUDIO
          </Link>
          <Lock className="size-8 text-accent" />
          <h1 className="mt-5 text-5xl font-black leading-none tracking-[-.07em]">
            Free resources, made for your next ad.
          </h1>
          <p className="mt-4 font-mono text-sm leading-6 text-muted-foreground">
            Create a free AdFix Studio account to access swipe files, templates, calculators, and practical resources.
          </p>
          <div className="mt-7 flex flex-col gap-3">
            <Link
              href="/login"
              className="flex items-center justify-between rounded-2xl bg-foreground px-5 py-4 font-bold text-background"
            >
              LOGIN <ArrowRight className="size-5" />
            </Link>
            <Link
              href="/create-account"
              className="flex items-center justify-between rounded-2xl border border-foreground px-5 py-4 font-bold"
            >
              CREATE FREE ACCOUNT <ArrowRight className="size-5" />
            </Link>
          </div>
        </div>
      </main>
    )
  }

  const publishedResources = resources.filter((r) => r.status === "published")

  return (
    <main className="min-h-screen bg-[#f5f4ef] px-5 py-6 md:px-8 md:py-12">
      <div className="mx-auto max-w-6xl">
        <div className="flex items-center justify-between">
          <Link
            href="/dashboard"
            className="inline-flex items-center gap-2 font-mono text-xs font-bold text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="size-4" /> BACK TO DASHBOARD
          </Link>
          <span className="font-mono text-xs uppercase tracking-widest text-accent font-bold">
            FREE COMMUNITY TOOLKIT
          </span>
        </div>

        <section className="py-12 md:py-16">
          <p className="font-mono text-xs tracking-[.2em] text-accent font-bold">ADFIX STUDIO / ASSETS</p>
          <h1 className="mt-3 max-w-3xl text-5xl font-black leading-[.95] tracking-[-.07em] md:text-7xl">
            Start making better ads.
          </h1>
          <p className="mt-4 max-w-xl font-mono text-sm leading-6 text-muted-foreground">
            Directly download high-converting frameworks, Excel trackers, budget calculators, and tested swipe files.
          </p>
        </section>

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <Loader2 className="size-8 animate-spin text-accent" />
          </div>
        ) : publishedResources.length === 0 ? (
          <div className="my-8 rounded-3xl border border-dashed border-border bg-background p-12 text-center flex flex-col items-center justify-center max-w-lg mx-auto shadow-sm">
            <div className="grid size-14 place-items-center rounded-2xl bg-muted text-muted-foreground mb-4">
              <FileText className="size-7 text-muted-foreground/60" />
            </div>
            <h3 className="text-xl font-black tracking-tight text-foreground">
              No Free Resources Available Yet
            </h3>
            <p className="mt-2 max-w-md font-mono text-xs text-muted-foreground leading-relaxed">
              Our team is actively preparing verified swipe files, spreadsheets, and guides. Please check back soon—new resources will appear here as soon as they are published!
            </p>
          </div>
        ) : (
          <section className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {publishedResources.map((item, index) => {
              const isExcel =
                item.type === "spreadsheet" ||
                item.fileName?.toLowerCase().endsWith(".xlsx") ||
                item.fileName?.toLowerCase().endsWith(".xls") ||
                item.fileName?.toLowerCase().endsWith(".csv")
              const isPdf = item.type === "pdf" || item.fileName?.toLowerCase().endsWith(".pdf")
              const formattedSize = formatFileSize(item.fileSize)
              const isDownloading = downloadingId === item.id
              const isDownloaded = downloadedIds.includes(item.id)

              return (
                <article
                  key={item.id}
                  className="flex flex-col justify-between rounded-3xl border border-border bg-background p-6 shadow-sm transition-all hover:border-foreground/30 hover:shadow-md"
                >
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div
                          className={cn(
                            "grid size-8 place-items-center rounded-lg text-xs font-bold",
                            isExcel
                              ? "bg-emerald-500/15 text-emerald-600"
                              : isPdf
                              ? "bg-rose-500/15 text-rose-600"
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
                        <span className="font-mono text-xs font-bold text-accent">0{index + 1}</span>
                      </div>

                      {formattedSize && (
                        <span className="font-mono text-[11px] font-bold text-muted-foreground">
                          {formattedSize}
                        </span>
                      )}
                    </div>

                    <h2 className="text-2xl font-black tracking-tight">{item.title}</h2>
                    <p className="font-mono text-xs leading-relaxed text-muted-foreground">
                      {item.description}
                    </p>

                    {item.fileName && (
                      <p className="truncate font-mono text-[11px] text-muted-foreground/80">
                        {item.fileName}
                      </p>
                    )}
                  </div>

                  <div className="mt-6 flex items-center gap-2 pt-4 border-t border-border">
                    <button
                      onClick={() => handleDownload(item)}
                      disabled={isDownloading}
                      className={cn(
                        "flex-1 inline-flex items-center justify-center gap-2 rounded-full px-4 py-2.5 font-mono text-xs font-bold transition-all",
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
                        className="inline-flex items-center justify-center gap-1.5 rounded-full border border-border bg-background px-3 py-2.5 font-mono text-xs font-bold text-foreground hover:bg-muted"
                        title="View or preview"
                      >
                        <ExternalLink className="size-3.5" />
                      </a>
                    )}
                  </div>
                </article>
              )
            })}
          </section>
        )}
      </div>
    </main>
  )
}

"use client"

import Link from "next/link"
import { useEffect, useMemo, useState } from "react"
import { 
  Plus, 
  RefreshCw, 
  FileText, 
  FileSpreadsheet, 
  FileArchive, 
  FileCode, 
  Link as LinkIcon, 
  File, 
  Download, 
  ExternalLink, 
  Trash2, 
  Eye, 
  EyeOff, 
  Check, 
  AlertCircle 
} from "lucide-react"
import { 
  getAdminResources, 
  updateAdminResourceStatus, 
  deleteAdminResource, 
  type AdminResourceItem 
} from "@/lib/api/resource-service"
import { SearchBox, Badge, ConfirmDialog, type ConfirmAction } from "@/components/admin/shell"
import { formatDate, cn } from "@/lib/utils"

const filterOptions = ["ALL", "PDF", "EXCEL", "TEMPLATE", "PUBLISHED", "DRAFT"] as const

function formatBytes(bytes?: number): string {
  if (!bytes || bytes <= 0) return "—"
  const k = 1024
  const sizes = ["Bytes", "KB", "MB", "GB"]
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i]
}

function getResourceIcon(type: string, fileName?: string) {
  const t = (type || "").toUpperCase()
  const fn = (fileName || "").toLowerCase()

  if (t === "PDF" || fn.endsWith(".pdf")) {
    return <FileText className="size-4 text-red-500" />
  }
  if (t === "EXCEL" || fn.endsWith(".xlsx") || fn.endsWith(".xls") || fn.endsWith(".csv")) {
    return <FileSpreadsheet className="size-4 text-emerald-500" />
  }
  if (t === "TEMPLATE" || fn.endsWith(".zip") || fn.endsWith(".rar")) {
    return <FileArchive className="size-4 text-amber-500" />
  }
  if (t === "CODE" || fn.endsWith(".js") || fn.endsWith(".ts") || fn.endsWith(".py")) {
    return <FileCode className="size-4 text-cyan-500" />
  }
  if (t === "LINK") {
    return <LinkIcon className="size-4 text-blue-500" />
  }
  return <File className="size-4 text-muted-foreground" />
}

export function AdminResourcesPage() {
  const [items, setItems] = useState<AdminResourceItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [query, setQuery] = useState("")
  const [filter, setFilter] = useState<string>("ALL")
  const [confirm, setConfirm] = useState<ConfirmAction>(null)
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null)

  const loadResources = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await getAdminResources()
      if (res.success && res.data) {
        setItems(res.data)
      } else {
        setError(res.error || "Failed to load resources from database")
      }
    } catch (err: any) {
      setError(err?.message || "Failed to connect to backend server")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadResources()
  }, [])

  const filtered = useMemo(() => {
    return items.filter((r) => {
      const typeUpper = (r.resourceType || "").toUpperCase()
      const statusUpper = (r.status || "").toUpperCase()

      const matchesFilter =
        filter === "ALL" ||
        (filter === "PDF" && (typeUpper === "PDF" || (r.fileName || "").toLowerCase().endsWith(".pdf"))) ||
        (filter === "EXCEL" && (typeUpper === "EXCEL" || (r.fileName || "").toLowerCase().endsWith(".xlsx") || (r.fileName || "").toLowerCase().endsWith(".csv"))) ||
        (filter === "TEMPLATE" && (typeUpper === "TEMPLATE" || (r.fileName || "").toLowerCase().endsWith(".zip"))) ||
        (filter === "PUBLISHED" && statusUpper === "PUBLISHED") ||
        (filter === "DRAFT" && statusUpper === "DRAFT")

      const q = query.toLowerCase()
      const matchesQuery =
        (r.title || "").toLowerCase().includes(q) ||
        (r.description || "").toLowerCase().includes(q) ||
        (r.fileName || "").toLowerCase().includes(q)

      return matchesFilter && matchesQuery
    })
  }, [items, filter, query])

  const handleToggleStatus = async (item: AdminResourceItem) => {
    const nextStatus = item.status === "PUBLISHED" ? "DRAFT" : "PUBLISHED"
    try {
      setActionLoadingId(item.id)
      const res = await updateAdminResourceStatus(item.id, nextStatus)
      if (res.success) {
        setItems((prev) =>
          prev.map((r) => (r.id === item.id ? { ...r, status: nextStatus } : r))
        )
      }
    } finally {
      setActionLoadingId(null)
    }
  }

  const handleDelete = (item: AdminResourceItem) => {
    setConfirm({
      title: "DELETE RESOURCE?",
      body: `Are you sure you want to permanently delete "${item.title}"? Any attached files will also be removed from the server.`,
      label: "DELETE RESOURCE",
      action: async () => {
        try {
          setActionLoadingId(item.id)
          const res = await deleteAdminResource(item.id)
          if (res.success) {
            setItems((prev) => prev.filter((r) => r.id !== item.id))
          }
        } finally {
          setActionLoadingId(null)
        }
      },
    })
  }

  return (
    <div className="flex flex-col gap-6">
      {/* Top Header */}
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="text-sm text-muted-foreground">
            Manage AdFix Studio free resources, downloadable swipe files, and templates.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadResources}
            disabled={loading}
            className="inline-flex items-center justify-center gap-2 rounded-md border border-border px-3 py-2.5 font-mono text-[10px] tracking-widest text-foreground hover:bg-muted/30 disabled:opacity-50"
            title="Refresh Resources"
          >
            <RefreshCw size={12} className={loading ? "animate-spin" : ""} /> REFRESH
          </button>
          <Link
            href="/admin/resources/new"
            className="inline-flex items-center justify-center gap-2 rounded-md bg-foreground px-4 py-2.5 font-mono text-[10px] tracking-widest text-background hover:opacity-90"
          >
            <Plus size={14} /> ADD FREE RESOURCE
          </Link>
        </div>
      </div>

      {/* Filter and Search */}
      <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <SearchBox value={query} onChange={setQuery} placeholder="SEARCH BY TITLE, DESCRIPTION OR FILENAME" />
        <div className="flex flex-wrap gap-2">
          {filterOptions.map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={cn(
                "rounded-md border px-3 py-2 font-mono text-[10px] tracking-widest transition",
                filter === f ? "bg-foreground text-background" : "border-border text-muted-foreground hover:text-foreground"
              )}
            >
              {f}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-md border border-red-500/30 bg-red-500/10 p-4 text-xs font-mono text-red-400">
          <AlertCircle size={15} /> {error}
        </div>
      )}

      {/* Resources Table */}
      <div className="overflow-hidden rounded-lg border border-border bg-card">
        {loading && items.length === 0 ? (
          <div className="flex items-center justify-center py-16">
            <RefreshCw size={18} className="animate-spin text-muted-foreground" />
          </div>
        ) : filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <p className="font-mono text-xs text-muted-foreground">NO FREE RESOURCES FOUND</p>
            <Link
              href="/admin/resources/new"
              className="mt-4 inline-flex items-center gap-1.5 rounded-md border border-border px-3 py-2 font-mono text-[10px] tracking-widest hover:bg-muted/30"
            >
              <Plus size={12} /> ADD FIRST RESOURCE
            </Link>
          </div>
        ) : (
          <div className="divide-y divide-border">
            {filtered.map((r) => {
              const isProcessing = actionLoadingId === r.id

              return (
                <div
                  key={r.id}
                  className="flex flex-col gap-4 p-5 transition-colors hover:bg-muted/20 md:flex-row md:items-center md:justify-between"
                >
                  <div className="flex items-start gap-3.5 flex-1 min-w-0">
                    <div className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-border bg-background">
                      {getResourceIcon(r.resourceType, r.fileName)}
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <h3 className="font-sans text-sm font-bold text-foreground truncate max-w-md">
                          {r.title}
                        </h3>
                        <span className="rounded bg-muted px-1.5 py-0.5 font-mono text-[9px] font-semibold text-muted-foreground uppercase">
                          {r.resourceType}
                        </span>
                        <Badge tone={r.status === "PUBLISHED" ? "good" : "default"}>
                          {r.status}
                        </Badge>
                      </div>

                      {r.description && (
                        <p className="mt-1 text-xs text-muted-foreground line-clamp-1">
                          {r.description}
                        </p>
                      )}

                      <div className="mt-1.5 flex flex-wrap items-center gap-3 font-mono text-[10px] text-muted-foreground">
                        {r.fileName && (
                          <span className="truncate max-w-[200px]" title={r.fileName}>
                            📎 {r.fileName}
                          </span>
                        )}
                        {r.fileSize ? <span>• {formatBytes(r.fileSize)}</span> : null}
                        <span>• {r.downloadCount} downloads</span>
                        <span>• Added {formatDate(r.createdAt)}</span>
                      </div>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-2 self-end md:self-center shrink-0">
                    {r.resourceUrl && (
                      <a
                        href={r.resourceUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="inline-flex items-center gap-1.5 rounded-md border border-border px-2.5 py-1.5 font-mono text-[10px] tracking-wider text-foreground hover:bg-muted/40 transition"
                        title="View / Download File"
                      >
                        <ExternalLink size={12} /> VIEW
                      </a>
                    )}

                    <button
                      onClick={() => handleToggleStatus(r)}
                      disabled={isProcessing}
                      className={cn(
                        "inline-flex items-center gap-1.5 rounded-md border px-2.5 py-1.5 font-mono text-[10px] tracking-wider transition disabled:opacity-50",
                        r.status === "PUBLISHED"
                          ? "border-amber-500/30 text-amber-400 hover:bg-amber-500/10"
                          : "border-emerald-500/30 text-emerald-400 hover:bg-emerald-500/10"
                      )}
                      title={r.status === "PUBLISHED" ? "Unpublish to Draft" : "Publish Resource"}
                    >
                      {r.status === "PUBLISHED" ? (
                        <>
                          <EyeOff size={12} /> UNPUBLISH
                        </>
                      ) : (
                        <>
                          <Eye size={12} /> PUBLISH
                        </>
                      )}
                    </button>

                    <button
                      onClick={() => handleDelete(r)}
                      disabled={isProcessing}
                      className="inline-flex items-center justify-center rounded-md border border-red-500/30 p-1.5 text-red-400 hover:bg-red-500/10 transition disabled:opacity-50"
                      title="Delete Resource"
                    >
                      <Trash2 size={13} />
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>

      <ConfirmDialog confirm={confirm} close={() => setConfirm(null)} />
    </div>
  )
}

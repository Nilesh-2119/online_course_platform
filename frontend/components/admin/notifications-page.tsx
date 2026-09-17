"use client"

import { useEffect, useMemo, useState } from "react"
import {
  Bell,
  Plus,
  RefreshCw,
  Search,
  Trash2,
  Eye,
  EyeOff,
  Flame,
  Zap,
  Sparkles,
  ExternalLink,
  X,
  AlertCircle,
  CheckCircle2,
  Calendar,
} from "lucide-react"
import {
  getAdminNotifications,
  createAdminNotification,
  toggleAdminNotification,
  deleteAdminNotification,
} from "@/lib/api/notification-service"
import type { AppNotification, CreateNotificationDto } from "@/lib/types"
import { Badge, ConfirmDialog, type ConfirmAction } from "@/components/admin/shell"
import { formatDate, cn } from "@/lib/utils"

const TAG_OPTIONS = ["ALL", "ANNOUNCEMENT", "LIVE EVENT", "UPDATE", "RESOURCE", "WORKSHOP"] as const

function getTagIcon(tag?: string) {
  const t = (tag || "").toUpperCase()
  if (t.includes("LIVE") || t.includes("EVENT") || t.includes("WORKSHOP")) {
    return <Flame className="size-4 text-orange-500" />
  }
  if (t.includes("RESOURCE")) {
    return <Sparkles className="size-4 text-yellow-500" />
  }
  if (t.includes("UPDATE")) {
    return <Zap className="size-4 text-emerald-500" />
  }
  return <Bell className="size-4 text-accent" />
}

export function AdminNotificationsPage() {
  const [items, setItems] = useState<AppNotification[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [query, setQuery] = useState("")
  const [selectedTag, setSelectedTag] = useState<string>("ALL")
  const [confirm, setConfirm] = useState<ConfirmAction>(null)
  const [actionLoadingId, setActionLoadingId] = useState<string | number | null>(null)

  // Create Modal State
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [formData, setFormData] = useState<CreateNotificationDto>({
    title: "",
    message: "",
    tag: "ANNOUNCEMENT",
    linkUrl: "",
    active: true,
    isNew: true,
  })

  const loadNotifications = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await getAdminNotifications()
      if (res.success && res.data) {
        setItems(res.data)
      } else {
        setError(res.error || "Failed to load notifications.")
      }
    } catch (err: any) {
      setError(err.message || "An unexpected error occurred while loading notifications.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadNotifications()
  }, [])

  const handleToggleActive = async (item: AppNotification) => {
    try {
      setActionLoadingId(item.id)
      const res = await toggleAdminNotification(item.id)
      if (res.success && res.data) {
        setItems((prev) =>
          prev.map((n) => (String(n.id) === String(item.id) ? { ...n, active: res.data!.active } : n))
        )
      } else {
        alert(res.error || "Failed to toggle notification status.")
      }
    } catch (err: any) {
      alert(err.message || "Failed to toggle notification status.")
    } finally {
      setActionLoadingId(null)
    }
  }

  const handleDelete = (item: AppNotification) => {
    setConfirm({
      title: "Delete Notification",
      body: `Are you sure you want to permanently delete notification "${item.title}"? It will immediately disappear from student dashboards.`,
      label: "Delete",
      action: async () => {
        try {
          setActionLoadingId(item.id)
          const res = await deleteAdminNotification(item.id)
          if (res.success) {
            setItems((prev) => prev.filter((n) => String(n.id) !== String(item.id)))
          } else {
            alert(res.error || "Failed to delete notification.")
          }
        } catch (err: any) {
          alert(err.message || "Failed to delete notification.")
        } finally {
          setActionLoadingId(null)
          setConfirm(null)
        }
      },
    })
  }

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!formData.title.trim()) {
      setFormError("Title is required.")
      return
    }
    if (!formData.message.trim()) {
      setFormError("Message content is required.")
      return
    }

    try {
      setSubmitting(true)
      setFormError(null)
      const res = await createAdminNotification(formData)
      if (res.success && res.data) {
        setItems((prev) => [res.data!, ...prev])
        setIsModalOpen(false)
        setFormData({
          title: "",
          message: "",
          tag: "ANNOUNCEMENT",
          linkUrl: "",
          active: true,
          isNew: true,
        })
      } else {
        setFormError(res.error || "Failed to create notification.")
      }
    } catch (err: any) {
      setFormError(err.message || "An unexpected error occurred.")
    } finally {
      setSubmitting(false)
    }
  }

  const filteredItems = useMemo(() => {
    return items.filter((item) => {
      const q = query.toLowerCase()
      const matchesQuery =
        item.title.toLowerCase().includes(q) ||
        item.message.toLowerCase().includes(q) ||
        (item.tag && item.tag.toLowerCase().includes(q))

      const matchesTag = selectedTag === "ALL" || (item.tag || "").toUpperCase() === selectedTag
      return matchesQuery && matchesTag
    })
  }, [items, query, selectedTag])

  const totalActive = items.filter((i) => i.active).length
  const totalInactive = items.length - totalActive

  return (
    <div className="flex flex-col gap-8 pb-12">
      {/* Page Header */}
      <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h2 className="text-2xl font-black tracking-tight">NOTIFICATIONS & ANNOUNCEMENTS</h2>
          <p className="mt-1 font-mono text-xs text-muted-foreground">
            Manage alerts and notifications displayed directly on student dashboards.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={loadNotifications}
            disabled={loading}
            className="inline-flex items-center gap-2 rounded-xl border border-border bg-background px-4 py-2.5 font-mono text-xs font-semibold text-muted-foreground hover:bg-muted hover:text-foreground transition-colors disabled:opacity-50"
          >
            <RefreshCw className={cn("size-3.5", loading && "animate-spin")} /> Refresh
          </button>
          <button
            onClick={() => {
              setFormError(null)
              setIsModalOpen(true)
            }}
            className="inline-flex items-center gap-2 rounded-xl bg-accent px-5 py-2.5 font-mono text-xs font-black text-accent-foreground shadow-sm hover:brightness-105 transition-all"
          >
            <Plus className="size-4" /> ADD NOTIFICATION
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="rounded-2xl border border-border bg-card p-5">
          <p className="font-mono text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
            Total Notifications
          </p>
          <p className="mt-2 text-3xl font-black tracking-tight">{items.length}</p>
          <p className="mt-1 font-mono text-[10px] text-muted-foreground">Across all categories</p>
        </div>
        <div className="rounded-2xl border border-border bg-card p-5">
          <div className="flex items-center justify-between">
            <p className="font-mono text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
              Active / Live
            </p>
            <span className="flex size-2 rounded-full bg-emerald-500 animate-pulse" />
          </div>
          <p className="mt-2 text-3xl font-black tracking-tight text-emerald-500">{totalActive}</p>
          <p className="mt-1 font-mono text-[10px] text-muted-foreground">Currently shown to students</p>
        </div>
        <div className="rounded-2xl border border-border bg-card p-5">
          <p className="font-mono text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
            Inactive / Hidden
          </p>
          <p className="mt-2 text-3xl font-black tracking-tight text-muted-foreground">{totalInactive}</p>
          <p className="mt-1 font-mono text-[10px] text-muted-foreground">Drafted or archived</p>
        </div>
      </div>

      {/* Controls & Filter Bar */}
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div className="relative max-w-sm flex-1">
          <Search className="absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <input
            type="text"
            placeholder="Search notifications..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="w-full rounded-xl border border-border bg-background py-2.5 pl-10 pr-4 font-mono text-xs focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
          />
        </div>

        <div className="flex flex-wrap items-center gap-1.5 overflow-x-auto">
          {TAG_OPTIONS.map((tag) => (
            <button
              key={tag}
              onClick={() => setSelectedTag(tag)}
              className={cn(
                "rounded-lg px-3 py-1.5 font-mono text-[11px] font-bold tracking-wider transition-colors",
                selectedTag === tag
                  ? "bg-foreground text-background"
                  : "bg-muted text-muted-foreground hover:text-foreground"
              )}
            >
              {tag}
            </button>
          ))}
        </div>
      </div>

      {/* Notifications List Table / Cards */}
      {error ? (
        <div className="rounded-2xl border border-destructive/20 bg-destructive/5 p-8 text-center">
          <AlertCircle className="mx-auto size-6 text-destructive" />
          <p className="mt-2 text-sm font-bold text-destructive">{error}</p>
          <button
            onClick={loadNotifications}
            className="mt-4 rounded-xl bg-destructive px-4 py-2 font-mono text-xs font-bold text-destructive-foreground"
          >
            Retry
          </button>
        </div>
      ) : loading ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-border bg-card py-20">
          <RefreshCw className="size-6 animate-spin text-muted-foreground" />
          <p className="mt-3 font-mono text-xs text-muted-foreground">Loading announcements...</p>
        </div>
      ) : filteredItems.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-card py-16 text-center">
          <div className="grid size-12 place-items-center rounded-full bg-muted">
            <Bell className="size-6 text-muted-foreground" />
          </div>
          <h3 className="mt-4 font-bold text-base">No notifications found</h3>
          <p className="mt-1 max-w-sm font-mono text-xs text-muted-foreground">
            {query || selectedTag !== "ALL"
              ? "No announcements matched your search query or selected tag."
              : "No announcements created yet. Add your first notification to display it on student dashboards."}
          </p>
          <button
            onClick={() => setIsModalOpen(true)}
            className="mt-5 inline-flex items-center gap-2 rounded-xl bg-accent px-4 py-2 font-mono text-xs font-bold text-accent-foreground"
          >
            <Plus className="size-3.5" /> Create Announcement
          </button>
        </div>
      ) : (
        <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-sm">
          <div className="divide-y divide-border">
            {filteredItems.map((item) => (
              <div
                key={item.id}
                className={cn(
                  "flex flex-col gap-4 p-5 transition-colors sm:flex-row sm:items-center sm:justify-between",
                  !item.active && "bg-muted/30 opacity-75"
                )}
              >
                <div className="flex items-start gap-4">
                  <div className="mt-1 grid size-10 shrink-0 place-items-center rounded-xl border border-border bg-background">
                    {getTagIcon(item.tag)}
                  </div>
                  <div className="space-y-1.5">
                    <div className="flex flex-wrap items-center gap-2">
                      <h4 className="font-bold text-sm tracking-tight">{item.title}</h4>
                      <span className="rounded-md bg-muted px-2 py-0.5 font-mono text-[9px] font-bold uppercase tracking-wider text-muted-foreground">
                        {item.tag || "ANNOUNCEMENT"}
                      </span>
                      {item.isNew && (
                        <span className="rounded-full bg-accent px-1.5 py-0.5 font-mono text-[8px] font-black text-accent-foreground">
                          NEW
                        </span>
                      )}
                      <Badge tone={item.active ? "good" : "bad"}>
                        {item.active ? "ACTIVE" : "INACTIVE"}
                      </Badge>
                    </div>
                    <p className="font-mono text-xs text-muted-foreground leading-relaxed">
                      {item.message}
                    </p>
                    <div className="flex flex-wrap items-center gap-4 pt-1 font-mono text-[10px] text-muted-foreground">
                      <span className="inline-flex items-center gap-1">
                        <Calendar className="size-3" />
                        {formatDate(item.createdAt)}
                      </span>
                      {item.linkUrl && (
                        <a
                          href={item.linkUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="inline-flex items-center gap-1 text-accent hover:underline"
                        >
                          <ExternalLink className="size-3" />
                          {item.linkUrl}
                        </a>
                      )}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 self-end sm:self-center">
                  <button
                    onClick={() => handleToggleActive(item)}
                    disabled={actionLoadingId === item.id}
                    title={item.active ? "Deactivate (Hide from users)" : "Activate (Show to users)"}
                    className={cn(
                      "inline-flex items-center gap-1.5 rounded-lg border px-3 py-1.5 font-mono text-[11px] font-bold transition-colors disabled:opacity-50",
                      item.active
                        ? "border-border bg-background text-muted-foreground hover:bg-muted hover:text-foreground"
                        : "border-emerald-500/30 bg-emerald-500/10 text-emerald-500 hover:bg-emerald-500/20"
                    )}
                  >
                    {item.active ? (
                      <>
                        <EyeOff className="size-3.5" /> HIDE
                      </>
                    ) : (
                      <>
                        <Eye className="size-3.5" /> SHOW
                      </>
                    )}
                  </button>

                  <button
                    onClick={() => handleDelete(item)}
                    disabled={actionLoadingId === item.id}
                    title="Delete Notification"
                    className="grid size-8 place-items-center rounded-lg border border-destructive/20 text-destructive hover:bg-destructive/10 transition-colors disabled:opacity-50"
                  >
                    <Trash2 className="size-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Create Notification Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="w-full max-w-lg rounded-3xl border border-border bg-background p-6 md:p-8 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between pb-4 border-b border-border">
              <div>
                <h3 className="text-lg font-black tracking-tight">CREATE NEW NOTIFICATION</h3>
                <p className="font-mono text-[11px] text-muted-foreground">
                  Broadcast this announcement to student dashboards.
                </p>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                className="grid size-8 place-items-center rounded-full hover:bg-muted text-muted-foreground hover:text-foreground transition-colors"
              >
                <X className="size-4" />
              </button>
            </div>

            {formError && (
              <div className="mt-4 flex items-center gap-2 rounded-xl border border-destructive/20 bg-destructive/10 p-3 text-xs font-mono text-destructive">
                <AlertCircle className="size-4 shrink-0" />
                <span>{formError}</span>
              </div>
            )}

            <form onSubmit={handleCreateSubmit} className="mt-5 space-y-4">
              <div>
                <label className="block font-mono text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
                  Title *
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Live Creative Workshop"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  className="mt-1.5 w-full rounded-xl border border-border bg-card px-3.5 py-2.5 text-xs font-mono focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
                />
              </div>

              <div>
                <label className="block font-mono text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
                  Category / Tag *
                </label>
                <select
                  value={formData.tag}
                  onChange={(e) => setFormData({ ...formData, tag: e.target.value })}
                  className="mt-1.5 w-full rounded-xl border border-border bg-card px-3.5 py-2.5 text-xs font-mono focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
                >
                  <option value="ANNOUNCEMENT">ANNOUNCEMENT</option>
                  <option value="LIVE EVENT">LIVE EVENT</option>
                  <option value="UPDATE">UPDATE</option>
                  <option value="RESOURCE">RESOURCE</option>
                  <option value="WORKSHOP">WORKSHOP</option>
                </select>
              </div>

              <div>
                <label className="block font-mono text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
                  Message Content *
                </label>
                <textarea
                  required
                  rows={3}
                  placeholder="Detailed announcement text shown on user dashboard..."
                  value={formData.message}
                  onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                  className="mt-1.5 w-full rounded-xl border border-border bg-card px-3.5 py-2.5 text-xs font-mono focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
                />
              </div>

              <div>
                <label className="block font-mono text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
                  Link URL (Optional)
                </label>
                <input
                  type="text"
                  placeholder="e.g. https://meet.google.com/... or /free-resources"
                  value={formData.linkUrl || ""}
                  onChange={(e) => setFormData({ ...formData, linkUrl: e.target.value })}
                  className="mt-1.5 w-full rounded-xl border border-border bg-card px-3.5 py-2.5 text-xs font-mono focus:border-accent focus:outline-none focus:ring-1 focus:ring-accent"
                />
                <p className="mt-1 font-mono text-[9px] text-muted-foreground">
                  Students will be able to click directly to this link from their dashboard.
                </p>
              </div>

              <div className="flex flex-wrap items-center gap-6 pt-2">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.active}
                    onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                    className="size-4 rounded border-border text-accent focus:ring-accent accent-accent"
                  />
                  <span className="font-mono text-xs font-semibold">Publish immediately (Active)</span>
                </label>

                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.isNew}
                    onChange={(e) => setFormData({ ...formData, isNew: e.target.checked })}
                    className="size-4 rounded border-border text-accent focus:ring-accent accent-accent"
                  />
                  <span className="font-mono text-xs font-semibold">Display &apos;NEW&apos; badge</span>
                </label>
              </div>

              <div className="flex items-center justify-end gap-3 pt-5 border-t border-border">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="rounded-xl border border-border px-4 py-2.5 font-mono text-xs font-bold text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="inline-flex items-center gap-2 rounded-xl bg-accent px-5 py-2.5 font-mono text-xs font-black text-accent-foreground shadow-sm hover:brightness-105 transition-all disabled:opacity-50"
                >
                  {submitting && <RefreshCw className="size-3.5 animate-spin" />}
                  {submitting ? "Publishing..." : "Publish Announcement"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Confirmation Dialog */}
      <ConfirmDialog confirm={confirm} close={() => setConfirm(null)} />
    </div>
  )
}

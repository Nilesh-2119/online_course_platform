"use client"

import Link from "next/link"
import { Plus, Loader2, RefreshCw } from "lucide-react"
import { useEffect, useMemo, useState } from "react"
import { Badge, SearchBox, ConfirmDialog, type ConfirmAction } from "@/components/admin/shell"
import { getAdminUsers, deleteAdminUser, type AdminUserItem } from "@/lib/api/admin-service"
import { formatDate } from "@/lib/utils"
import { cn } from "@/lib/utils"

const filterOptions = ["ALL", "FREE", "PAID", "ACTIVE", "SUSPENDED"] as const

export function AdminUsersPage() {
  const [items, setItems] = useState<AdminUserItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [query, setQuery] = useState("")
  const [filter, setFilter] = useState<string>("ALL")
  const [selected, setSelected] = useState<string[]>([])
  const [confirm, setConfirm] = useState<ConfirmAction>(null)

  const loadUsers = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await getAdminUsers()
      if (res.success && res.data) {
        setItems(res.data)
      } else {
        setError(res.error || "Failed to load users from database")
      }
    } catch (err: any) {
      setError(err?.message || "Failed to connect to backend server")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadUsers()
  }, [])

  const filtered = useMemo(() => {
    return items.filter((u) => {
      const matchesFilter =
        filter === "ALL" ||
        (filter === "FREE" && u.type === "free") ||
        (filter === "PAID" && u.type === "paid") ||
        (filter === "ACTIVE" && (u.status === "ACTIVE" || u.status === "active")) ||
        (filter === "SUSPENDED" && (u.status === "SUSPENDED" || u.status === "suspended" || u.status === "DISABLED"))

      const q = query.toLowerCase()
      const matchesQuery =
        (u.name || "").toLowerCase().includes(q) ||
        (u.email || "").toLowerCase().includes(q) ||
        (u.phone || "").toLowerCase().includes(q)

      return matchesFilter && matchesQuery
    })
  }, [items, filter, query])

  const toggle = (id: string) =>
    setSelected((s) => (s.includes(id) ? s.filter((x) => x !== id) : [...s, id]))

  const bulkDelete = () =>
    setConfirm({
      title: "DELETE SELECTED USERS?",
      body: `Are you sure you want to permanently delete ${selected.length} user(s) from the database? This action cannot be undone.`,
      label: "DELETE USERS",
      action: async () => {
        for (const id of selected) {
          await deleteAdminUser(id)
        }
        setSelected([])
        loadUsers()
      },
    })

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="text-sm text-muted-foreground">Manage AdFix Studio real accounts and course access from MySQL database.</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadUsers}
            disabled={loading}
            className="inline-flex items-center justify-center gap-2 rounded-md border border-border px-3 py-2.5 font-mono text-[10px] tracking-widest text-foreground hover:bg-muted/30 disabled:opacity-50"
            title="Refresh Users"
          >
            <RefreshCw size={12} className={loading ? "animate-spin" : ""} /> REFRESH
          </button>
          <Link
            href="/admin/users/new"
            className="inline-flex items-center justify-center gap-2 rounded-md bg-foreground px-4 py-2.5 font-mono text-[10px] tracking-widest text-background hover:opacity-90"
          >
            <Plus size={14} /> ADD USER
          </Link>
        </div>
      </div>

      <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <SearchBox value={query} onChange={setQuery} placeholder="SEARCH BY NAME, EMAIL OR PHONE" />
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

      {selected.length > 0 && (
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-md border border-accent/50 bg-accent/10 p-4">
          <span className="font-mono text-[10px] tracking-widest">{selected.length} USERS SELECTED</span>
          <div className="flex gap-2">
            <button
              onClick={bulkDelete}
              className="rounded-md bg-destructive px-3 py-2 font-mono text-[10px] text-destructive-foreground hover:opacity-90"
            >
              DELETE USERS
            </button>
          </div>
        </div>
      )}

      {error && (
        <div className="rounded-lg border border-destructive/40 bg-destructive/10 p-4 text-xs font-mono text-destructive">
          Error: {error}
        </div>
      )}

      <div className="overflow-hidden rounded-lg border border-border bg-card">
        {/* Table Header (PROGRESS removed) */}
        <div className="hidden grid-cols-[36px_1.5fr_1.8fr_.8fr_.9fr_1fr_1fr_.6fr] gap-3 bg-muted/50 px-4 py-3 font-mono text-[9px] tracking-widest text-muted-foreground md:grid">
          <input
            aria-label="Select all users"
            type="checkbox"
            checked={filtered.length > 0 && selected.length === filtered.length}
            onChange={() => setSelected(selected.length === filtered.length ? [] : filtered.map((u) => u.id.toString()))}
          />
          <div>NAME</div>
          <div>EMAIL</div>
          <div>TYPE</div>
          <div>STATUS</div>
          <div>ACCESS</div>
          <div>JOINED</div>
          <div>ACTION</div>
        </div>

        {loading ? (
          <div className="divide-y divide-border">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="flex items-center gap-4 p-4">
                <div className="size-4 animate-pulse rounded bg-muted" />
                <div className="h-4 w-28 animate-pulse rounded bg-muted" />
                <div className="h-4 w-44 animate-pulse rounded bg-muted" />
                <div className="h-4 w-16 animate-pulse rounded bg-muted" />
                <div className="h-4 w-16 animate-pulse rounded bg-muted" />
                <div className="h-4 w-20 animate-pulse rounded bg-muted" />
              </div>
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <div className="p-12 text-center">
            <div className="font-mono text-xs tracking-widest">NO USERS FOUND</div>
            <p className="mt-2 text-sm text-muted-foreground">No users match your current search or filter.</p>
          </div>
        ) : (
          filtered.map((u) => {
            const userId = u.id.toString()
            const isSuspended = u.status === "SUSPENDED" || u.status === "DISABLED" || u.status === "suspended"
            return (
              <div
                key={userId}
                className="grid gap-3 border-t border-border px-4 py-4 text-sm transition hover:bg-muted/20 md:grid-cols-[36px_1.5fr_1.8fr_.8fr_.9fr_1fr_1fr_.6fr] md:items-center"
              >
                <input
                  aria-label={`Select ${u.name}`}
                  type="checkbox"
                  checked={selected.includes(userId)}
                  onChange={() => toggle(userId)}
                />
                <div className="font-semibold text-foreground truncate">{u.name || "Unnamed"}</div>
                <div className="text-muted-foreground truncate">{u.email}</div>
                <div>
                  <Badge tone={u.type === "paid" ? "good" : "default"}>
                    {u.type.toUpperCase()}
                  </Badge>
                </div>
                <div>
                  <Badge tone={!isSuspended ? "good" : "bad"}>
                    {u.status.toUpperCase()}
                  </Badge>
                </div>
                <div>
                  <Badge tone={u.access ? "good" : "default"}>
                    {u.access ? "UNLOCKED" : "LOCKED"}
                  </Badge>
                </div>
                <div className="text-muted-foreground font-mono text-xs">
                  {formatDate(u.joinedAt)}
                </div>
                <div>
                  <Link
                    className="inline-block rounded border border-border/80 px-2 py-1 font-mono text-[10px] tracking-wider text-accent transition hover:border-accent hover:bg-accent/10"
                    href={`/admin/users/${userId}`}
                  >
                    VIEW
                  </Link>
                </div>
              </div>
            )
          })
        )}
      </div>

      <ConfirmDialog confirm={confirm} close={() => setConfirm(null)} />
    </div>
  )
}

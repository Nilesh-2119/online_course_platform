"use client"

import Link from "next/link"
import { useRouter } from "next/navigation"
import { useState, useEffect } from "react"
import { Badge, Card, ConfirmDialog, type ConfirmAction } from "@/components/admin/shell"
import {
  getAdminUserDetail,
  revokeUserAccess,
  grantUserAccess,
  deleteAdminUser,
  type AdminUserDetailData,
} from "@/lib/api/admin-service"
import { formatDate, formatDateTime } from "@/lib/utils"
import { Loader2, ShieldCheck, AlertTriangle, Trash2, KeyRound } from "lucide-react"

export function AdminUserDetail({ id }: { id: string }) {
  const router = useRouter()
  const [user, setUser] = useState<AdminUserDetailData | null>(null)
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [confirm, setConfirm] = useState<ConfirmAction>(null)

  const loadDetail = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await getAdminUserDetail(id)
      if (res.success && res.data) {
        setUser(res.data)
      } else {
        setError(res.error || "Failed to load user detail from database")
      }
    } catch (err: any) {
      setError(err?.message || "Failed to connect to backend server")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadDetail()
  }, [id])

  const handleToggleAccess = async () => {
    if (!user) return
    try {
      setActionLoading(true)
      if (user.access) {
        await revokeUserAccess(user.id)
      } else {
        await grantUserAccess(user.id)
      }
      await loadDetail()
    } catch (err: any) {
      alert(err?.message || "Failed to update course access")
    } finally {
      setActionLoading(false)
    }
  }

  const handleDeleteUser = () => {
    if (!user) return
    setConfirm({
      title: `PERMANENTLY DELETE USER: ${user.name}?`,
      body: `Are you sure you want to permanently delete ${user.email} from the database? All related verification records, progress, and purchase records will be permanently removed. This action cannot be undone.`,
      label: "DELETE USER PERMANENTLY",
      action: async () => {
        try {
          setActionLoading(true)
          const res = await deleteAdminUser(user.id)
          if (res.success) {
            router.push("/admin/users")
          } else {
            alert(res.error || "Failed to delete user")
          }
        } catch (err: any) {
          alert(err?.message || "Failed to delete user")
        } finally {
          setActionLoading(false)
        }
      },
    })
  }

  if (loading) {
    return (
      <div className="flex h-64 flex-col items-center justify-center gap-3">
        <Loader2 className="size-8 animate-spin text-accent" />
        <span className="font-mono text-xs text-muted-foreground tracking-widest">LOADING REAL USER DATA...</span>
      </div>
    )
  }

  if (error || !user) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 rounded-xl border border-destructive/40 bg-destructive/10 p-10 text-center">
        <AlertTriangle className="size-8 text-destructive" />
        <p className="font-mono text-xs text-destructive">{error || "User not found."}</p>
        <Link href="/admin/users" className="font-mono text-xs text-accent underline">
          ← Back to Users
        </Link>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-6">
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <p className="text-sm text-muted-foreground">Manage this account and course access in the database.</p>
        <Link href="/admin/users" className="font-mono text-[10px] text-accent tracking-wider hover:underline">
          ← BACK TO USERS
        </Link>
      </div>

      {/* Main Details Grid */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* User Details */}
        <Card title="USER DETAILS">
          <div className="grid gap-4 text-sm sm:grid-cols-2">
            <div>
              <span className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">NAME</span>
              <p className="mt-1 font-semibold text-foreground">{user.name || "Unnamed User"}</p>
            </div>
            <div>
              <span className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">EMAIL</span>
              <p className="mt-1 text-foreground break-all">{user.email}</p>
            </div>
            <div>
              <span className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">PHONE</span>
              <p className="mt-1 text-foreground">{user.phone || "—"}</p>
            </div>
            <div>
              <span className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">JOINED</span>
              <p className="mt-1 text-foreground">{formatDate(user.joinedAt)}</p>
            </div>
            <div>
              <span className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">ROLE</span>
              <p className="mt-1 font-mono text-xs text-foreground">{user.role}</p>
            </div>
            <div>
              <span className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">USER ID</span>
              <p className="mt-1 font-mono text-xs text-muted-foreground">#{user.id}</p>
            </div>
          </div>
        </Card>

        {/* Account Status & Admin Actions */}
        <Card title="ACCOUNT STATUS">
          <div className="flex flex-wrap gap-2.5">
            <Badge tone={user.type === "paid" ? "good" : "default"}>
              {user.type.toUpperCase()}
            </Badge>
            <Badge tone={user.status === "ACTIVE" ? "good" : "bad"}>
              {user.status.toUpperCase()}
            </Badge>
            <Badge tone={user.access ? "good" : "default"}>
              {user.access ? "UNLOCKED" : "LOCKED"}
            </Badge>
          </div>

          <div className="mt-6 flex flex-wrap gap-2.5">
            {/* REVOKE ACCESS / GRANT ACCESS */}
            <button
              onClick={handleToggleAccess}
              disabled={actionLoading}
              className={`rounded-md px-3.5 py-2 font-mono text-[10px] tracking-wider transition ${
                user.access
                  ? "bg-foreground text-background hover:bg-foreground/90"
                  : "bg-accent text-accent-foreground hover:bg-accent/90"
              }`}
            >
              {actionLoading ? "SAVING..." : user.access ? "REVOKE ACCESS" : "GRANT ACCESS"}
            </button>

            {/* DELETE USER */}
            <button
              onClick={handleDeleteUser}
              disabled={actionLoading}
              className="inline-flex items-center gap-1.5 rounded-md border border-destructive/50 bg-destructive/10 px-3.5 py-2 font-mono text-[10px] tracking-wider text-destructive transition hover:bg-destructive hover:text-destructive-foreground"
            >
              <Trash2 size={12} /> DELETE USER
            </button>
          </div>
        </Card>
      </div>

      {/* Course Access (Progress bar removed) */}
      <Card title="COURSE ACCESS">
        <div className="flex flex-col gap-5">
          <div className="flex items-center justify-between gap-4 text-sm">
            <span className="font-semibold text-foreground">{user.courseTitle || "AdFix High-Performing Ads Course"}</span>
            <Badge tone={user.access ? "good" : "default"}>
              {user.access ? "UNLOCKED" : "LOCKED"}
            </Badge>
          </div>

          <div className="grid gap-4 text-sm sm:grid-cols-2">
            <div>
              <span className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">LAST LOGIN / ACTIVE</span>
              <p className="mt-1 text-foreground font-medium">{user.lastLoginAt ? formatDateTime(user.lastLoginAt) : "—"}</p>
            </div>
            <div>
              <span className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground">COURSE ACCESS STATUS</span>
              <p className="mt-1 font-semibold text-foreground">
                {user.access ? "Full Course Access Granted" : "Preview / No Active Access"}
              </p>
            </div>
          </div>
        </div>
      </Card>

      {/* Coupons Applied Card (FREE RESOURCES ACCESSED completely removed) */}
      <div className="grid gap-6">
        <Card title="COUPONS APPLIED">
          <p className="text-sm text-muted-foreground">
            {user.purchases && user.purchases.length > 0 && user.type === "paid"
              ? "Standard enrollment or discounted checkout applied."
              : "No coupons used."}
          </p>
        </Card>
      </div>

      <ConfirmDialog confirm={confirm} close={() => setConfirm(null)} />
    </div>
  )
}

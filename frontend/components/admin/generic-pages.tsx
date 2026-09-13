"use client"

import Link from "next/link"
import { useState, useEffect, useCallback } from "react"
import { Plus, Trash2, Power, Loader2, Tag } from "lucide-react"
import { Badge, SearchBox } from "@/components/admin/shell"
import { mockResources, mockOrders } from "@/mocks/seed-data"
import { formatCurrency, formatDate } from "@/lib/utils"
import { couponService } from "@/lib/api"
import type { Coupon } from "@/lib/types"

export function AdminResourcesPage() {
  const [query, setQuery] = useState("")
  const filtered = mockResources.filter((r) => JSON.stringify(r).toLowerCase().includes(query.toLowerCase()))

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <p className="text-sm text-muted-foreground">Manage AdFix Studio free resources and platform operations.</p>
        <Link href="/admin/resources/new" className="rounded-md bg-foreground px-4 py-3 font-mono text-[10px] text-background"><Plus size={14} className="mr-2 inline" />ADD FREE RESOURCE</Link>
      </div>
      <SearchBox value={query} onChange={setQuery} placeholder="SEARCH FREE RESOURCES" />
      <div className="overflow-hidden rounded-lg border border-border">
        {filtered.length === 0 ? (
          <div className="p-10 text-center font-mono text-xs">NO FREE RESOURCES FOUND</div>
        ) : (
          filtered.map((r) => (
            <div key={r.id} className="grid gap-3 border-b border-border p-5 last:border-0 md:grid-cols-[1.5fr_1fr_1fr_1fr_auto] md:items-center">
              <div className="font-semibold">{r.title}<small className="block text-xs font-normal text-muted-foreground">{r.type.toUpperCase()}</small></div>
              <Badge tone="good">{r.status.toUpperCase()}</Badge>
              <div className="text-sm text-muted-foreground">{r.downloadCount} downloads</div>
              <div className="text-sm text-muted-foreground">{formatDate(r.createdAt)}</div>
              <span className="font-mono text-[10px] text-accent">EDIT</span>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export function AdminOrdersPage() {
  const [query, setQuery] = useState("")
  const filtered = mockOrders.filter((o) => JSON.stringify(o).toLowerCase().includes(query.toLowerCase()))

  return (
    <div className="flex flex-col gap-6">
      <p className="text-sm text-muted-foreground">Manage AdFix Studio orders and platform operations.</p>
      <SearchBox value={query} onChange={setQuery} placeholder="SEARCH ORDERS" />
      <div className="overflow-hidden rounded-lg border border-border">
        {filtered.length === 0 ? (
          <div className="p-10 text-center font-mono text-xs">NO ORDERS FOUND</div>
        ) : (
          filtered.map((o) => (
            <div key={o.id} className="grid gap-3 border-b border-border p-5 last:border-0 md:grid-cols-[1.5fr_1fr_1fr_1fr_auto] md:items-center">
              <div className="font-semibold">#{o.id}<small className="block text-xs font-normal text-muted-foreground">{o.productTitle}</small></div>
              <Badge tone={o.status === "paid" ? "good" : o.status === "failed" ? "bad" : "default"}>{o.status.toUpperCase()}</Badge>
              <div className="text-sm text-muted-foreground">{o.customerName}</div>
              <div className="text-sm text-muted-foreground">{formatDate(o.createdAt)}</div>
              <Link href={`/admin/orders/${o.id}`} className="font-mono text-[10px] text-accent">VIEW</Link>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export function AdminOrderDetail({ id }: { id: string }) {
  const order = mockOrders.find((o) => o.id === id || o.id === `AFX-${id}`) ?? mockOrders[0]

  return (
    <div className="flex max-w-3xl flex-col gap-6">
      <Link href="/admin/orders" className="font-mono text-[10px] text-accent">← BACK TO ORDERS</Link>
      <section className="rounded-lg border border-border bg-card">
        <div className="flex items-center justify-between border-b border-border p-5">
          <h2 className="font-mono text-xs font-bold tracking-widest">ORDER DETAILS</h2>
        </div>
        <div className="grid gap-5 p-5 text-sm sm:grid-cols-2">
          <div><span className="text-xs text-muted-foreground">ORDER ID</span><p className="mt-1 font-bold">#{order.id}</p></div>
          <div><span className="text-xs text-muted-foreground">PAYMENT STATUS</span><p className="mt-1"><Badge tone="good">{order.status.toUpperCase()}</Badge></p></div>
          <div><span className="text-xs text-muted-foreground">CUSTOMER</span><p className="mt-1">{order.customerName}<small className="block text-muted-foreground">{order.customerEmail}</small></p></div>
          <div><span className="text-xs text-muted-foreground">PRODUCT</span><p className="mt-1">{order.productTitle}</p></div>
          <div><span className="text-xs text-muted-foreground">PRICE</span><p className="mt-1">{formatCurrency(order.amount, order.currency)}</p></div>
          <div><span className="text-xs text-muted-foreground">COUPON / DISCOUNT</span><p className="mt-1">{order.couponCode ?? "—"} · {order.discountAmount > 0 ? formatCurrency(order.discountAmount, order.currency) : "—"}</p></div>
          <div><span className="text-xs text-muted-foreground">FINAL AMOUNT</span><p className="mt-1 text-xl font-bold">{formatCurrency(order.finalAmount, order.currency)}</p></div>
          <div><span className="text-xs text-muted-foreground">COURSE ACCESS</span><p className="mt-1"><Badge tone="good">UNLOCKED</Badge></p></div>
        </div>
        <div className="border-t border-border p-5">
          <Link href={`/admin/users/${order.userId}`} className="inline-flex items-center gap-2 rounded-md bg-foreground px-4 py-3 font-mono text-[10px] text-background">
            VIEW CUSTOMER
          </Link>
        </div>
      </section>
    </div>
  )
}

export function AdminCouponsPage() {
  const [coupons, setCoupons] = useState<Coupon[]>([])
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState<string | number | null>(null)
  const [query, setQuery] = useState("")
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const loadCoupons = useCallback(async () => {
    try {
      setLoading(true)
      const res = await couponService.getCoupons()
      if (res.success && res.data) {
        setCoupons(res.data)
      } else {
        setErrorMessage(res.error || "Failed to load coupons")
      }
    } catch (err: any) {
      setErrorMessage(err.message || "An unexpected error occurred while loading coupons")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadCoupons()
  }, [loadCoupons])

  const handleToggle = async (id: string | number) => {
    try {
      setActionLoading(id)
      const res = await couponService.toggleCouponActive(id)
      if (res.success && res.data) {
        setCoupons((prev) =>
          prev.map((c) =>
            String(c.id) === String(id)
              ? { ...c, active: res.data!.active, status: res.data!.status }
              : c
          )
        )
      } else {
        alert(res.error || "Failed to toggle coupon status")
      }
    } catch (err: any) {
      alert(err.message || "Failed to toggle coupon status")
    } finally {
      setActionLoading(null)
    }
  }

  const handleDelete = async (id: string | number, code: string) => {
    if (!window.confirm(`Are you sure you want to permanently delete coupon '${code}'?`)) {
      return
    }
    try {
      setActionLoading(id)
      const res = await couponService.deleteCoupon(id)
      if (res.success) {
        setCoupons((prev) => prev.filter((c) => String(c.id) !== String(id)))
      } else {
        alert(res.error || "Failed to delete coupon")
      }
    } catch (err: any) {
      alert(err.message || "Failed to delete coupon")
    } finally {
      setActionLoading(null)
    }
  }

  const filtered = coupons.filter((c) => {
    const q = query.toLowerCase()
    return (
      c.code.toLowerCase().includes(q) ||
      (c.discountType && c.discountType.toLowerCase().includes(q)) ||
      (c.status && c.status.toLowerCase().includes(q))
    )
  })

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <h2 className="font-bold text-lg">Discount Coupons</h2>
          <p className="text-sm text-muted-foreground">
            Create and manage promotional discount coupons for checkout.
          </p>
        </div>
        <Link
          href="/admin/coupons/new"
          className="rounded-md bg-foreground px-4 py-3 font-mono text-[10px] text-background hover:opacity-90 transition-opacity"
        >
          <Plus size={14} className="mr-2 inline" />ADD COUPON
        </Link>
      </div>

      <SearchBox value={query} onChange={setQuery} placeholder="SEARCH COUPONS BY CODE" />

      {errorMessage && (
        <div className="p-4 rounded-lg border border-destructive/40 bg-destructive/10 text-xs font-mono text-destructive">
          {errorMessage}
        </div>
      )}

      <div className="overflow-hidden rounded-lg border border-border bg-card">
        {loading ? (
          <div className="p-12 text-center font-mono text-xs text-muted-foreground flex items-center justify-center gap-2">
            <Loader2 className="size-4 animate-spin" /> LOADING COUPONS...
          </div>
        ) : filtered.length === 0 ? (
          <div className="p-12 text-center font-mono text-xs text-muted-foreground">
            {query ? "NO COUPONS MATCHING SEARCH" : "NO COUPONS FOUND. CLICK 'ADD COUPON' TO CREATE ONE."}
          </div>
        ) : (
          filtered.map((c) => {
            const isPerc = String(c.discountType).toUpperCase() === "PERCENTAGE"
            const statusUpper = String(c.status || "ACTIVE").toUpperCase()
            const tone =
              statusUpper === "ACTIVE"
                ? "good"
                : statusUpper === "EXPIRED" || statusUpper === "EXHAUSTED"
                ? "bad"
                : "default"

            const isBusy = actionLoading === c.id

            return (
              <div
                key={c.id}
                className="grid gap-3 border-b border-border p-5 last:border-0 md:grid-cols-[1.5fr_1fr_1fr_1.2fr_auto] md:items-center"
              >
                <div>
                  <span className="font-mono font-bold tracking-wider text-base">{c.code}</span>
                  <small className="block text-xs font-semibold text-accent mt-0.5">
                    {isPerc ? `${c.discountValue}% OFF` : `₹${c.discountValue} FLAT OFF`}
                    {c.minimumPurchase && Number(c.minimumPurchase) > 0 && (
                      <span className="text-muted-foreground font-normal ml-2">
                        (Min: ₹{c.minimumPurchase})
                      </span>
                    )}
                  </small>
                </div>

                <div>
                  <Badge tone={tone}>{statusUpper}</Badge>
                </div>

                <div className="text-xs font-mono text-muted-foreground">
                  <span className="font-semibold text-foreground">{c.usageCount}</span> / {c.usageLimit} uses
                  {c.perUserLimit && c.perUserLimit > 1 && (
                    <small className="block text-[10px] text-muted-foreground">
                      Max {c.perUserLimit}/user
                    </small>
                  )}
                </div>

                <div className="text-xs font-mono text-muted-foreground">
                  <span className="text-[10px] block uppercase text-muted-foreground/70">Expires</span>
                  {c.expiryDate ? formatDate(c.expiryDate) : "Never"}
                </div>

                <div className="flex items-center gap-2">
                  <button
                    disabled={isBusy}
                    onClick={() => handleToggle(c.id)}
                    className="flex items-center gap-1.5 rounded-md border border-border px-2.5 py-1.5 font-mono text-[10px] text-muted-foreground hover:bg-muted hover:text-foreground disabled:opacity-50 transition-colors"
                    title={statusUpper === "ACTIVE" ? "Deactivate Coupon" : "Activate Coupon"}
                  >
                    <Power size={12} className={statusUpper === "ACTIVE" ? "text-accent" : "text-muted-foreground"} />
                    {statusUpper === "ACTIVE" ? "DISABLE" : "ENABLE"}
                  </button>

                  <button
                    disabled={isBusy}
                    onClick={() => handleDelete(c.id, c.code)}
                    className="flex items-center gap-1.5 rounded-md border border-destructive/30 px-2.5 py-1.5 font-mono text-[10px] text-destructive hover:bg-destructive/10 disabled:opacity-50 transition-colors"
                    title="Delete Coupon"
                  >
                    <Trash2 size={12} />
                    DELETE
                  </button>
                </div>
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}

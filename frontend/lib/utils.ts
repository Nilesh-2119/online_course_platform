import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/** Format a numeric amount as Indian currency. */
export function formatCurrency(amount: number, currency = "INR"): string {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount)
}

/** Format a number with Indian-style commas (e.g. 7,48,000). */
export function formatNumber(value: number): string {
  return new Intl.NumberFormat("en-IN").format(value)
}

/** Format an ISO date string to a display date (e.g. "25 Aug 2026"). */
export function formatDate(iso: string): string {
  const date = new Date(iso)
  return date.toLocaleDateString("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
  })
}

/** Format an ISO date string to a display time (e.g. "10:42 AM"). */
export function formatTime(iso: string): string {
  const date = new Date(iso)
  return date.toLocaleTimeString("en-IN", {
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  })
}

/** Format an ISO date string to a display date & time (e.g. "8 Sept 2026, 10:42 AM"). */
export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return "—"
  const date = new Date(iso)
  if (isNaN(date.getTime())) return "—"
  const dateStr = date.toLocaleDateString("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
  })
  const timeStr = date.toLocaleTimeString("en-IN", {
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  })
  return `${dateStr}, ${timeStr}`
}


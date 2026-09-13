"use client"

import dynamic from "next/dynamic"
import { Loader2 } from "lucide-react"

const DashboardView = dynamic(
  () => import("@/components/dashboard/dashboard-view").then((mod) => mod.DashboardView),
  {
    ssr: false,
    loading: () => (
      <div className="flex min-h-[80vh] flex-col items-center justify-center px-4 py-16 text-foreground">
        <div className="flex flex-col items-center gap-4 rounded-3xl border border-border bg-card p-10 backdrop-blur-xl shadow-2xl">
          <div className="relative">
            <div className="size-12 rounded-full border-2 border-primary/20 border-t-primary animate-spin" />
            <Loader2 className="absolute inset-0 m-auto size-5 text-primary animate-pulse" />
          </div>
          <div className="text-center">
            <p className="font-mono text-xs uppercase tracking-widest text-primary font-bold">Loading Dashboard</p>
            <p className="mt-1 text-sm text-muted-foreground">Preparing your course learning space...</p>
          </div>
        </div>
      </div>
    ),
  }
)

export default function Page() {
  return <DashboardView />
}

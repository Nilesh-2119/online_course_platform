import type { ReactNode } from "react"
import { AuthGuard } from "@/components/auth/auth-guard"

export const metadata = {
  title: "Dashboard - Student Portal | Creative Ads Masterclass",
  description: "Access your courses, track your learning progress, and explore creative advertising resources.",
}

export default function DashboardLayout({ children }: { children: ReactNode }) {
  return <AuthGuard>{children}</AuthGuard>
}

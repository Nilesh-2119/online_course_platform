import { AdminShell } from "@/components/admin/shell"
import { AdminAnalyticsPage } from "@/components/admin/analytics-page"

export default function Page() {
  return (
    <AdminShell title="ANALYTICS">
      <AdminAnalyticsPage />
    </AdminShell>
  )
}

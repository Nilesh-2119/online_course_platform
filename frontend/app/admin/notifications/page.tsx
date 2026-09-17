import { AdminShell } from "@/components/admin/shell"
import { AdminNotificationsPage } from "@/components/admin/notifications-page"

export default function Page() {
  return (
    <AdminShell title="NOTIFICATIONS">
      <AdminNotificationsPage />
    </AdminShell>
  )
}

import { AdminShell } from "@/components/admin/shell"
import { AdminSettingsPage } from "@/components/admin/settings-page"

export default function Page() {
  return (
    <AdminShell title="SETTINGS">
      <AdminSettingsPage />
    </AdminShell>
  )
}

import { AdminShell } from "@/components/admin/shell"
import { AdminResourcesPage } from "@/components/admin/resources-page"

export default function Page() {
  return (
    <AdminShell title="FREE RESOURCES">
      <AdminResourcesPage />
    </AdminShell>
  )
}

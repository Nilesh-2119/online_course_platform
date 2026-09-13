import { AdminShell } from "@/components/admin/shell"
import { AdminOverview } from "@/components/admin/overview"

export const dynamic = "force-dynamic"
export const revalidate = 0

export default function AdminPage() {
  return (
    <AdminShell title="OVERVIEW">
      <AdminOverview />
    </AdminShell>
  )
}

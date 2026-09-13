import { AdminShell } from "@/components/admin/shell"
import { AdminUsersPage } from "@/components/admin/users-page"

export const dynamic = "force-dynamic"
export const revalidate = 0

export default function Page() {
  return (
    <AdminShell title="USERS">
      <AdminUsersPage />
    </AdminShell>
  )
}

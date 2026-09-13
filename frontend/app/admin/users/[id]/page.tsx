import { AdminShell } from "@/components/admin/shell"
import { AdminUserDetail } from "@/components/admin/user-detail"

export const dynamic = "force-dynamic"
export const revalidate = 0

export default async function Page({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params
  return (
    <AdminShell title="USER DETAIL">
      <AdminUserDetail id={id} />
    </AdminShell>
  )
}

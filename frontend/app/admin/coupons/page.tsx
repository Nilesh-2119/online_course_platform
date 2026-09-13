import { AdminShell } from "@/components/admin/shell"
import { AdminCouponsPage } from "@/components/admin/generic-pages"

export default function Page() {
  return (
    <AdminShell title="COUPONS">
      <AdminCouponsPage />
    </AdminShell>
  )
}

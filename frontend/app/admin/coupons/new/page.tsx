import { AdminShell } from "@/components/admin/shell"
import { AdminFormPage } from "@/components/admin/form-page"

export default function Page() {
  return (
    <AdminShell title="NEW COUPON">
      <AdminFormPage kind="COUPON" />
    </AdminShell>
  )
}

import { AdminShell } from "@/components/admin/shell"
import { CreateResourceForm } from "@/components/admin/create-resource-form"

export default function Page() {
  return (
    <AdminShell title="ADD FREE RESOURCE">
      <CreateResourceForm />
    </AdminShell>
  )
}

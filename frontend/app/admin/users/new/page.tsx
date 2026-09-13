import { AdminShell } from "@/components/admin/shell"
import { CreateUserForm } from "@/components/admin/create-user-form"

export default function Page() {
  return (
    <AdminShell title="ADD USER">
      <CreateUserForm />
    </AdminShell>
  )
}

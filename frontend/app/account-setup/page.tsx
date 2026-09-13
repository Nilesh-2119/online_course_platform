import { AuthGuard } from "@/components/auth/auth-guard"
import { AccountSetupView } from "@/components/auth/account-setup-view"

export const metadata = {
  title: "Account Setup | Creative Ads Masterclass",
  description: "Complete your profile information.",
}

export default function Page() {
  return (
    <AuthGuard>
      <AccountSetupView />
    </AuthGuard>
  )
}

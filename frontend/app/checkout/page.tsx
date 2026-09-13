import { AuthGuard } from "@/components/auth/auth-guard"
import { CheckoutView } from "@/components/purchase/checkout-view"

export const metadata = {
  title: "Secure Checkout | Creative Ads Masterclass",
  description: "Complete your course enrollment securely with instant lifetime access.",
}

export default function Page() {
  return (
    <AuthGuard>
      <CheckoutView />
    </AuthGuard>
  )
}

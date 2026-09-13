import { ArrowLeft } from "lucide-react"
import type { ReactNode } from "react"

export function FlowShell({ children, back = false }: { children: ReactNode; back?: boolean }) {
  return (
    <main className="min-h-screen bg-background px-5 py-6 md:px-8 md:py-8">
      <div className="mx-auto max-w-6xl">
        {back && (
          <a href="/" className="mb-6 inline-flex items-center gap-2 font-mono text-xs text-muted-foreground hover:text-foreground">
            <ArrowLeft className="size-4" /> BACK TO ADFIX STUDIO
          </a>
        )}
        {children}
      </div>
    </main>
  )
}

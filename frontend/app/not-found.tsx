import Link from "next/link"
import { ArrowLeft } from "lucide-react"

export default function NotFound() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-5 py-16 text-foreground">
      <div className="mx-auto max-w-md text-center">
        <p className="font-mono text-xs tracking-[.25em] text-accent">404 / NOT FOUND</p>
        <h1 className="mt-4 text-6xl font-black tracking-[-.07em]">Page not found.</h1>
        <p className="mt-4 font-mono text-sm leading-6 text-muted-foreground">
          The page you are looking for does not exist or has been moved.
        </p>
        <div className="mt-8 flex justify-center">
          <Link
            href="/"
            className="inline-flex items-center gap-2 rounded-full bg-foreground px-6 py-3.5 font-mono text-xs font-bold text-background"
          >
            <ArrowLeft className="size-4" /> BACK TO ADFIX STUDIO
          </Link>
        </div>
      </div>
    </main>
  )
}

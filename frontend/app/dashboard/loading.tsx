export default function DashboardLoading() {
  return (
    <main className="min-h-screen bg-[#f5f4ef] text-foreground">
      <div className="mx-auto max-w-5xl px-5 py-10 md:px-10 md:py-14">
        <div className="h-4 w-24 animate-pulse rounded bg-muted" />
        <div className="mt-4 h-10 w-64 animate-pulse rounded bg-muted" />
        <div className="mt-4 h-4 w-96 animate-pulse rounded bg-muted" />
        <div className="mt-8 grid gap-4 rounded-3xl border border-border bg-background p-6">
          <div className="h-8 w-full animate-pulse rounded bg-muted" />
          <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="aspect-video animate-pulse rounded-2xl bg-muted" />
            ))}
          </div>
        </div>
      </div>
    </main>
  )
}

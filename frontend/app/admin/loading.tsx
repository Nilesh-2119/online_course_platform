export default function AdminLoading() {
  return (
    <div className="flex flex-col gap-6">
      <div className="h-4 w-48 animate-pulse rounded bg-muted" />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-28 animate-pulse rounded-2xl border border-border bg-card p-6" />
        ))}
      </div>
      <div className="h-64 animate-pulse rounded-lg border border-border bg-card p-5" />
    </div>
  )
}

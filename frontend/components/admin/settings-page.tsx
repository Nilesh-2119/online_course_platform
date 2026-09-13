"use client"

import { Badge, Card } from "@/components/admin/shell"
import { mockAdminUser, mockAdminSessions, mockAuditLog } from "@/mocks/seed-data"
import { formatDate, formatTime } from "@/lib/utils"

export function AdminSettingsPage() {
  return (
    <div className="flex max-w-3xl flex-col gap-6">
      <p className="text-sm text-muted-foreground">Internal platform settings and frontend-only operational records.</p>

      <Card title="ADMIN PROFILE">
        <div className="grid gap-4 text-sm sm:grid-cols-2">
          <div><span className="text-xs text-muted-foreground">NAME</span><p className="mt-1">{mockAdminUser.name}</p></div>
          <div><span className="text-xs text-muted-foreground">EMAIL</span><p className="mt-1">{mockAdminUser.email}</p></div>
        </div>
      </Card>

      <Card title="PLATFORM">
        <div className="flex justify-between text-sm">
          <span>AdFix Studio · Course Price</span>
          <strong>₹4,000</strong>
        </div>
      </Card>

      <Card title="SECURITY">
        <div className="flex justify-between text-sm">
          <span>Two-factor authentication</span>
          <Badge tone="good">ENABLED · DISPLAY ONLY</Badge>
        </div>
      </Card>

      <Card title="ACTIVE SESSIONS">
        <div className="flex flex-col gap-4 text-sm">
          {mockAdminSessions.map((s) => (
            <div className="flex items-center justify-between border-b border-border pb-3 last:border-0 last:pb-0" key={s.id}>
              <span>{s.device} — {s.isCurrent ? "Current session" : formatDate(s.lastActiveAt)}</span>
              <button className="font-mono text-[10px] text-accent">SIGN OUT SESSION</button>
            </div>
          ))}
        </div>
      </Card>

      <Card title="AUDIT LOG">
        <div className="flex flex-col gap-4 text-sm">
          {mockAuditLog.map((entry) => (
            <div className="grid gap-1 border-b border-border pb-3 last:border-0 last:pb-0 sm:grid-cols-[1fr_auto_auto] sm:gap-5" key={entry.id}>
              <span>{entry.action}</span>
              <span className="text-muted-foreground">{formatDate(entry.createdAt)}</span>
              <span className="font-mono text-xs text-muted-foreground">{formatTime(entry.createdAt)}</span>
            </div>
          ))}
        </div>
      </Card>
    </div>
  )
}

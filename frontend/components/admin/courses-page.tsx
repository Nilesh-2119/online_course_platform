"use client"

import Link from "next/link"
import { useState } from "react"
import { Badge, Card } from "@/components/admin/shell"
import { mockCourses, mockLessons } from "@/mocks/seed-data"
import { formatCurrency } from "@/lib/utils"

export function AdminCoursesPage() {
  const course = mockCourses[0]
  return (
    <div className="flex flex-col gap-6">
      <p className="text-sm text-muted-foreground">Manage AdFix Studio courses and platform operations.</p>
      <Card title="ADFIX HIGH-PERFORMING ADS COURSE" action={<Link href="/admin/courses/adfix-course" className="font-mono text-[10px] text-accent">MANAGE →</Link>}>
        <div className="grid gap-4 text-sm sm:grid-cols-4">
          <div><span className="text-xs text-muted-foreground">STATUS</span><p className="mt-1"><Badge tone="good">{course.status.toUpperCase()}</Badge></p></div>
          <div><span className="text-xs text-muted-foreground">PRICE</span><p className="mt-1 font-bold">{formatCurrency(course.price, course.currency)}</p></div>
          <div><span className="text-xs text-muted-foreground">STUDENTS</span><p className="mt-1 font-bold">{course.studentCount}</p></div>
          <div><span className="text-xs text-muted-foreground">VIDEO HOST</span><p className="mt-1">External Platform</p></div>
        </div>
      </Card>
    </div>
  )
}

export function AdminCourseDetail() {
  const course = mockCourses[0]
  const [items, setItems] = useState(mockLessons)

  return (
    <div className="flex flex-col gap-6">
      <div className="grid gap-3 sm:grid-cols-4">
        {([["PRICE", formatCurrency(course.price, course.currency)], ["STATUS", course.status.toUpperCase()], ["STUDENTS", String(course.studentCount)], ["COMPLETION", `${course.completionRate}%`]] as const).map(([a, b]) => (
          <div className="rounded-lg border border-border bg-card p-5" key={a}>
            <div className="font-mono text-[9px] text-muted-foreground">{a}</div>
            <div className="mt-2 font-bold">{b}</div>
          </div>
        ))}
      </div>

      <Card title="COURSE DETAILS">
        <p className="text-sm leading-6 text-muted-foreground">{course.description} Access type: {course.accessType}.</p>
      </Card>

      <section className="rounded-lg border border-border bg-card">
        <div className="flex items-center justify-between border-b border-border p-5">
          <h2 className="font-mono text-xs font-bold tracking-widest">LESSONS</h2>
          <button
            onClick={() => setItems([...items, { id: `lesson-${items.length + 1}`, courseId: "adfix-course", title: "New Lesson", description: "", order: items.length + 1, status: "draft", hasPreview: false }])}
            className="rounded-md bg-foreground px-3 py-2 font-mono text-[10px] text-background"
          >
            + ADD LESSON
          </button>
        </div>
        {items.map((lesson, index) => (
          <div className="flex flex-col gap-3 border-b border-border p-5 last:border-0 sm:flex-row sm:items-center sm:justify-between" key={lesson.id}>
            <div className="flex items-center gap-4">
              <span className="font-mono text-xs text-muted-foreground">{String(index + 1).padStart(2, "0")}</span>
              <div>
                <div className="font-semibold">{lesson.title}</div>
                <div className="mt-1 text-xs text-muted-foreground">{lesson.hasPreview ? "Preview Available" : "No preview"}</div>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Badge>{lesson.status.toUpperCase()}</Badge>
              <Badge>EXTERNALLY HOSTED VIDEO</Badge>
            </div>
          </div>
        ))}
      </section>
    </div>
  )
}

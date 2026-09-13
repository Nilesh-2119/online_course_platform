"use client"

import Link from "next/link"
import { useState } from "react"
import { useRouter } from "next/navigation"
import { 
  UserPlus, 
  Check, 
  Copy, 
  Eye, 
  EyeOff, 
  RefreshCw, 
  AlertCircle, 
  ArrowLeft, 
  ShieldCheck, 
  BookOpen, 
  KeyRound,
  ExternalLink
} from "lucide-react"
import { createAdminUser, type AdminUserItem } from "@/lib/api/admin-service"
import { cn } from "@/lib/utils"

export function CreateUserForm() {
  const router = useRouter()
  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [phone, setPhone] = useState("")
  const [password, setPassword] = useState("")
  const [grantCourseAccess, setGrantCourseAccess] = useState(true)
  const [showPassword, setShowPassword] = useState(false)

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [createdUser, setCreatedUser] = useState<AdminUserItem | null>(null)
  const [createdPassword, setCreatedPassword] = useState<string>("")
  const [copied, setCopied] = useState(false)

  // Quick password generator
  const generatePassword = () => {
    const chars = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    let randomPart = ""
    for (let i = 0; i < 4; i++) {
      randomPart += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    const pin = Math.floor(1000 + Math.random() * 9000)
    const newPass = `Adfix@${randomPart}${pin}`
    setPassword(newPass)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim() || !email.trim() || !password.trim()) {
      setError("Please fill in all required fields (Name, Email, Password).")
      return
    }

    if (password.trim().length < 6) {
      setError("Password must be at least 6 characters long.")
      return
    }

    try {
      setLoading(true)
      setError(null)

      const res = await createAdminUser({
        name: name.trim(),
        email: email.trim(),
        phone: phone.trim() || undefined,
        password: password.trim(),
        grantCourseAccess,
      })

      if (res.success && res.data) {
        setCreatedUser(res.data)
        setCreatedPassword(password.trim())
      } else {
        setError(res.error || "Failed to create user. Please check your inputs.")
      }
    } catch (err: any) {
      setError(err?.message || "An unexpected error occurred while creating user.")
    } finally {
      setLoading(false)
    }
  }

  const handleCopyCredentials = () => {
    if (!createdUser) return
    const text = `🎓 *AdFix Studio Course Access Details*

Hello ${createdUser.name},
Your account has been created and granted full access to the course!

🔗 *Login URL:* https://adfixstudio.com/login
📧 *Email:* ${createdUser.email}
🔑 *Password:* ${createdPassword}

You can log in directly and start learning immediately.`

    navigator.clipboard.writeText(text)
    setCopied(true)
    setTimeout(() => setCopied(false), 3000)
  }

  const handleReset = () => {
    setName("")
    setEmail("")
    setPhone("")
    setPassword("")
    setGrantCourseAccess(true)
    setCreatedUser(null)
    setCreatedPassword("")
    setError(null)
    setCopied(false)
  }

  // ---------------------------------------------------------------------------
  // Success Screen
  // ---------------------------------------------------------------------------
  if (createdUser) {
    return (
      <div className="max-w-2xl">
        <div className="rounded-lg border border-border bg-card p-6 sm:p-8">
          <div className="flex items-center gap-3 text-accent">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-accent/10 border border-accent/20">
              <ShieldCheck size={20} className="text-accent" />
            </div>
            <div>
              <h2 className="font-mono text-sm tracking-wider font-bold text-foreground">
                STUDENT ACCOUNT CREATED SUCCESSFULLY
              </h2>
              <p className="font-mono text-[11px] text-muted-foreground mt-0.5">
                Active in database • Verified credentials • No OTP required
              </p>
            </div>
          </div>

          <div className="mt-6 rounded-md border border-border bg-background/50 p-5 space-y-3">
            <div className="flex flex-col sm:flex-row sm:justify-between py-1 border-b border-border/50 text-xs font-mono">
              <span className="text-muted-foreground">STUDENT NAME</span>
              <span className="font-sans font-medium text-foreground">{createdUser.name}</span>
            </div>
            <div className="flex flex-col sm:flex-row sm:justify-between py-1 border-b border-border/50 text-xs font-mono">
              <span className="text-muted-foreground">EMAIL ADDRESS</span>
              <span className="font-mono font-medium text-foreground">{createdUser.email}</span>
            </div>
            {createdUser.phone && (
              <div className="flex flex-col sm:flex-row sm:justify-between py-1 border-b border-border/50 text-xs font-mono">
                <span className="text-muted-foreground">PHONE NUMBER</span>
                <span className="font-mono text-foreground">{createdUser.phone}</span>
              </div>
            )}
            <div className="flex flex-col sm:flex-row sm:justify-between py-1 border-b border-border/50 text-xs font-mono items-start sm:items-center">
              <span className="text-muted-foreground">PASSWORD</span>
              <span className="font-mono font-bold text-accent bg-accent/10 px-2 py-0.5 rounded border border-accent/20">
                {createdPassword}
              </span>
            </div>
            <div className="flex flex-col sm:flex-row sm:justify-between py-1 border-b border-border/50 text-xs font-mono">
              <span className="text-muted-foreground">ACCOUNT STATUS</span>
              <span className="inline-flex items-center gap-1.5 text-accent font-medium">
                <span className="h-2 w-2 rounded-full bg-accent"></span>
                ACTIVE (No OTP Required)
              </span>
            </div>
            <div className="flex flex-col sm:flex-row sm:justify-between py-1 text-xs font-mono">
              <span className="text-muted-foreground">COURSE ACCESS</span>
              <span className={cn("font-medium", createdUser.access ? "text-accent" : "text-muted-foreground")}>
                {createdUser.access ? "FULL ACCESS GRANTED (PAID/ENROLLED)" : "FREE ACCOUNT"}
              </span>
            </div>
          </div>

          <div className="mt-6 flex flex-col sm:flex-row gap-3">
            <button
              onClick={handleCopyCredentials}
              className="inline-flex items-center justify-center gap-2 rounded-md bg-foreground px-5 py-3 font-mono text-[11px] tracking-widest text-background transition hover:opacity-90 flex-1"
            >
              {copied ? (
                <>
                  <Check size={14} className="text-background" /> COPIED TO CLIPBOARD!
                </>
              ) : (
                <>
                  <Copy size={14} /> COPY LOGIN DETAILS MESSAGE
                </>
              )}
            </button>

            <Link
              href={`/admin/users/${createdUser.id}`}
              className="inline-flex items-center justify-center gap-2 rounded-md border border-border px-4 py-3 font-mono text-[11px] tracking-widest hover:bg-muted/30"
            >
              VIEW USER <ExternalLink size={12} />
            </Link>
          </div>

          <div className="mt-4 flex items-center justify-between border-t border-border pt-4">
            <button
              onClick={handleReset}
              className="inline-flex items-center gap-1.5 font-mono text-[10px] tracking-wider text-muted-foreground hover:text-foreground"
            >
              <UserPlus size={12} /> CREATE ANOTHER USER
            </button>
            <Link
              href="/admin/users"
              className="font-mono text-[10px] tracking-wider text-muted-foreground hover:text-foreground"
            >
              ← BACK TO USERS LIST
            </Link>
          </div>
        </div>
      </div>
    )
  }

  // ---------------------------------------------------------------------------
  // Creation Form
  // ---------------------------------------------------------------------------
  return (
    <div className="max-w-2xl">
      <div className="mb-6 flex items-center justify-between">
        <Link
          href="/admin/users"
          className="inline-flex items-center gap-1.5 font-mono text-[10px] tracking-widest text-muted-foreground hover:text-foreground transition"
        >
          <ArrowLeft size={12} /> BACK TO USERS LIST
        </Link>
      </div>

      <div className="rounded-lg border border-border bg-card p-6 sm:p-8">
        <div className="mb-6 border-b border-border pb-5">
          <div className="flex items-center gap-2.5">
            <UserPlus size={18} className="text-foreground" />
            <h1 className="font-mono text-sm tracking-wider font-bold text-foreground">
              ADD NEW USER ACCOUNT
            </h1>
          </div>
          <p className="mt-1.5 text-xs text-muted-foreground">
            Create an active user directly in the database. The user can log in immediately with their credentials without needing OTP verification.
          </p>
        </div>

        {error && (
          <div className="mb-6 flex items-start gap-3 rounded-md border border-red-500/30 bg-red-500/10 p-4 text-red-400">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <div className="text-xs font-mono">{error}</div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          {/* Name Field */}
          <div>
            <label className="block font-mono text-[10px] tracking-widest text-foreground">
              FULL NAME <span className="text-red-500">*</span>
            </label>
            <input
              required
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={loading}
              placeholder="e.g. Rahul Sharma"
              className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm focus:border-foreground focus:outline-none transition disabled:opacity-50"
            />
          </div>

          {/* Email Field */}
          <div>
            <label className="block font-mono text-[10px] tracking-widest text-foreground">
              EMAIL ADDRESS <span className="text-red-500">*</span>
            </label>
            <input
              required
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
              placeholder="e.g. rahul@example.com"
              className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm focus:border-foreground focus:outline-none transition disabled:opacity-50"
            />
            <span className="mt-1 block font-mono text-[10px] text-muted-foreground">
              This will be the student&apos;s login identifier.
            </span>
          </div>

          {/* Phone Field */}
          <div>
            <label className="block font-mono text-[10px] tracking-widest text-foreground">
              PHONE NUMBER <span className="text-muted-foreground">(OPTIONAL)</span>
            </label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              disabled={loading}
              placeholder="e.g. +91 98765 43210"
              className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm focus:border-foreground focus:outline-none transition disabled:opacity-50"
            />
          </div>

          {/* Password Field */}
          <div>
            <div className="flex items-center justify-between">
              <label className="block font-mono text-[10px] tracking-widest text-foreground">
                PASSWORD <span className="text-red-500">*</span>
              </label>
              <button
                type="button"
                onClick={generatePassword}
                disabled={loading}
                className="inline-flex items-center gap-1 font-mono text-[10px] tracking-wider text-accent hover:underline disabled:opacity-50"
              >
                <KeyRound size={11} /> GENERATE RANDOM
              </button>
            </div>
            <div className="relative mt-2">
              <input
                required
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
                placeholder="Set password for user (min 6 characters)"
                className="w-full rounded-md border border-border bg-background p-3 pr-10 font-mono text-sm focus:border-foreground focus:outline-none transition disabled:opacity-50"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                title={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
            <span className="mt-1 block font-mono text-[10px] text-muted-foreground">
              Provide this password to the user so they can log in at the login page.
            </span>
          </div>

          {/* Course Access Checkbox */}
          <div className="rounded-md border border-border bg-background/50 p-4">
            <label className="flex items-start gap-3 cursor-pointer">
              <input
                type="checkbox"
                checked={grantCourseAccess}
                onChange={(e) => setGrantCourseAccess(e.target.checked)}
                disabled={loading}
                className="mt-1 h-4 w-4 rounded border-border text-foreground focus:ring-0 focus:outline-none"
              />
              <div>
                <span className="font-mono text-xs font-medium text-foreground flex items-center gap-1.5">
                  <BookOpen size={13} className="text-accent" />
                  GRANT INSTANT COURSE ACCESS (ENROLLED STUDENT)
                </span>
                <p className="mt-1 text-xs text-muted-foreground">
                  Automatically mark as paid/enrolled for the AdFix Studio course. The student can start watching videos right after login.
                </p>
              </div>
            </label>
          </div>

          {/* Instant Activation Info Box */}
          <div className="rounded-md border border-accent/20 bg-accent/5 p-4 flex items-start gap-3 text-xs">
            <ShieldCheck size={16} className="text-accent mt-0.5 shrink-0" />
            <div className="text-muted-foreground">
              <strong className="text-foreground font-mono text-[11px] block mb-0.5">NO OTP REQUIRED FOR ADMIN CREATION</strong>
              This account will be created directly as <span className="text-accent font-mono">ACTIVE</span> with verified status in MySQL. The user will not be prompted for email or phone OTP verification.
            </div>
          </div>

          {/* Submit Actions */}
          <div className="mt-2 flex flex-wrap gap-3">
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center justify-center gap-2 rounded-md bg-foreground px-6 py-3 font-mono text-[11px] tracking-widest text-background transition hover:opacity-90 disabled:opacity-50"
            >
              {loading ? (
                <>
                  <RefreshCw size={13} className="animate-spin" /> CREATING USER...
                </>
              ) : (
                <>
                  <UserPlus size={13} /> CREATE USER &amp; GRANT ACCESS
                </>
              )}
            </button>

            <Link
              href="/admin/users"
              className="inline-flex items-center justify-center rounded-md border border-border px-5 py-3 font-mono text-[11px] tracking-widest hover:bg-muted/30"
            >
              CANCEL
            </Link>
          </div>
        </form>
      </div>
    </div>
  )
}

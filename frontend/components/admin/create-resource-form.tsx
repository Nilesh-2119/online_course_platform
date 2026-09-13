"use client"

import Link from "next/link"
import { useState, useRef } from "react"
import { 
  UploadCloud, 
  FileText, 
  FileSpreadsheet, 
  FileArchive, 
  File, 
  X, 
  Check, 
  RefreshCw, 
  ArrowLeft, 
  AlertCircle, 
  Link as LinkIcon, 
  ExternalLink 
} from "lucide-react"
import { createAdminResource, type AdminResourceItem } from "@/lib/api/resource-service"
import { cn } from "@/lib/utils"

export function CreateResourceForm() {
  const [title, setTitle] = useState("")
  const [description, setDescription] = useState("")
  const [resourceType, setResourceType] = useState<string>("PDF")
  const [status, setStatus] = useState<string>("PUBLISHED")
  const [mode, setMode] = useState<"file" | "link">("file")
  const [externalUrl, setExternalUrl] = useState("")
  const [file, setFile] = useState<File | null>(null)

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [createdItem, setCreatedItem] = useState<AdminResourceItem | null>(null)

  const fileInputRef = useRef<HTMLInputElement>(null)

  const detectTypeFromFileName = (fileName: string): string => {
    const lower = fileName.toLowerCase()
    if (lower.endsWith(".pdf")) return "PDF"
    if (lower.endsWith(".xlsx") || lower.endsWith(".xls") || lower.endsWith(".csv")) return "EXCEL"
    if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")) return "TEMPLATE"
    if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "DOCUMENT"
    if (lower.endsWith(".js") || lower.endsWith(".ts") || lower.endsWith(".py") || lower.endsWith(".html")) return "CODE"
    return "OTHER"
  }

  const handleFileChange = (selectedFile: File | null) => {
    if (!selectedFile) return
    setFile(selectedFile)
    const detected = detectTypeFromFileName(selectedFile.name)
    setResourceType(detected)

    // Suggest title if empty
    if (!title.trim()) {
      const cleanName = selectedFile.name.replace(/\.[^/.]+$/, "").replace(/[_-]/g, " ")
      setTitle(cleanName.charAt(0).toUpperCase() + cleanName.slice(1))
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!title.trim()) {
      setError("Resource title is required.")
      return
    }

    if (mode === "file" && !file) {
      setError("Please select a file to upload (PDF, Excel, Word, Zip, etc.).")
      return
    }

    if (mode === "link" && !externalUrl.trim()) {
      setError("Please enter a valid external resource URL.")
      return
    }

    try {
      setLoading(true)
      setError(null)

      const formData = new FormData()
      formData.append("title", title.trim())
      if (description.trim()) {
        formData.append("description", description.trim())
      }
      formData.append("resourceType", resourceType)
      formData.append("status", status)

      if (mode === "file" && file) {
        formData.append("file", file)
      } else if (mode === "link" && externalUrl.trim()) {
        formData.append("externalUrl", externalUrl.trim())
      }

      const res = await createAdminResource(formData)
      if (res.success && res.data) {
        setCreatedItem(res.data)
      } else {
        setError(res.error || "Failed to create resource. Please try again.")
      }
    } catch (err: any) {
      setError(err?.message || "An error occurred while uploading resource.")
    } finally {
      setLoading(false)
    }
  }

  const handleReset = () => {
    setTitle("")
    setDescription("")
    setResourceType("PDF")
    setStatus("PUBLISHED")
    setMode("file")
    setExternalUrl("")
    setFile(null)
    setError(null)
    setCreatedItem(null)
  }

  // ---------------------------------------------------------------------------
  // Success View
  // ---------------------------------------------------------------------------
  if (createdItem) {
    return (
      <div className="max-w-2xl">
        <div className="rounded-lg border border-border bg-card p-6 sm:p-8">
          <div className="flex items-center gap-3 text-accent">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-accent/10 border border-accent/20">
              <Check size={20} className="text-accent" />
            </div>
            <div>
              <h2 className="font-mono text-sm tracking-wider font-bold text-foreground">
                FREE RESOURCE PUBLISHED SUCCESSFULLY
              </h2>
              <p className="font-mono text-[11px] text-muted-foreground mt-0.5">
                Students and registered users can now download and view this resource from their dashboard.
              </p>
            </div>
          </div>

          <div className="mt-6 rounded-md border border-border bg-background/50 p-5 space-y-3 font-mono text-xs">
            <div className="flex justify-between py-1 border-b border-border/50">
              <span className="text-muted-foreground">TITLE</span>
              <span className="font-sans font-bold text-foreground">{createdItem.title}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-border/50">
              <span className="text-muted-foreground">RESOURCE TYPE</span>
              <span className="font-bold text-accent">{createdItem.resourceType}</span>
            </div>
            {createdItem.fileName && (
              <div className="flex justify-between py-1 border-b border-border/50">
                <span className="text-muted-foreground">FILE NAME</span>
                <span className="text-foreground">{createdItem.fileName}</span>
              </div>
            )}
            <div className="flex justify-between py-1 border-b border-border/50">
              <span className="text-muted-foreground">STATUS</span>
              <span className="text-accent">{createdItem.status}</span>
            </div>
            {createdItem.resourceUrl && (
              <div className="flex justify-between py-1 items-center">
                <span className="text-muted-foreground">URL / LINK</span>
                <a
                  href={createdItem.resourceUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-accent underline flex items-center gap-1 text-[11px]"
                >
                  TEST OPEN FILE <ExternalLink size={11} />
                </a>
              </div>
            )}
          </div>

          <div className="mt-6 flex flex-wrap gap-3">
            <button
              onClick={handleReset}
              className="inline-flex items-center justify-center gap-2 rounded-md bg-foreground px-5 py-3 font-mono text-[11px] tracking-widest text-background transition hover:opacity-90"
            >
              ADD ANOTHER RESOURCE
            </button>
            <Link
              href="/admin/resources"
              className="inline-flex items-center justify-center rounded-md border border-border px-5 py-3 font-mono text-[11px] tracking-widest hover:bg-muted/30"
            >
              VIEW ALL RESOURCES
            </Link>
          </div>
        </div>
      </div>
    )
  }

  // ---------------------------------------------------------------------------
  // Upload / Creation Form
  // ---------------------------------------------------------------------------
  return (
    <div className="max-w-2xl">
      <div className="mb-6 flex items-center justify-between">
        <Link
          href="/admin/resources"
          className="inline-flex items-center gap-1.5 font-mono text-[10px] tracking-widest text-muted-foreground hover:text-foreground transition"
        >
          <ArrowLeft size={12} /> BACK TO RESOURCES
        </Link>
      </div>

      <div className="rounded-lg border border-border bg-card p-6 sm:p-8">
        <div className="mb-6 border-b border-border pb-5">
          <div className="flex items-center gap-2.5">
            <UploadCloud size={20} className="text-foreground" />
            <h1 className="font-mono text-sm tracking-wider font-bold text-foreground">
              ADD FREE RESOURCE
            </h1>
          </div>
          <p className="mt-1.5 text-xs text-muted-foreground">
            Upload PDF swipe files, Excel spreadsheets, templates, or link external resources for your students.
          </p>
        </div>

        {error && (
          <div className="mb-6 flex items-start gap-3 rounded-md border border-red-500/30 bg-red-500/10 p-4 text-red-400">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <div className="text-xs font-mono">{error}</div>
          </div>
        )}

        {/* Mode Selector: Direct File Upload vs External Link */}
        <div className="mb-6 flex rounded-lg border border-border p-1 bg-background">
          <button
            type="button"
            onClick={() => setMode("file")}
            className={cn(
              "flex-1 rounded-md py-2 font-mono text-xs tracking-wider transition flex items-center justify-center gap-2",
              mode === "file" ? "bg-card text-foreground font-bold shadow-sm" : "text-muted-foreground hover:text-foreground"
            )}
          >
            <UploadCloud size={14} /> UPLOAD FILE (PDF, EXCEL, ETC.)
          </button>
          <button
            type="button"
            onClick={() => setMode("link")}
            className={cn(
              "flex-1 rounded-md py-2 font-mono text-xs tracking-wider transition flex items-center justify-center gap-2",
              mode === "link" ? "bg-card text-foreground font-bold shadow-sm" : "text-muted-foreground hover:text-foreground"
            )}
          >
            <LinkIcon size={14} /> EXTERNAL URL / GOOGLE DRIVE
          </button>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          {/* File Upload Zone */}
          {mode === "file" && (
            <div>
              <label className="block font-mono text-[10px] tracking-widest text-foreground mb-2">
                RESOURCE FILE <span className="text-red-500">*</span>
              </label>

              <input
                ref={fileInputRef}
                type="file"
                accept=".pdf,.xlsx,.xls,.csv,.doc,.docx,.zip,.rar,.png,.jpg,.jpeg,.txt"
                className="hidden"
                onChange={(e) => handleFileChange(e.target.files?.[0] || null)}
              />

              {!file ? (
                <div
                  onClick={() => fileInputRef.current?.click()}
                  onDragOver={(e) => e.preventDefault()}
                  onDrop={(e) => {
                    e.preventDefault()
                    if (e.dataTransfer.files?.[0]) {
                      handleFileChange(e.dataTransfer.files[0])
                    }
                  }}
                  className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-border p-8 text-center cursor-pointer hover:border-foreground/40 hover:bg-muted/10 transition"
                >
                  <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted/30 mb-3">
                    <UploadCloud size={24} className="text-muted-foreground" />
                  </div>
                  <span className="font-mono text-xs font-bold text-foreground">
                    CLICK TO CHOOSE FILE OR DRAG &amp; DROP HERE
                  </span>
                  <p className="mt-1 text-xs text-muted-foreground">
                    Supports PDF, Excel (.xlsx, .xls, .csv), Word (.docx), Zip templates up to 50MB
                  </p>
                </div>
              ) : (
                <div className="flex items-center justify-between rounded-lg border border-border bg-background p-4">
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-md bg-muted/40">
                      {file.name.endsWith(".pdf") ? (
                        <FileText className="size-5 text-red-500" />
                      ) : file.name.endsWith(".xlsx") || file.name.endsWith(".csv") ? (
                        <FileSpreadsheet className="size-5 text-emerald-500" />
                      ) : (
                        <File className="size-5 text-muted-foreground" />
                      )}
                    </div>
                    <div className="min-w-0">
                      <p className="font-sans text-xs font-bold text-foreground truncate">
                        {file.name}
                      </p>
                      <p className="font-mono text-[10px] text-muted-foreground">
                        {(file.size / (1024 * 1024)).toFixed(2)} MB • Auto-detected: {resourceType}
                      </p>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => setFile(null)}
                    className="p-1 text-muted-foreground hover:text-foreground rounded"
                    title="Remove file"
                  >
                    <X size={16} />
                  </button>
                </div>
              )}
            </div>
          )}

          {/* External URL Field */}
          {mode === "link" && (
            <div>
              <label className="block font-mono text-[10px] tracking-widest text-foreground">
                EXTERNAL RESOURCE URL <span className="text-red-500">*</span>
              </label>
              <input
                required
                type="url"
                value={externalUrl}
                onChange={(e) => setExternalUrl(e.target.value)}
                disabled={loading}
                placeholder="https://drive.google.com/file/... or https://docs.google.com/spreadsheets/..."
                className="mt-2 w-full rounded-md border border-border bg-background p-3 font-mono text-sm focus:border-foreground focus:outline-none transition disabled:opacity-50"
              />
              <span className="mt-1 block font-mono text-[10px] text-muted-foreground">
                Ensure link sharing permissions are set to &quot;Anyone with the link can view&quot;.
              </span>
            </div>
          )}

          {/* Title Field */}
          <div>
            <label className="block font-mono text-[10px] tracking-widest text-foreground">
              RESOURCE TITLE <span className="text-red-500">*</span>
            </label>
            <input
              required
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              disabled={loading}
              placeholder="e.g. 2026 Ad Hook Swipe File &amp; Script Template"
              className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm focus:border-foreground focus:outline-none transition disabled:opacity-50"
            />
          </div>

          {/* Description Field */}
          <div>
            <label className="block font-mono text-[10px] tracking-widest text-foreground">
              DESCRIPTION
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              disabled={loading}
              placeholder="Describe what this resource provides and how students can use it in their campaigns."
              className="mt-2 w-full rounded-md border border-border bg-background p-3 font-sans text-sm focus:border-foreground focus:outline-none transition disabled:opacity-50"
            />
          </div>

          {/* Resource Type & Status */}
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="block font-mono text-[10px] tracking-widest text-foreground">
                RESOURCE TYPE
              </label>
              <select
                value={resourceType}
                onChange={(e) => setResourceType(e.target.value)}
                disabled={loading}
                className="mt-2 w-full rounded-md border border-border bg-background p-3 font-mono text-xs focus:border-foreground focus:outline-none transition"
              >
                <option value="PDF">PDF Document</option>
                <option value="EXCEL">Excel / Spreadsheet (.xlsx, .csv)</option>
                <option value="TEMPLATE">Template / Archive (.zip)</option>
                <option value="DOCUMENT">Word Document (.docx)</option>
                <option value="LINK">External Web Link</option>
                <option value="CODE">Code Snippet</option>
                <option value="OTHER">Other Format</option>
              </select>
            </div>

            <div>
              <label className="block font-mono text-[10px] tracking-widest text-foreground">
                STATUS
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value)}
                disabled={loading}
                className="mt-2 w-full rounded-md border border-border bg-background p-3 font-mono text-xs focus:border-foreground focus:outline-none transition"
              >
                <option value="PUBLISHED">PUBLISHED (Visible to Students)</option>
                <option value="DRAFT">DRAFT (Hidden)</option>
              </select>
            </div>
          </div>

          {/* Submit Actions */}
          <div className="mt-3 flex flex-wrap gap-3">
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center justify-center gap-2 rounded-md bg-foreground px-6 py-3 font-mono text-[11px] tracking-widest text-background transition hover:opacity-90 disabled:opacity-50"
            >
              {loading ? (
                <>
                  <RefreshCw size={13} className="animate-spin" /> UPLOADING &amp; PUBLISHING...
                </>
              ) : (
                <>
                  <UploadCloud size={14} /> PUBLISH RESOURCE
                </>
              )}
            </button>

            <Link
              href="/admin/resources"
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

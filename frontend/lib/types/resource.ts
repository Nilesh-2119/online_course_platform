/** Free resource domain types. */

export type ResourceStatus = "draft" | "published"
export type ResourceType = "pdf" | "doc" | "spreadsheet" | "archive" | "other"

export interface FreeResource {
  id: string
  title: string
  description: string
  type: ResourceType
  fileName?: string
  fileSize?: number // bytes
  status: ResourceStatus
  downloadCount: number
  createdAt: string // ISO 8601
  updatedAt?: string // ISO 8601
  /** Populated by the backend on download request; may be a signed/temporary URL. */
  downloadUrl?: string
}

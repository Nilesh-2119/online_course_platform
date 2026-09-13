/** Generic API response wrapper types. */

export interface ApiResponse<T> {
  data: T
  success: boolean
  error?: string
}

export type LoadingState = "idle" | "loading" | "success" | "error"

export interface PaginatedResponse<T> {
  data: T[]
  total: number
  page: number
  pageSize: number
}

export interface ApiError {
  message: string
  code?: string
  status?: number
}

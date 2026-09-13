/**
 * Mock adapter that simulates async backend responses.
 * When a real backend is connected, this file is no longer needed.
 */

import type { ApiResponse, ApiError } from "@/lib/types"

const MOCK_DELAY_MS = 300

/** Simulate a successful async API response with optional delay. */
export async function mockSuccess<T>(data: T, delayMs = MOCK_DELAY_MS): Promise<ApiResponse<T>> {
  await delay(delayMs)
  return { data, success: true }
}

/** Simulate a failed async API response. */
export async function mockError<T>(message: string, delayMs = MOCK_DELAY_MS): Promise<ApiResponse<T>> {
  await delay(delayMs)
  return { data: undefined as unknown as T, success: false, error: message }
}

/** Create an ApiError object. */
export function createApiError(message: string, code?: string, status?: number): ApiError {
  return { message, code, status }
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

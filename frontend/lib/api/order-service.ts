/**
 * Order service.
 *
 * BACKEND_INTEGRATION_POINT: Replace mock implementations with real API calls.
 */

import type { ApiResponse, Order } from "@/lib/types"
import { mockSuccess } from "@/mocks/mock-adapter"
import { mockOrders } from "@/mocks/seed-data"

/** BACKEND_INTEGRATION_POINT: Fetch all orders (admin). */
export async function getOrders(): Promise<ApiResponse<Order[]>> {
  return mockSuccess(mockOrders)
}

/** BACKEND_INTEGRATION_POINT: Fetch a single order by ID (admin). */
export async function getOrder(id: string): Promise<ApiResponse<Order | null>> {
  const order = mockOrders.find((o) => o.id === id || o.id === `AFX-${id}`) ?? null
  return mockSuccess(order)
}

/** BACKEND_INTEGRATION_POINT: Fetch orders for current user. */
export async function getUserOrders(): Promise<ApiResponse<Order[]>> {
  return mockSuccess(mockOrders.filter((o) => o.status === "paid"))
}

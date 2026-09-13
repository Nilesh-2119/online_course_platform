import { NextResponse } from "next/server"
import type { NextRequest } from "next/server"

export function middleware(request: NextRequest) {
  const host = request.headers.get("host") || ""
  const { pathname } = request.nextUrl

  // Check if accessing via admin subdomain (e.g. lighthousedashboard.adfixstudio.com or admin.adfixstudio.com)
  const isAdminSubdomain = host.startsWith("admin.") || host.startsWith("lighthousedashboard.")

  if (isAdminSubdomain) {
    // If accessing root "/" or student "/dashboard" on admin subdomain, redirect to "/admin"
    if (pathname === "/" || pathname === "/dashboard" || (!pathname.startsWith("/admin") && !pathname.startsWith("/api") && !pathname.startsWith("/login") && !pathname.startsWith("/forgot-password"))) {
      const url = request.nextUrl.clone()
      url.pathname = "/admin"
      return NextResponse.redirect(url)
    }
  }

  return NextResponse.next()
}

export const config = {
  matcher: [
    /*
     * Match all paths except:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    "/((?!_next/static|_next/image|favicon.ico).*)",
  ],
}

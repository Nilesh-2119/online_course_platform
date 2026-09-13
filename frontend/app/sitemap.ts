import type { MetadataRoute } from "next"

export default function sitemap(): MetadataRoute.Sitemap {
  const baseUrl = process.env.NEXT_PUBLIC_APP_URL || "https://adfixstudio.com"
  const lastModified = new Date()

  const routes = [
    "",
    "/buy",
    "/free-resources",
    "/terms",
    "/privacy",
    "/refund-policy",
    "/cookie-policy",
    "/course-usage",
    "/disclaimer",
    "/contact",
  ]

  return routes.map((route) => ({
    url: `${baseUrl}${route}`,
    lastModified,
    changeFrequency: route === "" ? "weekly" : "monthly",
    priority: route === "" ? 1.0 : route === "/buy" ? 0.9 : 0.6,
  }))
}

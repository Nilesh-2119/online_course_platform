import type { MetadataRoute } from "next"

export default function robots(): MetadataRoute.Robots {
  const baseUrl = process.env.NEXT_PUBLIC_APP_URL || "https://adfixstudio.com"

  return {
    rules: [
      {
        userAgent: "*",
        allow: "/",
        disallow: ["/admin/", "/dashboard/", "/checkout", "/account-setup", "/onboarding"],
      },
    ],
    sitemap: `${baseUrl}/sitemap.xml`,
  }
}

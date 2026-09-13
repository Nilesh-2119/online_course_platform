import type React from "react"
import type { Metadata, Viewport } from "next"
import { Inter, JetBrains_Mono } from "next/font/google"
import { Analytics } from "@vercel/analytics/next"
import { AuthProvider } from "@/lib/auth"
import "./globals.css"

const _inter = Inter({
  subsets: ["latin"],
  variable: "--font-sans",
})

const _jetbrainsMono = JetBrains_Mono({
  subsets: ["latin"],
  variable: "--font-mono",
})

const baseUrl = process.env.NEXT_PUBLIC_APP_URL || "https://adfixstudio.com"

export const metadata: Metadata = {
  metadataBase: new URL(baseUrl),
  title: {
    default: "AdFix Studio | Make Better-Performing Ads",
    template: "%s | AdFix Studio",
  },
  description:
    "AdFix Studio helps creators and brands turn average ads into high-performing stories through better hooks, scripts, storytelling, CTAs and strategy.",
  keywords: ["ad creative", "advertising strategy", "hooks", "ad scripts", "AdFix Studio", "high-conversion ads"],
  authors: [{ name: "AdFix Studio" }],
  creator: "AdFix Studio",
  openGraph: {
    type: "website",
    locale: "en_US",
    url: baseUrl,
    siteName: "AdFix Studio",
    title: "AdFix Studio | Make Better-Performing Ads",
    description:
      "Turn your average ad into a high-performing story through better hooks, scripts, storytelling, CTAs and strategy.",
  },
  twitter: {
    card: "summary_large_image",
    title: "AdFix Studio | Make Better-Performing Ads",
    description:
      "Turn your average ad into a high-performing story through better hooks, scripts, storytelling, CTAs and strategy.",
  },
  robots: {
    index: true,
    follow: true,
  },
}

export const viewport: Viewport = {
  themeColor: "#AFFF00",
  width: "device-width",
  initialScale: 1,
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="en" className="bg-background">
      <body className="font-sans antialiased">
        <a
          href="#main-content"
          className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-md focus:bg-foreground focus:px-4 focus:py-2 focus:font-mono focus:text-xs focus:text-background"
        >
          Skip to main content
        </a>
        <AuthProvider>
          <div id="main-content">
            {children}
          </div>
        </AuthProvider>
        <Analytics />
      </body>
    </html>
  )
}

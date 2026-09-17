import { NextResponse } from "next/server"

export async function GET() {
  try {
    const apiSecret = (process.env.VDOCIPHER_API_SECRET || "").trim().replace(/['"]/g, "")
    const videoId = (process.env.NEXT_PUBLIC_VSL_VIDEO_ID || "f4265eddf8334dfb98d2a34306a26d65").trim().replace(/['"]/g, "")

    if (!apiSecret || apiSecret.includes("placeholder")) {
      return NextResponse.json(
        {
          success: false,
          error: "VdoCipher API Secret is not configured. Please add VDOCIPHER_API_SECRET in your environment variables.",
          needsSecret: true,
        },
        { status: 503 }
      )
    }

    const response = await fetch(`https://dev.vdocipher.com/api/videos/${videoId}/otp`, {
      method: "POST",
      headers: {
        Authorization: `Apisecret ${apiSecret}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        ttl: 300,
      }),
      cache: "no-store",
    })

    if (!response.ok) {
      const errorText = await response.text().catch(() => "")
      console.error(`VdoCipher API returned ${response.status}:`, errorText)
      return NextResponse.json(
        {
          success: false,
          error: `Failed to request OTP from VdoCipher (${response.status}). Check if video ID ${videoId} exists.`,
        },
        { status: response.status }
      )
    }

    const data = await response.json()
    if (!data?.otp || !data?.playbackInfo) {
      return NextResponse.json(
        {
          success: false,
          error: "Incomplete playback credentials received from VdoCipher.",
        },
        { status: 502 }
      )
    }

    return NextResponse.json({
      success: true,
      otp: data.otp,
      playbackInfo: data.playbackInfo,
      playerUrl: `https://player.vdocipher.com/v2/?otp=${encodeURIComponent(data.otp)}&playbackInfo=${encodeURIComponent(data.playbackInfo)}&autoplay=true`,
    })
  } catch (err: any) {
    console.error("VSL playback OTP generation error:", err)
    return NextResponse.json(
      {
        success: false,
        error: err?.message || "Internal error generating VdoCipher credentials.",
      },
      { status: 500 }
    )
  }
}

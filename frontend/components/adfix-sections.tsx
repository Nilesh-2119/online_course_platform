"use client"

import { AnimatePresence, motion } from "framer-motion"
import { ArrowRight, Check, Play, Plus, X } from "lucide-react"
import { useEffect, useRef, useState } from "react"

const reveal = { hidden: { opacity: 0, y: 18 }, visible: { opacity: 1, y: 0, transition: { duration: 0.5 } } }
const learnItems = [["Hook Engineering", "Build openings that earn attention instead of asking for it."], ["Script Flow Rewrite", "Turn a list of features into a story people can follow."], ["High-Conversion CTAs", "Give viewers a clear reason to act."], ["Ad Strategy", "Make creative decisions support the campaign goal."]]
const modules = [["01", "Foundations", "Attention, clarity, story and intent."], ["02", "Sales", "Hooks, scripts, offers and CTAs working together."], ["03", "Outbound", "Apply ad thinking to outreach and opportunity."]]
const problems = [["The 3-Second Drop-off", "Your opening hook lacks the pattern interrupt needed to stop the scroll."], ["Confusing Script Architecture", "Feature lists without an emotional story arc leave prospects unsure."], ["Weak & Forgettable CTAs", "A plain Shop Now gives viewers no compelling next step."], ["Scaling Bad Creative", "More media spend cannot fix creative that is not built to convert."]]
const faqs = [["Who is AdFix for?", "AdFix is for creators, freelancers, marketers, founders, small businesses and agencies making ads or content for their own work or clients."], ["Is the course live or recorded?", "The course is a recorded video course."], ["What will I learn?", "You will learn hook engineering, script flow rewrite, high-conversion CTAs and ad strategy."], ["How do I get access after purchasing?", "Access details will be provided when the purchase flow is confirmed."]]
const openPurchase = () => { window.location.href = "/buy" }

/**
 * VSL (Video Sales Letter) Video Configuration
 * --------------------------------------------
 * Set your video link here or via the NEXT_PUBLIC_VSL_VIDEO_URL environment variable.
 * Supported formats:
 * - YouTube: "https://www.youtube.com/watch?v=VIDEO_ID" or "https://youtu.be/VIDEO_ID"
 * - Vimeo: "https://vimeo.com/VIDEO_ID"
 * - Direct MP4: "/vsl.mp4" (put the file in the frontend/public folder) or "https://cdn.example.com/vsl.mp4"
 * - Custom Embed / VdoCipher: Any embed URL (e.g. "https://player.vdocipher.com/...")
 */
export const VSL_VIDEO_CONFIG = {
  url: process.env.NEXT_PUBLIC_VSL_VIDEO_URL || "https://youtube.com/shorts/Ycm8t1w4Yig?si=EZawOUkehVLN4AlX",
  duration: "00:59",
}

function parseVideoSource(rawUrl?: string) {
  if (!rawUrl) return null
  const cleanUrl = rawUrl.trim()
  if (!cleanUrl) return null

  // VdoCipher embed (ensure autoplay=true is included so it plays immediately)
  if (cleanUrl.includes("player.vdocipher.com")) {
    let vdoUrl = cleanUrl
    if (!vdoUrl.includes("autoplay=")) {
      vdoUrl += (vdoUrl.includes("?") ? "&" : "?") + "autoplay=true"
    }
    return {
      type: "iframe" as const,
      src: vdoUrl,
    }
  }

  // YouTube match: watch?v=, youtu.be/, embed/, shorts/
  const ytMatch = cleanUrl.match(
    /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=|shorts\/)|youtu\.be\/)([^"&?\/\s]{11})/i
  )
  if (ytMatch && ytMatch[1]) {
    return {
      type: "iframe" as const,
      src: `https://www.youtube-nocookie.com/embed/${ytMatch[1]}?autoplay=1&rel=0&modestbranding=1&playsinline=1`,
    }
  }

  // Vimeo match
  const vimeoMatch = cleanUrl.match(
    /(?:vimeo\.com\/(?:channels\/(?:\w+\/)?|groups\/[^\/]*\/videos\/|album\/(?:\d+\/)?video\/|video\/|)|player\.vimeo\.com\/video\/)(\d+)/i
  )
  if (vimeoMatch && vimeoMatch[1]) {
    return {
      type: "iframe" as const,
      src: `https://player.vimeo.com/video/${vimeoMatch[1]}?autoplay=1&badge=0`,
    }
  }

  // Direct MP4 / WebM video file
  if (cleanUrl.match(/\.(mp4|webm|ogg|m4v)(\?.*)?$/i) || cleanUrl.startsWith("/")) {
    return {
      type: "video" as const,
      src: cleanUrl,
    }
  }

  // Default fallback iframe (VdoCipher or embed URL)
  return {
    type: "iframe" as const,
    src: cleanUrl,
  }
}

export function MediaFrame({
  label = "CREATIVE PREVIEW",
  videoUrl = VSL_VIDEO_CONFIG.url,
  duration = VSL_VIDEO_CONFIG.duration,
}: {
  label?: string
  videoUrl?: string
  duration?: string
}) {
  const containerRef = useRef<HTMLDivElement>(null)
  const userClosedRef = useRef(false)
  const [isPlaying, setIsPlaying] = useState(false)
  const [showSetupGuide, setShowSetupGuide] = useState(false)
  const [dynamicPlayerUrl, setDynamicPlayerUrl] = useState<string | null>(null)
  const [loadingDynamicOtp, setLoadingDynamicOtp] = useState(false)
  const [otpError, setOtpError] = useState<string | null>(null)

  const parsed = parseVideoSource(videoUrl)
  const isVdoCipher = Boolean(videoUrl && videoUrl.includes("player.vdocipher.com"))

  // When playing is requested, if it's VdoCipher, fetch a fresh OTP from /api/vsl-playback
  useEffect(() => {
    if (!isPlaying) return

    if (isVdoCipher) {
      setLoadingDynamicOtp(true)
      setOtpError(null)

      fetch("/api/vsl-playback")
        .then((res) => res.json())
        .then((data) => {
          if (data.success && data.playerUrl) {
            setDynamicPlayerUrl(data.playerUrl)
          } else if (data.needsSecret) {
            setOtpError(data.error)
          } else {
            // Fallback to parsed URL
            setDynamicPlayerUrl(parsed?.src || videoUrl)
          }
        })
        .catch(() => {
          setDynamicPlayerUrl(parsed?.src || videoUrl)
        })
        .finally(() => {
          setLoadingDynamicOtp(false)
        })
    } else if (parsed) {
      setDynamicPlayerUrl(parsed.src)
    }
  }, [isPlaying, isVdoCipher, videoUrl, parsed])

  // Automatically start playing once the user scrolls into or lands on this section
  useEffect(() => {
    if (!containerRef.current || (!parsed && !isVdoCipher) || userClosedRef.current) return
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !userClosedRef.current) {
          setIsPlaying(true)
        }
      },
      { threshold: 0.25 }
    )
    observer.observe(containerRef.current)
    return () => observer.disconnect()
  }, [parsed, isVdoCipher])

  if (isPlaying) {
    if (loadingDynamicOtp) {
      return (
        <div ref={containerRef} className="group relative aspect-video w-full overflow-hidden rounded-3xl border border-foreground/15 bg-black text-background shadow-2xl flex flex-col items-center justify-center gap-3">
          <div className="size-8 animate-spin rounded-full border-2 border-accent border-t-transparent" />
          <p className="font-mono text-xs text-background/60 tracking-wider">CONNECTING SECURE STREAM...</p>
        </div>
      )
    }

    if (otpError) {
      return (
        <div ref={containerRef} className="relative aspect-video w-full overflow-hidden rounded-3xl border border-accent/40 bg-foreground p-6 text-background shadow-2xl flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="font-mono text-[10px] tracking-widest text-accent">VDOCIPHER SETUP REQUIRED</span>
            <button
              type="button"
              onClick={() => {
                userClosedRef.current = true
                setIsPlaying(false)
                setOtpError(null)
              }}
              aria-label="Close error notice"
              className="rounded-full border border-background/20 p-1 hover:bg-background/10"
            >
              <X className="size-4" />
            </button>
          </div>
          <div>
            <h3 className="text-lg font-bold text-accent">Dynamic VdoCipher OTP Required</h3>
            <p className="mt-2 font-mono text-xs text-background/80 leading-relaxed">
              {otpError}
            </p>
            <p className="mt-3 font-mono text-[11px] text-background/60">
              Add <span className="text-accent font-mono font-bold">VDOCIPHER_API_SECRET</span> to your <span className="text-accent font-mono">frontend/.env</span> file so the server can generate fresh, unexpiring playback sessions automatically for all mobile and desktop visitors.
            </p>
          </div>
          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={() => {
                setDynamicPlayerUrl(parsed?.src || videoUrl)
                setOtpError(null)
              }}
              className="rounded-full border border-background/20 px-4 py-1.5 font-mono text-xs font-semibold"
            >
              Try Stored Link
            </button>
          </div>
        </div>
      )
    }

    if (dynamicPlayerUrl) {
      return (
        <div ref={containerRef} className="group relative aspect-video w-full overflow-hidden rounded-3xl border border-foreground/15 bg-black text-background shadow-2xl">
          {parsed?.type === "video" ? (
            <video
              src={dynamicPlayerUrl}
              controls
              autoPlay
              playsInline
              className="h-full w-full object-contain"
            />
          ) : (
            <iframe
              src={dynamicPlayerUrl}
              title={label}
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              allowFullScreen
              className="h-full w-full border-0"
            />
          )}
          <button
            type="button"
            onClick={() => {
              userClosedRef.current = true
              setIsPlaying(false)
              setDynamicPlayerUrl(null)
            }}
            aria-label="Close video"
            className="absolute right-4 top-4 z-20 flex size-9 items-center justify-center rounded-full bg-background/80 text-foreground shadow-md backdrop-blur-sm transition-transform hover:scale-110 hover:bg-background"
          >
            <X className="size-5" />
          </button>
        </div>
      )
    }
  }

  if (showSetupGuide && !parsed) {
    return (
      <div className="relative aspect-video w-full overflow-hidden rounded-3xl border border-accent/40 bg-foreground p-6 text-background shadow-2xl flex flex-col justify-between">
        <div className="flex items-center justify-between">
          <span className="font-mono text-[10px] tracking-widest text-accent">VSL VIDEO SETUP GUIDE</span>
          <button
            type="button"
            onClick={() => setShowSetupGuide(false)}
            aria-label="Close setup guide"
            className="rounded-full border border-background/20 p-1 hover:bg-background/10"
          >
            <X className="size-4" />
          </button>
        </div>
        <div>
          <h3 className="text-xl font-black tracking-tight">How to set your VSL Video:</h3>
          <ul className="mt-2 space-y-1.5 font-mono text-xs text-background/70">
            <li>• <strong>YouTube:</strong> Paste your YouTube link (e.g. <span className="text-accent">https://youtu.be/...</span>)</li>
            <li>• <strong>Vimeo:</strong> Paste your Vimeo link (e.g. <span className="text-accent">https://vimeo.com/...</span>)</li>
            <li>• <strong>Direct MP4:</strong> Put file in <span className="text-accent">frontend/public/vsl.mp4</span> and set url to <span className="text-accent">/vsl.mp4</span></li>
          </ul>
          <p className="mt-3 font-mono text-[11px] text-background/50">
            Open <span className="text-accent">frontend/components/adfix-sections.tsx</span> and set <span className="text-accent">VSL_VIDEO_CONFIG.url</span>, or add <span className="text-accent">NEXT_PUBLIC_VSL_VIDEO_URL</span> to your <span className="text-accent">.env</span>.
          </p>
        </div>
        <div className="flex justify-end">
          <button
            type="button"
            onClick={() => setShowSetupGuide(false)}
            className="rounded-full bg-accent px-4 py-1.5 font-mono text-xs font-bold text-accent-foreground"
          >
            Got it
          </button>
        </div>
      </div>
    )
  }

  return (
    <div ref={containerRef} className="group relative aspect-video overflow-hidden rounded-3xl border border-foreground/15 bg-foreground text-background shadow-xl">
      <div className="absolute inset-y-0 right-0 w-1/2 bg-[linear-gradient(135deg,rgba(204,255,0,.18),transparent_45%,rgba(255,255,255,.08))]" />
      <div className="absolute left-5 right-5 top-5 flex justify-between font-mono text-[9px] tracking-[.2em] text-background/50">
        <span>ADFIX / {label}</span>
        <span>{duration || "00:00"}</span>
      </div>
      <button
        type="button"
        onClick={() => {
          if (parsed) {
            setIsPlaying(true)
          } else {
            setShowSetupGuide(true)
          }
        }}
        aria-label={`Play ${label}`}
        className="absolute left-1/2 top-1/2 flex size-16 -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-full bg-accent text-accent-foreground shadow-lg transition-transform group-hover:scale-110 active:scale-95"
      >
        <Play className="ml-1 size-6 fill-current" />
      </button>
      <div className="absolute bottom-5 left-5 right-5 flex items-center gap-3">
        <span className="h-px flex-1 bg-background/25" />
        <span className="font-mono text-[9px] tracking-widest text-background/50">PLAY / 16:9</span>
      </div>
    </div>
  )
}

export function AdfixHero() { return <section id="hero" className="relative flex min-h-[calc(100svh-1rem)] items-center overflow-hidden bg-background px-5 pb-12 pt-28 noise-overlay md:h-svh md:px-8"><div className="mx-auto grid w-full max-w-7xl items-center gap-10 lg:grid-cols-[1.08fr_.92fr] lg:gap-14"><motion.div initial="hidden" animate="visible" variants={reveal}><span className="inline-flex items-center gap-2 rounded-full bg-foreground px-3 py-1.5 font-mono text-[10px] tracking-[.2em] text-background"><span className="size-1.5 rounded-full bg-accent" />AD CREATIVE EDUCATION</span><h1 className="mt-7 max-w-4xl text-balance text-[2.7rem] font-black leading-[.94] tracking-[-.07em] min-[360px]:text-[2.85rem] min-[390px]:text-5xl sm:mt-5 sm:text-6xl lg:text-7xl">Turn your average ad into a <span className="text-accent">high-performing story.</span></h1><p className="mt-5 max-w-xl text-pretty font-mono text-sm leading-6 text-muted-foreground md:text-base">We help creators and brands make their ads <strong>scroll-stopping</strong>, <strong>engaging</strong>, and <strong>profitable</strong> through better hooks, scripts, storytelling, and CTA strategy.</p><div className="mt-5 flex flex-wrap gap-2">{["Hook Engineering", "Script Flow Rewrite", "High-Conversion CTAs", "Ad Strategy"].map(tag => <span key={tag} className="rounded-full border border-border px-3 py-1.5 font-mono text-[10px] text-muted-foreground">{tag}</span>)}</div><div className="mt-7 flex flex-col gap-3 sm:flex-row"><button onClick={openPurchase} className="inline-flex items-center justify-center gap-3 rounded-full bg-accent px-6 py-3.5 text-sm font-bold text-accent-foreground">GET INSTANT ACCESS <ArrowRight className="size-4" /></button><a href="#vsl" className="inline-flex items-center justify-center gap-3 rounded-full border-2 border-foreground px-6 py-3.5 text-sm font-bold">WATCH THE VSL <Play className="size-4 fill-current" /></a></div></motion.div><motion.div initial="hidden" animate="visible" variants={reveal} className="relative hidden min-h-[420px] items-center justify-center lg:flex"><div className="absolute right-8 top-4 font-mono text-xs tracking-[.25em] text-muted-foreground"></div><div className="rotate-[-5deg] rounded-[2rem] border border-foreground/10 bg-foreground p-8 text-background shadow-2xl"><p className="font-mono text-xs text-accent">MAKE IT LAND</p><p className="mt-12 max-w-xs text-6xl font-black leading-[.8] tracking-[-.08em]">THE<br />ADFIX<br /><span className="text-accent">METHOD</span></p><p className="mt-16 font-mono text-xs leading-5 text-background/60">Make people stop.<br />Make them care.<br />Make them act.<br /><span className="text-accent">HOOK → STORY → CTA</span></p></div></motion.div></div></section> }

export function VslSection({ videoUrl, duration }: { videoUrl?: string; duration?: string }) { return <section id="vsl" className="bg-foreground px-5 py-16 text-background md:px-8 md:py-20"><div className="mx-auto max-w-5xl"><motion.div initial="hidden" whileInView="visible" viewport={{ once: true }} variants={reveal}><p className="font-mono text-[10px] tracking-[.25em] text-accent">WATCH BEFORE YOU DECIDE</p><h2 className="mt-2 text-4xl font-black leading-none tracking-[-.06em] md:text-6xl">Before you buy, see how AdFix thinks.</h2><p className="mt-3 max-w-xl font-mono text-xs leading-5 text-background/60">See how we approach hooks, scripts, storytelling and CTAs before deciding if the course is right for you.</p></motion.div><div className="mt-8 max-w-2xl"><MediaFrame label="VSL / WATCH THE FRAMEWORK" videoUrl={videoUrl} duration={duration} /></div></div></section> }

const examples = [{ before: "#1 BEFORE — COMMON BORING AD LINE", beforeText: "Society me gym, pool aur 24 ghante security hai ...", after: "AFTER — LUXURY, VISUAL, “WOW” FEEL", afterText: "Yeh simple pool nahi - rooftop infinity pool hai, jahan sunset ke saath-saath aapka stress bhi doob jaata hai. Aur gym? Sirf naam ka nahi. Yahan 6 personal-training stations hain, taaki aapko kabhi wait na karna pade." }, { before: "#2 BEFORE - WEAK, ROBOTIC LINE", beforeText: "Yeh moisturizer skin ko soft banata hai. Abhi buy karein.", after: "AFTER - BENEFIT-DRIVEN & ENGAGING", afterText: "Soft skin nahi ... yeh woh glow deta hai jisse log poochte hain - 'Skincare routine kya hai?' Lightweight, non-sticky aur sirf 7 din me visible result. Bas face wash, apply - aur skin fresh, without extra effort." }, { before: "#3 BEFORE - GENERIC GYM LINE", beforeText: "Humaray gym me modern machines aur certified trainers milte hain.", after: "AFTER - EMOTION & ACCOUNTABILITY", afterText: "Machines toh sabke paas hoti hain ... par yahan aapko milta hai woh personal push jo aapka 'kal se karunga' ko 'aaj se start' me badal deta hai. Aapka goal, humari accountability." }]
function Example({ item, index }: { item: typeof examples[number]; index: number }) { return <article className="grid gap-5 border-t border-border py-8"><div className="flex flex-col gap-6"><div><p className="font-mono text-[10px] tracking-[.15em] text-muted-foreground">{item.before}</p><p className="mt-2 text-xl font-bold leading-6">“{item.beforeText}”</p></div><div><p className="font-mono text-[10px] tracking-[.15em] text-foreground">{item.after}</p><p className="mt-2 text-xl font-bold leading-6">“{item.afterText}”</p></div></div></article> }
export function BeforeAfterSection() { const [more, setMore] = useState(false); return <section id="comparison" className="bg-background px-5 py-16 md:px-8 md:py-20"><div className="mx-auto max-w-7xl"><p className="font-mono text-xs tracking-[.25em] text-muted-foreground">AD TRANSFORMATION COMPARISON</p><h2 className="mt-3 max-w-3xl text-4xl font-black leading-none tracking-[-.06em] md:text-6xl">Average Ad vs. AdFix High-<br className="hidden md:block" />Performing Story</h2><p className="mt-4 max-w-2xl font-mono text-sm leading-6 text-muted-foreground">Compare how slight adjustments in hook engineering, script flow, and CTA design completely change viewer behavior.</p><div className="mt-8"><Example item={examples[0]} index={0} />{more && <AnimatePresence>{examples.slice(1).map((item, index) => <motion.div key={item.before} initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: "auto" }}><Example item={item} index={index + 1} /></motion.div>)}</AnimatePresence>}</div><button onClick={() => setMore(!more)} className="inline-flex items-center gap-2 rounded-full border border-foreground px-5 py-2.5 font-mono text-xs font-semibold">{more ? "HIDE EXAMPLES" : "MORE EXAMPLES"}<Plus className={`size-4 transition-transform ${more ? "rotate-45" : ""}`} /></button></div></section> }

export function ProblemSection() { return <section id="problems" className="bg-foreground px-5 py-16 text-background md:px-8 md:py-20"><div className="mx-auto max-w-7xl"><p className="font-mono text-xs tracking-[.25em] text-accent">THE REALITY OF VIDEO ADVERTISING</p><h2 className="mt-3 max-w-3xl text-4xl font-black leading-none tracking-[-.06em] md:text-6xl">Why Most Video Ads Fail To<br className="hidden md:block" /> Generate Profit</h2><p className="mt-4 max-w-2xl font-mono text-sm leading-6 text-background/60">You don&apos;t need a bigger ad budget. You need a video script engineered to capture attention and convert cold traffic.</p><div className="mt-10 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">{problems.map(([title, text], i) => <article key={title} className="rounded-2xl border border-background/10 bg-background/[.04] p-5"><span className="font-mono text-xs text-accent">PROBLEM #{i + 1}</span><h3 className="mt-10 text-xl font-black leading-tight">{title}</h3><p className="mt-4 font-mono text-xs leading-5 text-background/60">{text}</p></article>)}</div><div className="mt-10 grid gap-5 border-t border-background/15 pt-8 md:grid-cols-[.7fr_1.3fr]"><p className="font-mono text-xs tracking-[.25em] text-accent">THE ADFIX STUDIO FIX</p><div><h3 className="text-3xl font-black leading-none tracking-[-.05em] md:text-5xl">We replace guess-work with storytelling architecture.</h3><p className="mt-4 max-w-2xl font-mono text-sm leading-6 text-background/60">Learn how to audit your script, engineer scroll-stopping hooks, and build seamless CTAs that drive measurable sales.</p></div></div></div></section> }
export function LearningSection() { const [open, setOpen] = useState<number | null>(null); return <section id="learn" className="bg-background px-5 py-16 md:px-8 md:py-20"><div className="mx-auto max-w-7xl"><p className="font-mono text-xs tracking-[.25em] text-muted-foreground">WHAT YOU&apos;LL LEARN</p><div className="mt-7 grid gap-8 md:grid-cols-[.7fr_1.3fr]"><h2 className="text-4xl font-black leading-none tracking-[-.06em] md:text-6xl">The skills behind ads that <span className="text-accent">land.</span></h2><div className="grid gap-3 sm:grid-cols-2">{learnItems.map(([title, text], i) => <button key={title} onClick={() => setOpen(open === i ? null : i)} className="rounded-2xl border border-border p-5 text-left"><span className="font-mono text-xs text-accent">0{i + 1}</span><span className="mt-8 flex items-start justify-between gap-3 text-xl font-bold"><span>{title}</span><Plus className={`size-5 ${open === i ? "rotate-45 text-accent" : ""}`} /></span><AnimatePresence initial={false}>{open === i && <motion.p initial={{ height: 0, opacity: 0 }} animate={{ height: "auto", opacity: 1 }} exit={{ height: 0, opacity: 0 }} className="mt-2 overflow-hidden font-mono text-xs leading-5 text-muted-foreground">{text}</motion.p>}</AnimatePresence></button>)}</div></div></div></section> }
export function CurriculumSection() { const [open, setOpen] = useState<number | null>(null); return <section id="curriculum" className="bg-accent px-5 py-16 text-accent-foreground md:px-8 md:py-20"><div className="mx-auto max-w-7xl"><p className="font-mono text-xs tracking-[.25em]">COURSE MAP</p><div className="mt-7 divide-y divide-accent-foreground/20 border-y border-accent-foreground/20">{modules.map(([number, title, text], i) => <div key={number}><button onClick={() => setOpen(open === i ? null : i)} className="flex w-full items-center gap-4 py-6 text-left md:grid md:grid-cols-[100px_1fr_1fr]"><span className="font-mono text-sm">{number}</span><span className="flex flex-1 items-center justify-between text-3xl font-black tracking-[-.05em] md:text-5xl">{title}<Plus className={`size-5 md:hidden ${open === i ? "rotate-45" : ""}`} /></span><span className="hidden max-w-md font-mono text-xs leading-5 md:block">{text}</span></button><AnimatePresence initial={false}>{open === i && <motion.p initial={{ height: 0 }} animate={{ height: "auto" }} exit={{ height: 0 }} className="overflow-hidden pb-5 font-mono text-xs md:hidden">{text}</motion.p>}</AnimatePresence></div>)}</div></div></section> }
export function AudienceSection() { return <section id="for-you" className="bg-background px-5 py-16 md:px-8 md:py-20"><div className="mx-auto grid max-w-7xl gap-8 md:grid-cols-[.9fr_1.1fr] md:items-end"><div><p className="font-mono text-xs tracking-[.25em] text-muted-foreground">WHO IT&apos;S FOR</p><h2 className="mt-3 text-5xl font-black leading-[.88] tracking-[-.07em] md:text-7xl">Built for people who want their ads to perform.</h2></div><div className="grid border-t border-border">{["Creators", "Brands", "Marketers", "Founders", "Agencies"].map(item => <div key={item} className="flex items-center gap-3 border-b border-border py-4 text-xl font-bold"><Check className="size-5 text-accent" />{item}</div>)}</div></div></section> }
export function FaqSection() { const [open, setOpen] = useState<number | null>(null); return <section id="faq" className="bg-background px-5 py-16 md:px-8 md:py-20"><div className="mx-auto max-w-4xl"><p className="font-mono text-xs tracking-[.25em] text-muted-foreground">FAQ</p><h2 className="mt-3 text-5xl font-black tracking-[-.07em] md:text-7xl">Questions, answered.</h2><div className="mt-8 divide-y divide-border border-y border-border">{faqs.map(([question, answer], i) => <div key={question}><button onClick={() => setOpen(open === i ? null : i)} className="flex w-full items-center justify-between gap-4 py-5 text-left text-lg font-bold"><span>{question}</span><Plus className={`size-5 ${open === i ? "rotate-45 text-accent" : ""}`} /></button><AnimatePresence initial={false}>{open === i && <motion.p initial={{ height: 0 }} animate={{ height: "auto" }} exit={{ height: 0 }} className="max-w-2xl overflow-hidden pb-5 font-mono text-sm leading-6 text-muted-foreground">{answer}</motion.p>}</AnimatePresence></div>)}</div></div></section> }
export function FinalCta() { return <section className="bg-foreground px-5 py-16 text-center text-background md:px-8 md:py-20"><p className="font-mono text-xs tracking-[.25em] text-accent">MAKE THE NEXT AD COUNT</p><h2 className="mx-auto mt-4 max-w-4xl text-5xl font-black leading-[.86] tracking-[-.07em] md:text-8xl">Your next ad starts with a better <span className="text-accent">idea.</span></h2><button onClick={openPurchase} className="mt-8 inline-flex items-center gap-3 rounded-full bg-accent px-7 py-4 text-sm font-bold text-accent-foreground">GET INSTANT ACCESS <ArrowRight className="size-4" /></button></section> }
export function PurchaseModal() { const [open, setOpen] = useState(false); useEffect(() => { const handler = () => setOpen(true); window.addEventListener("adfix:purchase", handler); return () => window.removeEventListener("adfix:purchase", handler) }, []); if (!open) return null; return <div role="presentation" onMouseDown={e => e.currentTarget === e.target && setOpen(false)} className="fixed inset-0 z-[60] flex items-center justify-center bg-foreground/80 p-5 backdrop-blur-sm"><div role="dialog" aria-modal="true" aria-labelledby="purchase-title" className="w-full max-w-lg rounded-3xl border border-background/15 bg-foreground p-7 text-background shadow-2xl"><div className="flex items-start justify-between"><div><p className="font-mono text-xs tracking-[.2em] text-accent">ADFIX / NEXT STEP</p><h2 id="purchase-title" className="mt-2 text-4xl font-black tracking-[-.06em]">GET INSTANT ACCESS</h2></div><button aria-label="Close purchase modal" onClick={() => setOpen(false)} className="rounded-full border border-background/20 p-2"><X className="size-5" /></button></div><p className="mt-4 font-mono text-sm text-background/60">AdFix High-Performing Ads Course</p><p className="mt-2 max-w-md font-mono text-xs leading-5 text-background/60">A practical recorded course for creators, marketers, founders, freelancers and businesses who want to create better-performing ads.</p><div className="mt-5 grid gap-2 font-mono text-xs text-background/75 sm:grid-cols-2"><span>✓ Recorded video course</span><span>✓ Hook Engineering</span><span>✓ Script Flow Rewrite</span><span>✓ High-Conversion CTAs</span><span>✓ Ad Strategy</span><span>✓ Lifetime access</span></div><p className="mt-6 font-mono text-xs tracking-[.18em] text-accent">PRICE: [ACTUAL COURSE PRICE]</p><div className="mt-7 grid gap-3"><button className="rounded-2xl bg-accent px-5 py-4 text-left font-bold text-accent-foreground">BUY NOW<span className="mt-1 block font-mono text-xs font-normal opacity-70">Get immediate access to the AdFix course.</span></button><button onClick={() => setOpen(false)} className="rounded-2xl border border-background/20 px-5 py-4 text-left font-bold">CHECK OUR FREE RESOURCES<span className="mt-1 block font-mono text-xs font-normal text-background/60">Explore our free resources before buying.</span></button></div></div></div> }
export function MobileCta() { const [visible, setVisible] = useState(false); useEffect(() => { const hero = document.getElementById("hero"); if (!hero) return; const observer = new IntersectionObserver(([entry]) => setVisible(!entry.isIntersecting), { threshold: .15 }); observer.observe(hero); return () => observer.disconnect() }, []); return visible ? <button onClick={openPurchase} className="fixed bottom-4 left-4 right-4 z-40 inline-flex items-center justify-center gap-2 rounded-full bg-accent px-6 py-3.5 text-sm font-bold text-accent-foreground shadow-lg md:hidden">GET INSTANT ACCESS <ArrowRight className="size-4" /></button> : null }

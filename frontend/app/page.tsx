import { Navigation } from "@/components/navigation"
import { AdfixHero, VslSection, BeforeAfterSection, ProblemSection, LearningSection, CurriculumSection, AudienceSection, FaqSection, FinalCta, MobileCta } from "@/components/adfix-sections"
import { Footer } from "@/components/footer"

export default function Home() {
  return <main className="min-h-screen bg-background pb-4 md:pb-0"><Navigation /><AdfixHero /><VslSection /><BeforeAfterSection /><ProblemSection /><LearningSection /><CurriculumSection /><AudienceSection /><FaqSection /><FinalCta /><Footer /><MobileCta /></main>
}

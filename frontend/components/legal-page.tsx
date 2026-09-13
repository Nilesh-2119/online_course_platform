type LegalPageProps = { title: string; intro: string; sections: Array<{ heading: string; body: string }> }

export function LegalPage({ title, intro, sections }: LegalPageProps) {
  return (
    <main className="min-h-screen bg-background px-5 py-10 text-foreground md:px-8 md:py-16">
      <div className="mx-auto max-w-3xl">
        <a href="/" className="font-mono text-xs text-muted-foreground hover:text-foreground">← BACK TO ADFIX STUDIO</a>
        <p className="mt-12 font-mono text-xs tracking-[.2em] text-accent">ADFIX STUDIO / LEGAL</p>
        <h1 className="mt-3 text-5xl font-black leading-none tracking-[-.07em] md:text-7xl">{title}</h1>
        <p className="mt-5 max-w-2xl font-mono text-sm leading-6 text-muted-foreground">{intro}</p>
        <div className="mt-10 grid gap-8 border-t border-border pt-8">
          {sections.map((section) => <section key={section.heading}><h2 className="text-2xl font-black tracking-[-.04em]">{section.heading}</h2><p className="mt-3 whitespace-pre-line font-mono text-sm leading-6 text-muted-foreground">{section.body}</p></section>)}
        </div>
        <p className="mt-12 border-t border-border pt-5 font-mono text-xs leading-5 text-muted-foreground">Effective Date: [To be finalized]\nLast Updated: [To be finalized]\nSupport: support@adfixstudio.com</p>
      </div>
    </main>
  )
}

const protection = "Account sharing, selling, transferring or reselling access; sharing login credentials; downloading or screen-recording protected videos; redistributing videos or files; uploading content to Telegram, Discord, Google Drive or other file-sharing services; sharing content publicly; bypassing authentication or access controls; scraping protected content; and attempting to access paid content without authorization are prohibited. AdFix Studio may suspend or terminate access for material violations, subject to applicable law and the Terms."

export const legalContent: Record<string, LegalPageProps> = {
  terms: { title: "Terms & Conditions", intro: "These terms govern your use of AdFix Studio accounts, resources and digital course access.", sections: [{ heading: "Account and course access", body: "Course access is personal to your account. You are responsible for keeping your credentials secure and for activity under your account." }, { heading: "Course usage", body: protection }, { heading: "Digital purchases", body: "Digital course purchases are non-refundable once access has been provided, subject to applicable law. Contact support promptly for payment or access problems." }] },
  privacy: { title: "Privacy Policy", intro: "This policy describes how AdFix Studio may handle information submitted through the website and account flows.", sections: [{ heading: "Information provided", body: "We may receive information you submit, such as your name, email address and contact details, to provide accounts, resources, support and course access." }, { heading: "Questions", body: "Privacy details, retention practices and legal entity information will be finalized before production launch. Contact support for questions." }] },
  refund: { title: "Refund & Cancellation Policy", intro: "AdFix Studio sells digital course access. Please review this policy before purchasing.", sections: [{ heading: "General policy", body: "Once a customer successfully purchases the course and receives access to the purchased course, the purchase is non-refundable. There is no general 7-day, 14-day or similar cooling-off refund promise." }, { heading: "Access failure exception", body: "You may contact support for a refund if payment was successfully completed but access was not received, the course is unavailable due to a platform-side technical issue, or a verified duplicate payment occurred. Our support team will first attempt to resolve access issues where appropriate." }, { heading: "Support and disputes", body: "If you have working access, the purchase is final, subject to applicable law. Unauthorized sharing, redistribution, downloading, recording, copying or reselling does not create a right to a refund. Chargebacks should not replace contacting support about an access problem." }] },
  cookie: { title: "Cookie Policy", intro: "This policy explains the intended use of cookies and similar technologies on AdFix Studio.", sections: [{ heading: "How cookies may be used", body: "Cookies may support essential site operation, preferences, security and measurement. The final cookie inventory and consent controls will be documented before production launch." }] },
  usage: { title: "Course Usage Policy", intro: "Course access is licensed for individual learning and is not a transfer of ownership.", sections: [{ heading: "Prohibited conduct", body: protection }, { heading: "Enforcement", body: "Material violations may result in suspension or termination of access, subject to applicable law and the Terms. Contact support if you believe access was restricted in error." }] },
  disclaimer: { title: "Disclaimer", intro: "AdFix Studio provides educational material about advertising creative and strategy.", sections: [{ heading: "Educational information", body: "Course content is educational and does not guarantee a particular business, advertising, audience or revenue result. Outcomes depend on many factors outside AdFix Studio's control." }] },
  contact: { title: "Contact / Support", intro: "For account, course access, payment or policy questions, contact AdFix Studio.", sections: [{ heading: "Support email", body: "support@adfixstudio.com\n\nPlease include the email used for your account and a clear description of the issue. Do not send payment credentials or passwords." }] },
}

export function getLegalContent(slug: string) { return legalContent[slug] ?? legalContent.terms }

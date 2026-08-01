# AiVance Roadmap

> AiVance is an AI-powered Career Operating System. This roadmap describes where the product is going after the **v1.0.0 production launch**.

## Legend
- ✅ Shipped
- 🔜 In progress
- 🧭 Planned
- 💡 Exploratory

---

## v1.0.0 — Production Launch (2026-07-31) ✅
- Provider Platform (AI, Job, Enrichment) with 6 AI + 11 Job + 1 Enrichment providers.
- Resume, ATS, Job Discovery, Recruiter CRM, Cover Letter, Interview, Workflow, Analytics, Assistant, Security.
- Tokenized design system + honest state-driven UI.
- Release CI/CD pipeline, crash reporting, encrypted storage, Privacy Center.
- **Goal met**: production-ready Android application with enterprise-grade architecture.

---

## v1.1 — Hardening & Operational Depth 🔜
**Theme**: stability, migrations, and internationalization.

- **Security migration v21**: destructive plaintext cleanup worker for legacy PII; zero-downtime DB migration.
- **Recruiter persistence**: replace `RecruiterIntelligenceRepository` mock logic with real CRM storage; wire Hunter.io data flow end-to-end.
- **Provider factory runtime config**: metadata-driven credential entry for keyed job providers (Adzuna, USAJobs) so they leave dormant state.
- **Localization (i18n)**: string resource extraction; initial locales (EN, DE, ES, HI, JA, AR, PT).
- **Real-device QA**: instrumented test matrix expansion (API 26–37), foldable/large-screen validation.
- **Cloud sync (beta)**: secure blob sync of already-encrypted career data with server-side zero knowledge.

## v1.2 — Intelligence Depth 🔜
- **Semantic job matching v2**: learned embeddings + provider-agnostic deduplication.
- **Interview simulations**: role-specific question banks, timed practice, progress curves.
- **Outreach effectiveness analytics**: response-rate tracking and template A/B insights.
- **Career roadmap engine**: goal-based milestone planning with AI coaching loops.

---

## v2.0 — The Career OS Platform 🧭
**Theme**: ecosystem, pro workflows, and multi-surface.

- **Web + Desktop clients** sharing the same domain contracts and encrypted sync.
- **Workspace mode**: multiple career profiles (roles, industries, locations) with per-profile pipelines.
- **Team/agency mode**: coach view with shared candidate dashboards (permission-gated).
- **ATS partnerships**: direct application submission via partner APIs; interview scheduling integrations (Calendly, HireVue).
- **Analytics suite v2**: cohort trends, offer-velocity forecasts, market salary intelligence.

## v3.0 — Autonomous Career Co-pilot 🧭
**Theme**: agentic orchestration.

- **Always-on assistant**: proactive opportunity alerts, deadline reminders, and application follow-ups.
- **Document autopilot**: end-to-end tailoring of resume + cover letter per application with human-in-the-loop approval.
- **Career simulation**: "what-if" scenario modeling (skills → salary, role transition paths).
- **Multi-modal coaching**: voice practice interviews and video self-review with AI feedback.

---

## Non-Goals (until further notice)
- Server-side storage of plaintext career data.
- Ads or data monetization.
- Any change that breaks the frozen v1.0.0 contracts without a major version bump.

## Maintenance Policy
- **Hotfixes** only for v1.0.0 (frozen contracts).
- Minor releases (v1.x) may add features but must preserve database/API backward compatibility.
- Major releases (v2+, v3+) may break contracts with documented migration paths.

# Google Play Data Safety Declaration — AiVance v1.0.0

This document contains the exact answers and inventory for the Google Play Store Data Safety form, derived directly from AiVance's certified security architecture (AES-GCM Tink encryption, TLS 1.3 pinning, Keystore secret management, backup exclusions).

---

## 1. Data Collection & Sharing Overview

| Question | Answer | Details / Justification |
| :--- | :--- | :--- |
| Does your app collect or share any of the required user data types? | **Yes** | App collects personal info, career data (resumes, applications, notes) stored on-device. |
| Is all of the user data collected by your app encrypted in transit? | **Yes** | All network traffic uses HTTPS (TLS 1.3/1.2) with strict certificate pinning. |
| Do you provide a way for users to request that their data be deleted? | **Yes** | Full local data wipe available directly in-app under Privacy Center. |

---

## 2. Data Types & Handling Breakdown

### Personal Info
- **Name / Contact Info (e.g. Email)**
  - **Collected?** Yes (Stored locally for resume & profile creation).
  - **Shared?** No (Never shared with 3rd parties; optionally sent to user-configured AI providers).
  - **Ephemeral?** No (Stored encrypted on-device).
  - **Required or Optional?** Optional (User-provided).
  - **Purposes**: App functionality, Account management.

### Financial Info
- **Not Collected**.

### Location Info
- **Approximate Location**: Optional (city/region for location-based job searches).
- **Shared?** Sent to search APIs (Adzuna, Indeed, LinkedIn) strictly when job searching.

### Personal Documents & Career Data
- **Resumes, Cover Letters, Applications, Mock Interview Transcripts**
  - **Collected?** Yes (Stored locally in encrypted Room database v20).
  - **Shared?** Sent only to configured AI Providers (Gemini, Claude, Ollama) upon user request.
  - **Encrypted at rest?** Yes — AES-GCM (Google Tink) hardware-backed via Android KeyStore.

### App Info & Performance
- **Crash Logs / Diagnostic Info**
  - **Collected?** Yes (On-device telemetry & `CrashReporter`).
  - **Shared?** No external sharing. PII & secrets scrubbed automatically before logging.

---

## 3. Security Practices

- **Encryption in Transit**: Pinned TLS endpoints.
- **Encryption at Rest**: AES-GCM (128-bit) with Google Tink & Keystore.
- **Data Deletion Mechanism**: In-app "Wipe Data & Reset" in Privacy Center (`:feature:profile`).
- **Backup Policy**: Plaintext secrets and PII explicitly excluded from Android unencrypted cloud backups (`allowBackup="false"` or backup rules).

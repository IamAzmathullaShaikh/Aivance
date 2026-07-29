# 10. AI Integration Strategy

Date: 2026-07-28

## Status

Accepted

## Context

Aivance leverages AI for resume optimization, ATS checks, and cover letter generation. This requires a secure and efficient way to integrate with AI models like Gemini.

## Decision

- **Remote Execution**: Perform AI analysis on the server-side or via direct API calls (e.g., Google AI SDK) to avoid heavy on-device processing.
- **API Key Management**: Securely store API keys in `local.properties` and inject them via build configurations; never commit keys to version control.
- **User Privacy**: Ensure sensitive user data (like resume content) is handled securely and only sent to AI services with user consent.

## Consequences

- **Pros**:
    - Access to powerful AI models without increasing app size.
    - Secure key management.
    - Flexible to switch or update AI providers.
- **Cons**:
    - Requires internet connection for AI features.
    - Latency from network requests.

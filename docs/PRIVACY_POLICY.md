# Privacy Policy for Agent.AI (AI Mobile Control Agent)

**Last Updated:** July 16, 2026

Agent.AI ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and safeguard your information when you use our mobile application and backend services.

---

## 1. Information We Collect

### A. Accessibility Service Logs (Crucial)
- **What we do:** Our application utilizes the Android Accessibility Service API to automate screen actions (such as auto-typing in Chrome or auto-messaging on WhatsApp) based on your explicit voice or chat commands.
- **Privacy Guarantee:** All screen reading, parsing, and UI clicks are executed **locally on your device**. Accessibility log payloads are **NEVER** transmitted to our servers, shared with third parties, or sold.

### B. Audio and Speech Data
- **What we do:** We access your microphone to record speech inputs for command processing.
- **Privacy Guarantee:** Speech is converted locally to text via Android speech recognizer. The text transcript is sent to our secure backend API to resolve intent via Google Gemini. Raw audio recordings are **NEVER** saved on our servers.

### C. Paired Device and Synchronization Logs
- **What we do:** If you use cloud backup, your configuration preferences (e.g. theme, TTS settings) and history metadata logs are stored in our secure database.
- **Privacy Guarantee:** All transit data is encrypted via HTTPS and secured behind JWT-based authorization tokens.

---

## 2. Play Store Compliance & Data Retention
- We do not share user personal data with third-party advertising companies.
- Users can request complete account deletion by navigating to the Settings screen and selecting "Delete Account" or contact us at `support@agent.ai`.

---

## 3. Contact Us
For any questions regarding this Privacy Policy, contact `support@agent.ai`.

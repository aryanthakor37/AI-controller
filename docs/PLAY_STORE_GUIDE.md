# Play Store Submission Guide for Agent.AI

This document serves as the guide for preparing assets and submitting the Android app bundle (`.aab`) to the Google Play Console.

---

## 1. App Listing Metadata

### App Title
- **Name:** Agent.AI (or "AI Mobile Control Agent")
- **Limit:** 30 characters maximum.

### Short Description
- **Text:** AI-powered mobile voice controller and automation agent.
- **Limit:** 80 characters maximum.

### Full Description
- **Text:**
  Agent.AI is a state-of-the-art voice and chat assistant that allows you to control your Android device hands-free. Powered by Google Gemini, the agent understands natural language commands and executes tasks instantly.
  
  Key Features:
  - Turn on/off flashlight and toggle system controls.
  - Launch system apps like Chrome, YouTube, Spotify, and Camera.
  - Automated WhatsApp message dispatching and Google Search querying via advanced accessibility service simulation.
  - Pair and sync settings directly to your secure cloud web dashboard.
  - Lightweight offline fallback mode that keeps executing commands even when offline!

---

## 2. Graphic Assets Checklist

1. **App Icon:**
   - **Size:** 512px by 512px.
   - **Format:** 32-bit PNG (with alpha).
   - **File:** Use `res/drawable/app_launcher_icon.png`.

2. **Feature Graphic:**
   - **Size:** 1024px by 500px.
   - **Format:** PNG or JPEG.

3. **Screenshots:**
   - At least 2 screenshots showing the main AI Chat, Voice Assistant, and Dashboard widgets.

---

## 3. Play Console Upload Steps

1. **Create Release:** Under *Production*, click *Create new release*.
2. **App Bundle:** Upload `app-release.aab` from `android/app/build/outputs/bundle/release/`.
3. **Privacy Policy Link:** Provide the URL pointing to your hosted version of `PRIVACY_POLICY.md`.
4. **Accessibility Service Declaration:** Complete the Accessibility declaration form in Play Console. State clearly:
   *"Our app uses the AccessibilityService API to perform automated clicks and text input on behalf of the user in response to their explicit voice and chat commands."*

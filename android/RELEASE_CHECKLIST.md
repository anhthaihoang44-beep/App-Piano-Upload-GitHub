# PianoFlow — Production Release Checklist & Guidelines

## 1. Signing & Keystore Configuration
1. Generate a production upload key:
   ```bash
   keytool -genkey -v -keystore pianoflow-release.jks -alias pianoflow -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Configure credentials in environment variables or `keystore.properties` (never commit keys to source control):
   ```properties
   RELEASE_STORE_FILE=../pianoflow-release.jks
   RELEASE_KEY_ALIAS=pianoflow
   RELEASE_STORE_PASSWORD=your_store_password
   RELEASE_KEY_PASSWORD=your_key_password
   ```
3. Generate the Google Play App Bundle (AAB):
   ```bash
   ./gradlew bundleRelease
   ```
   Output: `app/build/outputs/bundle/release/app-release.aab`

---

## 2. ProGuard & R8 Shrinking Verification
- Minification and resource shrinking are enabled for `release` builds in `app/build.gradle.kts`.
- Rules in `app/proguard-rules.pro` preserve Room entities, DAOs, and Play Billing interfaces.
- Test release build locally:
  ```bash
  ./gradlew assembleRelease
  ```

---

## 3. Google Play Console Listing Checklist
- **App Title**: PianoFlow — Learn Real Piano Keyboard
- **Short Description**: Learn piano with realistic multi-touch keyboard, interactive falling notes, and courses.
- **Graphic Assets**:
  - App Icon: 512 × 512 PNG (32-bit with alpha)
  - Feature Graphic: 1024 × 500 PNG/JPEG
  - Screenshots: Phone (min 4 screenshots, 1080×1920 or 1080×2400) + 7-inch & 10-inch Tablet screenshots (portrait & landscape)
- **Content Rating**: Complete IARC questionnaire (Rating: Everyone / 3+)
- **Target Audience**: All ages, Education & Music categories
- **Privacy Policy URL**: Hosted link to Privacy Policy text.

---

## 4. Google Play Billing Setup
- Create In-app Subscriptions in Google Play Console under **Monetize > Subscriptions**:
  - `pianoflow_weekly` (Weekly VIP)
  - `pianoflow_monthly` (Monthly VIP, 7-day free trial base plan)
  - `pianoflow_yearly` (Yearly VIP)
- Swap `MockBillingRepository` to `PlayBillingRepository` in `PianoFlowApplication.kt` when publishing live.

---

## 5. Known Limitations & Roadmap
- **SoundPool Engine**: Excellent for MVP sub-50ms playback. Future update can integrate an Oboe / AAudio C++ audio backend for ultra-low (<15ms) professional audio on specialized hardware.
- **Physical USB MIDI**: USB-OTG MIDI controller support can be attached via Android `MidiManager`.
- **Custom SoundFonts**: Support for external SF2 SoundFonts for Grand Piano, Electric Piano, and Synth voices.

# PianoFlow iOS — App Store Release Checklist & Guidelines

## 1. Signing & Capabilities (Xcode)
1. Open `ios/PianoFlow.xcodeproj` in **Xcode 15+** (or run `xcodegen generate` inside `ios/`).
2. Select the `PianoFlow` target → **Signing & Capabilities**:
   - Select your Apple Developer **Team**.
   - Confirm Bundle Identifier: `com.pianoflow.app`.
   - (Optional) Add **Background Modes → Audio, AirPlay, and Picture in Picture** if background audio playback is desired.

---

## 2. StoreKit 2 Subscriptions Setup
1. In **App Store Connect**, create an Auto-Renewable Subscription Group (`PianoFlow VIP`) with product IDs matching `StoreKitBillingRepository.productIds`:
   - `pianoflow_weekly` (Weekly VIP)
   - `pianoflow_monthly` (Monthly VIP, 7-day free trial introductory offer)
   - `pianoflow_yearly` (Yearly VIP)
2. For local testing in Xcode Simulator:
   - Edit Scheme → Run → Options → **StoreKit Configuration**: select `PianoFlow.storekit`.
3. For Production release:
   - In `ios/PianoFlow/App/AppContainer.swift`, swap `MockBillingRepository` to `StoreKitBillingRepository(preferencesRepository: prefs)`.

---

## 3. Running Tests & Building
- **Pure Swift Core Tests (SwiftPM)**:
  ```bash
  cd ios
  swift test
  ```
- **Full iOS App & XCTest (macOS + Xcode)**:
  ```bash
  cd ios
  xcodebuild test -project PianoFlow.xcodeproj -scheme PianoFlow -destination 'platform=iOS Simulator,name=iPhone 16'
  ```

---

## 4. App Store Connect Listing Checklist
- **App Name**: PianoFlow — Learn Real Piano Keyboard
- **Category**: Music / Education
- **Localization**: English (`en`) and Vietnamese (`vi`) via `Localizable.strings`
- **Privacy**: No third-party tracking; local persistence via Apple SwiftData & UserDefaults.

---

## 5. Audio Engine Roadmap
- **Current**: `AVAudioPianoEngine` pre-synthesizes 88-key acoustic piano PCM buffers (A0..C8) across a 32-voice `AVAudioPlayerNode` pool with 5ms `AVAudioSession` I/O buffer duration.
- **Future**: Swap `AudioEngineProtocol` with an `AVAudioUnitSampler` backed by a bundled `.sf2` SoundFont or AUv3 instrument.

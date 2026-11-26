# PianoFlow 🎹 — Native iOS (Swift) & Native Android (Kotlin)

**PianoFlow** is a dual-platform native piano learning and playing application built natively for both mobile ecosystems:

- **Native iOS – Swift** (`ios/`): SwiftUI, UIKit multi-touch keyboard, Core Graphics, `AVAudioEngine`, SwiftData, StoreKit 2, and `UNUserNotificationCenter`.
- **Native Android – Kotlin** (`android/`): Jetpack Compose, Material 3, Compose Canvas, `SoundPool`, Room, DataStore, Google Play Billing, and WorkManager.

---

## 📁 Repository Structure

```text
App-Piano-Upload-GitHub/
├── android/                 # Native Android – Kotlin (Jetpack Compose)
│   ├── app/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle/
│   └── RELEASE_CHECKLIST.md
├── ios/                     # Native iOS – Swift (SwiftUI + AVAudioEngine + SwiftData)
│   ├── Package.swift
│   ├── project.yml          # XcodeGen specification
│   ├── PianoFlow.xcodeproj/ # Ready-to-open Xcode project & scheme
│   ├── PianoFlow/
│   ├── Tests/PianoFlowTests/
│   └── RELEASE_CHECKLIST.md
└── README.md
```

---

## 🌟 Feature Parity Across iOS & Android

### 1. Realistic Multi-Touch Piano Keyboard
- **Low-Latency Polyphonic Audio**:
  - **iOS (Swift)**: `AVAudioPianoEngine` (`AVAudioEngine` + 32-voice `AVAudioPlayerNode` pool, 88-key A0..C8 pre-rendered PCM waveforms with 6 harmonics & hammer click, 5ms I/O buffer).
  - **Android (Kotlin)**: `SoundPoolAudioEngine` (`SoundPool` with 32 streams and pre-synthesized acoustic piano waveforms C2..C7).
- **Multi-Touch & Glissando**:
  - **iOS**: `MultiTouchPianoUIView` (`isMultipleTouchEnabled = true`) bridged into SwiftUI via `PianoCanvasView`.
  - **Android**: `PianoCanvas` using Compose `awaitEachGesture` pointer tracking.
- **Customizable Notation**: Toggle on-the-fly between **C D E**, **Do Re Mi** (Solfege), or **Off**.
- **Sustain Pedal, Volume, Octave Shift & Zoom**: Real-time sustain toggle, master volume slider, octave shifting, dynamic key zoom, and mini octave navigator bar.
- **Double Keyboard & 2-Player Duet Modes**: Stacked Treble/Bass keyboards (`C4–C6` / `C2–C4`) and 180°-rotated split-screen duet mode.

### 2. Interactive Song Player with Falling Notes
- **Smooth 60 FPS Waterfall Engine**:
  - **iOS**: `FallingNotesView` (`SwiftUI Canvas` + `MonotonicClock` via `DispatchTime.now().uptimeNanoseconds`).
  - **Android**: `FallingNotesCanvas` (`Compose Canvas` + `MonotonicClock` via `System.nanoTime`).
- **3 Learning Modes**: **Watch**, **Follow**, and **Perform** (Perfect/Good/Miss scoring, combo multipliers, accuracy %, 1–3 stars).
- **10 Bundled Public Domain Pieces** + native Standard MIDI File (`SMF 0/1`) parser (`MidiParser`).

### 3. Step-by-Step Piano Academy Lessons
- 12 progressive lessons across 6 stages (Piano Basics, Right Hand, Left Hand & Bass, Two-Hand Coordination, Chords, Master Pieces) with sequential unlocking, XP rewards, and star ratings.

### 4. Performance Recording & WAV Export
- Record performances as timestamped note events, replay with the falling-notes waterfall, render to 16-bit 44.1kHz PCM WAV (`AudioWavExporter`), and share via iOS Share Sheet / Android ShareSheet.

### 5. Monetization & Localization
- **iOS**: `BillingRepositoryProtocol` with `MockBillingRepository` (dev) and `StoreKitBillingRepository` (StoreKit 2 + `PianoFlow.storekit`).
- **Android**: `BillingRepository` with `MockBillingRepository` (dev) and Google Play Billing 7+.
- **Localization**: Full **English** and **Vietnamese** support (`en.lproj` / `vi.lproj` on iOS; `values/` / `values-vi/` on Android).

---

## 🚀 How to Build & Run

### Native iOS – Swift (`ios/`)
- **Requirements**: macOS with Xcode 15+ (iOS 17.0 deployment target).
- **Open in Xcode**:
  ```bash
  open ios/PianoFlow.xcodeproj
  ```
- **Run Core Unit Tests via SwiftPM**:
  ```bash
  cd ios
  swift test
  ```
- **Build & Test via Xcode CLI**:
  ```bash
  cd ios
  xcodebuild test -project PianoFlow.xcodeproj -scheme PianoFlow -destination 'platform=iOS Simulator,name=iPhone 16'
  ```

### Native Android – Kotlin (`android/`)
- **Requirements**: JDK 17, Android SDK 35 (minSdk 24, targetSdk 35).
- **Run Unit Tests**:
  ```bash
  cd android
  ./gradlew testDebugUnitTest
  ```
- **Build Debug APK**:
  ```bash
  cd android
  ./gradlew assembleDebug
  ```
  Output: `android/app/build/outputs/apk/debug/app-debug.apk`

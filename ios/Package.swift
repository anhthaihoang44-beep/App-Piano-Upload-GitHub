// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "PianoFlow",
    defaultLocalization: "en",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(
            name: "PianoFlow",
            targets: ["PianoFlow"]
        )
    ],
    targets: [
        .target(
            name: "PianoFlow",
            path: "PianoFlow",
            exclude: [
                "App",
                "Core/Audio/AudioEngine.swift",
                "Core/Audio/AVAudioPianoEngine.swift",
                "Core/Billing",
                "Core/Database",
                "Core/Notifications",
                "Core/Preferences",
                "Core/Util/HapticHelper.swift",
                "Features/Keyboard",
                "Features/Lessons/LessonsScreen.swift",
                "Features/Lessons/LessonPracticeScreen.swift",
                "Features/Onboarding",
                "Features/Paywall",
                "Features/Profile",
                "Features/Recording",
                "Features/Songs",
                "Resources",
                "UI"
            ],
            sources: [
                "Core/Audio/NotePitch.swift",
                "Core/Audio/AudioWavExporter.swift",
                "Core/MIDI/MidiSong.swift",
                "Core/MIDI/MidiParser.swift",
                "Core/MIDI/BundledSongs.swift",
                "Core/MIDI/ScoringEngine.swift",
                "Core/Util/MonotonicClock.swift",
                "Features/Lessons/Lesson.swift"
            ]
        ),
        .testTarget(
            name: "PianoFlowTests",
            dependencies: ["PianoFlow"],
            path: "Tests/PianoFlowTests"
        )
    ]
)

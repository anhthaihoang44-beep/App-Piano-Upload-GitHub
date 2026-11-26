import SwiftUI
import UIKit

public struct LessonPracticeScreen: View {

    let lessonId: String
    let audioEngine: AudioEngineProtocol
    let dataStore: PianoFlowDataStore
    let onBack: () -> Void

    @State private var currentStepIndex: Int = 0
    @State private var isLessonFinished: Bool = false

    private let startMidi = 48 // C3
    private let endMidi = 76   // E5

    public init(
        lessonId: String,
        audioEngine: AudioEngineProtocol,
        dataStore: PianoFlowDataStore,
        onBack: @escaping () -> Void
    ) {
        self.lessonId = lessonId
        self.audioEngine = audioEngine
        self.dataStore = dataStore
        self.onBack = onBack
    }

    private var lesson: Lesson {
        LessonCurriculum.getById(lessonId) ?? LessonCurriculum.allLessons[0]
    }

    private var currentTargetMidi: Int? {
        guard currentStepIndex < lesson.targetNotes.count else { return nil }
        return lesson.targetNotes[currentStepIndex]
    }

    private var highlightedKeys: [Int: UIColor] {
        guard let target = currentTargetMidi else { return [:] }
        return [target: PianoFlowColors.uiBrandSecondary]
    }

    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.title3.bold())
                        .foregroundColor(.white)
                        .padding(8)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(L10n.tr(lesson.titleKey))
                        .font(.headline)
                        .foregroundColor(.white)
                    Text(L10n.tr(lesson.stageTitleKey))
                        .font(.caption2)
                        .foregroundColor(PianoFlowColors.brandSecondary)
                }

                Spacer()
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 6)

            // Instruction Card
            VStack(alignment: .leading, spacing: 10) {
                Text(L10n.tr("lessons_instruction_label"))
                    .font(.caption.bold())
                    .foregroundColor(PianoFlowColors.brandPrimary)

                Text(L10n.tr(lesson.instructionKey))
                    .font(.subheadline)
                    .foregroundColor(.white)

                let pitch = currentTargetMidi.flatMap { NotePitch.fromMidi($0) }
                HStack {
                    Text(L10n.format("lessons_target_note", pitch?.nameEn ?? L10n.tr("lessons_done_bang"), pitch?.nameSolfege ?? ""))
                        .font(.subheadline.bold())
                        .foregroundColor(PianoFlowColors.brandSecondary)

                    Spacer()

                    Text(L10n.format("lessons_step_progress", min(currentStepIndex, lesson.targetNotes.count), lesson.targetNotes.count))
                        .font(.caption)
                        .foregroundColor(.gray)
                }

                ProgressView(
                    value: Double(currentStepIndex),
                    total: Double(max(1, lesson.targetNotes.count))
                )
                .tint(PianoFlowColors.brandSecondary)
            }
            .padding(16)
            .background(PianoFlowColors.surfaceDark)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .padding(.horizontal, 16)
            .padding(.vertical, 8)

            Spacer()

            PianoCanvasView(
                startMidi: startMidi,
                endMidi: endMidi,
                keyWidthPt: 52,
                notationType: .cde,
                highlightedKeys: highlightedKeys,
                onKeyDown: { handleKeyDown($0) },
                onKeyUp: { audioEngine.stopNote(midiNumber: $0) }
            )
            .frame(height: 280)
        }
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
        .sheet(isPresented: $isLessonFinished) {
            completedSheet
                .presentationDetents([.medium])
        }
    }

    private func handleKeyDown(_ midiNumber: Int) {
        audioEngine.playNote(midiNumber: midiNumber)
        guard midiNumber == currentTargetMidi else { return }

        let nextStep = currentStepIndex + 1
        currentStepIndex = nextStep
        if nextStep >= lesson.targetNotes.count {
            dataStore.saveLessonProgress(
                lessonId: lesson.id,
                stageId: lesson.stageId,
                stars: 3,
                bestAccuracy: 100,
                xpEarned: lesson.xpReward
            )
            isLessonFinished = true
        }
    }

    private var completedSheet: some View {
        VStack(spacing: 14) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 56))
                .foregroundColor(PianoFlowColors.successGreen)

            Text(L10n.tr("lessons_lesson_completed"))
                .font(.title2.bold())
                .foregroundColor(.white)

            HStack(spacing: 6) {
                ForEach(1...3, id: \.self) { _ in
                    Image(systemName: "star.fill")
                        .font(.title)
                        .foregroundColor(PianoFlowColors.goldStarColor)
                }
            }

            Text(L10n.xpEarned(lesson.xpReward))
                .font(.headline.bold())
                .foregroundColor(PianoFlowColors.brandSecondary)

            Button(L10n.tr("common_continue")) {
                isLessonFinished = false
                onBack()
            }
            .font(.headline)
            .foregroundColor(.white)
            .padding(.horizontal, 28)
            .padding(.vertical, 12)
            .background(PianoFlowColors.brandPrimary)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .padding(.top, 8)
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(PianoFlowColors.surfaceDark)
    }
}

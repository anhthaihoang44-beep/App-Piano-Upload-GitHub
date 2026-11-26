import SwiftUI

public struct LessonsScreen: View {

    @ObservedObject var dataStore: PianoFlowDataStore
    @ObservedObject var preferencesRepository: UserPreferencesRepository
    let billingRepository: BillingRepositoryProtocol
    let onLessonSelected: (String) -> Void
    let onPaywallRequired: () -> Void

    public init(
        dataStore: PianoFlowDataStore,
        preferencesRepository: UserPreferencesRepository,
        billingRepository: BillingRepositoryProtocol,
        onLessonSelected: @escaping (String) -> Void,
        onPaywallRequired: @escaping () -> Void
    ) {
        self.dataStore = dataStore
        self.preferencesRepository = preferencesRepository
        self.billingRepository = billingRepository
        self.onLessonSelected = onLessonSelected
        self.onPaywallRequired = onPaywallRequired
    }

    private var progressMap: [String: LessonProgressEntity] {
        Dictionary(uniqueKeysWithValues: dataStore.lessonProgress.map { ($0.lessonId, $0) })
    }

    private var completedIds: Set<String> {
        Set(dataStore.lessonProgress.filter { $0.isCompleted }.map { $0.lessonId })
    }

    public var body: some View {
        let _ = preferencesRepository.appLanguage
        VStack(spacing: 14) {
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(L10n.tr("lessons_title"))
                        .font(.title.bold())
                        .foregroundColor(.white)
                    Text(L10n.tr("lessons_course_map"))
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }

                Spacer()

                HStack(spacing: 8) {
                    Text("🔥 \(preferencesRepository.streakDays)d")
                        .font(.caption.bold())
                        .foregroundColor(.orange)
                    Text("✨ \(dataStore.totalXp) XP")
                        .font(.caption.bold())
                        .foregroundColor(PianoFlowColors.brandSecondary)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(PianoFlowColors.surfaceDark)
                .clipShape(Capsule())
            }
            .padding(.top, 12)

            ScrollView {
                LazyVStack(spacing: 10) {
                    ForEach(Array(LessonCurriculum.allLessons.enumerated()), id: \.element.id) { index, lesson in
                        let progress = progressMap[lesson.id]
                        let isCompleted = progress?.isCompleted ?? false
                        let isUnlocked = LessonCurriculum.isLessonUnlocked(at: index, completedLessonIds: completedIds)
                        let isVipGated = lesson.isPremium && !preferencesRepository.isPremium

                        LessonItemCard(
                            lesson: lesson,
                            index: index + 1,
                            isCompleted: isCompleted,
                            isUnlocked: isUnlocked,
                            isVipGated: isVipGated,
                            stars: progress?.stars ?? 0
                        ) {
                            if isVipGated {
                                onPaywallRequired()
                            } else if isUnlocked {
                                onLessonSelected(lesson.id)
                            }
                        }
                    }
                }
                .padding(.bottom, 24)
            }
        }
        .padding(.horizontal, 16)
        .background(PianoFlowColors.backgroundDark.ignoresSafeArea())
    }
}

private struct LessonItemCard: View {
    let lesson: Lesson
    let index: Int
    let isCompleted: Bool
    let isUnlocked: Bool
    let isVipGated: Bool
    let stars: Int
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(badgeBackground)
                        .frame(width: 44, height: 44)

                    if isCompleted {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(PianoFlowColors.successGreen)
                    } else if isVipGated {
                        Image(systemName: "crown.fill")
                            .foregroundColor(PianoFlowColors.goldStarColor)
                    } else if isUnlocked {
                        Text("\(index)")
                            .font(.headline.bold())
                            .foregroundColor(PianoFlowColors.brandPrimary)
                    } else {
                        Image(systemName: "lock.fill")
                            .foregroundColor(.gray)
                    }
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text(L10n.tr(lesson.titleKey))
                        .font(.headline)
                        .foregroundColor((isUnlocked || isVipGated) ? .white : .gray)
                    Text(L10n.tr(lesson.descriptionKey))
                        .font(.caption)
                        .foregroundColor(.gray)
                        .lineLimit(1)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 3) {
                    if stars > 0 {
                        HStack(spacing: 2) {
                            ForEach(1...3, id: \.self) { star in
                                Image(systemName: "star.fill")
                                    .font(.caption2)
                                    .foregroundColor(star <= stars ? PianoFlowColors.goldStarColor : .gray.opacity(0.4))
                            }
                        }
                    }
                    Text("+\(lesson.xpReward) XP")
                        .font(.caption2.bold())
                        .foregroundColor(PianoFlowColors.brandSecondary)
                }
            }
            .padding(14)
            .background(PianoFlowColors.surfaceDark.opacity(isUnlocked ? 1.0 : 0.55))
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .disabled(!isUnlocked && !isVipGated)
        .buttonStyle(.plain)
    }

    private var badgeBackground: Color {
        if isCompleted { return PianoFlowColors.successGreen.opacity(0.2) }
        if isVipGated { return PianoFlowColors.goldStarColor.opacity(0.2) }
        if isUnlocked { return PianoFlowColors.brandPrimary.opacity(0.2) }
        return PianoFlowColors.surfaceElevatedDark
    }
}

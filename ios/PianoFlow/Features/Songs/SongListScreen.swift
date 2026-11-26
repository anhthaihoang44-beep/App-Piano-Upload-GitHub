import SwiftUI

public struct SongListScreen: View {

    @ObservedObject var dataStore: PianoFlowDataStore
    @ObservedObject var preferencesRepository: UserPreferencesRepository
    let billingRepository: BillingRepositoryProtocol
    let onSongSelected: (String) -> Void
    let onPaywallRequired: () -> Void

    @State private var selectedFilter: String = "All"
    private let filters = ["All", "Easy", "Intermediate", "Hard"]

    public init(
        dataStore: PianoFlowDataStore,
        preferencesRepository: UserPreferencesRepository,
        billingRepository: BillingRepositoryProtocol,
        onSongSelected: @escaping (String) -> Void,
        onPaywallRequired: @escaping () -> Void
    ) {
        self.dataStore = dataStore
        self.preferencesRepository = preferencesRepository
        self.billingRepository = billingRepository
        self.onSongSelected = onSongSelected
        self.onPaywallRequired = onPaywallRequired
    }

    private var filteredSongs: [Song] {
        if selectedFilter == "All" {
            return BundledSongs.allSongs
        }
        return BundledSongs.allSongs.filter {
            $0.difficulty.localizedCaseInsensitiveCompare(selectedFilter) == .orderedSame
        }
    }

    private var scoreMap: [String: SongScoreEntity] {
        Dictionary(uniqueKeysWithValues: dataStore.songScores.map { ($0.songId, $0) })
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(L10n.tr("nav_songs"))
                    .font(.title.bold())
                    .foregroundColor(.white)
                Text("Master classic masterpieces step by step")
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
            .padding(.top, 12)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(filters, id: \.self) { filter in
                        let isSelected = selectedFilter == filter
                        Button {
                            selectedFilter = filter
                        } label: {
                            Text(filter)
                                .font(.subheadline.weight(.medium))
                                .foregroundColor(isSelected ? .white : .gray)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 8)
                                .background(isSelected ? PianoFlowColors.brandPrimary : PianoFlowColors.surfaceDark)
                                .clipShape(Capsule())
                        }
                    }
                }
            }

            ScrollView {
                LazyVStack(spacing: 10) {
                    ForEach(filteredSongs) { song in
                        let isLocked = song.isPremium && !preferencesRepository.isPremium
                        SongItemCard(
                            song: song,
                            isLocked: isLocked,
                            scoreEntity: scoreMap[song.id]
                        ) {
                            if isLocked {
                                onPaywallRequired()
                            } else {
                                onSongSelected(song.id)
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

private struct SongItemCard: View {
    let song: Song
    let isLocked: Bool
    let scoreEntity: SongScoreEntity?
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(isLocked ? PianoFlowColors.surfaceElevatedDark : PianoFlowColors.brandPrimary.opacity(0.2))
                        .frame(width: 44, height: 44)
                    Image(systemName: isLocked ? "lock.fill" : "play.fill")
                        .foregroundColor(isLocked ? PianoFlowColors.goldStarColor : PianoFlowColors.brandSecondary)
                }

                VStack(alignment: .leading, spacing: 3) {
                    HStack(spacing: 6) {
                        Text(song.title)
                            .font(.headline)
                            .foregroundColor(.white)
                            .lineLimit(1)

                        if song.isPremium {
                            Text("VIP")
                                .font(.system(size: 9, weight: .bold))
                                .foregroundColor(PianoFlowColors.backgroundDark)
                                .padding(.horizontal, 5)
                                .padding(.vertical, 2)
                                .background(PianoFlowColors.goldStarColor)
                                .clipShape(RoundedRectangle(cornerRadius: 4))
                        }
                    }

                    Text("\(song.composer) • \(song.difficulty) • \(song.bpm) BPM")
                        .font(.caption)
                        .foregroundColor(.gray)
                }

                Spacer()

                if let score = scoreEntity {
                    VStack(alignment: .trailing, spacing: 2) {
                        HStack(spacing: 2) {
                            ForEach(1...3, id: \.self) { star in
                                Image(systemName: "star.fill")
                                    .font(.caption2)
                                    .foregroundColor(star <= score.stars ? PianoFlowColors.goldStarColor : .gray.opacity(0.4))
                            }
                        }
                        Text("\(Int(score.accuracyPercent))%")
                            .font(.caption2.bold())
                            .foregroundColor(PianoFlowColors.brandSecondary)
                    }
                }
            }
            .padding(14)
            .background(PianoFlowColors.surfaceDark)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .buttonStyle(.plain)
    }
}

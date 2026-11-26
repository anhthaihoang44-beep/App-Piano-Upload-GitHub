import Foundation

public enum PlayMode: String, CaseIterable, Sendable {
    case watch
    case follow
    case perform
}

public enum HitRating: String, Sendable {
    case perfect = "PERFECT"
    case good = "GOOD"
    case miss = "MISS"
}

public struct ScoreSummary: Equatable, Sendable {
    public let score: Int
    public let maxCombo: Int
    public let accuracyPercent: Float
    public let stars: Int
    public let perfectHits: Int
    public let goodHits: Int
    public let misses: Int

    public init(
        score: Int,
        maxCombo: Int,
        accuracyPercent: Float,
        stars: Int,
        perfectHits: Int,
        goodHits: Int,
        misses: Int
    ) {
        self.score = score
        self.maxCombo = maxCombo
        self.accuracyPercent = accuracyPercent
        self.stars = stars
        self.perfectHits = perfectHits
        self.goodHits = goodHits
        self.misses = misses
    }
}

public enum ScoringEngine {

    public static func calculateScore(perfect: Int, good: Int, total: Int) -> (accuracy: Float, stars: Int) {
        let accuracy: Float
        if total > 0 {
            accuracy = ((Float(perfect) * 100.0 + Float(good) * 70.0) / (Float(total) * 100.0)) * 100.0
        } else {
            accuracy = 0
        }

        let stars: Int
        switch accuracy {
        case 85...:
            stars = 3
        case 70..<85:
            stars = 2
        case 45..<70:
            stars = 1
        default:
            stars = 0
        }

        return (accuracy, stars)
    }

    public static func evaluateSummary(
        score: Int,
        maxCombo: Int,
        perfectHits: Int,
        goodHits: Int,
        totalNotes: Int
    ) -> ScoreSummary {
        let misses = max(0, totalNotes - (perfectHits + goodHits))
        let result = calculateScore(perfect: perfectHits, good: goodHits, total: totalNotes)
        return ScoreSummary(
            score: score,
            maxCombo: maxCombo,
            accuracyPercent: result.accuracy,
            stars: result.stars,
            perfectHits: perfectHits,
            goodHits: goodHits,
            misses: misses
        )
    }
}

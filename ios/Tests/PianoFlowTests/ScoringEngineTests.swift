import XCTest
@testable import PianoFlow

final class ScoringEngineTests: XCTestCase {

    func testPerfectScoreGivesThreeStars() {
        let result = ScoringEngine.calculateScore(perfect: 20, good: 0, total: 20)
        XCTAssertEqual(result.accuracy, 100, accuracy: 0.01)
        XCTAssertEqual(result.stars, 3)
    }

    func testMixedScoreGivesTwoOrThreeStars() {
        let result1 = ScoringEngine.calculateScore(perfect: 10, good: 10, total: 20)
        XCTAssertEqual(result1.accuracy, 85, accuracy: 0.01)
        XCTAssertEqual(result1.stars, 3)

        let result2 = ScoringEngine.calculateScore(perfect: 8, good: 10, total: 20)
        XCTAssertEqual(result2.accuracy, 75, accuracy: 0.01)
        XCTAssertEqual(result2.stars, 2)
    }

    func testLowScoreGivesOneOrZeroStars() {
        let result1 = ScoringEngine.calculateScore(perfect: 5, good: 5, total: 20)
        XCTAssertEqual(result1.accuracy, 42.5, accuracy: 0.01)
        XCTAssertEqual(result1.stars, 0)

        let result2 = ScoringEngine.calculateScore(perfect: 8, good: 4, total: 20)
        XCTAssertEqual(result2.accuracy, 54, accuracy: 0.01)
        XCTAssertEqual(result2.stars, 1)
    }
}

import XCTest
@testable import PianoFlow

final class LessonProgressionTests: XCTestCase {

    func testCurriculumSetup() {
        let allLessons = LessonCurriculum.allLessons
        XCTAssertEqual(allLessons.count, 12)

        // First 5 lessons are free
        let freeLessons = allLessons.filter { !$0.isPremium }
        XCTAssertEqual(freeLessons.count, 5)

        for lesson in allLessons {
            XCTAssertFalse(lesson.targetNotes.isEmpty)
            XCTAssertGreaterThan(lesson.xpReward, 0)
        }
    }

    func testLessonUnlockLogic() {
        var completedLessonIds: Set<String> = []
        let allLessons = LessonCurriculum.allLessons

        XCTAssertTrue(LessonCurriculum.isLessonUnlocked(at: 0, completedLessonIds: completedLessonIds))
        XCTAssertFalse(LessonCurriculum.isLessonUnlocked(at: 1, completedLessonIds: completedLessonIds))

        completedLessonIds.insert(allLessons[0].id)
        XCTAssertTrue(LessonCurriculum.isLessonUnlocked(at: 1, completedLessonIds: completedLessonIds))
        XCTAssertFalse(LessonCurriculum.isLessonUnlocked(at: 2, completedLessonIds: completedLessonIds))

        completedLessonIds.insert(allLessons[1].id)
        XCTAssertTrue(LessonCurriculum.isLessonUnlocked(at: 2, completedLessonIds: completedLessonIds))
    }
}

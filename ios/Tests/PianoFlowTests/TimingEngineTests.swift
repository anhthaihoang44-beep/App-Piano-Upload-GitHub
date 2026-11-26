import XCTest
@testable import PianoFlow

final class TimingEngineTests: XCTestCase {

    func testClockStartAndPause() {
        var fakeNanos: UInt64 = 1_000_000_000
        let clock = MonotonicClock(nanoTimeProvider: { fakeNanos })

        XCTAssertFalse(clock.isPlaying)
        XCTAssertEqual(clock.currentPositionMs(), 0)

        clock.start(fromPositionMs: 100)
        XCTAssertTrue(clock.isPlaying)
        fakeNanos += 50_000_000 // +50ms
        XCTAssertEqual(clock.currentPositionMs(), 150)

        clock.pause()
        XCTAssertFalse(clock.isPlaying)
        let pausedPos = clock.currentPositionMs()
        fakeNanos += 30_000_000 // +30ms while paused
        XCTAssertEqual(clock.currentPositionMs(), pausedPos)

        clock.resume()
        XCTAssertTrue(clock.isPlaying)
        fakeNanos += 30_000_000 // +30ms
        XCTAssertEqual(clock.currentPositionMs(), pausedPos + 30)
    }

    func testClockSpeedMultiplier() {
        var fakeNanos: UInt64 = 1_000_000_000
        let clock = MonotonicClock(nanoTimeProvider: { fakeNanos })

        clock.setSpeed(1.5)
        XCTAssertEqual(clock.getSpeed(), 1.5, accuracy: 0.01)

        clock.start(fromPositionMs: 0)
        fakeNanos += 50_000_000 // +50ms
        XCTAssertEqual(clock.currentPositionMs(), 75)
    }

    func testClockSeek() {
        var fakeNanos: UInt64 = 1_000_000_000
        let clock = MonotonicClock(nanoTimeProvider: { fakeNanos })

        clock.start(fromPositionMs: 0)
        clock.seekTo(5000)
        XCTAssertEqual(clock.currentPositionMs(), 5000)

        fakeNanos += 100_000_000
        XCTAssertEqual(clock.currentPositionMs(), 5100)
    }
}

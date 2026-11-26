import Foundation

/// High-precision monotonic clock for the piano playback and falling notes rendering engine.
/// Uses monotonic uptime nanoseconds instead of wall-clock Date() to prevent drift or jitter.
public final class MonotonicClock {

    private var startNanoTime: UInt64 = 0
    private var pausedPositionMs: Int64 = 0
    private var isRunning: Bool = false
    private var speedMultiplier: Float = 1.0
    private let nanoTimeProvider: () -> UInt64

    public var isPlaying: Bool { isRunning }

    public init(nanoTimeProvider: @escaping () -> UInt64 = { DispatchTime.now().uptimeNanoseconds }) {
        self.nanoTimeProvider = nanoTimeProvider
    }

    public func start(fromPositionMs: Int64 = 0) {
        pausedPositionMs = fromPositionMs
        startNanoTime = nanoTimeProvider()
        isRunning = true
    }

    public func pause() {
        guard isRunning else { return }
        pausedPositionMs = currentPositionMs()
        isRunning = false
    }

    public func resume() {
        guard !isRunning else { return }
        startNanoTime = nanoTimeProvider()
        isRunning = true
    }

    public func seekTo(_ positionMs: Int64) {
        pausedPositionMs = positionMs
        if isRunning {
            startNanoTime = nanoTimeProvider()
        }
    }

    public func setSpeed(_ speed: Float) {
        if isRunning {
            pausedPositionMs = currentPositionMs()
            startNanoTime = nanoTimeProvider()
        }
        speedMultiplier = min(max(speed, 0.25), 2.5)
    }

    public func getSpeed() -> Float {
        speedMultiplier
    }

    public func currentPositionMs() -> Int64 {
        guard isRunning else { return pausedPositionMs }
        let now = nanoTimeProvider()
        let elapsedNanos = now >= startNanoTime ? (now - startNanoTime) : 0
        let elapsedMs = Float(elapsedNanos / 1_000_000) * speedMultiplier
        return pausedPositionMs + Int64(elapsedMs)
    }

    public func reset() {
        isRunning = false
        pausedPositionMs = 0
        startNanoTime = 0
    }
}

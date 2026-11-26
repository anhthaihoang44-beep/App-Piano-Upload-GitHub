import Combine
import Foundation

/// Clean audio engine protocol for low-latency polyphonic piano playback.
@MainActor
public protocol AudioEngineProtocol: AnyObject {
    var isLoaded: Bool { get }
    var loadingProgress: Float { get }
    var isLoadedPublisher: AnyPublisher<Bool, Never> { get }
    var loadingProgressPublisher: AnyPublisher<Float, Never> { get }

    func initialize() async
    @discardableResult
    func playNote(midiNumber: Int, velocity: Float) -> Int
    func stopNote(midiNumber: Int)
    func stopAllNotes()
    func setSustain(_ enabled: Bool)
    func isSustainEnabled() -> Bool
    func setVolume(_ volume: Float)
    func getVolume() -> Float
    func release()
}

public extension AudioEngineProtocol {
    @discardableResult
    func playNote(midiNumber: Int) -> Int {
        playNote(midiNumber: midiNumber, velocity: 1.0)
    }
}

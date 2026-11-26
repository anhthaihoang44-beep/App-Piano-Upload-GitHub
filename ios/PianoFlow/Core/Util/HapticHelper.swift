import Foundation
#if canImport(UIKit)
import UIKit
#endif

@MainActor
public final class HapticHelper {

    #if os(iOS)
    private let generator = UIImpactFeedbackGenerator(style: .light)
    #endif

    public init() {
        #if os(iOS)
        generator.prepare()
        #endif
    }

    public func keyPress(enabled: Bool = true) {
        guard enabled else { return }
        #if os(iOS)
        generator.impactOccurred(intensity: 0.7)
        generator.prepare()
        #endif
    }
}

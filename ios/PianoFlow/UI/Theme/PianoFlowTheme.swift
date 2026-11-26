import SwiftUI
#if canImport(UIKit)
import UIKit
#endif

public enum PianoFlowColors {
    public static let brandPrimary = Color(hex: 0x7C4DFF)
    public static let brandPrimaryDark = Color(hex: 0x651FFF)
    public static let brandSecondary = Color(hex: 0x00E5FF)
    public static let brandTertiary = Color(hex: 0xFF4081)

    public static let backgroundDark = Color(hex: 0x0B0D14)
    public static let surfaceDark = Color(hex: 0x141724)
    public static let surfaceVariantDark = Color(hex: 0x1E2235)
    public static let surfaceElevatedDark = Color(hex: 0x262C44)

    public static let whiteKeyColor = Color(hex: 0xFAFBFD)
    public static let whiteKeyPressedColor = Color(hex: 0xE0E7FF)
    public static let blackKeyColor = Color(hex: 0x14161F)
    public static let blackKeyPressedColor = Color(hex: 0x3B4361)

    public static let rightHandColor = Color(hex: 0x00E5FF)
    public static let leftHandColor = Color(hex: 0xFF4081)
    public static let goldStarColor = Color(hex: 0xFFD700)
    public static let successGreen = Color(hex: 0x00E676)
    public static let errorRed = Color(hex: 0xFF5252)

    #if canImport(UIKit)
    public static let uiBrandPrimary = UIColor(hex: 0x7C4DFF)
    public static let uiBrandSecondary = UIColor(hex: 0x00E5FF)
    public static let uiRightHand = UIColor(hex: 0x00E5FF)
    public static let uiLeftHand = UIColor(hex: 0xFF4081)
    public static let uiWhiteKey = UIColor(hex: 0xFAFBFD)
    public static let uiWhiteKeyPressed = UIColor(hex: 0xE0E7FF)
    public static let uiBlackKey = UIColor(hex: 0x14161F)
    public static let uiBlackKeyPressed = UIColor(hex: 0x3B4361)
    #endif
}

public extension Color {
    init(hex: UInt32, alpha: Double = 1.0) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: alpha)
    }
}

#if canImport(UIKit)
public extension UIColor {
    convenience init(hex: UInt32, alpha: CGFloat = 1.0) {
        let r = CGFloat((hex >> 16) & 0xFF) / 255.0
        let g = CGFloat((hex >> 8) & 0xFF) / 255.0
        let b = CGFloat(hex & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b, alpha: alpha)
    }
}
#endif

public enum L10n {
    public static func tr(_ key: String) -> String {
        NSLocalizedString(key, comment: "")
    }

    public static func xpEarned(_ xp: Int) -> String {
        String(format: NSLocalizedString("lessons_xp_earned", comment: ""), xp)
    }
}

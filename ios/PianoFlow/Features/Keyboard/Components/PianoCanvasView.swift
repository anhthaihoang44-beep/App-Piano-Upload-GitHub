import SwiftUI
import UIKit

public struct PianoKeyGeometry {
    public let midiNumber: Int
    public let isBlack: Bool
    public let rect: CGRect
    public let note: NotePitch
}

/// High-performance multi-touch piano keyboard view supporting chords, glissando,
/// dynamic zoom, horizontal scrolling, note labels (C-D-E / Do-Re-Mi / Off), and target highlights.
public struct PianoCanvasView: UIViewRepresentable {

    public var startMidi: Int = 36
    public var endMidi: Int = 84
    public var scrollOffset: CGFloat = 0
    public var keyWidthPt: CGFloat = 54
    public var notationType: NotationType = .cde
    public var pressedKeys: Set<Int> = []
    public var highlightedKeys: [Int: UIColor] = [:]
    public var onKeyDown: (Int) -> Void
    public var onKeyUp: (Int) -> Void

    public init(
        startMidi: Int = 36,
        endMidi: Int = 84,
        scrollOffset: CGFloat = 0,
        keyWidthPt: CGFloat = 54,
        notationType: NotationType = .cde,
        pressedKeys: Set<Int> = [],
        highlightedKeys: [Int: UIColor] = [:],
        onKeyDown: @escaping (Int) -> Void,
        onKeyUp: @escaping (Int) -> Void
    ) {
        self.startMidi = startMidi
        self.endMidi = endMidi
        self.scrollOffset = scrollOffset
        self.keyWidthPt = keyWidthPt
        self.notationType = notationType
        self.pressedKeys = pressedKeys
        self.highlightedKeys = highlightedKeys
        self.onKeyDown = onKeyDown
        self.onKeyUp = onKeyUp
    }

    public func makeUIView(context: Context) -> MultiTouchPianoUIView {
        let view = MultiTouchPianoUIView()
        view.backgroundColor = UIColor(hex: 0x0B0D14)
        view.isMultipleTouchEnabled = true
        view.isExclusiveTouch = false
        view.onKeyDown = onKeyDown
        view.onKeyUp = onKeyUp
        return view
    }

    public func updateUIView(_ uiView: MultiTouchPianoUIView, context: Context) {
        uiView.onKeyDown = onKeyDown
        uiView.onKeyUp = onKeyUp
        uiView.updateConfiguration(
            startMidi: startMidi,
            endMidi: endMidi,
            scrollOffset: scrollOffset,
            keyWidthPt: keyWidthPt,
            notationType: notationType,
            externalPressedKeys: pressedKeys,
            highlightedKeys: highlightedKeys
        )
    }
}

public final class MultiTouchPianoUIView: UIView {

    var onKeyDown: ((Int) -> Void)?
    var onKeyUp: ((Int) -> Void)?

    private var startMidi: Int = 36
    private var endMidi: Int = 84
    private var scrollOffset: CGFloat = 0
    private var keyWidthPt: CGFloat = 54
    private var notationType: NotationType = .cde
    private var externalPressedKeys: Set<Int> = []
    private var highlightedKeys: [Int: UIColor] = [:]

    private var geometries: [PianoKeyGeometry] = []
    private var currentTouchLocations: [ObjectIdentifier: CGPoint] = [:]
    private var activeTouchNotes: [ObjectIdentifier: Int] = [:]

    func updateConfiguration(
        startMidi: Int,
        endMidi: Int,
        scrollOffset: CGFloat,
        keyWidthPt: CGFloat,
        notationType: NotationType,
        externalPressedKeys: Set<Int>,
        highlightedKeys: [Int: UIColor]
    ) {
        let layoutChanged = self.startMidi != startMidi ||
            self.endMidi != endMidi ||
            self.keyWidthPt != keyWidthPt

        self.startMidi = startMidi
        self.endMidi = endMidi
        self.scrollOffset = scrollOffset
        self.keyWidthPt = keyWidthPt
        self.notationType = notationType
        self.externalPressedKeys = externalPressedKeys
        self.highlightedKeys = highlightedKeys

        if layoutChanged {
            recomputeGeometry()
        }
        setNeedsDisplay()
    }

    public override func layoutSubviews() {
        super.layoutSubviews()
        recomputeGeometry()
        setNeedsDisplay()
    }

    private func recomputeGeometry() {
        guard bounds.height > 0 else {
            geometries = []
            return
        }

        let whiteKeyWidth = keyWidthPt
        let whiteKeyHeight = bounds.height
        let blackKeyWidth = whiteKeyWidth * 0.62
        let blackKeyHeight = whiteKeyHeight * 0.62

        var result: [PianoKeyGeometry] = []
        var currentWhiteX: CGFloat = 0

        for midi in startMidi...endMidi {
            guard let note = NotePitch.fromMidi(midi), !note.isBlack else { continue }
            let rect = CGRect(x: currentWhiteX, y: 0, width: whiteKeyWidth, height: whiteKeyHeight)
            result.append(PianoKeyGeometry(midiNumber: midi, isBlack: false, rect: rect, note: note))
            currentWhiteX += whiteKeyWidth
        }

        for midi in startMidi...endMidi {
            guard let note = NotePitch.fromMidi(midi), note.isBlack else { continue }
            if let prevWhite = result.last(where: { !$0.isBlack && $0.midiNumber == midi - 1 }) {
                let blackX = prevWhite.rect.maxX - (blackKeyWidth / 2.0)
                let rect = CGRect(x: blackX, y: 0, width: blackKeyWidth, height: blackKeyHeight)
                result.append(PianoKeyGeometry(midiNumber: midi, isBlack: true, rect: rect, note: note))
            }
        }

        geometries = result
    }

    public override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        for touch in touches {
            currentTouchLocations[ObjectIdentifier(touch)] = touch.location(in: self)
        }
        reconcileActiveTouches()
    }

    public override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        for touch in touches {
            currentTouchLocations[ObjectIdentifier(touch)] = touch.location(in: self)
        }
        reconcileActiveTouches()
    }

    public override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        for touch in touches {
            currentTouchLocations.removeValue(forKey: ObjectIdentifier(touch))
        }
        reconcileActiveTouches()
    }

    public override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        for touch in touches {
            currentTouchLocations.removeValue(forKey: ObjectIdentifier(touch))
        }
        reconcileActiveTouches()
    }

    private func reconcileActiveTouches() {
        if geometries.isEmpty {
            recomputeGeometry()
        }

        var newActiveNotes: [ObjectIdentifier: Int] = [:]

        for (id, point) in currentTouchLocations {
            let canvasPoint = CGPoint(x: point.x + scrollOffset, y: point.y)
            if let blackHit = geometries.first(where: { $0.isBlack && $0.rect.contains(canvasPoint) }) {
                newActiveNotes[id] = blackHit.midiNumber
            } else if let whiteHit = geometries.first(where: { !$0.isBlack && $0.rect.contains(canvasPoint) }) {
                newActiveNotes[id] = whiteHit.midiNumber
            }
        }

        let prevNotes = Set(activeTouchNotes.values)
        let currNotes = Set(newActiveNotes.values)

        let released = prevNotes.subtracting(currNotes)
        let pressed = currNotes.subtracting(prevNotes)

        released.forEach { onKeyUp?($0) }
        pressed.forEach { onKeyDown?($0) }

        activeTouchNotes = newActiveNotes
        setNeedsDisplay()
    }

    public override func draw(_ rect: CGRect) {
        guard let ctx = UIGraphicsGetCurrentContext() else { return }
        if geometries.isEmpty {
            recomputeGeometry()
        }

        ctx.saveGState()
        ctx.translateBy(x: -scrollOffset, y: 0)

        let activeTouchNoteSet = Set(activeTouchNotes.values)

        // Draw White Keys first
        for geom in geometries where !geom.isBlack {
            let isPressed = externalPressedKeys.contains(geom.midiNumber) || activeTouchNoteSet.contains(geom.midiNumber)
            let highlight = highlightedKeys[geom.midiNumber]
            drawWhiteKey(in: ctx, geom: geom, isPressed: isPressed, highlightColor: highlight)
        }

        // Draw Black Keys on top
        for geom in geometries where geom.isBlack {
            let isPressed = externalPressedKeys.contains(geom.midiNumber) || activeTouchNoteSet.contains(geom.midiNumber)
            let highlight = highlightedKeys[geom.midiNumber]
            drawBlackKey(in: ctx, geom: geom, isPressed: isPressed, highlightColor: highlight)
        }

        ctx.restoreGState()
    }

    private func drawWhiteKey(
        in ctx: CGContext,
        geom: PianoKeyGeometry,
        isPressed: Bool,
        highlightColor: UIColor?
    ) {
        let r = geom.rect
        let keyRect = CGRect(x: r.minX + 1, y: r.minY, width: max(1, r.width - 2), height: max(1, r.height - 4))
        let bevelRect = CGRect(x: r.minX + 1, y: r.maxY - 8, width: max(1, r.width - 2), height: 8)

        // Bottom bevel
        let bevelPath = UIBezierPath(roundedRect: bevelRect, cornerRadius: 8)
        UIColor(hex: 0xBDC3C7).setFill()
        bevelPath.fill()

        // Main key rounded bottom path
        let keyPath = UIBezierPath(
            roundedRect: keyRect,
            byRoundingCorners: [.bottomLeft, .bottomRight],
            cornerRadii: CGSize(width: 8, height: 8)
        )

        let fillColor: UIColor
        if isPressed {
            fillColor = PianoFlowColors.uiWhiteKeyPressed
        } else if let highlightColor {
            fillColor = highlightColor.withAlphaComponent(0.35)
        } else {
            fillColor = PianoFlowColors.uiWhiteKey
        }
        fillColor.setFill()
        keyPath.fill()

        // 3D top-to-bottom subtle gradient
        ctx.saveGState()
        keyPath.addClip()
        let colors = [
            UIColor.white.withAlphaComponent(0.8).cgColor,
            UIColor(hex: 0xECEFF1).withAlphaComponent(0.4).cgColor
        ] as CFArray
        if let gradient = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(), colors: colors, locations: [0, 1]) {
            ctx.drawLinearGradient(
                gradient,
                start: CGPoint(x: r.midX, y: r.minY),
                end: CGPoint(x: r.midX, y: r.maxY),
                options: []
            )
        }
        ctx.restoreGState()

        if isPressed || highlightColor != nil {
            let accent = highlightColor ?? PianoFlowColors.uiBrandSecondary
            let accentRect = CGRect(x: r.minX + 2, y: r.maxY - 12, width: max(1, r.width - 4), height: 8)
            let accentPath = UIBezierPath(roundedRect: accentRect, cornerRadius: 4)
            accent.setFill()
            accentPath.fill()
        }

        let label = NotePitch.displayName(for: geom.midiNumber, notation: notationType)
        if !label.isEmpty {
            let textColor: UIColor = (isPressed || highlightColor != nil)
                ? PianoFlowColors.uiBrandPrimary
                : UIColor(red: 74 / 255, green: 85 / 255, blue: 104 / 255, alpha: 1)
            let attrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 12, weight: .bold),
                .foregroundColor: textColor
            ]
            let size = (label as NSString).size(withAttributes: attrs)
            let point = CGPoint(x: r.midX - size.width / 2, y: r.maxY - size.height - 14)
            (label as NSString).draw(at: point, withAttributes: attrs)
        }
    }

    private func drawBlackKey(
        in ctx: CGContext,
        geom: PianoKeyGeometry,
        isPressed: Bool,
        highlightColor: UIColor?
    ) {
        let r = geom.rect

        // Shadow behind black key
        let shadowRect = CGRect(x: r.minX - 2, y: r.minY, width: r.width + 4, height: r.height + 4)
        let shadowPath = UIBezierPath(roundedRect: shadowRect, cornerRadius: 6)
        UIColor.black.withAlphaComponent(0.45).setFill()
        shadowPath.fill()

        let keyPath = UIBezierPath(
            roundedRect: r,
            byRoundingCorners: [.bottomLeft, .bottomRight],
            cornerRadii: CGSize(width: 6, height: 6)
        )

        let baseColor: UIColor
        if isPressed {
            baseColor = PianoFlowColors.uiBlackKeyPressed
        } else if let highlightColor {
            baseColor = highlightColor.withAlphaComponent(0.7)
        } else {
            baseColor = PianoFlowColors.uiBlackKey
        }
        baseColor.setFill()
        keyPath.fill()

        // Glossy 3D gradient unless pressed/highlighted brightly
        if !isPressed && highlightColor == nil {
            ctx.saveGState()
            keyPath.addClip()
            let colors = [
                UIColor(hex: 0x4A4E69).cgColor,
                UIColor(hex: 0x1E2028).cgColor,
                UIColor(hex: 0x111318).cgColor
            ] as CFArray
            if let gradient = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(), colors: colors, locations: [0, 0.5, 1]) {
                ctx.drawLinearGradient(
                    gradient,
                    start: CGPoint(x: r.midX, y: r.minY),
                    end: CGPoint(x: r.midX, y: r.maxY),
                    options: []
                )
            }
            ctx.restoreGState()
        }

        if isPressed || highlightColor != nil {
            let accent = highlightColor ?? PianoFlowColors.uiBrandSecondary
            let accentRect = CGRect(x: r.minX + 3, y: r.maxY - 8, width: max(1, r.width - 6), height: 5)
            let accentPath = UIBezierPath(roundedRect: accentRect, cornerRadius: 3)
            accent.setFill()
            accentPath.fill()
        }

        if notationType != .off {
            let label = NotePitch.displayName(for: geom.midiNumber, notation: notationType)
            if !label.isEmpty {
                let attrs: [NSAttributedString.Key: Any] = [
                    .font: UIFont.systemFont(ofSize: 9, weight: .semibold),
                    .foregroundColor: UIColor.white
                ]
                let size = (label as NSString).size(withAttributes: attrs)
                let point = CGPoint(x: r.midX - size.width / 2, y: r.maxY - size.height - 10)
                (label as NSString).draw(at: point, withAttributes: attrs)
            }
        }
    }
}

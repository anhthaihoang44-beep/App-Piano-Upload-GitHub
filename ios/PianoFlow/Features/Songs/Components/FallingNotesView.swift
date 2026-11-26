import SwiftUI

public struct FallingNotesView: View {

    public let song: Song
    public let currentPositionMs: Int64
    public let startMidi: Int
    public let endMidi: Int
    public let keyWidthPt: CGFloat
    public let scrollOffset: CGFloat
    public let previewWindowMs: Int64

    public init(
        song: Song,
        currentPositionMs: Int64,
        startMidi: Int,
        endMidi: Int,
        keyWidthPt: CGFloat,
        scrollOffset: CGFloat = 0,
        previewWindowMs: Int64 = 2400
    ) {
        self.song = song
        self.currentPositionMs = currentPositionMs
        self.startMidi = startMidi
        self.endMidi = endMidi
        self.keyWidthPt = keyWidthPt
        self.scrollOffset = scrollOffset
        self.previewWindowMs = previewWindowMs
    }

    public var body: some View {
        Canvas { context, size in
            let whiteKeyWidth = keyWidthPt
            let blackKeyWidth = whiteKeyWidth * 0.62
            let hitLineY = size.height - 4

            // Hit target horizontal gradient line
            var hitLinePath = Path()
            hitLinePath.move(to: CGPoint(x: 0, y: hitLineY))
            hitLinePath.addLine(to: CGPoint(x: size.width, y: hitLineY))
            context.stroke(
                hitLinePath,
                with: .linearGradient(
                    Gradient(colors: [
                        PianoFlowColors.brandPrimary.opacity(0.2),
                        PianoFlowColors.brandSecondary.opacity(0.8),
                        PianoFlowColors.brandTertiary.opacity(0.8),
                        PianoFlowColors.brandPrimary.opacity(0.2)
                    ]),
                    startPoint: CGPoint(x: 0, y: hitLineY),
                    endPoint: CGPoint(x: size.width, y: hitLineY)
                ),
                lineWidth: 3
            )

            // Compute key center X positions
            var keyCenterXs: [Int: CGFloat] = [:]
            var currentWhiteX = -scrollOffset

            for midi in startMidi...endMidi {
                guard let note = NotePitch.fromMidi(midi), !note.isBlack else { continue }
                keyCenterXs[midi] = currentWhiteX + (whiteKeyWidth / 2.0)
                currentWhiteX += whiteKeyWidth
            }

            for midi in startMidi...endMidi {
                guard let note = NotePitch.fromMidi(midi), note.isBlack else { continue }
                if let prevWhiteCenterX = keyCenterXs[midi - 1] {
                    keyCenterXs[midi] = prevWhiteCenterX + (whiteKeyWidth / 2.0)
                }
            }

            // Render falling notes inside preview window
            for note in song.allNotes {
                let noteEndMs = note.startMs + note.durationMs
                if noteEndMs < currentPositionMs { continue }
                if note.startMs > currentPositionMs + previewWindowMs { break }

                guard let centerX = keyCenterXs[note.midi] else { continue }
                let isBlack = NotePitch.fromMidi(note.midi)?.isBlack ?? false
                let noteWidth = isBlack ? blackKeyWidth * 0.9 : whiteKeyWidth * 0.75

                let timeToHitMs = note.startMs - currentPositionMs
                let bottomY = hitLineY * (1.0 - (CGFloat(timeToHitMs) / CGFloat(previewWindowMs)))
                let heightPt = max(6, (CGFloat(note.durationMs) / CGFloat(previewWindowMs)) * hitLineY)
                let topY = bottomY - heightPt
                let leftX = centerX - (noteWidth / 2.0)

                let baseColor: Color
                switch note.hand {
                case .right:
                    baseColor = PianoFlowColors.rightHandColor
                case .left:
                    baseColor = PianoFlowColors.leftHandColor
                case .both:
                    baseColor = Color(hex: 0xB388FF)
                }

                // Glow shadow
                let glowRect = CGRect(x: leftX - 3, y: topY - 2, width: noteWidth + 6, height: heightPt + 4)
                context.fill(
                    Path(roundedRect: glowRect, cornerRadius: 8),
                    with: .color(baseColor.opacity(0.25))
                )

                // Note body gradient
                let noteRect = CGRect(x: leftX, y: topY, width: noteWidth, height: heightPt)
                context.fill(
                    Path(roundedRect: noteRect, cornerRadius: 6),
                    with: .linearGradient(
                        Gradient(colors: [baseColor.opacity(0.9), baseColor.opacity(0.6)]),
                        startPoint: CGPoint(x: leftX, y: topY),
                        endPoint: CGPoint(x: leftX, y: bottomY)
                    )
                )

                // Hit pulse when crossing target line
                if currentPositionMs >= note.startMs && currentPositionMs <= noteEndMs {
                    let outerPulse = CGRect(x: centerX - 16, y: hitLineY - 16, width: 32, height: 32)
                    context.fill(Path(ellipseIn: outerPulse), with: .color(baseColor.opacity(0.5)))

                    let innerPulse = CGRect(x: centerX - 8, y: hitLineY - 8, width: 16, height: 16)
                    context.fill(Path(ellipseIn: innerPulse), with: .color(.white))
                }
            }
        }
        .background(Color(hex: 0x0F121C))
    }
}

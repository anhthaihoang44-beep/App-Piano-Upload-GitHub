package com.pianoflow.app.feature.songs.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pianoflow.app.core.audio.NotePitch
import com.pianoflow.app.core.midi.HandType
import com.pianoflow.app.core.midi.Song
import com.pianoflow.app.ui.theme.LeftHandColor
import com.pianoflow.app.ui.theme.RightHandColor

@Composable
fun FallingNotesCanvas(
    song: Song,
    currentPositionMs: Long,
    startMidi: Int,
    endMidi: Int,
    keyWidthDp: Float,
    scrollOffset: Float,
    previewWindowMs: Long = 2400L,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val density = this.density
        val whiteKeyWidth = keyWidthDp * density
        val blackKeyWidth = whiteKeyWidth * 0.62f
        val hitLineY = size.height - 4f // Just above the piano keyboard

        // Draw hit target line
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF7C4DFF).copy(alpha = 0.2f),
                    Color(0xFF00E5FF).copy(alpha = 0.8f),
                    Color(0xFFFF4081).copy(alpha = 0.8f),
                    Color(0xFF7C4DFF).copy(alpha = 0.2f)
                )
            ),
            start = Offset(0f, hitLineY),
            end = Offset(size.width, hitLineY),
            strokeWidth = 3.dp.toPx()
        )

        // Compute Key X positions map for current range
        val keyCenterXs = mutableMapOf<Int, Float>()
        var currentWhiteX = -scrollOffset

        // Pass 1: White keys
        for (midi in startMidi..endMidi) {
            val note = NotePitch.fromMidi(midi) ?: continue
            if (!note.isBlack) {
                keyCenterXs[midi] = currentWhiteX + (whiteKeyWidth / 2f)
                currentWhiteX += whiteKeyWidth
            }
        }

        // Pass 2: Black keys
        for (midi in startMidi..endMidi) {
            val note = NotePitch.fromMidi(midi) ?: continue
            if (note.isBlack) {
                val prevWhiteCenterX = keyCenterXs[midi - 1]
                if (prevWhiteCenterX != null) {
                    keyCenterXs[midi] = prevWhiteCenterX + (whiteKeyWidth / 2f)
                }
            }
        }

        // Render all falling notes in current time window
        for (note in song.allNotes) {
            val noteEndMs = note.startMs + note.durationMs
            if (noteEndMs < currentPositionMs) continue
            if (note.startMs > currentPositionMs + previewWindowMs) break

            val centerX = keyCenterXs[note.midi] ?: continue
            val isBlack = NotePitch.fromMidi(note.midi)?.isBlack ?: false
            val noteWidth = if (isBlack) blackKeyWidth * 0.9f else whiteKeyWidth * 0.75f

            // Calculate vertical positions
            val timeToHitMs = note.startMs - currentPositionMs
            val bottomY = hitLineY * (1.0f - (timeToHitMs.toFloat() / previewWindowMs))
            val heightPx = (note.durationMs.toFloat() / previewWindowMs) * hitLineY
            val topY = bottomY - heightPx

            val leftX = centerX - (noteWidth / 2f)
            val cornerRad = 6.dp.toPx()

            val baseColor = when (note.hand) {
                HandType.RIGHT -> RightHandColor
                HandType.LEFT -> LeftHandColor
                HandType.BOTH -> Color(0xFFB388FF)
            }

            // Glow / shadow
            drawRoundRect(
                color = baseColor.copy(alpha = 0.25f),
                topLeft = Offset(leftX - 3f, topY - 2f),
                size = Size(noteWidth + 6f, heightPx + 4f),
                cornerRadius = CornerRadius(cornerRad + 2f, cornerRad + 2f)
            )

            // Note body
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.9f),
                        baseColor.copy(alpha = 0.6f)
                    ),
                    startY = topY,
                    endY = bottomY
                ),
                topLeft = Offset(leftX, topY),
                size = Size(noteWidth, heightPx),
                cornerRadius = CornerRadius(cornerRad, cornerRad)
            )

            // Bright hit pulse if note is currently crossing the hit line
            if (currentPositionMs in note.startMs..noteEndMs) {
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(centerX, hitLineY)
                )
                drawCircle(
                    color = baseColor.copy(alpha = 0.5f),
                    radius = 16.dp.toPx(),
                    center = Offset(centerX, hitLineY)
                )
            }
        }
    }
}

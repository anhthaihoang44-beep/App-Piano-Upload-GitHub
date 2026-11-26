package com.pianoflow.app.feature.keyboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.pianoflow.app.core.audio.NotationType
import com.pianoflow.app.core.audio.NotePitch
import com.pianoflow.app.ui.theme.BlackKeyColor
import com.pianoflow.app.ui.theme.BlackKeyPressedColor
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.WhiteKeyColor
import com.pianoflow.app.ui.theme.WhiteKeyPressedColor

data class KeyGeometry(
    val midiNumber: Int,
    val isBlack: Boolean,
    val rect: Rect,
    val note: NotePitch
)

@Composable
fun PianoCanvas(
    startMidi: Int = 36, // C2
    endMidi: Int = 84,   // C6 (4 full octaves)
    scrollOffset: Float = 0f,
    keyWidthDp: Float = 54f,
    notationType: NotationType = NotationType.CDE,
    pressedKeys: Set<Int> = emptySet(),
    highlightedKeys: Map<Int, Color> = emptyMap(),
    onKeyDown: (Int) -> Unit,
    onKeyUp: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnKeyDown by rememberUpdatedState(onKeyDown)
    val currentOnKeyUp by rememberUpdatedState(onKeyUp)

    // Touch pointer mapping: pointerId -> midiNumber
    val activePointers = remember { mutableStateMapOf<Long, Int>() }
    var currentGeometries by remember { mutableStateOf<List<KeyGeometry>>(emptyList()) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(startMidi, endMidi, scrollOffset, keyWidthDp) {
                awaitEachGesture {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedPointers = event.changes.filter { it.pressed }

                        val newActiveNotes = mutableMapOf<Long, Int>()

                        for (change in pressedPointers) {
                            val localX = change.position.x + scrollOffset
                            val localY = change.position.y

                            // 1. Check black keys first (top layer)
                            val hitBlackKey = currentGeometries.firstOrNull { geom ->
                                geom.isBlack && geom.rect.contains(Offset(localX, localY))
                            }

                            val hitKey = hitBlackKey ?: currentGeometries.firstOrNull { geom ->
                                !geom.isBlack && geom.rect.contains(Offset(localX, localY))
                            }

                            if (hitKey != null) {
                                newActiveNotes[change.id.value] = hitKey.midiNumber
                            }
                        }

                        // Determine note changes (glissando / new presses / releases)
                        val prevNotes = activePointers.values.toSet()
                        val currNotes = newActiveNotes.values.toSet()

                        val releasedNotes = prevNotes - currNotes
                        val pressedNewNotes = currNotes - prevNotes

                        releasedNotes.forEach { currentOnKeyUp(it) }
                        pressedNewNotes.forEach { currentOnKeyDown(it) }

                        activePointers.clear()
                        activePointers.putAll(newActiveNotes)

                        if (pressedPointers.isEmpty()) {
                            // All fingers lifted
                            prevNotes.forEach { currentOnKeyUp(it) }
                            activePointers.clear()
                            break
                        }
                    }
                }
            }
    ) {
        val density = this.density
        val whiteKeyWidth = keyWidthDp * density
        val whiteKeyHeight = size.height
        val blackKeyWidth = whiteKeyWidth * 0.62f
        val blackKeyHeight = whiteKeyHeight * 0.62f

        val geometries = mutableListOf<KeyGeometry>()

        // 1. Compute positions of white keys
        var currentWhiteX = 0f
        val whiteKeys = mutableListOf<NotePitch>()
        val blackKeys = mutableListOf<Pair<NotePitch, Float>>() // note to center X

        for (midi in startMidi..endMidi) {
            val note = NotePitch.fromMidi(midi) ?: continue
            if (!note.isBlack) {
                val rect = Rect(currentWhiteX, 0f, currentWhiteX + whiteKeyWidth, whiteKeyHeight)
                geometries.add(KeyGeometry(midi, false, rect, note))
                whiteKeys.add(note)
                currentWhiteX += whiteKeyWidth
            }
        }

        // 2. Compute positions of black keys relative to white keys
        for (midi in startMidi..endMidi) {
            val note = NotePitch.fromMidi(midi) ?: continue
            if (note.isBlack) {
                // Find white key immediately preceding this black key
                val prevWhite = geometries.lastOrNull { !it.isBlack && it.midiNumber == midi - 1 }
                if (prevWhite != null) {
                    val blackX = prevWhite.rect.right - (blackKeyWidth / 2f)
                    val rect = Rect(blackX, 0f, blackX + blackKeyWidth, blackKeyHeight)
                    geometries.add(KeyGeometry(midi, true, rect, note))
                }
            }
        }

        currentGeometries = geometries

        // DRAWING PASS:
        // Translate by -scrollOffset for horizontal scrolling
        drawContext.canvas.save()
        drawContext.canvas.translate(-scrollOffset, 0f)

        // Draw White Keys
        geometries.filter { !it.isBlack }.forEach { geom ->
            val isPressed = geom.midiNumber in pressedKeys || geom.midiNumber in activePointers.values
            val highlightColor = highlightedKeys[geom.midiNumber]

            drawWhiteKey(
                geom = geom,
                isPressed = isPressed,
                highlightColor = highlightColor,
                notationType = notationType
            )
        }

        // Draw Black Keys (layered on top)
        geometries.filter { it.isBlack }.forEach { geom ->
            val isPressed = geom.midiNumber in pressedKeys || geom.midiNumber in activePointers.values
            val highlightColor = highlightedKeys[geom.midiNumber]

            drawBlackKey(
                geom = geom,
                isPressed = isPressed,
                highlightColor = highlightColor,
                notationType = notationType
            )
        }

        drawContext.canvas.restore()
    }
}

private fun DrawScope.drawWhiteKey(
    geom: KeyGeometry,
    isPressed: Boolean,
    highlightColor: Color?,
    notationType: NotationType
) {
    val rect = geom.rect
    val cornerRadius = 8.dp.toPx()

    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = rect.left + 1f,
                top = rect.top,
                right = rect.right - 1f,
                bottom = rect.bottom - 4f,
                bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        )
    }

    val fillColor = when {
        isPressed -> WhiteKeyPressedColor
        highlightColor != null -> highlightColor.copy(alpha = 0.35f)
        else -> WhiteKeyColor
    }

    // Shadow & base bevel
    drawRoundRect(
        color = Color(0xFFBDC3C7),
        topLeft = Offset(rect.left + 1f, rect.bottom - 8f),
        size = Size(rect.width - 2f, 8f),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // Key surface
    drawPath(path = path, color = fillColor)

    // Subtle 3D gradient overlay
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                Color(0xFFECEFF1).copy(alpha = 0.4f)
            ),
            startY = rect.top,
            endY = rect.bottom
        )
    )

    // Active pressed highlight border
    if (isPressed || highlightColor != null) {
        val accentColor = highlightColor ?: BrandSecondary
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(rect.left + 2f, rect.bottom - 12f),
            size = Size(rect.width - 4f, 8f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
    }

    // Key Label text at bottom
    val label = NotePitch.getDisplayName(geom.midiNumber, notationType)
    if (label.isNotEmpty()) {
        val textPaint = android.graphics.Paint().apply {
            color = if (isPressed || highlightColor != null) android.graphics.Color.rgb(124, 77, 255) else android.graphics.Color.rgb(74, 85, 104)
            textSize = 28f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            label,
            rect.center.x,
            rect.bottom - 24f,
            textPaint
        )
    }
}

private fun DrawScope.drawBlackKey(
    geom: KeyGeometry,
    isPressed: Boolean,
    highlightColor: Color?,
    notationType: NotationType
) {
    val rect = geom.rect
    val cornerRadius = 6.dp.toPx()

    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = rect.left,
                top = rect.top,
                right = rect.right,
                bottom = rect.bottom,
                bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        )
    }

    val baseColor = when {
        isPressed -> BlackKeyPressedColor
        highlightColor != null -> highlightColor.copy(alpha = 0.7f)
        else -> BlackKeyColor
    }

    // Shadow below black key
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(rect.left - 2f, rect.top),
        size = Size(rect.width + 4f, rect.height + 4f),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // Black key body
    drawPath(path = path, color = baseColor)

    // Glossy 3D top reflection
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4A4E69),
                Color(0xFF1E2028),
                Color(0xFF111318)
            ),
            startY = rect.top,
            endY = rect.bottom
        )
    )

    if (isPressed || highlightColor != null) {
        val accent = highlightColor ?: BrandSecondary
        drawRoundRect(
            color = accent,
            topLeft = Offset(rect.left + 3f, rect.bottom - 8f),
            size = Size(rect.width - 6f, 5f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
    }

    // Optional solfege / note label on black key
    if (notationType != NotationType.OFF) {
        val label = NotePitch.getDisplayName(geom.midiNumber, notationType)
        if (label.isNotEmpty()) {
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 20f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(
                label,
                rect.center.x,
                rect.bottom - 16f,
                textPaint
            )
        }
    }
}

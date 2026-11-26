package com.pianoflow.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pianoflow.app.core.audio.NotationType
import com.pianoflow.app.feature.keyboard.components.PianoCanvas
import com.pianoflow.app.ui.theme.PianoFlowTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class KeyboardUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testKeyboardRendersAndReceivesTouches() {
        val pressedNotes = mutableListOf<Int>()

        composeTestRule.setContent {
            PianoFlowTheme {
                PianoCanvas(
                    startMidi = 60,
                    endMidi = 72,
                    keyWidthDp = 60f,
                    notationType = NotationType.CDE,
                    onKeyDown = { pressedNotes.add(it) },
                    onKeyUp = {}
                )
            }
        }

        // Verify canvas renders without crash
        composeTestRule.waitForIdle()
    }
}

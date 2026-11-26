package com.pianoflow.app.core.audio

import kotlin.math.pow

data class NotePitch(
    val midiNumber: Int,
    val nameEn: String,
    val nameSolfege: String,
    val octave: Int,
    val isBlack: Boolean,
    val frequencyHz: Float
) {
    companion object {
        private val NOTE_NAMES_EN = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        private val NOTE_NAMES_SOLFEGE = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
        private val BLACK_KEY_INDICES = setOf(1, 3, 6, 8, 10) // C#, D#, F#, G#, A#

        val ALL_NOTES: List<NotePitch> = (21..108).map { midi ->
            val noteIndex = (midi - 12) % 12
            val octave = (midi - 12) / 12
            val isBlack = noteIndex in BLACK_KEY_INDICES
            val freq = (440.0 * 2.0.pow((midi - 69) / 12.0)).toFloat()
            NotePitch(
                midiNumber = midi,
                nameEn = "${NOTE_NAMES_EN[noteIndex]}$octave",
                nameSolfege = "${NOTE_NAMES_SOLFEGE[noteIndex]}$octave",
                octave = octave,
                isBlack = isBlack,
                frequencyHz = freq
            )
        }

        private val NOTE_MAP = ALL_NOTES.associateBy { it.midiNumber }

        fun fromMidi(midiNumber: Int): NotePitch? = NOTE_MAP[midiNumber]

        fun getDisplayName(midiNumber: Int, notation: NotationType): String {
            val note = fromMidi(midiNumber) ?: return ""
            return when (notation) {
                NotationType.CDE -> note.nameEn
                NotationType.DO_RE_MI -> note.nameSolfege
                NotationType.OFF -> ""
            }
        }
    }
}

enum class NotationType {
    CDE,
    DO_RE_MI,
    OFF
}

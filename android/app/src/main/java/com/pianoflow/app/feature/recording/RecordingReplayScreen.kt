package com.pianoflow.app.feature.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pianoflow.app.R
import com.pianoflow.app.core.audio.AudioEngine
import com.pianoflow.app.core.audio.NotationType
import com.pianoflow.app.core.database.entity.RecordingEntity
import com.pianoflow.app.core.midi.HandType
import com.pianoflow.app.core.midi.MidiNoteEvent
import com.pianoflow.app.core.midi.Song
import com.pianoflow.app.core.midi.SongTrack
import com.pianoflow.app.core.util.MonotonicClock
import com.pianoflow.app.feature.keyboard.components.PianoCanvas
import com.pianoflow.app.feature.songs.components.FallingNotesCanvas
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.SurfaceDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.json.JSONArray

@Composable
fun RecordingReplayScreen(
    recording: RecordingEntity,
    audioEngine: AudioEngine,
    onBack: () -> Unit
) {
    val song = remember(recording) {
        val notes = mutableListOf<MidiNoteEvent>()
        try {
            val arr = JSONArray(recording.notesJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                notes.add(
                    MidiNoteEvent(
                        midi = obj.getInt("midi"),
                        startMs = obj.getLong("startMs"),
                        durationMs = obj.optLong("durationMs", 400L),
                        hand = HandType.RIGHT
                    )
                )
            }
        } catch (_: Exception) {}

        Song(
            id = "recording_${recording.id}",
            title = recording.title,
            composer = "My Recording",
            difficulty = "Custom",
            bpm = 120,
            timeSignature = "4/4",
            isPremium = false,
            tracks = listOf(SongTrack(HandType.RIGHT, notes))
        )
    }

    val clock = remember { MonotonicClock() }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    val playedIndices = remember { mutableSetOf<Int>() }
    val highlightedKeys = remember { mutableStateMapOf<Int, Color>() }

    val startMidi = 48
    val endMidi = 84

    LaunchedEffect(Unit) {
        clock.start(0L)
        isPlaying = true
        while (isActive) {
            if (clock.isPlaying) {
                val pos = clock.currentPositionMs()
                currentPositionMs = pos

                // Audio playback
                song.allNotes.forEachIndexed { idx, note ->
                    if (idx !in playedIndices && pos >= note.startMs) {
                        playedIndices.add(idx)
                        audioEngine.playNote(note.midi)
                    }
                }

                // Visual target keys
                highlightedKeys.clear()
                song.allNotes.forEach { note ->
                    if (pos in (note.startMs - 80L)..(note.startMs + note.durationMs)) {
                        highlightedKeys[note.midi] = BrandSecondary
                    }
                }

                if (pos >= recording.durationMs + 1000L) {
                    clock.pause()
                    isPlaying = false
                }
            }
            delay(16)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            clock.reset()
            audioEngine.stopAllNotes()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column {
                Text(
                    text = recording.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.recordings_replay_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandSecondary
                )
            }
        }

        // Waterfall
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .background(Color(0xFF0F121C))
        ) {
            FallingNotesCanvas(
                song = song,
                currentPositionMs = currentPositionMs,
                startMidi = startMidi,
                endMidi = endMidi,
                keyWidthDp = 50f,
                scrollOffset = 0f,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Keyboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            PianoCanvas(
                startMidi = startMidi,
                endMidi = endMidi,
                keyWidthDp = 50f,
                notationType = NotationType.CDE,
                highlightedKeys = highlightedKeys,
                onKeyDown = { audioEngine.playNote(it) },
                onKeyUp = { audioEngine.stopNote(it) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                val duration = recording.durationMs.toFloat().coerceAtLeast(1000f)
                Slider(
                    value = currentPositionMs.toFloat().coerceIn(0f, duration),
                    onValueChange = {
                        clock.seekTo(it.toLong())
                        currentPositionMs = it.toLong()
                        playedIndices.clear()
                        song.allNotes.forEachIndexed { idx, n -> if (n.startMs < it) playedIndices.add(idx) }
                    },
                    valueRange = 0f..duration,
                    colors = SliderDefaults.colors(thumbColor = BrandSecondary, activeTrackColor = BrandPrimary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                clock.pause()
                                isPlaying = false
                            } else {
                                clock.resume()
                                isPlaying = true
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(BrandPrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

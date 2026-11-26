package com.pianoflow.app.feature.songs

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pianoflow.app.core.audio.AudioEngine
import com.pianoflow.app.core.database.AppDatabase
import com.pianoflow.app.core.database.entity.SongScoreEntity
import com.pianoflow.app.core.midi.BundledSongs
import com.pianoflow.app.core.midi.HandType
import com.pianoflow.app.core.midi.MidiNoteEvent
import com.pianoflow.app.core.midi.Song
import com.pianoflow.app.core.util.MonotonicClock
import com.pianoflow.app.ui.theme.LeftHandColor
import com.pianoflow.app.ui.theme.RightHandColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class PlayMode {
    WATCH,
    FOLLOW,
    PERFORM
}

enum class HitRating {
    PERFECT,
    GOOD,
    MISS
}

data class ScoreSummary(
    val score: Int,
    val maxCombo: Int,
    val accuracyPercent: Float,
    val stars: Int,
    val perfectHits: Int,
    val goodHits: Int,
    val misses: Int
)

class SongPlayerViewModel(
    private val songId: String,
    private val audioEngine: AudioEngine,
    private val database: AppDatabase
) : ViewModel() {

    private val _song = MutableStateFlow(BundledSongs.getById(songId) ?: BundledSongs.ALL_SONGS.first())
    val song: StateFlow<Song> = _song.asStateFlow()

    private val _playMode = MutableStateFlow(PlayMode.WATCH)
    val playMode: StateFlow<PlayMode> = _playMode.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _selectedHand = MutableStateFlow(HandType.BOTH)
    val selectedHand: StateFlow<HandType> = _selectedHand.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    // Scoring & Combo state for PERFORM mode
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _currentCombo = MutableStateFlow(0)
    val currentCombo: StateFlow<Int> = _currentCombo.asStateFlow()

    private val _maxCombo = MutableStateFlow(0)
    val maxCombo: StateFlow<Int> = _maxCombo.asStateFlow()

    private val _lastHitRating = MutableStateFlow<HitRating?>(null)
    val lastHitRating: StateFlow<HitRating?> = _lastHitRating.asStateFlow()

    private val _highlightedKeys = MutableStateFlow<Map<Int, Color>>(emptyMap())
    val highlightedKeys: StateFlow<Map<Int, Color>> = _highlightedKeys.asStateFlow()

    private val _scoreSummary = MutableStateFlow<ScoreSummary?>(null)
    val scoreSummary: StateFlow<ScoreSummary?> = _scoreSummary.asStateFlow()

    private val clock = MonotonicClock()
    private var clockJob: Job? = null
    private val playedSongNoteIndices = mutableSetOf<Int>()
    private val scoredNoteIndices = mutableSetOf<Int>()

    private var perfectHits = 0
    private var goodHits = 0
    private var misses = 0

    init {
        startClockLoop()
    }

    private fun startClockLoop() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (isActive) {
                if (clock.isPlaying) {
                    val pos = clock.currentPositionMs()
                    _currentPositionMs.value = pos

                    // 1. Update target highlighted keys for notes currently hitting the line
                    updateTargetKeys(pos)

                    // 2. Watch mode: auto play notes
                    if (_playMode.value == PlayMode.WATCH) {
                        handleWatchModePlayback(pos)
                    } else if (_playMode.value == PlayMode.FOLLOW) {
                        handleFollowModeCheck(pos)
                    }

                    // 3. Check song end
                    if (pos >= _song.value.durationMs + 1000L) {
                        if (_isLooping.value) {
                            seekTo(0L)
                        } else {
                            pause()
                            finishSongPerformance()
                        }
                    }
                }
                delay(16) // ~60fps refresh tick
            }
        }
    }

    private fun updateTargetKeys(pos: Long) {
        val targets = mutableMapOf<Int, Color>()
        val filteredNotes = getFilteredNotes()

        filteredNotes.forEach { note ->
            if (pos in (note.startMs - 100L)..(note.startMs + note.durationMs)) {
                val color = if (note.hand == HandType.LEFT) LeftHandColor else RightHandColor
                targets[note.midi] = color
            }
        }
        _highlightedKeys.value = targets
    }

    private fun handleWatchModePlayback(pos: Long) {
        val filteredNotes = getFilteredNotes()
        filteredNotes.forEachIndexed { index, note ->
            if (index !in playedSongNoteIndices && pos >= note.startMs) {
                playedSongNoteIndices.add(index)
                audioEngine.playNote(note.midi, note.velocity)
            }
        }
    }

    private fun handleFollowModeCheck(pos: Long) {
        val filteredNotes = getFilteredNotes()
        val nextNote = filteredNotes.firstOrNull { it.startMs > pos - 150L && it.startMs <= pos + 50L }
        if (nextNote != null && pos >= nextNote.startMs) {
            // Pause clock until user hits the target note
            clock.pause()
            _isPlaying.value = false
        }
    }

    fun onUserKeyDown(midiNumber: Int) {
        audioEngine.playNote(midiNumber)

        val pos = _currentPositionMs.value
        val filteredNotes = getFilteredNotes()

        if (_playMode.value == PlayMode.FOLLOW) {
            val targetNote = filteredNotes.firstOrNull { abs(it.startMs - pos) <= 150L && it.midi == midiNumber }
            if (targetNote != null) {
                // Resume follow mode
                clock.resume()
                _isPlaying.value = true
            }
        } else if (_playMode.value == PlayMode.PERFORM) {
            // Evaluate hit accuracy
            val candidateIndex = filteredNotes.indexOfFirst {
                it.midi == midiNumber && it !in scoredNoteIndices.map { idx -> filteredNotes[idx] } && abs(it.startMs - pos) <= 220L
            }

            if (candidateIndex != -0 && candidateIndex != -1) {
                scoredNoteIndices.add(candidateIndex)
                val targetNote = filteredNotes[candidateIndex]
                val diff = abs(targetNote.startMs - pos)

                if (diff <= 80L) {
                    // Perfect
                    perfectHits++
                    _currentCombo.value++
                    _score.value += 100 + (_currentCombo.value * 5)
                    _lastHitRating.value = HitRating.PERFECT
                } else {
                    // Good
                    goodHits++
                    _currentCombo.value++
                    _score.value += 70 + (_currentCombo.value * 2)
                    _lastHitRating.value = HitRating.GOOD
                }

                if (_currentCombo.value > _maxCombo.value) {
                    _maxCombo.value = _currentCombo.value
                }
            }
        }
    }

    fun onUserKeyUp(midiNumber: Int) {
        audioEngine.stopNote(midiNumber)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        clock.resume()
        _isPlaying.value = true
    }

    fun pause() {
        clock.pause()
        _isPlaying.value = false
    }

    fun seekTo(positionMs: Long) {
        clock.seekTo(positionMs)
        _currentPositionMs.value = positionMs
        playedSongNoteIndices.clear()
        _song.value.allNotes.forEachIndexed { index, note ->
            if (note.startMs < positionMs) {
                playedSongNoteIndices.add(index)
            }
        }
    }

    fun setMode(mode: PlayMode) {
        _playMode.value = mode
        resetScoring()
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        clock.setSpeed(speed)
    }

    fun toggleLoop() {
        _isLooping.value = !_isLooping.value
    }

    fun setSelectedHand(hand: HandType) {
        _selectedHand.value = hand
        resetScoring()
    }

    private fun resetScoring() {
        _score.value = 0
        _currentCombo.value = 0
        _maxCombo.value = 0
        _lastHitRating.value = null
        _scoreSummary.value = null
        scoredNoteIndices.clear()
        perfectHits = 0
        goodHits = 0
        misses = 0
    }

    private fun getFilteredNotes(): List<MidiNoteEvent> {
        return when (_selectedHand.value) {
            HandType.BOTH -> _song.value.allNotes
            HandType.RIGHT -> _song.value.tracks.filter { it.hand == HandType.RIGHT }.flatMap { it.notes }
            HandType.LEFT -> _song.value.tracks.filter { it.hand == HandType.LEFT }.flatMap { it.notes }
        }
    }

    private fun finishSongPerformance() {
        val totalNotes = getFilteredNotes().size
        if (totalNotes == 0) return

        misses = kotlin.math.max(0, totalNotes - (perfectHits + goodHits))
        val accuracy = if (totalNotes > 0) {
            ((perfectHits * 100f + goodHits * 70f) / (totalNotes * 100f)) * 100f
        } else 0f

        val stars = when {
            accuracy >= 85f -> 3
            accuracy >= 70f -> 2
            accuracy >= 45f -> 1
            else -> 0
        }

        val summary = ScoreSummary(
            score = _score.value,
            maxCombo = _maxCombo.value,
            accuracyPercent = accuracy,
            stars = stars,
            perfectHits = perfectHits,
            goodHits = goodHits,
            misses = misses
        )
        _scoreSummary.value = summary

        // Save high score to Room
        viewModelScope.launch {
            val entity = SongScoreEntity(
                songId = _song.value.id,
                highScore = _score.value,
                stars = stars,
                accuracyPercent = accuracy,
                maxCombo = _maxCombo.value
            )
            database.songScoreDao().insertOrUpdateScore(entity)
        }
    }

    fun dismissSummary() {
        _scoreSummary.value = null
    }

    override fun onCleared() {
        super.onCleared()
        clockJob?.cancel()
        clock.reset()
    }
}

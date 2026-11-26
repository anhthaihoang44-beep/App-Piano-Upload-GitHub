package com.pianoflow.app.feature.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pianoflow.app.core.audio.AudioEngine
import com.pianoflow.app.core.audio.NotationType
import com.pianoflow.app.core.audio.RecordedNoteEvent
import com.pianoflow.app.core.database.AppDatabase
import com.pianoflow.app.core.database.entity.RecordingEntity
import com.pianoflow.app.core.datastore.UserPreferencesRepository
import com.pianoflow.app.core.util.HapticHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class ActiveNoteInfo(
    val midiNumber: Int,
    val startTimestamp: Long
)

class KeyboardViewModel(
    private val audioEngine: AudioEngine,
    private val preferencesRepository: UserPreferencesRepository,
    private val database: AppDatabase,
    private val hapticHelper: HapticHelper
) : ViewModel() {

    val isAudioLoaded = audioEngine.isLoaded
    val audioLoadingProgress = audioEngine.loadingProgress

    val notationType = preferencesRepository.notationType.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NotationType.CDE
    )

    val isSustainEnabled = preferencesRepository.sustainEnabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val masterVolume = preferencesRepository.volume.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0.9f
    )

    val hapticsEnabled = preferencesRepository.hapticsEnabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    private val _scrollOffset = MutableStateFlow(0f)
    val scrollOffset: StateFlow<Float> = _scrollOffset.asStateFlow()

    private val _keyWidthDp = MutableStateFlow(54f)
    val keyWidthDp: StateFlow<Float> = _keyWidthDp.asStateFlow()

    private val _currentOctaveOffset = MutableStateFlow(0)
    val currentOctaveOffset: StateFlow<Int> = _currentOctaveOffset.asStateFlow()

    // Recording state
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private var recordingStartTimeMs = 0L
    private val recordedNotesList = mutableListOf<RecordedNoteEvent>()
    private val activeHeldNotes = mutableMapOf<Int, ActiveNoteInfo>()

    init {
        viewModelScope.launch {
            preferencesRepository.sustainEnabled.collect {
                audioEngine.setSustain(it)
            }
        }
        viewModelScope.launch {
            preferencesRepository.volume.collect {
                audioEngine.setVolume(it)
            }
        }
    }

    fun onKeyDown(midiNumber: Int) {
        val now = System.currentTimeMillis()
        audioEngine.playNote(midiNumber)
        if (hapticsEnabled.value) {
            hapticHelper.keyPress()
        }

        if (_isRecording.value) {
            activeHeldNotes[midiNumber] = ActiveNoteInfo(midiNumber, now)
        }
    }

    fun onKeyUp(midiNumber: Int) {
        audioEngine.stopNote(midiNumber)

        if (_isRecording.value) {
            activeHeldNotes.remove(midiNumber)?.let { info ->
                val startMs = info.startTimestamp - recordingStartTimeMs
                val durationMs = kotlin.math.max(100L, System.currentTimeMillis() - info.startTimestamp)
                recordedNotesList.add(
                    RecordedNoteEvent(
                        midiNumber = midiNumber,
                        startMs = startMs,
                        durationMs = durationMs
                    )
                )
            }
        }
    }

    fun toggleSustain() {
        viewModelScope.launch {
            val nextState = !isSustainEnabled.value
            preferencesRepository.setSustainEnabled(nextState)
            audioEngine.setSustain(nextState)
        }
    }

    fun cycleNotationType() {
        viewModelScope.launch {
            val nextType = when (notationType.value) {
                NotationType.CDE -> NotationType.DO_RE_MI
                NotationType.DO_RE_MI -> NotationType.OFF
                NotationType.OFF -> NotationType.CDE
            }
            preferencesRepository.setNotationType(nextType)
        }
    }

    fun setVolume(vol: Float) {
        viewModelScope.launch {
            preferencesRepository.setVolume(vol)
            audioEngine.setVolume(vol)
        }
    }

    fun zoomIn() {
        _keyWidthDp.value = (_keyWidthDp.value + 6f).coerceAtMost(80f)
    }

    fun zoomOut() {
        _keyWidthDp.value = (_keyWidthDp.value - 6f).coerceAtLeast(36f)
    }

    fun resetZoom() {
        _keyWidthDp.value = 54f
        _scrollOffset.value = 0f
    }

    fun setScrollOffset(offset: Float) {
        _scrollOffset.value = offset.coerceAtLeast(0f)
    }

    fun shiftOctave(delta: Int) {
        _currentOctaveOffset.value = (_currentOctaveOffset.value + delta).coerceIn(-2, 2)
    }

    // Recording Controls
    fun startRecording() {
        recordedNotesList.clear()
        activeHeldNotes.clear()
        recordingStartTimeMs = System.currentTimeMillis()
        _isRecording.value = true
        _recordingDurationMs.value = 0L
    }

    fun stopRecording(onComplete: (Int, Long) -> Unit) {
        if (!_isRecording.value) return
        _isRecording.value = false
        val durationMs = System.currentTimeMillis() - recordingStartTimeMs

        // Finish any held notes
        val now = System.currentTimeMillis()
        activeHeldNotes.forEach { (midi, info) ->
            recordedNotesList.add(
                RecordedNoteEvent(
                    midiNumber = midi,
                    startMs = info.startTimestamp - recordingStartTimeMs,
                    durationMs = kotlin.math.max(120L, now - info.startTimestamp)
                )
            )
        }
        activeHeldNotes.clear()

        onComplete(recordedNotesList.size, durationMs)
    }

    fun saveRecording(title: String, durationMs: Long) {
        viewModelScope.launch {
            val jsonArray = JSONArray()
            recordedNotesList.forEach { note ->
                val obj = JSONObject().apply {
                    put("midi", note.midiNumber)
                    put("startMs", note.startMs)
                    put("durationMs", note.durationMs)
                }
                jsonArray.put(obj)
            }

            val entity = RecordingEntity(
                title = title.ifBlank { "Piano Melody ${System.currentTimeMillis() % 10000}" },
                durationMs = durationMs,
                noteCount = recordedNotesList.size,
                notesJson = jsonArray.toString()
            )
            database.recordingDao().insertRecording(entity)
        }
    }
}

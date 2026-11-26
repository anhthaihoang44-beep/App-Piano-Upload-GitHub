package com.pianoflow.app.feature.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoflow.app.R
import com.pianoflow.app.core.audio.NotationType
import com.pianoflow.app.feature.keyboard.components.PianoCanvas
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.BrandTertiary
import com.pianoflow.app.ui.theme.SurfaceDark
import com.pianoflow.app.ui.theme.SurfaceElevatedDark
import com.pianoflow.app.ui.theme.SurfaceVariantDark

@Composable
fun KeyboardScreen(
    viewModel: KeyboardViewModel,
    onNavigateToDoubleKeyboard: () -> Unit,
    onNavigateToTwoPlayer: () -> Unit
) {
    val isAudioLoaded by viewModel.isAudioLoaded.collectAsState()
    val audioLoadingProgress by viewModel.audioLoadingProgress.collectAsState()
    val notationType by viewModel.notationType.collectAsState()
    val isSustainEnabled by viewModel.isSustainEnabled.collectAsState()
    val volume by viewModel.masterVolume.collectAsState()
    val scrollOffset by viewModel.scrollOffset.collectAsState()
    val keyWidthDp by viewModel.keyWidthDp.collectAsState()
    val octaveOffset by viewModel.currentOctaveOffset.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    var showVolumeDialog by remember { mutableStateOf(false) }
    var showSaveRecordingDialog by remember { mutableStateOf(false) }
    var recordingSaveDurationMs by remember { mutableFloatStateOf(0f) }
    var recordingNoteCount by remember { mutableStateOf(0) }
    var recordingTitleInput by remember { mutableStateOf("") }

    val startMidi = (36 + octaveOffset * 12).coerceIn(21, 72)
    val endMidi = (startMidi + 36).coerceIn(48, 108)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Toolbar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sustain Button
                Button(
                    onClick = { viewModel.toggleSustain() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSustainEnabled) BrandSecondary else SurfaceElevatedDark,
                        contentColor = if (isSustainEnabled) BackgroundDark else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.keyboard_sustain),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Note Labels Toggle
                Button(
                    onClick = { viewModel.cycleNotationType() },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevatedDark),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (notationType) {
                            NotationType.CDE -> stringResource(R.string.keyboard_label_cde)
                            NotationType.DO_RE_MI -> stringResource(R.string.keyboard_label_doremi)
                            NotationType.OFF -> stringResource(R.string.keyboard_label_off)
                        },
                        fontSize = 12.sp,
                        color = BrandPrimary
                    )
                }

                // Octave controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { viewModel.shiftOctave(-1) },
                        enabled = octaveOffset > -2
                    ) {
                        Text("◀ 8va", color = if (octaveOffset > -2) Color.White else Color.Gray, fontSize = 12.sp)
                    }
                    Text(
                        text = "C${startMidi / 12}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { viewModel.shiftOctave(1) },
                        enabled = octaveOffset < 2
                    ) {
                        Text("8va ▶", color = if (octaveOffset < 2) Color.White else Color.Gray, fontSize = 12.sp)
                    }
                }

                // Zoom controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.zoomOut() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.zoomIn() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                    }
                }

                // Modes & Volume
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showVolumeDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Volume", tint = Color.White)
                    }

                    IconButton(onClick = onNavigateToDoubleKeyboard, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Keyboard, contentDescription = "Double Keyboard", tint = BrandPrimary)
                    }

                    IconButton(onClick = onNavigateToTwoPlayer, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.People, contentDescription = "2-Player", tint = BrandSecondary)
                    }

                    // Recording button
                    IconButton(
                        onClick = {
                            if (!isRecording) {
                                viewModel.startRecording()
                            } else {
                                viewModel.stopRecording { count, durationMs ->
                                    recordingNoteCount = count
                                    recordingSaveDurationMs = durationMs.toFloat()
                                    recordingTitleInput = ""
                                    showSaveRecordingDialog = true
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isRecording) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Stop Recording",
                                tint = BrandTertiary,
                                modifier = Modifier.alpha(pulseAlpha)
                            )
                        } else {
                            Icon(
                                Icons.Default.FiberManualRecord,
                                contentDescription = "Record",
                                tint = BrandTertiary
                            )
                        }
                    }
                }
            }
        }

        // Audio Loading banner if soundpool is still loading
        AnimatedVisibility(visible = !isAudioLoaded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.keyboard_loading_audio),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { audioLoadingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = BrandSecondary,
                    trackColor = SurfaceVariantDark
                )
            }
        }

        // Mini Octave Navigator Bar (Full Range Scroller)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceDark)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        viewModel.setScrollOffset(scrollOffset + dragAmount.x * 2.5f)
                    }
                }
        ) {
            // Viewport indicator
            Box(
                modifier = Modifier
                    .fillMaxSize(0.35f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(BrandPrimary.copy(alpha = 0.45f))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Interactive Piano Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            PianoCanvas(
                startMidi = startMidi,
                endMidi = endMidi,
                scrollOffset = scrollOffset,
                keyWidthDp = keyWidthDp,
                notationType = notationType,
                onKeyDown = { viewModel.onKeyDown(it) },
                onKeyUp = { viewModel.onKeyUp(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Volume Dialog
    if (showVolumeDialog) {
        AlertDialog(
            onDismissRequest = { showVolumeDialog = false },
            title = { Text(stringResource(R.string.keyboard_volume)) },
            text = {
                Column {
                    Text("${(volume * 100).toInt()}%")
                    Slider(
                        value = volume,
                        onValueChange = { viewModel.setVolume(it) },
                        colors = SliderDefaults.colors(thumbColor = BrandSecondary, activeTrackColor = BrandPrimary)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showVolumeDialog = false }) {
                    Text("OK", color = BrandSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Save Recording Dialog
    if (showSaveRecordingDialog) {
        AlertDialog(
            onDismissRequest = { showSaveRecordingDialog = false },
            title = { Text(stringResource(R.string.recordings_save_dialog_title)) },
            text = {
                Column {
                    Text("$recordingNoteCount notes recorded (${(recordingSaveDurationMs / 1000).toInt()}s)")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recordingTitleInput,
                        onValueChange = { recordingTitleInput = it },
                        label = { Text(stringResource(R.string.recordings_name_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveRecording(recordingTitleInput, recordingSaveDurationMs.toLong())
                        showSaveRecordingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text(stringResource(R.string.recordings_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveRecordingDialog = false }) {
                    Text(stringResource(R.string.recordings_discard), color = Color.Gray)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

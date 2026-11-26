package com.pianoflow.app.feature.songs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoflow.app.R
import com.pianoflow.app.core.audio.NotationType
import com.pianoflow.app.core.midi.HandType
import com.pianoflow.app.feature.keyboard.components.PianoCanvas
import com.pianoflow.app.feature.songs.components.FallingNotesCanvas
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.GoldStarColor
import com.pianoflow.app.ui.theme.LeftHandColor
import com.pianoflow.app.ui.theme.RightHandColor
import com.pianoflow.app.ui.theme.SurfaceDark
import com.pianoflow.app.ui.theme.SurfaceElevatedDark

@Composable
fun SongPlayerScreen(
    viewModel: SongPlayerViewModel,
    onBack: () -> Unit
) {
    val song by viewModel.song.collectAsState()
    val playMode by viewModel.playMode.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val selectedHand by viewModel.selectedHand.collectAsState()
    val speed by viewModel.playbackSpeed.collectAsState()
    val isLooping by viewModel.isLooping.collectAsState()
    val score by viewModel.score.collectAsState()
    val currentCombo by viewModel.currentCombo.collectAsState()
    val lastHitRating by viewModel.lastHitRating.collectAsState()
    val highlightedKeys by viewModel.highlightedKeys.collectAsState()
    val scoreSummary by viewModel.scoreSummary.collectAsState()

    var showSpeedDialog by remember { mutableStateOf(false) }

    val startMidi = 48 // C3
    val endMidi = 84   // C6 (3 full octaves visible)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Toolbar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = song.composer,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                // Mode Chips (Watch, Follow, Perform)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PlayMode.values().forEach { mode ->
                        val isSelected = playMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setMode(mode) },
                            label = {
                                Text(
                                    text = when (mode) {
                                        PlayMode.WATCH -> stringResource(R.string.song_mode_watch)
                                        PlayMode.FOLLOW -> stringResource(R.string.song_mode_follow)
                                        PlayMode.PERFORM -> stringResource(R.string.song_mode_perform)
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceElevatedDark,
                                labelColor = Color.LightGray
                            )
                        )
                    }
                }
            }
        }

        // Live HUD in Perform mode (Score & Combo)
        AnimatedVisibility(visible = playMode == PlayMode.PERFORM) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.song_score_value, score),
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandSecondary,
                    fontWeight = FontWeight.Bold
                )

                if (currentCombo > 1) {
                    Text(
                        text = stringResource(R.string.song_combo_value, currentCombo),
                        style = MaterialTheme.typography.titleMedium,
                        color = BrandPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                lastHitRating?.let { rating ->
                    val ratingLabel = when (rating) {
                        HitRating.PERFECT -> stringResource(R.string.song_hit_perfect)
                        HitRating.GOOD -> stringResource(R.string.song_hit_good)
                        HitRating.MISS -> stringResource(R.string.song_hit_miss)
                    }
                    Text(
                        text = ratingLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (rating == HitRating.PERFECT) GoldStarColor else BrandSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Falling Notes Canvas (Waterfall)
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

        // Interactive Piano Keyboard (Lower portion)
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
                onKeyDown = { viewModel.onUserKeyDown(it) },
                onKeyUp = { viewModel.onUserKeyUp(it) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom Controls Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                // Progress Slider
                val maxDuration = song.durationMs.toFloat().coerceAtLeast(1000f)
                Slider(
                    value = currentPositionMs.toFloat().coerceIn(0f, maxDuration),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..maxDuration,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandSecondary,
                        activeTrackColor = BrandPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Hand selector chips
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = selectedHand == HandType.BOTH,
                            onClick = { viewModel.setSelectedHand(HandType.BOTH) },
                            label = { Text(stringResource(R.string.song_hand_both), fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedHand == HandType.RIGHT,
                            onClick = { viewModel.setSelectedHand(HandType.RIGHT) },
                            label = { Text(stringResource(R.string.song_hand_right), fontSize = 11.sp, color = RightHandColor) }
                        )
                        FilterChip(
                            selected = selectedHand == HandType.LEFT,
                            onClick = { viewModel.setSelectedHand(HandType.LEFT) },
                            label = { Text(stringResource(R.string.song_hand_left), fontSize = 11.sp, color = LeftHandColor) }
                        )
                    }

                    // Play / Pause Button
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
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

                    // Speed & Loop
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showSpeedDialog = true }) {
                            Text("${speed}x", color = BrandSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        IconButton(onClick = { viewModel.toggleLoop() }) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = "Loop",
                                tint = if (isLooping) BrandSecondary else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    // Speed selector dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text(stringResource(R.string.song_speed)) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f).forEach { s ->
                        Button(
                            onClick = {
                                viewModel.setSpeed(s)
                                showSpeedDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (speed == s) BrandSecondary else SurfaceElevatedDark
                            )
                        ) {
                            Text("${s}x")
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = SurfaceDark
        )
    }

    // Score Summary Dialog (End of Song)
    scoreSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSummary() },
            title = {
                Text(
                    text = if (summary.stars >= 2) stringResource(R.string.song_great_job) else stringResource(R.string.song_keep_practicing),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Star Rating
                    Row(modifier = Modifier.padding(8.dp)) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $i",
                                tint = if (i <= summary.stars) GoldStarColor else Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.song_score_value, summary.score), style = MaterialTheme.typography.titleLarge, color = BrandSecondary)
                    Text(stringResource(R.string.song_accuracy_value, summary.accuracyPercent.toInt()), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.song_max_combo_value, summary.maxCombo), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.song_hits_breakdown, summary.perfectHits, summary.goodHits, summary.misses), fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.seekTo(0L)
                        viewModel.dismissSummary()
                        viewModel.play()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text(stringResource(R.string.song_try_again))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        viewModel.dismissSummary()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevatedDark)
                ) {
                    Text(stringResource(R.string.common_done))
                }
            },
            containerColor = SurfaceDark
        )
    }
}

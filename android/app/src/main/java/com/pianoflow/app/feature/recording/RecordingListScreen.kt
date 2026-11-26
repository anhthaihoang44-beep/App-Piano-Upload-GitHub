package com.pianoflow.app.feature.recording

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.pianoflow.app.R
import com.pianoflow.app.core.audio.AudioWavExporter
import com.pianoflow.app.core.audio.RecordedNoteEvent
import com.pianoflow.app.core.database.AppDatabase
import com.pianoflow.app.core.database.entity.RecordingEntity
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.SurfaceDark
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordingListScreen(
    database: AppDatabase,
    onReplayRecording: (RecordingEntity) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recordings by database.recordingDao().getAllRecordings().collectAsState(initial = emptyList())
    var exportingId by remember { mutableStateOf<Long?>(null) }

    fun shareRecordingWav(recording: RecordingEntity) {
        scope.launch {
            exportingId = recording.id
            val notesList = parseNotesFromJson(recording.notesJson)
            val wavFile = AudioWavExporter.exportToWav(context, recording.title, notesList)

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                wavFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "PianoFlow Recording: ${recording.title}")
                putExtra(Intent.EXTRA_TEXT, "Listen to my piano performance on PianoFlow! 🎹")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Piano Recording"))
            exportingId = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header
        Text(
            text = stringResource(R.string.recordings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Replay and share your piano performances",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (recordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.recordings_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(recordings) { recording ->
                    RecordingCardItem(
                        recording = recording,
                        isExporting = exportingId == recording.id,
                        onPlay = { onReplayRecording(recording) },
                        onShare = { shareRecordingWav(recording) },
                        onDelete = {
                            scope.launch {
                                database.recordingDao().deleteRecording(recording)
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun RecordingCardItem(
    recording: RecordingEntity,
    isExporting: Boolean,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    val formattedDate = remember(recording.createdAt) { dateFormat.format(Date(recording.createdAt)) }
    val durationSec = recording.durationMs / 1000
    val durationFormatted = String.format("%02d:%02d", durationSec / 60, durationSec % 60)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play Icon button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BrandPrimary.copy(alpha = 0.2f))
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = BrandSecondary)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recording.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$formattedDate  |  $durationFormatted  |  ${recording.noteCount} notes",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // Share WAV Button
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = BrandSecondary,
                    strokeWidth = 2.5.dp
                )
            } else {
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share WAV", tint = BrandSecondary)
                }
            }

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}

private fun parseNotesFromJson(json: String): List<RecordedNoteEvent> {
    val result = mutableListOf<RecordedNoteEvent>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(
                RecordedNoteEvent(
                    midiNumber = obj.getInt("midi"),
                    startMs = obj.getLong("startMs"),
                    durationMs = obj.optLong("durationMs", 400L)
                )
            )
        }
    } catch (_: Exception) {}
    return result
}

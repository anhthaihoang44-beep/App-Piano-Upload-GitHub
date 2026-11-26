package com.pianoflow.app.feature.lessons

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoflow.app.R
import com.pianoflow.app.core.audio.AudioEngine
import com.pianoflow.app.core.audio.NotationType
import com.pianoflow.app.core.audio.NotePitch
import com.pianoflow.app.core.database.AppDatabase
import com.pianoflow.app.core.database.entity.LessonProgressEntity
import com.pianoflow.app.feature.keyboard.components.PianoCanvas
import com.pianoflow.app.feature.lessons.model.LessonCurriculum
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.GoldStarColor
import com.pianoflow.app.ui.theme.SuccessGreen
import com.pianoflow.app.ui.theme.SurfaceDark
import com.pianoflow.app.ui.theme.SurfaceElevatedDark
import kotlinx.coroutines.launch

@Composable
fun LessonPracticeScreen(
    lessonId: String,
    audioEngine: AudioEngine,
    database: AppDatabase,
    onBack: () -> Unit,
    onNextLesson: ((String) -> Unit)? = null
) {
    val lesson = remember(lessonId) {
        LessonCurriculum.getById(lessonId) ?: LessonCurriculum.ALL_LESSONS.first()
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isLessonFinished by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val targetNotes = lesson.targetNotes
    val currentTargetMidi = if (currentStepIndex < targetNotes.size) targetNotes[currentStepIndex] else null
    val targetPitch = currentTargetMidi?.let { NotePitch.fromMidi(it) }

    val highlightedMap = remember(currentTargetMidi) {
        if (currentTargetMidi != null) mapOf(currentTargetMidi to BrandSecondary) else emptyMap()
    }

    val startMidi = 48 // C3
    val endMidi = 76   // E5

    fun handleKeyDown(midiNumber: Int) {
        audioEngine.playNote(midiNumber)

        if (midiNumber == currentTargetMidi) {
            val nextStep = currentStepIndex + 1
            if (nextStep >= targetNotes.size) {
                // Completed!
                currentStepIndex = nextStep
                isLessonFinished = true
                scope.launch {
                    val entity = LessonProgressEntity(
                        lessonId = lesson.id,
                        stageId = lesson.stageId,
                        isCompleted = true,
                        stars = 3,
                        bestAccuracy = 100f,
                        xpEarned = lesson.xpReward,
                        completedAt = System.currentTimeMillis()
                    )
                    database.lessonProgressDao().saveProgress(entity)
                }
            } else {
                currentStepIndex = nextStep
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Bar
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
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = stringResource(lesson.stageTitleRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandSecondary
                )
            }
        }

        // Instruction Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Instruction",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar & Step Target
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Note: ${targetPitch?.nameEn ?: "Done!"} (${targetPitch?.nameSolfege ?: ""})",
                        style = MaterialTheme.typography.titleSmall,
                        color = BrandSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Step ${currentStepIndex.coerceAtMost(targetNotes.size)} / ${targetNotes.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { currentStepIndex.toFloat() / targetNotes.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BrandSecondary,
                    trackColor = SurfaceElevatedDark
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Practice Keyboard Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            PianoCanvas(
                startMidi = startMidi,
                endMidi = endMidi,
                keyWidthDp = 52f,
                notationType = NotationType.CDE,
                highlightedKeys = highlightedMap,
                onKeyDown = { handleKeyDown(it) },
                onKeyUp = { audioEngine.stopNote(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Lesson Completed Modal
    if (isLessonFinished) {
        AlertDialog(
            onDismissRequest = { isLessonFinished = false },
            title = {
                Text(
                    text = stringResource(R.string.lessons_lesson_completed),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = SuccessGreen,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldStarColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.lessons_xp_earned, lesson.xpReward),
                        style = MaterialTheme.typography.titleMedium,
                        color = BrandSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isLessonFinished = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Continue")
                }
            },
            containerColor = SurfaceDark
        )
    }
}

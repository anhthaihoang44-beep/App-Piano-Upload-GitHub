package com.pianoflow.app.feature.lessons

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoflow.app.R
import com.pianoflow.app.core.billing.BillingRepository
import com.pianoflow.app.core.database.AppDatabase
import com.pianoflow.app.core.datastore.UserPreferencesRepository
import com.pianoflow.app.feature.lessons.model.Lesson
import com.pianoflow.app.feature.lessons.model.LessonCurriculum
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.GoldStarColor
import com.pianoflow.app.ui.theme.SuccessGreen
import com.pianoflow.app.ui.theme.SurfaceDark
import com.pianoflow.app.ui.theme.SurfaceElevatedDark

@Composable
fun LessonsScreen(
    database: AppDatabase,
    billingRepository: BillingRepository,
    preferencesRepository: UserPreferencesRepository,
    onLessonSelected: (String) -> Unit,
    onPaywallRequired: () -> Unit
) {
    val isPremium by billingRepository.isPremium.collectAsState()
    val totalXp by database.lessonProgressDao().getTotalXp().collectAsState(initial = 0)
    val progressList by database.lessonProgressDao().getAllProgress().collectAsState(initial = emptyList())
    val streakDays by preferencesRepository.streakDays.collectAsState(initial = 1)

    val progressMap = remember(progressList) { progressList.associateBy { it.lessonId } }
    val lessons = LessonCurriculum.ALL_LESSONS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with Streak & XP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.lessons_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.lessons_course_map),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Stats pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥 ${streakDays}d", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800), fontSize = 13.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("✨ ${totalXp ?: 0} XP", fontWeight = FontWeight.Bold, color = BrandSecondary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lessons List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(lessons) { index, lesson ->
                val prevLesson = if (index > 0) lessons[index - 1] else null
                val prevCompleted = prevLesson == null || (progressMap[prevLesson.id]?.isCompleted == true)
                val isCompleted = progressMap[lesson.id]?.isCompleted == true
                val isUnlocked = index == 0 || prevCompleted
                val isGatedByVip = lesson.isPremium && !isPremium

                LessonItemCard(
                    lesson = lesson,
                    index = index + 1,
                    isCompleted = isCompleted,
                    isUnlocked = isUnlocked,
                    isVipGated = isGatedByVip,
                    stars = progressMap[lesson.id]?.stars ?: 0,
                    onClick = {
                        if (isGatedByVip) {
                            onPaywallRequired()
                        } else if (isUnlocked) {
                            onLessonSelected(lesson.id)
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

@Composable
private fun LessonItemCard(
    lesson: Lesson,
    index: Int,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    isVipGated: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = isUnlocked || isVipGated, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) SurfaceDark else SurfaceDark.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index / Status Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> SuccessGreen.copy(alpha = 0.2f)
                            isVipGated -> GoldStarColor.copy(alpha = 0.2f)
                            isUnlocked -> BrandPrimary.copy(alpha = 0.2f)
                            else -> SurfaceElevatedDark
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCompleted -> Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = SuccessGreen)
                    isVipGated -> Icon(Icons.Default.WorkspacePremium, contentDescription = "VIP", tint = GoldStarColor)
                    isUnlocked -> Text("$index", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 16.sp)
                    else -> Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isUnlocked || isVipGated) Color.White else Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Stars & XP
            Column(horizontalAlignment = Alignment.End) {
                if (stars > 0) {
                    Row {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= stars) GoldStarColor else Color.DarkGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "+${lesson.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

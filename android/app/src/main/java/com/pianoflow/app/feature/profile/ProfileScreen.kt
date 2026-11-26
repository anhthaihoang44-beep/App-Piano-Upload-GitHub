package com.pianoflow.app.feature.profile

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoflow.app.R
import com.pianoflow.app.core.billing.BillingRepository
import com.pianoflow.app.core.database.AppDatabase
import com.pianoflow.app.core.datastore.UserPreferencesRepository
import com.pianoflow.app.core.notifications.DailyPracticeWorker
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.GoldStarColor
import com.pianoflow.app.ui.theme.SurfaceDark
import com.pianoflow.app.ui.theme.SurfaceElevatedDark
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    database: AppDatabase,
    billingRepository: BillingRepository,
    preferencesRepository: UserPreferencesRepository,
    onNavigatePaywall: () -> Unit,
    onNavigatePrivacy: () -> Unit,
    onNavigateTerms: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isPremium by billingRepository.isPremium.collectAsState()
    val totalXp by database.lessonProgressDao().getTotalXp().collectAsState(initial = 0)
    val completedLessonsCount by database.lessonProgressDao().getCompletedLessonsCount().collectAsState(initial = 0)
    val completedSongsCount by database.songScoreDao().getCompletedSongsCount().collectAsState(initial = 0)
    val streakDays by preferencesRepository.streakDays.collectAsState(initial = 1)
    val hapticsEnabled by preferencesRepository.hapticsEnabled.collectAsState(initial = true)
    val reminderEnabled by preferencesRepository.dailyReminderEnabled.collectAsState(initial = true)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            DailyPracticeWorker.scheduleDailyReminder(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title Header
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // VIP Membership Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigatePaywall() },
            colors = CardDefaults.cardColors(
                containerColor = if (isPremium) SurfaceElevatedDark else BrandPrimary
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = GoldStarColor,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPremium) stringResource(R.string.paywall_premium_active) else stringResource(R.string.paywall_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPremium) "All features unlocked" else "Tap to unlock unlimited access",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = stringResource(R.string.profile_xp),
                value = "${totalXp ?: 0}",
                color = BrandSecondary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.profile_streak),
                value = "$streakDays Days",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Lessons Passed",
                value = "$completedLessonsCount",
                color = BrandPrimary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.profile_songs_played),
                value = "$completedSongsCount",
                color = Color(0xFF00E676),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly Activity Chart Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Activity",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val heights = listOf(40.dp, 65.dp, 30.dp, 80.dp, 95.dp, 60.dp, 85.dp)
                    days.forEachIndexed { idx, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(heights[idx])
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (idx == 6) BrandSecondary else BrandPrimary.copy(alpha = 0.5f)
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(day, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings Section
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                // Practice Reminder Setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandSecondary)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.profile_daily_reminder), color = Color.White, fontSize = 14.sp)
                        Text(stringResource(R.string.profile_reminder_time), color = Color.Gray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { isChecked ->
                            scope.launch {
                                preferencesRepository.setDailyReminderEnabled(isChecked)
                                if (isChecked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        DailyPracticeWorker.scheduleDailyReminder(context)
                                    }
                                } else {
                                    DailyPracticeWorker.cancelReminder(context)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = BrandSecondary, checkedTrackColor = BrandPrimary)
                    )
                }

                Divider(color = SurfaceElevatedDark)

                // Haptic Feedback Setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Vibration, contentDescription = null, tint = BrandSecondary)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = stringResource(R.string.profile_haptics),
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { isChecked ->
                            scope.launch { preferencesRepository.setHapticsEnabled(isChecked) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = BrandSecondary, checkedTrackColor = BrandPrimary)
                    )
                }

                Divider(color = SurfaceElevatedDark)

                // Audio Latency info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = BrandSecondary)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.profile_sound_latency), color = Color.White, fontSize = 14.sp)
                        Text("Sub-50ms Low-Latency SoundPool Engine", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Privacy & Terms Links
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigatePrivacy() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = Color.LightGray)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(stringResource(R.string.profile_privacy), color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }

                Divider(color = SurfaceElevatedDark)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateTerms() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = Color.LightGray)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(stringResource(R.string.profile_terms), color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

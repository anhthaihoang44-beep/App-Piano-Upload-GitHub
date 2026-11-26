package com.pianoflow.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoflow.app.R
import com.pianoflow.app.core.datastore.UserPreferencesRepository
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.SurfaceDark
import com.pianoflow.app.ui.theme.SurfaceElevatedDark
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    preferencesRepository: UserPreferencesRepository,
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentStep by remember { mutableIntStateOf(0) }
    var selectedSkillLevel by remember { mutableStateOf("Beginner") }
    var selectedDailyMinutes by remember { mutableIntStateOf(15) }

    fun handleNext() {
        if (currentStep < 2) {
            currentStep++
        } else {
            scope.launch {
                preferencesRepository.setSkillLevel(selectedSkillLevel)
                preferencesRepository.setDailyGoalMinutes(selectedDailyMinutes)
                preferencesRepository.setOnboardingCompleted(true)
                onComplete()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Step Dots Indicator
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0..2) {
                Box(
                    modifier = Modifier
                        .size(if (i == currentStep) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (i == currentStep) BrandSecondary else SurfaceElevatedDark)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Step Content
        when (currentStep) {
            0 -> {
                // Step 1: Welcome & Intro
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = BrandSecondary,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.onboarding_step1_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.onboarding_step1_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
            }

            1 -> {
                // Step 2: Skill Level
                Text(
                    text = stringResource(R.string.onboarding_step2_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                val levels = listOf(
                    "Beginner" to stringResource(R.string.onboarding_level_beginner),
                    "Intermediate" to stringResource(R.string.onboarding_level_intermediate),
                    "Advanced" to stringResource(R.string.onboarding_level_advanced)
                )

                levels.forEach { (level, desc) ->
                    SelectableOptionCard(
                        title = level,
                        subtitle = desc,
                        isSelected = selectedSkillLevel == level,
                        onSelect = { selectedSkillLevel = level }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            2 -> {
                // Step 3: Daily Goal
                Text(
                    text = stringResource(R.string.onboarding_step3_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                val goals = listOf(
                    5 to stringResource(R.string.onboarding_goal_casual),
                    15 to stringResource(R.string.onboarding_goal_regular),
                    30 to stringResource(R.string.onboarding_goal_serious)
                )

                goals.forEach { (mins, desc) ->
                    SelectableOptionCard(
                        title = "$mins min / day",
                        subtitle = desc,
                        isSelected = selectedDailyMinutes == mins,
                        onSelect = { selectedDailyMinutes = mins }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next / Get Started Button
        Button(
            onClick = { handleNext() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
        ) {
            Text(
                text = if (currentStep == 2) stringResource(R.string.onboarding_get_started) else "Next",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SelectableOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, if (isSelected) BrandSecondary else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceElevatedDark else SurfaceDark
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) BrandSecondary else Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}

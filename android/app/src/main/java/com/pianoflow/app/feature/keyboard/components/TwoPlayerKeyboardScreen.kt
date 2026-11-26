package com.pianoflow.app.feature.keyboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pianoflow.app.R
import com.pianoflow.app.feature.keyboard.KeyboardViewModel
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.BrandTertiary

@Composable
fun TwoPlayerKeyboardScreen(
    viewModel: KeyboardViewModel,
    onBack: () -> Unit
) {
    val notationType by viewModel.notationType.collectAsState()
    val keyWidthDp by viewModel.keyWidthDp.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Player 1 Keyboard (Rotated 180 degrees for opposite player)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .rotate(180f)
        ) {
            PianoCanvas(
                startMidi = 48, // C3
                endMidi = 72,   // C5
                keyWidthDp = keyWidthDp * 0.95f,
                notationType = notationType,
                onKeyDown = { viewModel.onKeyDown(it) },
                onKeyUp = { viewModel.onKeyUp(it) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Center Divider Bar with Exit Button & Player Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E2235))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Player 1 (Top)",
                style = MaterialTheme.typography.labelSmall,
                color = BrandTertiary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.keyboard_mode_duet),
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Player 2 (Bottom)",
                style = MaterialTheme.typography.labelSmall,
                color = BrandSecondary
            )
        }

        // Player 2 Keyboard (Normal orientation)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            PianoCanvas(
                startMidi = 48, // C3
                endMidi = 72,   // C5
                keyWidthDp = keyWidthDp * 0.95f,
                notationType = notationType,
                onKeyDown = { viewModel.onKeyDown(it) },
                onKeyUp = { viewModel.onKeyUp(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

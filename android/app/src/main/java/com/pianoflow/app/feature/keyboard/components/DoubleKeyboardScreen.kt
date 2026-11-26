package com.pianoflow.app.feature.keyboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pianoflow.app.R
import com.pianoflow.app.feature.keyboard.KeyboardViewModel
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary

@Composable
fun DoubleKeyboardScreen(
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
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = stringResource(R.string.keyboard_mode_double),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Upper: C4–C6  |  Lower: C2–C4",
                style = MaterialTheme.typography.labelSmall,
                color = BrandSecondary
            )
        }

        // Upper Keyboard (Treble / Right Hand)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            PianoCanvas(
                startMidi = 60, // C4
                endMidi = 84,   // C6
                keyWidthDp = keyWidthDp * 0.9f,
                notationType = notationType,
                onKeyDown = { viewModel.onKeyDown(it) },
                onKeyUp = { viewModel.onKeyUp(it) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Divider(
            color = BrandPrimary,
            thickness = 3.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Lower Keyboard (Bass / Left Hand)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            PianoCanvas(
                startMidi = 36, // C2
                endMidi = 60,   // C4
                keyWidthDp = keyWidthDp * 0.9f,
                notationType = notationType,
                onKeyDown = { viewModel.onKeyDown(it) },
                onKeyUp = { viewModel.onKeyUp(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

package com.pianoflow.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pianoflow.app.R
import com.pianoflow.app.ui.theme.BackgroundDark

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = stringResource(R.string.profile_privacy),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PianoFlow Privacy Policy",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Last updated: September 2026\n\n" +
                    "Your privacy is important to us. PianoFlow does not collect, sell, or transmit any personal data or recorded melodies to external servers without your permission.\n\n" +
                    "1. Local Storage: All song scores, lesson progress, and recorded piano files are stored securely on your local device using Android Room and DataStore.\n\n" +
                    "2. Audio & Recordings: Audio recordings are processed on-device and only exported or shared when you explicitly initiate the Android ShareSheet.\n\n" +
                    "3. Subscriptions & Billing: In-app purchases are securely processed through Google Play Billing. We do not store your credit card or payment credentials.\n\n" +
                    "4. Contact: For privacy questions, contact support@pianoflow.app.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = stringResource(R.string.profile_terms),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PianoFlow Terms of Service",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Last updated: September 2026\n\n" +
                    "By downloading or using PianoFlow, you agree to the following terms:\n\n" +
                    "1. License: PianoFlow grants you a revocable, non-exclusive, non-transferable personal license to learn and play piano for personal entertainment and educational use.\n\n" +
                    "2. Songs & Intellectual Property: All musical arrangements included within the application are in the public domain or independently created. You own full rights to original songs you compose and record using PianoFlow.\n\n" +
                    "3. Subscriptions: PianoFlow Premium provides recurring weekly, monthly, and yearly options. Subscriptions automatically renew unless cancelled at least 24 hours prior to the end of the billing period via your Google Play account settings.\n\n" +
                    "4. Disclaimer: The software is provided 'as is' without warranties of any kind.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

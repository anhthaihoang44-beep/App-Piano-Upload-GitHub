package com.pianoflow.app.feature.songs

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.pianoflow.app.core.billing.BillingRepository
import com.pianoflow.app.core.database.AppDatabase
import com.pianoflow.app.core.database.entity.SongScoreEntity
import com.pianoflow.app.core.midi.BundledSongs
import com.pianoflow.app.core.midi.Song
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.GoldStarColor
import com.pianoflow.app.ui.theme.SurfaceDark
import com.pianoflow.app.ui.theme.SurfaceElevatedDark

@Composable
fun SongListScreen(
    database: AppDatabase,
    billingRepository: BillingRepository,
    onSongSelected: (String) -> Unit,
    onPaywallRequired: () -> Unit
) {
    val isPremium by billingRepository.isPremium.collectAsState()
    val scores by database.songScoreDao().getAllScores().collectAsState(initial = emptyList())
    val scoreMap = remember(scores) { scores.associateBy { it.songId } }

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Easy", "Intermediate", "Hard")

    val filteredSongs = remember(selectedFilter) {
        if (selectedFilter == "All") BundledSongs.ALL_SONGS
        else BundledSongs.ALL_SONGS.filter { it.difficulty.equals(selectedFilter, ignoreCase = true) }
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
            text = stringResource(R.string.nav_songs),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Master classic masterpieces step by step",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Difficulty Filters
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceDark,
                        labelColor = Color.LightGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Songs List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredSongs) { song ->
                SongItemCard(
                    song = song,
                    isLocked = song.isPremium && !isPremium,
                    scoreEntity = scoreMap[song.id],
                    onClick = {
                        if (song.isPremium && !isPremium) {
                            onPaywallRequired()
                        } else {
                            onSongSelected(song.id)
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
private fun SongItemCard(
    song: Song,
    isLocked: Boolean,
    scoreEntity: SongScoreEntity?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play / Lock Circle Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isLocked) SurfaceElevatedDark else BrandPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = GoldStarColor, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = BrandSecondary, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (song.isPremium) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldStarColor)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("VIP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${song.composer} • ${song.difficulty} • ${song.bpm} BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // High Score & Stars
            scoreEntity?.let { score ->
                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= score.stars) GoldStarColor else Color.DarkGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${score.accuracyPercent.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandSecondary
                    )
                }
            }
        }
    }
}

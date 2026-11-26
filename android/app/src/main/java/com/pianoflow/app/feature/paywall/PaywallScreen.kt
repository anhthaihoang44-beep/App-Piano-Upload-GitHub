package com.pianoflow.app.feature.paywall

import android.app.Activity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianoflow.app.R
import com.pianoflow.app.core.billing.BillingRepository
import com.pianoflow.app.core.billing.SubscriptionPlan
import com.pianoflow.app.core.util.LocaleHelper.findActivity
import com.pianoflow.app.ui.theme.BackgroundDark
import com.pianoflow.app.ui.theme.BrandPrimary
import com.pianoflow.app.ui.theme.BrandSecondary
import com.pianoflow.app.ui.theme.GoldStarColor
import com.pianoflow.app.ui.theme.SuccessGreen
import com.pianoflow.app.ui.theme.SurfaceDark
import com.pianoflow.app.ui.theme.SurfaceElevatedDark
import kotlinx.coroutines.launch

@Composable
fun PaywallScreen(
    billingRepository: BillingRepository,
    onDismiss: () -> Unit,
    onNavigatePrivacy: () -> Unit,
    onNavigateTerms: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPremium by billingRepository.isPremium.collectAsState()
    val availablePlans by billingRepository.availablePlans.collectAsState()

    var selectedPlanId by remember { mutableStateOf("pianoflow_monthly") }
    var isLoading by remember { mutableStateOf(false) }

    fun handleSubscribe() {
        val activity = context.findActivity() ?: return
        scope.launch {
            isLoading = true
            billingRepository.purchasePlan(activity, selectedPlanId)
            isLoading = false
            onDismiss()
        }
    }

    fun handleRestore() {
        scope.launch {
            isLoading = true
            billingRepository.restorePurchases()
            isLoading = false
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dismiss 'X' Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            // VIP Crown Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GoldStarColor.copy(alpha = 0.3f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = GoldStarColor,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Subtitle
            Text(
                text = stringResource(R.string.paywall_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Feature Highlights
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureCheckRow(text = stringResource(R.string.paywall_feature_songs))
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureCheckRow(text = stringResource(R.string.paywall_feature_lessons))
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureCheckRow(text = stringResource(R.string.paywall_feature_audio))
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureCheckRow(text = stringResource(R.string.paywall_feature_no_limits))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Plan Selection Cards
            availablePlans.forEach { plan ->
                PlanCard(
                    plan = plan,
                    isSelected = selectedPlanId == plan.id,
                    onSelect = { selectedPlanId = plan.id }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subscribe Button
            Button(
                onClick = { handleSubscribe() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = stringResource(R.string.paywall_subscribe_now),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Restore Purchases
            TextButton(onClick = { handleRestore() }) {
                Text(
                    text = stringResource(R.string.paywall_restore),
                    color = BrandSecondary,
                    fontSize = 13.sp
                )
            }

            // Footer legal links
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.profile_privacy),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onNavigatePrivacy() }
                )
                Text("  •  ", color = Color.Gray, fontSize = 12.sp)
                Text(
                    text = stringResource(R.string.profile_terms),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onNavigateTerms() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureCheckRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) BrandSecondary else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceElevatedDark else SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                val localizedTitle = when (plan.id) {
                    "pianoflow_weekly" -> stringResource(R.string.paywall_plan_weekly_title)
                    "pianoflow_monthly" -> stringResource(R.string.paywall_plan_monthly_title)
                    "pianoflow_yearly" -> stringResource(R.string.paywall_plan_yearly_title)
                    else -> plan.title
                }
                val localizedPeriod = when (plan.id) {
                    "pianoflow_weekly" -> stringResource(R.string.paywall_plan_weekly_period)
                    "pianoflow_monthly" -> stringResource(R.string.paywall_plan_monthly_period)
                    "pianoflow_yearly" -> stringResource(R.string.paywall_plan_yearly_period)
                    else -> plan.period
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = localizedTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (plan.isRecommended) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BrandPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(stringResource(R.string.paywall_popular_badge), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = localizedPeriod,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Text(
                text = plan.priceFormatted,
                style = MaterialTheme.typography.titleLarge,
                color = if (isSelected) BrandSecondary else Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

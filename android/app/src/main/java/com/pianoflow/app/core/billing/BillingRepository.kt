package com.pianoflow.app.core.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val priceFormatted: String,
    val period: String,
    val isRecommended: Boolean = false,
    val features: List<String> = emptyList()
)

interface BillingRepository {
    val isPremium: StateFlow<Boolean>
    val availablePlans: StateFlow<List<SubscriptionPlan>>

    suspend fun startConnection()
    suspend fun purchasePlan(activity: Activity, planId: String): Boolean
    suspend fun restorePurchases(): Boolean
}

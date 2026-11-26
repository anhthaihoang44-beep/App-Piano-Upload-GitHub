package com.pianoflow.app.core.billing

import android.app.Activity
import com.pianoflow.app.core.datastore.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MockBillingRepository(
    private val preferencesRepository: UserPreferencesRepository
) : BillingRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _availablePlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    override val availablePlans: StateFlow<List<SubscriptionPlan>> = _availablePlans.asStateFlow()

    init {
        scope.launch {
            preferencesRepository.isPremium.collect {
                _isPremium.value = it
            }
        }

        _availablePlans.value = listOf(
            SubscriptionPlan(
                id = "pianoflow_weekly",
                title = "Weekly VIP",
                priceFormatted = "$2.99",
                period = "per week",
                isRecommended = false,
                features = listOf("All 50+ songs unlocked", "Unlimited recording playback")
            ),
            SubscriptionPlan(
                id = "pianoflow_monthly",
                title = "Monthly VIP",
                priceFormatted = "$7.99",
                period = "per month",
                isRecommended = true,
                features = listOf("All songs & master lessons", "High-fidelity audio export", "7-Day Free Trial included")
            ),
            SubscriptionPlan(
                id = "pianoflow_yearly",
                title = "Yearly VIP",
                priceFormatted = "$39.99",
                period = "per year (Save 60%)",
                isRecommended = false,
                features = listOf("Best long-term value", "All future songs and features", "VIP priority support")
            )
        )
    }

    override suspend fun startConnection() {
        // Mock connection ready immediately
    }

    override suspend fun purchasePlan(activity: Activity, planId: String): Boolean {
        delay(600) // Simulate Play Store checkout dialog animation
        _isPremium.value = true
        preferencesRepository.setPremium(true)
        return true
    }

    override suspend fun restorePurchases(): Boolean {
        delay(500)
        _isPremium.value = true
        preferencesRepository.setPremium(true)
        return true
    }
}

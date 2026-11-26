package com.pianoflow.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.pianoflow.app.core.billing.MockBillingRepository
import com.pianoflow.app.core.datastore.UserPreferencesRepository
import com.pianoflow.app.feature.paywall.PaywallScreen
import com.pianoflow.app.ui.theme.PianoFlowTheme
import org.junit.Rule
import org.junit.Test

class PaywallUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPaywallDisplaysPlansAndRestore() {
        val app = PianoFlowApplication.instance
        val mockRepo = MockBillingRepository(app.preferencesRepository)

        composeTestRule.setContent {
            PianoFlowTheme {
                PaywallScreen(
                    billingRepository = mockRepo,
                    onDismiss = {},
                    onNavigatePrivacy = {},
                    onNavigateTerms = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        // Verify key paywall texts are displayed
        composeTestRule.onNodeWithText("Unlock PianoFlow Premium").assertIsDisplayed()
        composeTestRule.onNodeWithText("Restore Purchases").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekly VIP").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly VIP").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yearly VIP").assertIsDisplayed()
    }
}  

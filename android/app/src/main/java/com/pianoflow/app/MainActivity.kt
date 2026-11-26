package com.pianoflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.pianoflow.app.core.util.LocaleHelper
import com.pianoflow.app.ui.navigation.PianoFlowNavGraph
import com.pianoflow.app.ui.theme.PianoFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = PianoFlowApplication.instance
            val appLanguage by app.preferencesRepository.appLanguage.collectAsState(initial = LocaleHelper.LANG_EN)
            val baseContext = LocalContext.current
            val localizedContext = remember(appLanguage, baseContext) {
                LocaleHelper.wrapContext(baseContext, appLanguage)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration
            ) {
                PianoFlowTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        PianoFlowNavGraph()
                    }
                }
            }
        }
    }
}

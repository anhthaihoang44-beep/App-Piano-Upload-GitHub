package com.pianoflow.app

import android.app.Application
import com.pianoflow.app.core.audio.AudioEngine
import com.pianoflow.app.core.audio.SoundPoolAudioEngine
import com.pianoflow.app.core.billing.BillingRepository
import com.pianoflow.app.core.billing.MockBillingRepository
import com.pianoflow.app.core.database.AppDatabase
import com.pianoflow.app.core.datastore.UserPreferencesRepository
import com.pianoflow.app.core.notifications.DailyPracticeWorker
import com.pianoflow.app.core.util.HapticHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PianoFlowApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var audioEngine: AudioEngine
        private set

    lateinit var billingRepository: BillingRepository
        private set

    lateinit var hapticHelper: HapticHelper
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        preferencesRepository = UserPreferencesRepository(this)
        audioEngine = SoundPoolAudioEngine(this)
        billingRepository = MockBillingRepository(preferencesRepository)
        hapticHelper = HapticHelper(this)

        // Initialize audio engine preload asynchronously at app startup
        applicationScope.launch {
            audioEngine.initialize()
            if (preferencesRepository.dailyReminderEnabled.first()) {
                DailyPracticeWorker.scheduleDailyReminder(this@PianoFlowApplication)
            }
        }
    }

    companion object {
        lateinit var instance: PianoFlowApplication
            private set
    }
}

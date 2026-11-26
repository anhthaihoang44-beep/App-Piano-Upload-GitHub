package com.pianoflow.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pianoflow.app.MainActivity
import com.pianoflow.app.R
import com.pianoflow.app.core.datastore.UserPreferencesRepository
import com.pianoflow.app.core.util.LocaleHelper
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class DailyPracticeWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val lang = UserPreferencesRepository(context).appLanguage.first()
        showNotification(LocaleHelper.wrapContext(context, lang))
        return Result.success()
    }

    private fun showNotification(localizedContext: Context) {
        createNotificationChannel(localizedContext)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(localizedContext.getString(R.string.reminder_notification_title))
            .setContentText(localizedContext.getString(R.string.reminder_notification_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Android 13+ POST_NOTIFICATIONS permission not granted
        }
    }

    private fun createNotificationChannel(localizedContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = localizedContext.getString(R.string.notification_channel_name)
            val descriptionText = localizedContext.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "pianoflow_daily_reminder"
        const val NOTIFICATION_ID = 1001
        private const val WORK_NAME = "pianoflow_daily_practice_work"

        fun scheduleDailyReminder(context: Context) {
            val constraints = Constraints.Builder().build()
            val periodicWork = PeriodicWorkRequestBuilder<DailyPracticeWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(8, TimeUnit.HOURS) // schedule forward
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
        }

        fun cancelReminder(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

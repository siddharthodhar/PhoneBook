package com.siddhartho.phonebook.application

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import android.util.Log

class PhoneBookApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")

        val missedCallChannelName = "Missed Call Alert"
        val missedCallChannelDesc = "Get a notification for missed call"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val missedCallChannel =
                NotificationChannel(
                    getMissedCallChannelId(this),
                    missedCallChannelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
            missedCallChannel.description = missedCallChannelDesc
            missedCallChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            missedCallChannel.setSound(
                Settings.System.DEFAULT_NOTIFICATION_URI,
                Notification.AUDIO_ATTRIBUTES_DEFAULT
            )
            missedCallChannel.vibrationPattern = longArrayOf(0, 250, 250, 250)
            missedCallChannel.setShowBadge(true)

            (getSystemService(NotificationManager::class.java)).createNotificationChannel(
                missedCallChannel
            )
        }
    }

    companion object {
        private const val TAG = "PhoneBookApplication"

        fun getMissedCallChannelId(context: Context): String {
            Log.d(TAG, "getMissedCallChannelId() called with: context = $context")
            return "${context.packageName}.missed_call_channel"
        }
    }
}
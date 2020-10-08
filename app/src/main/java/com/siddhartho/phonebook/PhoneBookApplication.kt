package com.siddhartho.phonebook

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.provider.Settings
import android.util.Log
import com.siddhartho.phonebook.utils.Constants
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import javax.inject.Inject
import javax.inject.Named

class PhoneBookApplication : DaggerApplication() {

    @Inject
    @field:[Named(Constants.MISSED_CALL_CHANNEL_DESC_KEY)]
    lateinit var missedCallChannelDesc: String

    @Inject
    lateinit var missedCallChannel: NotificationChannel

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        Log.d(TAG, "applicationInjector() called")
        return DaggerPhoneBookAppComponent.builder().application(this).build()
    }

    companion object {
        private const val TAG = "PhoneBookApplication"
    }
}
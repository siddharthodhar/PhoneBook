package com.siddhartho.phonebook

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.siddhartho.phonebook.utils.Constants
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class PhoneBookAppModule {

    @Module
    companion object {
        @JvmStatic
        @Singleton
        @Provides
        @Named(Constants.MISSED_CALL_CHANNEL_NAME_KEY)
        fun provideMissedCallChannelName(): String = "Missed Call Alert"

        @JvmStatic
        @Singleton
        @Provides
        @Named(Constants.MISSED_CALL_CHANNEL_DESC_KEY)
        fun provideMissedCallChannelDesc(): String = "Get a notification for missed call"

        @JvmStatic
        @Singleton
        @Provides
        @Named(Constants.MISSED_CALL_CHANNEL_ID_KEY)
        fun provideMissedCallChannelId(application: Application) =
            "${application.packageName}.missed_call_channel"

        @RequiresApi(Build.VERSION_CODES.O)
        @JvmStatic
        @Singleton
        @Provides
        fun provideNotificationChannel(
            @Named(Constants.MISSED_CALL_CHANNEL_ID_KEY)
            missedCallChannelId: String,
            @Named(Constants.MISSED_CALL_CHANNEL_NAME_KEY)
            missedCallChannelName: String
        ) =
            NotificationChannel(
                missedCallChannelId,
                missedCallChannelName,
                NotificationManager.IMPORTANCE_HIGH
            )
    }
}

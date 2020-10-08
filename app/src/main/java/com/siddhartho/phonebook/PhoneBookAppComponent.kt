package com.siddhartho.phonebook

import android.app.Application
import com.siddhartho.phonebook.viewmodel.RoomModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AndroidSupportInjectionModule::class, PhoneBookAppModule::class, ActivityBuildersModule::class, RoomModule::class]
)
interface PhoneBookAppComponent : AndroidInjector<PhoneBookApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): PhoneBookAppComponent
    }
}
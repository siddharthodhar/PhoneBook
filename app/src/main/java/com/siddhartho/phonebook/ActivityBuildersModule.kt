package com.siddhartho.phonebook

import com.siddhartho.phonebook.activities.ActivityScope
import com.siddhartho.phonebook.activities.addcontact.AddContactActivity
import com.siddhartho.phonebook.activities.addcontact.AddContactModule
import com.siddhartho.phonebook.activities.displaycontacts.DisplayContactsActivity
import com.siddhartho.phonebook.activities.displaycontacts.DisplayContactsModule
import com.siddhartho.phonebook.services.ServiceScope
import com.siddhartho.phonebook.services.showcalllog.ShowCallLogService
import com.siddhartho.phonebook.broadcastreceiver.ContactsBroadcastReceiver
import com.siddhartho.phonebook.services.showcalllog.ShowCallLogModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [DisplayContactsModule::class])
    abstract fun contributeDisplayContactsActivity(): DisplayContactsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [AddContactModule::class])
    abstract fun contributeAddContactActivity(): AddContactActivity

    @ServiceScope
    @ContributesAndroidInjector(modules = [ShowCallLogModule::class])
    abstract fun contributeShowCallLogService(): ShowCallLogService

    @ContributesAndroidInjector
    abstract fun contributeContactsBroadcastReceiver(): ContactsBroadcastReceiver
}

package com.siddhartho.phonebook.services.showcalllog

import com.siddhartho.phonebook.services.ServiceScope
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class ShowCallLogModule {

    @Module
    companion object {

        @JvmStatic
        @ServiceScope
        @Provides
        fun provideDisposables(): CompositeDisposable = CompositeDisposable()
    }
}
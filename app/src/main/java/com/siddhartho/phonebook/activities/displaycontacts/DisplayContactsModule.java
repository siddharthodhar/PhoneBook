package com.siddhartho.phonebook.activities.displaycontacts;

import android.content.IntentFilter;

import com.siddhartho.phonebook.activities.ActivityScope;
import com.siddhartho.phonebook.dataclass.ContactWithContactNumbers;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class DisplayContactsModule {

    @ActivityScope
    @Provides
    static IntentFilter provideIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        return filter;
    }

    @Provides
    static ArrayList<ContactWithContactNumbers> provideContactList() {
        return new ArrayList<>();
    }

    @ActivityScope
    @Provides
    static CompositeDisposable provideDisposables() {
        return new CompositeDisposable();
    }
}

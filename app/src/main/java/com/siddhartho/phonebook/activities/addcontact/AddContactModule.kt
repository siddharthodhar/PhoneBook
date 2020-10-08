package com.siddhartho.phonebook.activities.addcontact

import com.siddhartho.phonebook.activities.ActivityScope
import com.siddhartho.phonebook.dataclass.Contact
import com.siddhartho.phonebook.dataclass.ContactNumber
import com.siddhartho.phonebook.dataclass.ContactWithContactNumbers
import com.siddhartho.phonebook.utils.Constants
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class AddContactModule {

    @Module
    companion object {

        @JvmStatic
        @Provides
        fun provideContact(): Contact = Contact("")

        @JvmStatic
        @Provides
        fun provideContactNumber(): ContactNumber = ContactNumber(null, Constants.DEFAULT_CC, "")

        @JvmStatic
        @Provides
        fun provideContactNumberList(contactNumber: ContactNumber): List<ContactNumber> =
            arrayListOf(contactNumber)

        @JvmStatic
        @Provides
        fun provideContactWithContactNumbers(
            contact: Contact,
            contactNumbers: List<ContactNumber>
        ): ContactWithContactNumbers = ContactWithContactNumbers(contact, contactNumbers)

        @JvmStatic
        @ActivityScope
        @Provides
        fun provideContactNumbersToDelete(): ArrayList<ContactNumber> = ArrayList()

        @JvmStatic
        @ActivityScope
        @Provides
        fun provideDisposables(): CompositeDisposable = CompositeDisposable()
    }
}
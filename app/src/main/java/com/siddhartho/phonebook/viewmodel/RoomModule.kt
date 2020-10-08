package com.siddhartho.phonebook.viewmodel

import android.app.Application
import androidx.room.Room
import com.siddhartho.phonebook.viewmodel.repository.ContactsDao
import com.siddhartho.phonebook.viewmodel.repository.ContactsDataSource
import com.siddhartho.phonebook.viewmodel.repository.ContactsRepository
import com.siddhartho.phonebook.viewmodel.repository.databasecomponent.ContactDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule {

    @Module
    companion object {

        @JvmStatic
        @Singleton
        @Provides
        fun provideContactDatabase(application: Application): ContactDatabase {
            return Room.databaseBuilder(
                application,
                ContactDatabase::class.java, ContactDatabase.DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }

        @JvmStatic
        @Singleton
        @Provides
        fun provideContactsDao(contactDatabase: ContactDatabase): ContactsDao {
            return contactDatabase.contactsDao()
        }

        @JvmStatic
        @Singleton
        @Provides
        fun provideContactDataSource(contactsDao: ContactsDao): ContactsDataSource {
            return ContactsRepository(contactsDao)
        }
    }
}
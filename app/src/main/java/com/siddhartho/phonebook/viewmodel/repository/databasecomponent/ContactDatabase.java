package com.siddhartho.phonebook.viewmodel.repository.databasecomponent;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.siddhartho.phonebook.dataclass.CallLogsCount;
import com.siddhartho.phonebook.dataclass.Contact;
import com.siddhartho.phonebook.dataclass.ContactNumber;
import com.siddhartho.phonebook.dataclass.NotificationId;
import com.siddhartho.phonebook.viewmodel.repository.ContactsDao;

@Database(entities = {Contact.class, ContactNumber.class, CallLogsCount.class, NotificationId.class}, version = ContactDatabase.VERSION)
public abstract class ContactDatabase extends RoomDatabase {

    public abstract ContactsDao contactsDao();

    static final int VERSION = 1;

    public static final String DATABASE_NAME = "contact_database.db";
}

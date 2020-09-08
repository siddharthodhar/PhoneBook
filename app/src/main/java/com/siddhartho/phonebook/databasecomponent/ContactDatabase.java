package com.siddhartho.phonebook.databasecomponent;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.siddhartho.phonebook.dataclass.CallLogsCount;
import com.siddhartho.phonebook.dataclass.Contact;
import com.siddhartho.phonebook.dataclass.ContactNumber;
import com.siddhartho.phonebook.dataclass.NotificationId;
import com.siddhartho.phonebook.repository.ContactsDao;

@Database(entities = {Contact.class, ContactNumber.class, CallLogsCount.class, NotificationId.class}, version = 1)
public abstract class ContactDatabase extends RoomDatabase {

    public abstract ContactsDao contactsDao();

    private static ContactDatabase instance;

    public static synchronized ContactDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), ContactDatabase.class, "contact_database.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}

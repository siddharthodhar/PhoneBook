package com.siddhartho.phonebook.repository;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.siddhartho.phonebook.dataclass.CallLogsCount;
import com.siddhartho.phonebook.dataclass.Contact;
import com.siddhartho.phonebook.dataclass.ContactNumber;
import com.siddhartho.phonebook.dataclass.ContactWithContactNumbers;
import com.siddhartho.phonebook.dataclass.NotificationId;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface ContactsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertContact(Contact contact);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertContactNumber(ContactNumber contactNumber);

    @Delete
    Completable deleteContact(Contact contact);

    @Delete
    Completable deleteContactNumber(ContactNumber contactNumber);

    @Transaction
    @Query("SELECT * FROM contact_table ORDER BY _name ASC")
    Flowable<List<ContactWithContactNumbers>> getAllContacts();

    @Transaction
    @Query("SELECT * FROM contact_table WHERE _contactId = (SELECT _contactOwnerId FROM contact_number_table WHERE _number = :number)")
    Maybe<ContactWithContactNumbers> getContact(String number);

    @Query("SELECT _name FROM contact_table WHERE _name LIKE :name")
    Flowable<List<String>> getContactNames(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertCallLogCount(CallLogsCount callLogsCount);

    @Query("SELECT EXISTS(SELECT * FROM call_logs_count_table)")
    Single<Boolean> hasCallLogsCount();

    @Query("SELECT * FROM call_logs_count_table WHERE _countId = (SELECT MAX(_countId) FROM call_logs_count_table)")
    Single<CallLogsCount> getCallLogsCount();

    @Insert
    Completable insertNotificationId(NotificationId notificationId);

    @Query("SELECT IFNULL(COUNT(_notificationAutoId), 0) FROM notification_id_table WHERE _number = :number")
    Single<Integer> getMissedCount(String number);

    @Query("SELECT * FROM notification_id_table WHERE _number = :number LIMIT 1")
    Maybe<NotificationId> getNotificationId(String number);

    @Query("DELETE FROM notification_id_table WHERE _number = :number")
    Completable deleteNotificationIds(String number);
}

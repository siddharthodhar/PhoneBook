package com.siddhartho.phonebook.viewmodel.repository;

import android.util.Log;

import com.siddhartho.phonebook.dataclass.CallLogsCount;
import com.siddhartho.phonebook.dataclass.Contact;
import com.siddhartho.phonebook.dataclass.ContactNumber;
import com.siddhartho.phonebook.dataclass.ContactWithContactNumbers;
import com.siddhartho.phonebook.dataclass.NotificationId;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

import io.reactivex.Completable;
import io.reactivex.Single;

public class ContactsRepository implements ContactsDataSource {
    private static final String TAG = "ContactsRepository";

    private final ContactsDao contactsDao;

    public ContactsRepository(ContactsDao contactsDao) {
        this.contactsDao = contactsDao;
    }

    @Override
    public Flowable<List<ContactWithContactNumbers>> getAllContacts() {
        Log.d(TAG, "getAllContacts() called");
        return contactsDao.getAllContacts();
    }

    @Override
    public Maybe<ContactWithContactNumbers> getContact(String countryCode, String number) {
        Log.d(TAG, "getContact() called with: countryCode = [" + countryCode + "], number = [" + number + "]");
        return contactsDao.getContact(countryCode, number);
    }

    @Override
    public Flowable<List<String>> getContactNames(String name) {
        Log.d(TAG, "getContactNames() called with: name = [" + name + "]");
        return contactsDao.getContactNames(name);
    }

    @Override
    public Single<Long> insertOrUpdateContact(Contact contact) {
        Log.d(TAG, "insertOrUpdateContact() called with: contact = [" + contact + "]");
        return contactsDao.insertContact(contact);
    }

    @Override
    public Completable insertOrUpdateContactNumber(ContactNumber contactNumber) {
        Log.d(TAG, "insertOrUpdateContactNumber() called with: contactNumbers = [" + contactNumber + "]");
        return contactsDao.insertContactNumber(contactNumber);
    }

    @Override
    public Completable deleteContact(Contact contact) {
        Log.d(TAG, "deleteContact() called with: contact = [" + contact + "]");
        return contactsDao.deleteContact(contact);
    }

    @Override
    public Completable deleteContactNumber(ContactNumber contactNumber) {
        Log.d(TAG, "deleteContactNumber() called with: contactNumbers = [" + contactNumber + "]");
        return contactsDao.deleteContactNumber(contactNumber);
    }

    @Override
    public Single<Boolean> hasCallLogsCount() {
        Log.d(TAG, "hasCallLogsCount() called");
        return contactsDao.hasCallLogsCount();
    }

    @Override
    public Single<CallLogsCount> getCallLogsCount() {
        Log.d(TAG, "getCallLogsCount() called");
        return contactsDao.getCallLogsCount();
    }

    @Override
    public Completable insertOrUpdateCallLogsCount(CallLogsCount callLogsCount) {
        Log.d(TAG, "insertOrUpdateCallLogsCount() called with: callLogsCount = [" + callLogsCount + "]");
        return contactsDao.insertCallLogCount(callLogsCount);
    }

    @Override
    public Single<Integer> getMissedCount(String number) {
        Log.d(TAG, "getMissedCount() called with: number = [" + number + "]");
        return contactsDao.getMissedCount(number);
    }

    @Override
    public Maybe<NotificationId> getNotificationId(String number) {
        Log.d(TAG, "getNotificationId() called with: number = [" + number + "]");
        return contactsDao.getNotificationId(number);
    }

    @Override
    public Completable insertNotificationId(NotificationId notificationId) {
        Log.d(TAG, "insertNotificationId() called with: notificationId = [" + notificationId + "]");
        return contactsDao.insertNotificationId(notificationId);
    }

    @Override
    public Completable deleteNotificationIds(String number) {
        Log.d(TAG, "deleteNotificationIds() called with: number = [" + number + "]");
        return contactsDao.deleteNotificationIds(number);
    }
}

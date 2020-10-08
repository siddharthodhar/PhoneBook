package com.siddhartho.phonebook.viewmodel.repository;

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

public interface ContactsDataSource {

    Flowable<List<ContactWithContactNumbers>> getAllContacts();

    Maybe<ContactWithContactNumbers> getContact(String countryCode, String number);

    Flowable<List<String>> getContactNames(String name);

    Single<Long> insertOrUpdateContact(Contact contact);

    Completable insertOrUpdateContactNumber(ContactNumber contactNumber);

    Completable deleteContact(Contact contact);

    Completable deleteContactNumber(ContactNumber contactNumber);

    Single<Boolean> hasCallLogsCount();

    Single<CallLogsCount> getCallLogsCount();

    Completable insertOrUpdateCallLogsCount(CallLogsCount callLogsCount);

    Single<Integer> getMissedCount(String number);

    Maybe<NotificationId> getNotificationId(String number);

    Completable insertNotificationId(NotificationId notificationId);

    Completable deleteNotificationIds(String number);
}

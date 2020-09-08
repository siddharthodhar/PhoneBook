package com.siddhartho.phonebook.viewmodel;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.siddhartho.phonebook.dataclass.CallLogsCount;
import com.siddhartho.phonebook.dataclass.ContactNumber;
import com.siddhartho.phonebook.dataclass.ContactWithContactNumbers;
import com.siddhartho.phonebook.dataclass.NotificationId;
import com.siddhartho.phonebook.repository.ContactsDataSource;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ContactsViewModel extends ViewModel {
    private static final String TAG = "ContactsViewModel";

    private final ContactsDataSource contactsDataSource;
    private static ContactsViewModel mInstance;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public ContactsViewModel(ContactsDataSource contactsDataSource) {
        this.contactsDataSource = contactsDataSource;
    }

    public static synchronized ContactsViewModel getInstance(ContactsDataSource contactsDataSource) {
        if (mInstance == null)
            mInstance = new ContactsViewModel(contactsDataSource);
        return mInstance;
    }

    public Flowable<List<ContactWithContactNumbers>> getAllContacts() {
        Log.d(TAG, "getAllContacts() called");
        return contactsDataSource.getAllContacts();
    }

    public Maybe<ContactWithContactNumbers> getContact(String number) {
        Log.d(TAG, "getContact() called with: number = [" + number + "]");
        return contactsDataSource.getContact(number);
    }

    public Flowable<List<String>> getContactNames(String name) {
        Log.d(TAG, "getContactNames() called with: name = [" + name + "]");
        return contactsDataSource.getContactNames(name.concat("%"));
    }

    public Completable insertOrUpdateContact(ContactWithContactNumbers contactWithContactNumbers) {
        Log.d(TAG, "insertOrUpdateContact() called with: contact = [" + contactWithContactNumbers + "]");
        return Completable.create(emitter ->
                disposables.add(contactsDataSource.insertOrUpdateContact(contactWithContactNumbers.getContact())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMapCompletable(aLong -> {
                            Log.d(TAG, "insertOrUpdateContact() flatMapCompletable called with: rowId = [" + aLong + "]");
                            if (contactWithContactNumbers.getContactNumbers() != null)
                                return Observable.fromIterable(contactWithContactNumbers.getContactNumbers())
                                        .map(contactNumber -> {
                                            Log.d(TAG, "insertOrUpdateContact: map() called with: contactNumber = [" + contactNumber + "]");
                                            contactNumber.setContactOwnerId(aLong);
                                            return contactNumber;
                                        })
                                        .flatMapCompletable(contactsDataSource::insertOrUpdateContactNumber)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread());
                            else return Completable.error(new NullPointerException());
                        })
                        .subscribe(() -> {
                            Log.d(TAG, "insertOrUpdateContact: onComplete() called");
                            emitter.onComplete();
                        }, e -> {
                            Log.e(TAG, "insertOrUpdateContact: onError: " + e.getMessage(), e);
                            emitter.onError(e);
                        })));
    }

    public Completable deleteContact(ContactWithContactNumbers contactWithContactNumbers) {
        Log.d(TAG, "deleteContact() called with: contactWithContactNumbers = [" + contactWithContactNumbers + "]");
        return Completable.create(emitter -> disposables.add(Completable.fromAction(() -> {
                    if (contactWithContactNumbers.getContactNumbers() != null)
                        disposables.add(Observable.fromIterable(contactWithContactNumbers.getContactNumbers())
                                .flatMapCompletable(this::deleteNumber)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> Log.d(TAG, "deleteContact: deleteNumber onComplete() called"),
                                        e -> {
                                            Log.e(TAG, "deleteContact: deleteNumber onError: " + e.getMessage(), e);
                                            emitter.onError(e);
                                        }));
                    else emitter.onError(new NullPointerException());
                })
                        .andThen(contactsDataSource.deleteContact(contactWithContactNumbers.getContact()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Log.d(TAG, "deleteContact() onComplete() called");
                            emitter.onComplete();
                        }, e -> {
                            Log.e(TAG, "deleteContact: onError: " + e.getMessage(), e);
                            emitter.onError(e);
                        })
        ));
    }

    public Completable deleteNumber(ContactNumber contactNumber) {
        Log.d(TAG, "deleteContact() called with: contact = [" + contactNumber + "]");
        return contactsDataSource.deleteContactNumber(contactNumber);
    }

    public Single<CallLogsCount> getCallLogsCount() {
        Log.d(TAG, "getCallLogsCount() called");
        return contactsDataSource.getCallLogsCount();
    }

    public Completable insertOrUpdateCallLogsCount(CallLogsCount callLogsCount) {
        Log.d(TAG, "insertOrUpdateCallLogsCount() called with: callLogsCount = [" + callLogsCount + "]");
        return Completable.create(emitter -> disposables.add(contactsDataSource.hasCallLogsCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapCompletable(hasCallLogsCount -> {
                    if (hasCallLogsCount) {
                        return Completable.fromAction(() -> disposables.add(contactsDataSource.getCallLogsCount()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .flatMapCompletable(oldCallLogsCount -> {
                                    Log.d(TAG, "getCallLogsCount() called with: callLogsCount = [" + callLogsCount + "]");
                                    callLogsCount.setCountId(callLogsCount.getCountId());
                                    return contactsDataSource.insertOrUpdateCallLogsCount(callLogsCount)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread());
                                })
                                .subscribe(() -> Log.d(TAG, "insertOrUpdateCallLogsCount: completed"), e -> {
                                            Log.e(TAG, "insertOrUpdateCallLogsCount: " + e.getMessage(), e);
                                            emitter.onError(e);
                                        }
                                )));
                    } else
                        return contactsDataSource.insertOrUpdateCallLogsCount(callLogsCount)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread());
                })
                .subscribe(() -> {
                    Log.d(TAG, "insertOrUpdateCallLogsCount: completed");
                    emitter.onComplete();
                }, e -> {
                    Log.e(TAG, "insertOrUpdateCallLogsCount: " + e.getMessage(), e);
                    emitter.onError(e);
                })));
    }

    public Single<Integer> getMissedCount(String number) {
        Log.d(TAG, "getMissedCount() called with: number = [" + number + "]");
        return contactsDataSource.getMissedCount(number);
    }

    public Maybe<NotificationId> getNotificationId(String number) {
        Log.d(TAG, "getNotificationId() called with: number = [" + number + "]");
        return contactsDataSource.getNotificationId(number);
    }

    public Completable insertNotificationId(NotificationId notificationId) {
        Log.d(TAG, "insertNotificationId() called with: notificationId = [" + notificationId + "]");
        return contactsDataSource.insertNotificationId(notificationId);
    }

    public Completable deleteNotificationIds(String number) {
        Log.d(TAG, "deleteNotificationIds() called with: number = [" + number + "]");
        return contactsDataSource.deleteNotificationIds(number);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared() called");
        disposables.dispose();
    }
}

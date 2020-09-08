package com.siddhartho.phonebook.databasecomponent;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.siddhartho.phonebook.repository.ContactsDataSource;
import com.siddhartho.phonebook.viewmodel.ContactsViewModel;

public class ContactsViewModelFactory implements ViewModelProvider.Factory {

    private final ContactsDataSource contactsDataSource;

    public ContactsViewModelFactory(ContactsDataSource contactsDataSource) {
        this.contactsDataSource = contactsDataSource;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ContactsViewModel.class)) {
            //noinspection unchecked
            return (T) new ContactsViewModel(contactsDataSource);
        } else throw new IllegalArgumentException("unknown view model class");
    }
}

package com.example.phonebook.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.phonebook.data.entity.Contact;
import com.example.phonebook.data.repo.PhoneBookRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final PhoneBookRepository repo;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repo = new PhoneBookRepository(application);
    }

    public LiveData<Contact> myContact(long userId) { return repo.observeMyContact(userId); }
}

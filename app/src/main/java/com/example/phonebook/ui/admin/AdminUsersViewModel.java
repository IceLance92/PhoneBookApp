package com.example.phonebook.ui.admin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.phonebook.data.entity.User;
import com.example.phonebook.data.repo.PhoneBookRepository;

import java.util.List;

public class AdminUsersViewModel extends AndroidViewModel {

    private final PhoneBookRepository repo;
    private final MutableLiveData<String> query = new MutableLiveData<>("");

    public AdminUsersViewModel(@NonNull Application application) {
        super(application);
        repo = new PhoneBookRepository(application);
    }

    public void setQuery(String q) {
        if (q == null) q = "";
        query.postValue(q.trim());
    }

    public LiveData<List<User>> users() {
        return Transformations.switchMap(query, q -> {
            if (q == null || q.isEmpty()) return repo.observeAllUsers();
            return repo.observeSearchUsers("%" + q + "%");
        });
    }
}

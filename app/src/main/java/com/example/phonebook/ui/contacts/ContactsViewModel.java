package com.example.phonebook.ui.contacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.phonebook.data.entity.Contact;
import com.example.phonebook.data.repo.PhoneBookRepository;

import java.util.List;

public class ContactsViewModel extends AndroidViewModel {

    private final PhoneBookRepository repo;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<PhoneBookRepository.SortMode> sort = new MutableLiveData<>(PhoneBookRepository.SortMode.NAME_ASC);

    public ContactsViewModel(@NonNull Application application) {
        super(application);
        repo = new PhoneBookRepository(application);
    }

    public void setQuery(String q) {
        if (q == null) q = "";
        query.postValue(q.trim());
    }

    public void setSort(PhoneBookRepository.SortMode m) {
        if (m == null) m = PhoneBookRepository.SortMode.NAME_ASC;
        sort.postValue(m);
    }

    public PhoneBookRepository.SortMode getSort() {
        PhoneBookRepository.SortMode m = sort.getValue();
        return m == null ? PhoneBookRepository.SortMode.NAME_ASC : m;
    }

    public LiveData<List<Contact>> contacts() {
        return Transformations.switchMap(sort, s ->
                Transformations.switchMap(query, q -> {
                    if (q == null || q.trim().isEmpty()) return repo.observeContacts(s);
                    return repo.observeSearch("%" + q + "%", s);
                })
        );
    }
}

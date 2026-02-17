package com.example.phonebook.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.phonebook.data.entity.Contact;

import java.util.List;

@Dao
public interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<Contact>> observeAllAsc();



    @Query("SELECT * FROM contacts ORDER BY name COLLATE NOCASE DESC")
    LiveData<List<Contact>> observeAllDesc();

    @Query("SELECT * FROM contacts WHERE ownerUserId = :userId LIMIT 1")
    Contact findByOwner(long userId);

    @Query("SELECT * FROM contacts ORDER BY createdAt DESC")
    LiveData<List<Contact>> observeRecent();

    @Query("SELECT * FROM contacts WHERE name LIKE :q OR phone LIKE :q ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<Contact>> observeSearchAsc(String q);

    @Query("SELECT * FROM contacts WHERE name LIKE :q OR phone LIKE :q ORDER BY name COLLATE NOCASE DESC")
    LiveData<List<Contact>> observeSearchDesc(String q);

    @Query("SELECT * FROM contacts WHERE name LIKE :q OR phone LIKE :q ORDER BY createdAt DESC")
    LiveData<List<Contact>> observeSearchRecent(String q);

    @Insert
    void insert(Contact c);

    // ЖЁСТКАЯ ПРОВЕРКА: обновление только владельцем
    @Query("UPDATE contacts SET name = :name, phone = :phone, email = :email, note = :note, photoUri = :photoUri " +
           "WHERE id = :contactId AND ownerUserId = :currentUserId")
    int updateIfOwner(long contactId, long currentUserId, String name, String phone, String email, String note, String photoUri);

    // Удаление только владельцем
    @Query("DELETE FROM contacts WHERE id = :contactId AND ownerUserId = :currentUserId")
    int deleteIfOwner(long contactId, long currentUserId);

    // Администратор может обновлять любой контакт (без проверки ownerUserId)
    @Query("UPDATE contacts SET name = :name, phone = :phone, email = :email, note = :note, photoUri = :photoUri " +
            "WHERE id = :contactId")
    int updateAsAdmin(long contactId, String name, String phone, String email, String note, String photoUri);

    // Администратор может удалять любой контакт
    @Query("DELETE FROM contacts WHERE id = :contactId")
    int deleteAsAdmin(long contactId);

    @Query("SELECT * FROM contacts WHERE ownerUserId = :userId LIMIT 1")
    LiveData<Contact> observeMyContact(long userId);
}

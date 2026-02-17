package com.example.phonebook.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.phonebook.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE username = :u LIMIT 1")
    User findByUsername(String u);

    @Insert
    long insert(User user);

    @Query("SELECT COUNT(*) FROM users")
    int countUsers();

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users ORDER BY username COLLATE NOCASE ASC")
    LiveData<List<User>> observeAllUsers();

    @Query("SELECT * FROM users WHERE username LIKE :q ORDER BY username COLLATE NOCASE ASC")
    LiveData<List<User>> observeSearchUsers(String q);
}

package com.example.phonebook.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "username", unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    public String username;

    @NonNull
    public String passwordHash;

    @NonNull
    public String role; // "ADMIN" или "USER"
}

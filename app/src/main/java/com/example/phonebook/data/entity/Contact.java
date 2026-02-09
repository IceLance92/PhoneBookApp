package com.example.phonebook.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts",
        indices = {@Index("name"), @Index("phone"), @Index("ownerUserId"), @Index("createdAt")})
public class Contact {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    @NonNull
    public String phone;

    public String email;
    public String note;

    // Фото (URI, например content://...)
    public String photoUri;

    // Для сортировки "недавно добавленные"
    public long createdAt;

    public long ownerUserId; // только владелец может редактировать
}

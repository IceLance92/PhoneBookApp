package com.example.phonebook.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.phonebook.data.dao.ContactDao;
import com.example.phonebook.data.dao.UserDao;
import com.example.phonebook.data.entity.Contact;
import com.example.phonebook.data.entity.User;

@Database(entities = {User.class, Contact.class}, version = 2, exportSchema = false)
public abstract class AppDb extends RoomDatabase {

    private static volatile AppDb INSTANCE;

    public abstract UserDao userDao();
    public abstract ContactDao contactDao();

    public static AppDb get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDb.class, "phonebook.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

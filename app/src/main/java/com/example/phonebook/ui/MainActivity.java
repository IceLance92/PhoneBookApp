package com.example.phonebook.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonebook.R;
import com.example.phonebook.auth.LoginActivity;
import com.example.phonebook.auth.SessionManager;
import com.example.phonebook.databinding.ActivityMainBinding;
import com.example.phonebook.ui.contacts.ContactsFragment;
import com.example.phonebook.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding b;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new ContactsFragment())
                .commit();

        b.bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_contacts) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new ContactsFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new ProfileFragment())
                        .commit();
                return true;
            }
            return false;
        });
    }
}

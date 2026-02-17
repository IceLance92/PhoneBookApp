package com.example.phonebook.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonebook.data.repo.PhoneBookRepository;
import com.example.phonebook.databinding.ActivityLoginBinding;
import com.example.phonebook.ui.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding b;
    private PhoneBookRepository repo;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        repo = new PhoneBookRepository(this);
        session = new SessionManager(this);

        b.progress.setVisibility(View.VISIBLE);

        // Инициализация БД один раз
        repo.seedIfEmpty(() -> {
            repo.ensureContactsForAllUsers();
            runOnUiThread(() -> b.progress.setVisibility(View.GONE));
        });

        b.btnLogin.setOnClickListener(v -> doLogin());
        b.btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void doLogin() {
        String u = b.etUsername.getText().toString().trim();
        String p = b.etPassword.getText().toString();

        if (u.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        b.progress.setVisibility(View.VISIBLE);
        repo.login(u, p, user -> runOnUiThread(() -> {
            b.progress.setVisibility(View.GONE);
            if (user == null) {
                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
            } else {
                session.login(user.id, user.role);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }));
    }
}

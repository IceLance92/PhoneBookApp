package com.example.phonebook.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonebook.data.repo.PhoneBookRepository;
import com.example.phonebook.databinding.ActivityRegisterBinding;
import com.example.phonebook.ui.MainActivity;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding b;
    private PhoneBookRepository repo;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        repo = new PhoneBookRepository(this);
        session = new SessionManager(this);

        b.btnCreate.setOnClickListener(v -> create());
    }

    private void create() {
        String username = b.etUsername.getText().toString().trim();
        String pass = b.etPassword.getText().toString();
        String name = b.etName.getText().toString().trim();
        String phone = b.etPhone.getText().toString().trim();
        String email = b.etEmail.getText().toString().trim();

        if (username.isEmpty() || pass.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Логин/пароль/имя/телефон обязательны", Toast.LENGTH_SHORT).show();
            return;
        }

        repo.createUserWithContact(username, pass, name, phone, email, "", "USER",
                (ok, msg) -> runOnUiThread(() -> {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    if (!ok) return;

                    repo.login(username, pass, u -> runOnUiThread(() -> {
                        if (u != null) {
                            session.login(u.id, u.role);
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }
                    }));
                }));
    }
}

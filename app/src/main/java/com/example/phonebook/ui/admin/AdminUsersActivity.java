package com.example.phonebook.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phonebook.auth.SessionManager;
import com.example.phonebook.databinding.ActivityAdminUsersBinding;

public class AdminUsersActivity extends AppCompatActivity {

    private ActivityAdminUsersBinding b;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAdminUsersBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        session = new SessionManager(this);

        if (!session.isAdmin()) {
            Toast.makeText(this, "Доступ только для администратора", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        b.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        b.toolbar.setNavigationOnClickListener(v -> finish());

        UserListAdapter adapter = new UserListAdapter(u -> {
            String[] items = new String[]{"Сменить пароль"};

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Пользователь: " + u.username)
                    .setItems(items, (d, which) -> {
                        if (which == 0) {
                            ChangePasswordDialog.forUser(u.id, u.username)
                                    .show(getSupportFragmentManager(), "chg_pwd");
                        }
                    })
                    .setNegativeButton("Отмена", (d,w) -> {})
                    .show();
        });
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        AdminUsersViewModel vm = new ViewModelProvider(this).get(AdminUsersViewModel.class);
        vm.users().observe(this, adapter::submit);

        b.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                vm.setQuery(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}

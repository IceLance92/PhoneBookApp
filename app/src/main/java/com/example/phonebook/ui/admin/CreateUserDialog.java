package com.example.phonebook.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.phonebook.data.repo.PhoneBookRepository;
import com.example.phonebook.databinding.DialogCreateUserBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CreateUserDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DialogCreateUserBinding b = DialogCreateUserBinding.inflate(getLayoutInflater());
        PhoneBookRepository repo = new PhoneBookRepository(requireContext());

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Создать пользователя (USER) + контакт")
                .setView(b.getRoot())
                .setNegativeButton("Отмена", (x, y) -> dismiss())
                .setPositiveButton("Создать", null)
                .create();

        dialog.setOnShowListener(di -> {
            ((androidx.appcompat.app.AlertDialog) dialog)
                    .getButton(Dialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String username = b.etUsername.getText().toString().trim();
                        String pass = b.etPassword.getText().toString();
                        String name = b.etName.getText().toString().trim();
                        String phone = b.etPhone.getText().toString().trim();
                        String email = b.etEmail.getText().toString().trim();
                        String note = b.etNote.getText().toString().trim();

                        if (username.isEmpty() || pass.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                            Toast.makeText(requireContext(), "Логин/пароль/имя/телефон обязательны", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        repo.createUserWithContact(username, pass, name, phone, email, note, "USER",
                                (ok, msg) -> requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                                    if (ok) dismiss();
                                }));
                    });
        });

        return dialog;
    }
}

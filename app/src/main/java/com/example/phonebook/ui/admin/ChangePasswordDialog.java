package com.example.phonebook.ui.admin;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.phonebook.databinding.DialogChangePasswordBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ChangePasswordDialog extends DialogFragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USERNAME = "username";

    public static ChangePasswordDialog forUser(long userId, String username) {
        ChangePasswordDialog d = new ChangePasswordDialog();
        Bundle b = new Bundle();
        b.putLong(ARG_USER_ID, userId);
        b.putString(ARG_USERNAME, username);
        d.setArguments(b);
        return d;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DialogChangePasswordBinding b = DialogChangePasswordBinding.inflate(LayoutInflater.from(requireContext()));

        long userId = getArguments().getLong(ARG_USER_ID);
        String username = getArguments().getString(ARG_USERNAME, "");

        AdminUsersViewModel vm = new ViewModelProvider(requireActivity()).get(AdminUsersViewModel.class);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Сменить пароль: " + username)
                .setView(b.getRoot())
                .setNegativeButton("Отмена", (d,w) -> dismiss())
                .setPositiveButton("Сохранить", null)
                .create();

        dialog.setOnShowListener(di -> {
            ((androidx.appcompat.app.AlertDialog) dialog)
                    .getButton(Dialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String p1 = b.etPassword.getText().toString();
                        String p2 = b.etPassword2.getText().toString();

                        if (p1.trim().length() < 6) {
                            Toast.makeText(requireContext(), "Пароль минимум 6 символов", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!p1.equals(p2)) {
                            Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        vm.changePassword(userId, p1, (ok, msg) ->
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                                    if (ok) dismiss();
                                })
                        );
                    });
        });

        return dialog;
    }
}

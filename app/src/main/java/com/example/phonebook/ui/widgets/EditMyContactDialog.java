package com.example.phonebook.ui.widgets;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.phonebook.PhotoStorage;
import com.example.phonebook.auth.SessionManager;
import com.example.phonebook.data.entity.Contact;
import com.example.phonebook.data.repo.PhoneBookRepository;
import com.example.phonebook.databinding.DialogEditMyContactBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class EditMyContactDialog extends DialogFragment {

    public static final String REQ_KEY = "edit_contact_req";
    public static final String RES_OK = "ok";

    private static final String ARG_IS_CREATE = "is_create";
    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_NOTE = "note";
    private static final String ARG_PHOTO = "photo";

    private Uri selectedPhoto;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        selectedPhoto = uri;
                        String savedPath = PhotoStorage.saveToAppStorage(requireContext(), uri);
                        selectedPhoto = Uri.parse(savedPath); // теперь это file://... или абсолютный путь
                        Toast.makeText(requireContext(), "Фото сохранено", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Не удалось сохранить фото", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public static EditMyContactDialog newCreate() {
        EditMyContactDialog d = new EditMyContactDialog();
        Bundle b = new Bundle();
        b.putBoolean(ARG_IS_CREATE, true);
        d.setArguments(b);
        return d;
    }

    public static EditMyContactDialog newEdit(Contact c) {
        EditMyContactDialog d = new EditMyContactDialog();
        Bundle b = new Bundle();
        b.putBoolean(ARG_IS_CREATE, false);
        b.putLong(ARG_ID, c.id);
        b.putString(ARG_NAME, c.name);
        b.putString(ARG_PHONE, c.phone);
        b.putString(ARG_EMAIL, c.email);
        b.putString(ARG_NOTE, c.note);
        b.putString(ARG_PHOTO, c.photoUri);
        d.setArguments(b);
        return d;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DialogEditMyContactBinding binding = DialogEditMyContactBinding.inflate(getLayoutInflater());
        PhoneBookRepository repo = new PhoneBookRepository(requireContext());
        SessionManager session = new SessionManager(requireContext());

        Bundle args = getArguments();
        boolean isCreate = args == null || args.getBoolean(ARG_IS_CREATE, true);

        if (!isCreate) {
            binding.etName.setText(args.getString(ARG_NAME, ""));
            binding.etPhone.setText(args.getString(ARG_PHONE, ""));
            binding.etEmail.setText(args.getString(ARG_EMAIL, ""));
            binding.etNote.setText(args.getString(ARG_NOTE, ""));
            String p = args.getString(ARG_PHOTO, null);
            if (p != null && !p.isEmpty()) selectedPhoto = Uri.parse(p);
        }

        binding.btnPickPhoto.setOnClickListener(v -> pickImage.launch("image/*"));

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isCreate ? "Заполнить" : "Редактировать")
                .setView(binding.getRoot())
                .setNegativeButton("Отмена", (d, w) -> dismiss())
                .setPositiveButton("Сохранить", null)
                .create();

        dialog.setOnShowListener(di -> {
            ((androidx.appcompat.app.AlertDialog) dialog)
                    .getButton(Dialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String name = binding.etName.getText().toString().trim();
                        String phone = binding.etPhone.getText().toString().trim();
                        String email = binding.etEmail.getText().toString().trim();
                        String note = binding.etNote.getText().toString().trim();

                        if (name.isEmpty() || phone.isEmpty()) {
                            Toast.makeText(requireContext(), "Имя и телефон обязательны", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String photoUri = selectedPhoto == null ? "" : selectedPhoto.toString();

                        if (isCreate) {
                            Contact c = new Contact();
                            c.name = name;
                            c.phone = phone;
                            c.email = email;
                            c.note = note;
                            c.photoUri = photoUri;
                            c.ownerUserId = session.userId();

                            repo.createContact(c, () -> {
                                Bundle res = new Bundle();
                                res.putBoolean(RES_OK, true);
                                getParentFragmentManager().setFragmentResult(REQ_KEY, res);
                            });
                            dismiss();
                        } else {
                            long contactId = args.getLong(ARG_ID);
                            repo.updateMyContact(contactId, session.userId(), session.isAdmin(),
                                    name, phone, email, note, photoUri, ok -> {
                                        Bundle res = new Bundle();
                                        res.putBoolean(RES_OK, ok);
                                        getParentFragmentManager().setFragmentResult(REQ_KEY, res);
                                    }
                            );
                            dismiss();
                        }
                    });
        });

        return dialog;
    }
}

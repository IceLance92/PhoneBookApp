package com.example.phonebook.ui.widgets;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.phonebook.auth.SessionManager;
import com.example.phonebook.data.entity.Contact;
import com.example.phonebook.data.repo.PhoneBookRepository;
import com.example.phonebook.databinding.SheetContactBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.InputStream;

public class ContactBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_NOTE = "note";
    private static final String ARG_OWNER = "owner";
    private static final String ARG_CAN_EDIT = "can_edit";
    private static final String ARG_PHOTO = "photo";
    private static final String ARG_CREATE_ADMIN = "create_admin";

    public static ContactBottomSheet show(Contact c, boolean canEdit) {
        ContactBottomSheet s = new ContactBottomSheet();
        Bundle b = new Bundle();
        b.putLong(ARG_ID, c.id);
        b.putString(ARG_NAME, c.name);
        b.putString(ARG_PHONE, c.phone);
        b.putString(ARG_EMAIL, c.email);
        b.putString(ARG_NOTE, c.note);
        b.putLong(ARG_OWNER, c.ownerUserId);
        b.putBoolean(ARG_CAN_EDIT, canEdit);
        b.putString(ARG_PHOTO, c.photoUri);
        s.setArguments(b);
        return s;
    }

    public static ContactBottomSheet createNewByAdmin() {
        ContactBottomSheet s = new ContactBottomSheet();
        Bundle b = new Bundle();
        b.putBoolean(ARG_CREATE_ADMIN, true);
        s.setArguments(b);
        return s;
    }

    private SheetContactBinding b;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = SheetContactBinding.inflate(inflater, container, false);
        session = new SessionManager(requireContext());

        Bundle args = getArguments();
        boolean createByAdmin = args != null && args.getBoolean(ARG_CREATE_ADMIN, false);

        if (createByAdmin) {
            b.tvTitle.setText("Новый контакт (владелец: админ)");
            b.tvName.setText("—");
            b.tvPhone.setText("—");
            b.tvEmail.setText("—");
            b.tvNote.setText("Администратор может добавлять и редактировать любые контакты. Пользователь редактирует только свою запись.");
            b.btnCall.setVisibility(View.GONE);
            b.btnSms.setVisibility(View.GONE);
            b.btnDelete.setVisibility(View.GONE);

            b.btnEdit.setVisibility(View.VISIBLE);
            b.btnEdit.setText("Создать как админ");

            b.btnEdit.setOnClickListener(v -> {
                EditMyContactDialog.newCreate().show(getParentFragmentManager(), "create_admin_contact");
                dismiss();
            });

            return b.getRoot();
        }

        long id = args.getLong(ARG_ID);
        String name = args.getString(ARG_NAME, "");
        String phone = args.getString(ARG_PHONE, "");
        String email = args.getString(ARG_EMAIL, "");
        String note = args.getString(ARG_NOTE, "");
        String photo = args.getString(ARG_PHOTO, "");
        boolean canEdit = args.getBoolean(ARG_CAN_EDIT, false);
        long owner = args.getLong(ARG_OWNER, -1);

        b.tvTitle.setText("Контакт");
        b.tvName.setText(name);
        b.tvPhone.setText(phone);
        b.tvEmail.setText((email == null || email.isEmpty()) ? "—" : email);
        b.tvNote.setText((note == null || note.isEmpty()) ? "—" : note);

        // load photo if any
        if (photo != null && !photo.isEmpty()) {
            try {
                Uri uri = Uri.parse(photo);
                try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
                    if (is != null) {
                        android.graphics.Bitmap bm = android.graphics.BitmapFactory.decodeStream(is);
                        b.imgPhoto.setImageBitmap(bm);
                    }
                }
            } catch (Exception ignore) {}
        }

        b.btnEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        b.btnDelete.setVisibility(canEdit ? View.VISIBLE : View.GONE);

        b.btnEdit.setOnClickListener(v -> {
            Contact c = new Contact();
            c.id = id;
            c.name = name;
            c.phone = phone;
            c.email = email;
            c.note = note;
            c.ownerUserId = owner;
            c.photoUri = photo;
            EditMyContactDialog.newEdit(c).show(getParentFragmentManager(), "edit_contact");
            dismiss();
        });

        b.btnDelete.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Удалить контакт?")
                .setMessage("Это действие нельзя отменить.")
                .setNegativeButton("Отмена", (d,w) -> {})
                .setPositiveButton("Удалить", (d,w) -> {
                    PhoneBookRepository repo = new PhoneBookRepository(requireContext());
                    repo.deleteMyContact(id, session.userId(), session.isAdmin(), ok ->
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(),
                                        ok ? "Удалено" : "Удаление запрещено (недостаточно прав)",
                                        Toast.LENGTH_SHORT).show();
                                dismiss();
                            })
                    );
                }).show());

        b.btnCall.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone))));
        b.btnSms.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone))));

        return b.getRoot();
    }
}

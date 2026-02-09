package com.example.phonebook.ui.importer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.phonebook.auth.SessionManager;
import com.example.phonebook.data.entity.Contact;
import com.example.phonebook.data.repo.PhoneBookRepository;
import com.example.phonebook.databinding.ActivityImportContactsBinding;

import java.util.ArrayList;
import java.util.List;

public class ImportContactsActivity extends AppCompatActivity {

    private ActivityImportContactsBinding b;
    private PhoneContactsAdapter adapter;
    private PhoneBookRepository repo;
    private SessionManager session;

    private final ActivityResultLauncher<String> requestPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) loadPhoneContacts();
                else Toast.makeText(this, "Нет доступа к контактам", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityImportContactsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        repo = new PhoneBookRepository(this);
        session = new SessionManager(this);

        b.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        b.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new PhoneContactsAdapter();
        b.recycler.setAdapter(adapter);

        b.btnImport.setOnClickListener(v -> doImport());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadPhoneContacts();
        } else {
            requestPerm.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void loadPhoneContacts() {
        List<PhoneContact> list = new ArrayList<>();

        Cursor cur = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC"
        );

        if (cur != null) {
            while (cur.moveToNext()) {
                String name = cur.getString(0);
                String phone = cur.getString(1);
                if (name == null) name = "Без имени";
                if (phone == null) continue;
                list.add(new PhoneContact(name, phone));
            }
            cur.close();
        }

        adapter.submit(list);
    }

    private void doImport() {
        List<PhoneContact> selected = adapter.selected();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Ничего не выбрано", Toast.LENGTH_SHORT).show();
            return;
        }

        for (PhoneContact pc : selected) {
            Contact c = new Contact();
            c.name = pc.name;
            c.phone = pc.phone;
            c.email = "";
            c.note = "Импортировано из телефона";
            c.photoUri = "";
            c.ownerUserId = session.userId(); // владелец — текущий пользователь
            repo.createContact(c, null);
        }

        Toast.makeText(this, "Импортировано: " + selected.size(), Toast.LENGTH_SHORT).show();
        finish();
    }
}

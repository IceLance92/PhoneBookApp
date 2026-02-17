package com.example.phonebook.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phonebook.R;
import com.example.phonebook.auth.SessionManager;
import com.example.phonebook.data.entity.Contact;
import com.example.phonebook.data.repo.PhoneBookRepository;
import com.example.phonebook.databinding.FragmentContactsBinding;
import com.example.phonebook.ui.admin.CreateUserDialog;
import com.example.phonebook.ui.admin.AdminUsersActivity;
import com.example.phonebook.ui.importer.ImportContactsActivity;
import com.example.phonebook.ui.widgets.ContactBottomSheet;

public class ContactsFragment extends Fragment {

    public ContactsFragment() { super(R.layout.fragment_contacts); }

    private FragmentContactsBinding b;
    private ContactsViewModel vm;
    private ContactsAdapter adapter;
    private SessionManager session;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new PhoneBookRepository(requireContext()).ensureContactsForAllUsers();
        b = FragmentContactsBinding.bind(view);

        session = new SessionManager(requireContext());
        vm = new ViewModelProvider(this).get(ContactsViewModel.class);

        adapter = new ContactsAdapter(this::openContact);
        b.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recycler.setAdapter(adapter);

        // Single observer
        vm.contacts().observe(getViewLifecycleOwner(), adapter::submit);

        // Alphabet index scroll
        b.indexView.setOnLetterSelected(letter -> {
            int pos = adapter.positionForLetter(letter);
            if (pos >= 0) b.recycler.scrollToPosition(pos);
        });


        // toolbar actions
        b.toolbar.getMenu().findItem(R.id.action_admin_create_user).setVisible(session.isAdmin());
        b.toolbar.getMenu().findItem(R.id.action_admin_users).setVisible(session.isAdmin());

        b.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_admin_create_user) {
                new CreateUserDialog().show(getParentFragmentManager(), "create_user");
                return true;
            } else if (id == R.id.action_admin_users) {
                startActivity(new Intent(requireContext(), AdminUsersActivity.class));
                return true;
            } else if (id == R.id.action_import) {
                startActivity(new Intent(requireContext(), ImportContactsActivity.class));
                return true;
            } else if (id == R.id.sort_az) {
                vm.setSort(PhoneBookRepository.SortMode.NAME_ASC);
                Toast.makeText(requireContext(), "Сортировка: A→Z", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.sort_za) {
                vm.setSort(PhoneBookRepository.SortMode.NAME_DESC);
                Toast.makeText(requireContext(), "Сортировка: Z→A", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.sort_recent) {
                vm.setSort(PhoneBookRepository.SortMode.RECENT);
                Toast.makeText(requireContext(), "Сортировка: недавно", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // search
        b.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                vm.setQuery(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Admin-only FAB (создание контакта владельца = админ)
        b.fabAdd.setVisibility(session.isAdmin() ? View.VISIBLE : View.GONE);
        b.fabAdd.setOnClickListener(v ->
                ContactBottomSheet.createNewByAdmin().show(getParentFragmentManager(), "create_by_admin")
        );
    }

    private void openContact(Contact c) {
        boolean canEdit = session.isAdmin() || (c.ownerUserId == session.userId());
        ContactBottomSheet.show(c, canEdit).show(getParentFragmentManager(), "contact_sheet");
    }
}

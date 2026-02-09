package com.example.phonebook.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.phonebook.R;
import com.example.phonebook.auth.LoginActivity;
import com.example.phonebook.auth.SessionManager;
import com.example.phonebook.data.entity.Contact;
import com.example.phonebook.databinding.FragmentProfileBinding;
import com.example.phonebook.ui.widgets.EditMyContactDialog;

public class ProfileFragment extends Fragment {

    public ProfileFragment() { super(R.layout.fragment_profile); }

    private FragmentProfileBinding b;
    private ProfileViewModel vm;
    private SessionManager session;
    private Contact current;

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        b = FragmentProfileBinding.bind(view);

        session = new SessionManager(requireContext());
        vm = new ViewModelProvider(this).get(ProfileViewModel.class);

        b.chipRole.setText(session.isAdmin() ? "ADMIN" : "USER");

        getParentFragmentManager().setFragmentResultListener(
                EditMyContactDialog.REQ_KEY,
                getViewLifecycleOwner(),
                (key, bundle) -> {
                    boolean ok = bundle.getBoolean(EditMyContactDialog.RES_OK, false);
                    if (!ok) Toast.makeText(requireContext(), "Редактирование запрещено: можно менять только себя", Toast.LENGTH_SHORT).show();
                }
        );

        new com.example.phonebook.data.repo.PhoneBookRepository(requireContext())
                .observeMyContact(session.userId())
                .observe(getViewLifecycleOwner(), c -> {
                    current = c;
                    if (c == null) {
                        b.tvName.setText("Нет данных");
                        b.tvPhone.setText("—");
                        b.tvEmail.setText("—");
                        b.tvNote.setText("Нажмите «Заполнить», чтобы добавить свою контактную информацию.");
                        b.btnEdit.setText("Заполнить");
                    } else {
                        b.tvName.setText(c.name);
                        b.tvPhone.setText(c.phone);
                        b.tvEmail.setText((c.email == null || c.email.isEmpty()) ? "—" : c.email);
                        b.tvNote.setText((c.note == null || c.note.isEmpty()) ? "—" : c.note);
                        b.btnEdit.setText("Редактировать");
                    }
                });

        b.btnEdit.setOnClickListener(v -> {
            if (current == null) EditMyContactDialog.newCreate().show(getParentFragmentManager(), "edit_my");
            else EditMyContactDialog.newEdit(current).show(getParentFragmentManager(), "edit_my");
        });

        b.btnLogout.setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }
}

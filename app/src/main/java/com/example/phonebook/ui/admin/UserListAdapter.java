package com.example.phonebook.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonebook.data.entity.User;
import com.example.phonebook.databinding.ItemUserBinding;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.VH> {

    public interface OnUserClick { void onClick(User u); }

    private final List<User> items = new ArrayList<>();
    private final OnUserClick onClick;

    public UserListAdapter(OnUserClick onClick) { this.onClick = onClick; }

    public void submit(List<User> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding b = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        User u = items.get(position);
        h.b.tvUsername.setText(u.username);
        h.b.tvRole.setText("Роль: " + u.role + " • id=" + u.id);
        h.b.getRoot().setOnClickListener(v -> onClick.onClick(u));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemUserBinding b;
        VH(ItemUserBinding b) { super(b.getRoot()); this.b = b; }
    }
}

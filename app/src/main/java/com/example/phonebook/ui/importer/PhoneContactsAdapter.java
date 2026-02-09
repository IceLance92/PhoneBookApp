package com.example.phonebook.ui.importer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonebook.databinding.ItemPhoneContactBinding;

import java.util.ArrayList;
import java.util.List;

public class PhoneContactsAdapter extends RecyclerView.Adapter<PhoneContactsAdapter.VH> {

    private final List<PhoneContact> items = new ArrayList<>();

    public void submit(List<PhoneContact> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public List<PhoneContact> selected() {
        List<PhoneContact> out = new ArrayList<>();
        for (PhoneContact c : items) if (c.selected) out.add(c);
        return out;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPhoneContactBinding b = ItemPhoneContactBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PhoneContact c = items.get(position);
        h.b.tvName.setText(c.name);
        h.b.tvPhone.setText(c.phone);
        h.b.check.setChecked(c.selected);

        h.b.getRoot().setOnClickListener(v -> {
            c.selected = !c.selected;
            h.b.check.setChecked(c.selected);
        });
        h.b.check.setOnClickListener(v -> c.selected = h.b.check.isChecked());
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemPhoneContactBinding b;
        VH(ItemPhoneContactBinding b) { super(b.getRoot()); this.b = b; }
    }
}

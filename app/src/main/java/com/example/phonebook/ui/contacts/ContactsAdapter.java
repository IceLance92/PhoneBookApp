package com.example.phonebook.ui.contacts;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonebook.data.entity.Contact;
import com.example.phonebook.databinding.ItemContactBinding;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.VH> {

    public interface OnContactClick { void onClick(Contact c); }

    private final List<Contact> items = new ArrayList<>();
    private final OnContactClick onClick;

    // Для алфавитного индекса: буква -> позиция
    private final Map<String, Integer> letterToPos = new HashMap<>();

    public ContactsAdapter(OnContactClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<Contact> list) {
        items.clear();
        if (list != null) items.addAll(list);
        rebuildIndex();
        notifyDataSetChanged();
    }

    private void rebuildIndex() {
        letterToPos.clear();
        for (int i = 0; i < items.size(); i++) {
            String letter = firstLetter(items.get(i).name);
            if (!letterToPos.containsKey(letter)) letterToPos.put(letter, i);
        }
    }

    public int positionForLetter(String letter) {
        Integer p = letterToPos.get(letter);
        if (p == null) return -1;
        return p;
    }

    public String[] availableLetters() {
        return letterToPos.keySet().toArray(new String[0]);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContactBinding b = ItemContactBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Contact c = items.get(position);
        h.b.tvName.setText(c.name);
        h.b.tvPhone.setText(c.phone);
        h.b.tvEmail.setText((c.email == null || c.email.isEmpty()) ? "—" : c.email);

        String letter = firstLetter(c.name);
        h.b.tvLetter.setText(letter);

        // Фото, если есть — иначе буква на аватар
        if (c.photoUri != null && !c.photoUri.isEmpty()) {
            try {
                Uri uri = Uri.parse(c.photoUri);
                ContentResolver cr = h.b.getRoot().getContext().getContentResolver();
                try (InputStream is = cr.openInputStream(uri)) {
                    if (is != null) {
                        Bitmap bm = android.graphics.BitmapFactory.decodeStream(is);
                        h.b.imgAvatar.setImageBitmap(bm);
                    } else {
                        h.b.imgAvatar.setImageBitmap(letterBitmap(letter));
                    }
                }
            } catch (Exception e) {
                h.b.imgAvatar.setImageBitmap(letterBitmap(letter));
            }
        } else {
            h.b.imgAvatar.setImageBitmap(letterBitmap(letter));
        }

        h.b.getRoot().setOnClickListener(v -> onClick.onClick(c));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemContactBinding b;
        VH(ItemContactBinding b) { super(b.getRoot()); this.b = b; }
    }

    private static String firstLetter(String name) {
        if (name == null || name.trim().isEmpty()) return "#";
        String s = name.trim().substring(0, 1).toUpperCase(Locale.getDefault());
        // Только A-Z/А-Я, иначе #
        if (!s.matches("[A-ZА-ЯЁ]")) return "#";
        return s;
    }

    private static Bitmap letterBitmap(String letter) {
        int size = 128;
        Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFFE5E7EB); // light gray (fixed)
        c.drawCircle(size/2f, size/2f, size/2f, p);

        p.setColor(0xFF111827); // near black
        p.setTextSize(56f);
        p.setTextAlign(Paint.Align.CENTER);

        Rect r = new Rect();
        p.getTextBounds(letter, 0, letter.length(), r);
        float y = size/2f - r.centerY();
        c.drawText(letter, size/2f, y, p);
        return b;
    }
}

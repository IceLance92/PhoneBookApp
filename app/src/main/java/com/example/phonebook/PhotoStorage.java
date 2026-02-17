package com.example.phonebook;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class PhotoStorage {

    // Возвращает строку пути (Uri.toString())
    public static String saveToAppStorage(Context ctx, Uri sourceUri) throws Exception {
        File dir = new File(ctx.getFilesDir(), "avatars");
        if (!dir.exists()) dir.mkdirs();

        File outFile = new File(dir, UUID.randomUUID().toString() + ".jpg");

        try (InputStream in = ctx.getContentResolver().openInputStream(sourceUri);
             FileOutputStream out = new FileOutputStream(outFile)) {

            if (in == null) throw new IllegalStateException("InputStream is null");

            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                out.write(buf, 0, r);
            }
        }

        // Можно вернуть "file://" + outFile.getAbsolutePath(), или просто абсолютный путь
        return Uri.fromFile(outFile).toString(); // file:///data/user/0/.../avatars/xxx.jpg
    }
}
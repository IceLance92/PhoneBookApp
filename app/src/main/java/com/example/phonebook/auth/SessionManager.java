package com.example.phonebook.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        sp = ctx.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public void login(long userId, String role) {
        sp.edit().putLong("userId", userId).putString("role", role).apply();
    }

    public boolean isLoggedIn() { return userId() > 0; }
    public long userId() { return sp.getLong("userId", -1); }
    public String role() { return sp.getString("role", ""); }
    public boolean isAdmin() { return "ADMIN".equals(role()); }

    public void logout() { sp.edit().clear().apply(); }
}

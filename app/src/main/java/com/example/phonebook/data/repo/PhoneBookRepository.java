package com.example.phonebook.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.phonebook.auth.PasswordUtil;
import com.example.phonebook.data.AppDb;
import com.example.phonebook.data.dao.ContactDao;
import com.example.phonebook.data.dao.UserDao;
import com.example.phonebook.data.entity.Contact;
import com.example.phonebook.data.entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhoneBookRepository {

    public enum SortMode { NAME_ASC, NAME_DESC, RECENT }

    private final AppDb db;
    private final UserDao userDao;
    private final ContactDao contactDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public PhoneBookRepository(Context ctx) {
        db = AppDb.get(ctx);
        userDao = db.userDao();
        contactDao = db.contactDao();
    }

    public void seedIfEmpty(Runnable done) {
        io.execute(() -> {
            if (userDao.countUsers() == 0) {
                User admin = new User();
                admin.username = "admin";
                admin.passwordHash = PasswordUtil.sha256("admin123");
                admin.role = "ADMIN";
                long adminId = userDao.insert(admin);

                User user = new User();
                user.username = "user";
                user.passwordHash = PasswordUtil.sha256("user123");
                user.role = "USER";
                long userId = userDao.insert(user);

                long now = System.currentTimeMillis();

                Contact c1 = new Contact();
                c1.name = "Администратор";
                c1.phone = "+423 111 11 11";
                c1.email = "admin@example.com";
                c1.note = "Админ может добавлять пользователей, редактирует только себя";
                c1.ownerUserId = adminId;
                c1.createdAt = now;
                contactDao.insert(c1);

                Contact c2 = new Contact();
                c2.name = "Пользователь";
                c2.phone = "+423 222 22 22";
                c2.email = "user@example.com";
                c2.note = "Обычный пользователь: редактирует только себя";
                c2.ownerUserId = userId;
                c2.createdAt = now + 1;
                contactDao.insert(c2);
            }
            if (done != null) done.run();
        });
    }

    public void ensureContactsForAllUsers() {
        io.execute(() -> {
            List<User> users = userDao.getAllUsers();
            for (User u : users) {
                Contact existing = contactDao.findByOwner(u.id);
                if (existing == null) {
                    Contact c = new Contact();
                    c.name = u.username;
                    c.phone = "_";
                    c.email = "";
                    c.note = "";
                    c.photoUri = "";
                    c.ownerUserId = u.id;
                    c.createdAt = System.currentTimeMillis();
                    contactDao.insert(c);
                }
            }
        });
    }

    public interface LoginCallback { void onResult(User user); }

    public void login(String username, String plainPassword, LoginCallback cb) {
        io.execute(() -> {
            User u = userDao.findByUsername(username);
            if (u == null) { cb.onResult(null); return; }
            String hash = PasswordUtil.sha256(plainPassword);
            if (!u.passwordHash.equals(hash)) { cb.onResult(null); return; }
            cb.onResult(u);
        });
    }

    public interface ChangePasswordCallback { void onResult(boolean ok, String message); }

    public void adminChangeUserPassword(long userId, String newPlainPassword, ChangePasswordCallback cb) {
        io.execute(() -> {
            try {
                String p = (newPlainPassword == null) ? "" : newPlainPassword.trim();
                if (p.length() < 6) {
                    if (cb != null) cb.onResult(false, "Пароль должен быть минимум 6 символов");
                    return;
                }
                String hash = PasswordUtil.sha256(p);
                int updated = userDao.updatePasswordHash(userId, hash);
                if (cb != null) cb.onResult(updated > 0, updated > 0 ? "Пароль изменён" : "Пользователь не найден");
            } catch (Exception e) {
                if (cb != null) cb.onResult(false, "Ошибка: " + e.getMessage());
            }
        });
    }

    public LiveData<List<Contact>> observeContacts(SortMode mode) {
        if (mode == SortMode.NAME_DESC) return contactDao.observeAllDesc();
        if (mode == SortMode.RECENT) return contactDao.observeRecent();
        return contactDao.observeAllAsc();
    }

    public LiveData<List<Contact>> observeSearch(String q, SortMode mode) {
        if (mode == SortMode.NAME_DESC) return contactDao.observeSearchDesc(q);
        if (mode == SortMode.RECENT) return contactDao.observeSearchRecent(q);
        return contactDao.observeSearchAsc(q);
    }

    public LiveData<Contact> observeMyContact(long userId) { return contactDao.observeMyContact(userId); }

    public void createContact(Contact c, Runnable done) {
        io.execute(() -> {
            if (c.createdAt == 0) c.createdAt = System.currentTimeMillis();
            contactDao.insert(c);
            if (done != null) done.run();
        });
    }

    public interface UpdateCallback { void onResult(boolean ok); }

    public void updateMyContact(long contactId, long currentUserId, boolean admin,
                                String name, String phone, String email, String note, String photoUri,
                                UpdateCallback cb) {
        io.execute(() -> {
            int updated = admin
                    ? contactDao.updateAsAdmin(contactId, name, phone, email, note, photoUri)
                    : contactDao.updateIfOwner(contactId, currentUserId, name, phone, email, note, photoUri);

            if (cb != null) cb.onResult(updated > 0);
        });
    }


    public interface DeleteCallback { void onResult(boolean ok); }

    public void deleteMyContact(long contactId, long currentUserId, boolean admin, DeleteCallback cb) {
        io.execute(() -> {
            int d = admin
                    ? contactDao.deleteAsAdmin(contactId)
                    : contactDao.deleteIfOwner(contactId, currentUserId);

            if (cb != null) cb.onResult(d > 0);
        });
    }

    public interface CreateUserCallback { void onResult(boolean ok, String message); }

    // Админ создаёт пользователя + контакт (owner = новый пользователь)
    public void createUserWithContact(String username, String plainPassword,
                                      String name, String phone, String email, String note,
                                      String role, CreateUserCallback cb) {
        io.execute(() -> {
            try {
                if (userDao.findByUsername(username) != null) {
                    if (cb != null) cb.onResult(false, "Логин уже существует");
                    return;
                }
                db.runInTransaction(() -> {
                    User u = new User();
                    u.username = username;
                    u.passwordHash = PasswordUtil.sha256(plainPassword);
                    u.role = role;
                    long userId = userDao.insert(u);

                    Contact c = new Contact();
                    c.name = name;
                    c.phone = phone;
                    c.email = email;
                    c.note = note;
                    c.ownerUserId = userId;
                    c.createdAt = System.currentTimeMillis();
                    contactDao.insert(c);
                });
                if (cb != null) cb.onResult(true, "Пользователь создан");
            } catch (Exception e) {
                if (cb != null) cb.onResult(false, "Ошибка: " + e.getMessage());
            }
        });
    }

    public LiveData<List<User>> observeAllUsers() { return userDao.observeAllUsers(); }
    public LiveData<List<User>> observeSearchUsers(String q) { return userDao.observeSearchUsers(q); }
}

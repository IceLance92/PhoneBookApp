package com.example.phonebook.ui.importer;

public class PhoneContact {
    public String name;
    public String phone;
    public boolean selected;

    public PhoneContact(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.selected = false;
    }
}

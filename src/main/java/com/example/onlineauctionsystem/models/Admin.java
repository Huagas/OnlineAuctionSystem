package com.example.onlineauctionsystem.models;

public class Admin extends User {
    private int accessLevel;

    public Admin(String username, String password, String email, String phone, String accountType, int accessLevel) {
        super(username, password, email, phone, accountType);
        this.accessLevel = accessLevel;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public void printInfo() {
        System.out.println("[ADMIN] ID: " + this.id + " | User: " + this.username + " | AccessLevel: " + this.accessLevel);
    }
}

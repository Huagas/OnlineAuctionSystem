package com.example.onlineauctionsystem.models;

public class Admin extends User {
    public Admin(String username, String password, String email, String phone) {
        super(username, password, email, phone);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public void printInfo() {
        System.out.println("[ADMIN] ID: " + this.id + " | User: " + this.username);
    }
}

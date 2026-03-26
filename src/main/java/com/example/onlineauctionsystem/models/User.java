package com.example.onlineauctionsystem.models;

public abstract class User extends Entity {
    protected String username;
    protected String password;
    protected String email;
    protected String phone;
    protected String accountType;

    public User(String username, String password, String email, String phone, String accountType) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.accountType = accountType;
    }

    public abstract String getRole();

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAccountType() { return accountType; }

    public abstract void printInfo();
}

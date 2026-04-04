package com.example.onlineauctionsystem.models;

public class Bidder extends User {
    private double walletBalance;

    public Bidder(String username, String password, String email, String phone) {
        super(username, password, email, phone);
        this.walletBalance = 0;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    @Override
    public String getRole() {
        return "Bidder";
    }

    @Override
    public void printInfo() {
        System.out.println("[BIDDER] ID: " + this.id + " | User: " + this.username + " | Balance: $" + this.walletBalance);
    }

    @Override
    public String toCSV() {
        return super.toCSV() + "," + walletBalance;
    }
}

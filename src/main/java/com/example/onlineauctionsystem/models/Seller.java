package com.example.onlineauctionsystem.models;

public class Seller extends User {
    private double reputationScore;

    public Seller(String username, String password, String email, String phone, String accountType) {
        super(username, password, email, phone, accountType);
    }

    public double getReputationScore() {
        return reputationScore;
    }
    public void setReputationScore(int reputationScore) {
        this.reputationScore = reputationScore;
    }

    @Override
    public String getRole() {
        return "Seller";
    }

    @Override
    public void printInfo() {
        System.out.println("[SELLER] ID: " + this.id + " | User: " + this.username + " | ReputationScore: *" + this.reputationScore);
    }
}

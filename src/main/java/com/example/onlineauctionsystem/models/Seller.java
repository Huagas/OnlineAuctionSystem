package com.example.onlineauctionsystem.models;

public class Seller extends User {
    private double reputationScore;

    public Seller(String username, String password, String email, String phone) {
        super(username, password, email, phone);
        reputationScore = 0;
    }

    public double getReputationScore() {
        return reputationScore;
    }
    public void setReputationScore(double reputationScore) {
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

    @Override
    public String toCSV() {
        return super.toCSV() + "," + reputationScore;
    }
}

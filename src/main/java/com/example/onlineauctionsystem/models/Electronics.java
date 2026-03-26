package com.example.onlineauctionsystem.models;

import java.time.LocalDateTime;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronics(String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String sellerId, String brand, int warrantyMonths) {
        super(name, description, startingPrice, startTime, endTime, sellerId);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    @Override
    public String getCategory() {
        return "Thiết bị điện tử";
    }

    @Override
    public void printItemDetails() {
        System.out.println("[ĐIỆN TỬ] " + name + " | Hãng: " + brand + " | Giá hiện tại: $" + currentHighestBid);
    }
}

package com.example.onlineauctionsystem.models;

import java.time.LocalDateTime;

public class Art extends Item {
    private String artist;
    private String material;

    public Art(String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String sellerId, String artist, String material) {
        super(name, description, startingPrice, startTime, endTime, sellerId);
        this.artist = artist;
        this.material = material;
    }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    @Override
    public String getCategory() {
        return "Nghệ thuật & Sưu tầm";
    }

    @Override
    public void printItemDetails() {
        System.out.println("[NGHỆ THUẬT] " + name + " | Tác giả: " + artist + " | Giá hiện tại: $" + currentHighestBid);
    }
}

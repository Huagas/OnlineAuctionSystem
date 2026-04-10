package com.example.onlineauctionsystem.models;

import java.time.LocalDateTime;

public class AutoBid {
    private String itemId;
    private String userId;
    private double maxBid;
    private LocalDateTime timestamp;

    public AutoBid(String itemId, String userId, double maxBid) {
        this.itemId = itemId;
        this.userId = userId;
        this.maxBid = maxBid;
        this.timestamp = LocalDateTime.now();
    }

    public String getItemId() { return itemId; }
    public String getUserId() { return userId; }
    public double getMaxBid() { return maxBid; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

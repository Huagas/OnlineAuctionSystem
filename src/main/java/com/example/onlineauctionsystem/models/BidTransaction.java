package com.example.onlineauctionsystem.models;

import java.time.LocalDateTime;

public class BidTransaction extends Entity {
    public enum BidType {
        MANUAL, AUTO
    }

    private String itemId;
    private String bidderId;
    private double bidAmount;
    private BidType bidType;

    public BidTransaction(String itemId, String bidderId, double bidAmount, BidType bidType) {
        super();
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidType = bidType;
    }

    public BidTransaction(String id, LocalDateTime createdAt, String itemId, String bidderId, double bidAmount, BidType bidType) {
        super(id, createdAt);
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidType = bidType;
    }

    public BidTransaction(BidTransaction tx) {
        this.id = tx.id;
        this.createdAt = tx.createdAt;
        this.itemId = tx.itemId;
        this.bidderId = tx.bidderId;
        this.bidAmount = tx.bidAmount;
        this.bidType = tx.bidType;
    }

    public String getItemId() { return itemId; }
    public String getBidderId() { return bidderId; }
    public double getBidAmount() { return bidAmount; }
    public BidType getBidType() { return bidType; }

    public String toCSV() {
        return String.format("%s,%s,%s,%s,%.0f,%s",
                getId(),
                getCreatedAt(),
                itemId,
                bidderId,
                bidAmount,
                bidType
        );
    }

    @Override
    public String toString() {
        return String.format("[%s] User %s đặt giá $%,.0f (%s)",
                getFormattedTime(), bidderId, bidAmount, bidType);
    }
}

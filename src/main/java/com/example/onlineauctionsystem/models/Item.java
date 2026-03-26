package com.example.onlineauctionsystem.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Item extends Entity {
    protected String name;
    protected String description;
    protected double startingPrice;
    protected double currentHighestBid;
    protected LocalDateTime startTime;
    protected LocalDateTime endTime;

    protected String sellerId;
    protected String currentWinnerId = "NONE";
    protected String paymentStatus = "NONE";

    protected double bidIncrement = 10;
    protected List<AutoBid> autoBids = new ArrayList<>();

    public Item(String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String sellerId) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerId = sellerId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }

    public double getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(double currentHighestBid) { this.currentHighestBid = currentHighestBid; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getSellerId() { return sellerId; }

    public String getCurrentWinnerId() { return currentWinnerId; }
    public void setCurrentWinnerId(String currentWinnerId) { this.currentWinnerId = currentWinnerId; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public abstract String getCategory();
    public abstract void printItemDetails();

    public double getBidIncrement() { return bidIncrement; }
    public void setBidIncrement(double bidIncrement) { this.bidIncrement = bidIncrement; }
    public List<AutoBid> getAutoBids() { return autoBids; }

    public String getStatus() {
        if (paymentStatus.equals("PAID")) return "Đã thanh toán";
        if (paymentStatus.equals("CANCELED")) return "Đã hủy";

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) return "Chờ bắt đầu";
        if (now.isAfter(endTime)) return "Đã kết thúc";
        return "Đang diễn ra";
    }
}

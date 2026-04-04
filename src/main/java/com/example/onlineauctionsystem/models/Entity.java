package com.example.onlineauctionsystem.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public abstract class Entity {
    protected String id;
    protected LocalDateTime createdAt;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Entity(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    public Entity(String id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedTime() {
        return createdAt.format(TIME_FORMAT);
    }

    public String getFormattedDateTime() {
        return createdAt.format(DATE_TIME_FORMAT);
    }
}

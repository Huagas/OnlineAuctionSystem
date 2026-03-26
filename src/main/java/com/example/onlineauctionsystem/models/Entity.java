package com.example.onlineauctionsystem.models;

import java.util.UUID;

public abstract class Entity {
    protected String id;

    public Entity() {
        this.id = UUID.randomUUID().toString();
    }

    public Entity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

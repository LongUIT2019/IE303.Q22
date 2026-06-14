package com.example.taxi;

public class Driver {
    private String id;
    private String name;
    private boolean online;

    public Driver() {
    }

    public Driver(String id, String name, boolean online) {
        this.id = id;
        this.name = name;
        this.online = online;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}

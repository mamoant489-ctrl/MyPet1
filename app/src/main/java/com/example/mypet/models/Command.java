package com.example.mypet.models;

public class Command {
    private String id, name, dateAdded, status;

    public Command() {}

    public Command(String id, String name, String dateAdded, String status) {
        this.id = id;
        this.name = name;
        this.dateAdded = dateAdded;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDateAdded() { return dateAdded; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

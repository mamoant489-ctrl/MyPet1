package com.example.mypet.models;

public class Reminder {

    private String id;
    private String title;
    private String description;

    private long reminderTime;

    private boolean completed;

    private String completedAt;

    public Reminder() {
    }

    public Reminder(String id,
                    String title,
                    String description,
                    long reminderTime,
                    boolean completed,
                    String completedAt) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.reminderTime = reminderTime;
        this.completed = completed;
        this.completedAt = completedAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
}
package com.example.mypet;

public class Note {
    public String id, text, reminderTime;
    public long timestamp;

    public Note() {}

    public Note(String text, String reminderTime) {
        this.id = "";
        this.text = text;
        this.reminderTime = reminderTime;
        this.timestamp = System.currentTimeMillis();
    }
}

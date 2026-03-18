package com.example.mypet;
public class Achievement {
    private String id, title, date, photoUrl;

    public Achievement() {}

    public Achievement(String title, String date, String photoUrl) {
        this.title = title;
        this.date = date;
        this.photoUrl = photoUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}

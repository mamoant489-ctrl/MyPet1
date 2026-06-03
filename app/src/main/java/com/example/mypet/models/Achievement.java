package com.example.mypet.models;

public class Achievement {

    private String id;
    private String title;
    private String imageUrl;
    private String date;

    public Achievement() {
    }

    public Achievement(String id, String title, String imageUrl, String date) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
package com.example.mypet;

public class Meal {

    private String id;
    private String type;
    private String title;
    private String comment;
    private String amount;
    private String dateTime;

    public Meal() {
    }

    public Meal(String id,
                String type,
                String title,
                String comment,
                String amount,
                String dateTime) {

        this.id = id;
        this.type = type;
        this.title = title;
        this.comment = comment;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getComment() {
        return comment;
    }

    public String getAmount() {
        return amount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setId(String id) {
        this.id = id;
    }
}
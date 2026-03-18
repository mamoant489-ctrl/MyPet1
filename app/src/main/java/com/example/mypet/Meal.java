package com.example.mypet;

public class Meal {
    private String id, type, title, subtitle, amount, dateTime;


    public Meal() {}

    public Meal(String id, String type, String title, String subtitle,
                String amount, String dateTime) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.dateTime = dateTime;
    }


    public String getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getAmount() { return amount; }
    public String getDateTime() { return dateTime; }
}

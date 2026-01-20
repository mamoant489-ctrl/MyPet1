package com.example.mypet;

public class Meal {
    private String id;
    private String type;      // "еда", "лакомство"
    private String title;     // "Утренний корм", "Печенька"
    private String subtitle;  // "Royal Canin", "Куриная"
    private String amount;    // "150г", "2шт"
    private String dateTime;  // "13.01.2026 08:30"

    public Meal() {}

    public Meal(String id, String type, String title, String subtitle, String amount, String dateTime) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    // Геттеры
    public String getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getAmount() { return amount; }
    public String getDateTime() { return dateTime; }
}


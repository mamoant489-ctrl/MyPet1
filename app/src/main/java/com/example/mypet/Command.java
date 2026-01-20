package com.example.mypet;

public class Command {
    private String id;
    private String name;
    private String learnDate;
    private String level;
    private String status;

    public Command() {
        // Пустой конструктор для Firebase/SharedPreferences
    }

    public Command(String id, String name, String learnDate, String level, String status) {
        this.id = id;
        this.name = name;
        this.learnDate = learnDate;
        this.level = level;
        this.status = status;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLearnDate() { return learnDate; }
    public void setLearnDate(String learnDate) { this.learnDate = learnDate; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}




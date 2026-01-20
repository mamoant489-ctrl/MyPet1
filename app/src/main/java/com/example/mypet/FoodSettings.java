package com.example.mypet;

public class FoodSettings {
    public String foodName, dailyNorm, mealsPerDay;
    public FoodSettings() {}
    public FoodSettings(String foodName, String dailyNorm, String mealsPerDay) {
        this.foodName = foodName; this.dailyNorm = dailyNorm; this.mealsPerDay = mealsPerDay;
    }
}
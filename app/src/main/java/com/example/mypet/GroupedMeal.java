package com.example.mypet;

public abstract class GroupedMeal {
    public static class Header extends GroupedMeal {
        public String dateHeader;
        public Header(String dateHeader) { this.dateHeader = dateHeader; }
    }

    public static class Item extends GroupedMeal {
        public Meal meal;
        public Item(Meal meal) { this.meal = meal; }
    }
}
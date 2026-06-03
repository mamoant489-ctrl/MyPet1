package com.example.mypet.interfaces;

import com.example.mypet.models.Meal;

public interface MealClickListener {
    void onEdit(Meal meal);
    void onDelete(Meal meal);
}
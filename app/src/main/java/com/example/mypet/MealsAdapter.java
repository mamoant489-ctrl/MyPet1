package com.example.mypet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MealsAdapter extends RecyclerView.Adapter<MealsAdapter.ViewHolder> {
    private List<Meal> meals = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(meals.get(position));
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public void updateMeals(List<Meal> newMeals) {
        meals.clear();
        meals.addAll(newMeals);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivType;
        TextView tvMealTitle, tvMealSubtitle, tvMealAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivType = itemView.findViewById(R.id.ivType);
            tvMealTitle = itemView.findViewById(R.id.tvMealTitle);
            tvMealSubtitle = itemView.findViewById(R.id.tvMealSubtitle);
            tvMealAmount = itemView.findViewById(R.id.tvMealAmount);
        }

        void bind(Meal meal) {
            tvMealTitle.setText(meal.getTitle());
            tvMealSubtitle.setText(meal.getSubtitle());
            tvMealAmount.setText(meal.getAmount());

            // Иконка по типу
            if ("еда".equals(meal.getType())) {
                ivType.setImageResource(android.R.drawable.ic_menu_compass);
            } else {
                ivType.setImageResource(android.R.drawable.ic_menu_info_details);
            }
        }
    }
}

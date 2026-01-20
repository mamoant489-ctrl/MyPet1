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

public class GroupedMealsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<GroupedMeal> groupedMeals = new ArrayList<>();
    private static MealClickListener listener;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public GroupedMealsAdapter(List<Meal> mealsList, FoodActivity foodActivity) {
    }

    public void setListener(MealClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return groupedMeals.get(position) instanceof GroupedMeal.Header ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_date_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_meal, parent, false);
            return new MealViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupedMeal item = groupedMeals.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((GroupedMeal.Header) item);
        } else {
            ((MealViewHolder) holder).bind(((GroupedMeal.Item) item).meal);
        }
    }

    @Override
    public int getItemCount() {
        return groupedMeals.size();
    }

    public void updateGroupedMeals(List<GroupedMeal> newGroupedMeals) {
        groupedMeals.clear();
        groupedMeals.addAll(newGroupedMeals);
        notifyDataSetChanged();
    }

    // 📅 Header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        }
        void bind(GroupedMeal.Header header) {
            tvDateHeader.setText(header.dateHeader);
        }
    }

    // 🥩 Meal с кнопками
    static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView ivType, ivDelete, ivEdit;
        TextView tvMealTitle, tvMealSubtitle, tvMealAmount;

        MealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivType = itemView.findViewById(R.id.ivType);
            tvMealTitle = itemView.findViewById(R.id.tvMealTitle);
            tvMealSubtitle = itemView.findViewById(R.id.tvMealSubtitle);
            tvMealAmount = itemView.findViewById(R.id.tvMealAmount);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivEdit = itemView.findViewById(R.id.ivEdit);
        }

        void bind(Meal meal) {
            tvMealTitle.setText(meal.getTitle());
            tvMealSubtitle.setText(meal.getSubtitle());
            tvMealAmount.setText(meal.getAmount());

            if ("еда".equals(meal.getType())) {
                ivType.setImageResource(android.R.drawable.ic_menu_compass);
            } else {
                ivType.setImageResource(android.R.drawable.ic_menu_info_details);
            }

            // ✅ КЛИКИ
            ivDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(meal);
            });
            ivEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(meal);
            });
        }
    }
}

package com.example.mypet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mypet.interfaces.MealClickListener;
import com.example.mypet.models.Meal;
import com.example.mypet.R;
import java.util.List;

public class GroupedMealsAdapter extends RecyclerView.Adapter<GroupedMealsAdapter.MealViewHolder> {

    private List<Meal> mealsList;
    private MealClickListener listener;

    public GroupedMealsAdapter(List<Meal> mealsList, MealClickListener listener) {
        this.mealsList = mealsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealsList.get(position);


        holder.tvTitle.setText(meal.getTitle());
        holder.tvSubtitle.setText(meal.getSubtitle());
        holder.tvAmount.setText(meal.getAmount());


        if ("еда".equals(meal.getType())) {
            holder.ivType.setImageResource(android.R.drawable.ic_menu_compass);
            holder.ivType.setBackgroundColor(0xFFD4A284);
        } else {
            holder.ivType.setImageResource(android.R.drawable.ic_menu_info_details);
            holder.ivType.setBackgroundColor(0xFF90EE90);
        }


        holder.ivDelete.setOnClickListener(v -> listener.onDelete(meal));
        holder.ivEdit.setOnClickListener(v -> listener.onEdit(meal));
        holder.itemView.setOnClickListener(v -> listener.onEdit(meal));
    }

    @Override
    public int getItemCount() {
        return mealsList.size();
    }


    static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView ivType, ivDelete, ivEdit;
        TextView tvTitle, tvSubtitle, tvAmount;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivType = itemView.findViewById(R.id.ivType);
            tvTitle = itemView.findViewById(R.id.tvMealTitle);
            tvSubtitle = itemView.findViewById(R.id.tvMealSubtitle);
            tvAmount = itemView.findViewById(R.id.tvMealAmount);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivEdit = itemView.findViewById(R.id.ivEdit);
        }
    }
}

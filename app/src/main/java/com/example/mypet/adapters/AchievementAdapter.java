package com.example.mypet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypet.R;
import com.example.mypet.models.Achievement;

import java.util.List;

public class AchievementAdapter
        extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {

    public interface DeleteListener {
        void onDelete(Achievement achievement);
    }

    private final List<Achievement> list;
    private final DeleteListener listener;

    public AchievementAdapter(List<Achievement> list,
                              DeleteListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_achievement,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Achievement achievement = list.get(position);

        holder.tvTitle.setText(achievement.getTitle());
        holder.tvDate.setText(achievement.getDate());

        holder.ivDelete.setOnClickListener(v ->
                listener.onDelete(achievement));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivDelete;
        TextView tvTitle;
        TextView tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivDelete = itemView.findViewById(R.id.ivDelete);

            tvTitle = itemView.findViewById(R.id.tvAchievementTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
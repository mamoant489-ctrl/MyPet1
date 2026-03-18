package com.example.mypet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {
    private List<Achievement> list;
    private OnAchievementChangedListener listener;

    public interface OnAchievementChangedListener {
        void onAchievementChanged(String achievementId);
        void onDeleteAchievement(String achievementId);
    }

    public AchievementsAdapter(List<Achievement> list, OnAchievementChangedListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement ach = list.get(position);
        holder.tvTitle.setText(ach.getTitle());
        holder.tvDate.setText(ach.getDate());

        if (ach.getPhotoUrl() != null) {
            Glide.with(holder.itemView).load(ach.getPhotoUrl()).into(holder.ivPhoto);
        }

        holder.itemView.setOnClickListener(v -> listener.onAchievementChanged(ach.getId()));
        holder.ivDelete.setOnClickListener(v -> listener.onDeleteAchievement(ach.getId()));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        ImageView ivPhoto, ivDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}

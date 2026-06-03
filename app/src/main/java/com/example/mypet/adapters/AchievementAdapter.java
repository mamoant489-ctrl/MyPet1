package com.example.mypet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mypet.R;
import com.example.mypet.models.Achievement;

import java.util.List;

public class AchievementAdapter
        extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {

    public interface DeleteListener {
        void onDelete(Achievement achievement);
    }

    private List<Achievement> list;
    private DeleteListener listener;

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
                .inflate(R.layout.item_achievement,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Achievement achievement = list.get(position);

        holder.tvTitle.setText(achievement.getTitle());
        holder.tvDate.setText(achievement.getDate());

        Glide.with(holder.itemView.getContext())
                .load(achievement.getImageUrl())
                .into(holder.ivPhoto);

        holder.ivDelete.setOnClickListener(v ->
                listener.onDelete(achievement));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPhoto, ivDelete;
        TextView tvTitle, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            ivDelete = itemView.findViewById(R.id.ivDelete);

            tvTitle = itemView.findViewById(R.id.tvAchievementTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
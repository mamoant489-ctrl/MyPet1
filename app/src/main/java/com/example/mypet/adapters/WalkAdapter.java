package com.example.mypet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mypet.models.Walk;
import com.example.mypet.R;
import java.util.List;

public class WalkAdapter extends RecyclerView.Adapter<WalkAdapter.ViewHolder> {
    private List<Walk> walks;
    private DeleteListener listener;

    public interface DeleteListener {
        void onDelete(String walkId);
    }

    public WalkAdapter(List<Walk> walks, DeleteListener listener) {
        this.walks = walks;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_walk, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Walk walk = walks.get(position);
        holder.tvDate.setText(walk.getDate());
        holder.tvTime.setText(walk.getTime());
        holder.tvDistance.setText(walk.getDistance() + " км");

        holder.ivDelete.setOnClickListener(v -> listener.onDelete(walk.getId()));
    }

    @Override
    public int getItemCount() {
        return walks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvDistance;
        ImageView ivDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}


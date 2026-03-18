package com.example.mypet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HeightAdapter extends RecyclerView.Adapter<HeightAdapter.ViewHolder> {
    private List<HeightRecord> records;
    private OnDeleteListener listener;

    public interface OnDeleteListener {
        void onDelete(String date);
    }

    public HeightAdapter(List<HeightRecord> records, OnDeleteListener listener) {
        this.records = records;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_measurement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HeightRecord record = records.get(position);


        String formattedDate = record.date.substring(8,10) + "." +
                record.date.substring(5,7) + "." +
                record.date.substring(0,4);

        holder.tvDate.setText(formattedDate);
        holder.tvValue.setText(record.height + " см");

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(record.date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvValue;
        ImageView ivDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvValue = itemView.findViewById(R.id.tvValue);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}

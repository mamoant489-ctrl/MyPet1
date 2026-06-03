package com.example.mypet.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypet.R;
import com.example.mypet.models.Measurement;

import java.util.List;

public class MeasurementAdapter extends RecyclerView.Adapter<MeasurementAdapter.ViewHolder> {

    public interface Listener {
        void onDelete(Measurement measurement);
        void onEdit(Measurement measurement);
    }

    private List<Measurement> list;
    private Listener listener;
    private String unit;

    public MeasurementAdapter(List<Measurement> list,
                              Listener listener,
                              String unit) {

        this.list = list;
        this.listener = listener;
        this.unit = unit;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_measurement,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Measurement item = list.get(position);

        holder.tvValue.setText(item.getValue() + " " + unit);
        holder.tvDate.setText(item.getDate());

        holder.ivDelete.setOnClickListener(v ->
                listener.onDelete(item));

        holder.ivEdit.setOnClickListener(v ->
                listener.onEdit(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvValue, tvDate;
        ImageView ivEdit, ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvValue = itemView.findViewById(R.id.tvValue);
            tvDate = itemView.findViewById(R.id.tvDate);

            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
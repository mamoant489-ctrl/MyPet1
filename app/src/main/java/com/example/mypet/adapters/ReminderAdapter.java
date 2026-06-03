package com.example.mypet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypet.R;
import com.example.mypet.models.Reminder;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter
        extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    public interface ReminderListener {
        void onDelete(Reminder reminder);
        void onDone(Reminder reminder);
    }

    private List<Reminder> list;
    private ReminderListener listener;

    public ReminderAdapter(List<Reminder> list,
                           ReminderListener listener) {

        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                 int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);

        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder,
                                 int position) {

        Reminder reminder = list.get(position);

        holder.tvTitle.setText(reminder.getTitle());

        holder.tvDesc.setText(reminder.getDescription());

        SimpleDateFormat sdf =
                new SimpleDateFormat("dd.MM.yyyy HH:mm",
                        Locale.getDefault());

        holder.tvTime.setText(
                sdf.format(reminder.getReminderTime()));

        if (reminder.isCompleted()) {

            holder.tvCompleted.setText(
                    "Выполнено: " + reminder.getCompletedAt());

        } else {

            holder.tvCompleted.setText("");
        }

        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(reminder));

        holder.btnDone.setOnClickListener(v ->
                listener.onDone(reminder));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ReminderViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDesc, tvTime, tvCompleted;

        Button btnDone, btnDelete;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvDesc = itemView.findViewById(R.id.tvReminderDesc);
            tvTime = itemView.findViewById(R.id.tvReminderTime);
            tvCompleted = itemView.findViewById(R.id.tvCompleted);

            btnDone = itemView.findViewById(R.id.btnDone);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
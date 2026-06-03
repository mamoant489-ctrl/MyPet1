package com.example.mypet.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mypet.interfaces.CommandClickListener;
import com.example.mypet.models.Command;
import com.example.mypet.R;
import java.util.ArrayList;
import java.util.List;

public class CommandsAdapter extends RecyclerView.Adapter<CommandsAdapter.ViewHolder> {
    private List<Command> commands = new ArrayList<>();
    private CommandClickListener listener;

    public CommandsAdapter(CommandClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_command, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(commands.get(position));
    }

    @Override
    public int getItemCount() {
        return commands.size();
    }

    public void updateList(List<Command> newList) {
        commands.clear();
        commands.addAll(newList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvStatus;
        ImageView ivEdit, ivDelete, ivIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }

        void bind(Command command) {
            // ✅ ПОЛНОЕ НАЗВАНИЕ - БЕЗ maxLines!
            tvName.setText(command.getName() != null ? command.getName() : "Без названия");

            // ✅ ДАТА ВСЕГДА ВИДНА
            tvDate.setText("Добавлена: " + (command.getDateAdded() != null ? command.getDateAdded() : "--"));

            // ✅ СТАТУС
            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setCornerRadius(12f);

            String status = command.getStatus() != null ? command.getStatus().toLowerCase() : "новая";
            switch (status) {
                case "выучена":
                    statusBg.setColor(Color.parseColor("#4CAF50"));
                    tvStatus.setText("✅ Выучена");
                    break;
                case "в процессе":
                    statusBg.setColor(Color.parseColor("#FF9800"));
                    tvStatus.setText("⏳ В процессе");
                    break;
                default:
                    statusBg.setColor(Color.parseColor("#B0BEC5"));
                    tvStatus.setText("➕ Новая");
                    break;
            }
            tvStatus.setBackground(statusBg);
            tvStatus.setTextColor(Color.WHITE);

            ivEdit.setOnClickListener(v -> listener.onCommandEdit(command));
            ivDelete.setOnClickListener(v -> listener.onCommandDelete(command));
        }

    }
}

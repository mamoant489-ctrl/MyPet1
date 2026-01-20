package com.example.mypet;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CommandsAdapter extends RecyclerView.Adapter<CommandsAdapter.CommandViewHolder> {
    private List<Command> commands = new ArrayList<>();

    @NonNull
    @Override
    public CommandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_command, parent, false);
        return new CommandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommandViewHolder holder, int position) {
        holder.bind(commands.get(position));
    }

    @Override
    public int getItemCount() {
        return commands.size();
    }

    public void updateCommands(List<Command> newCommands) {
        commands.clear();
        commands.addAll(newCommands);
        notifyDataSetChanged();
    }

    public static class CommandViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView commandName, learnDate, level, statusIndicator;

        public CommandViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            commandName = itemView.findViewById(R.id.commandName);
            learnDate = itemView.findViewById(R.id.learnDate);
            level = itemView.findViewById(R.id.level);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }

        public void bind(Command command) {
            commandName.setText(command.getName());
            learnDate.setText("Дата: " + (command.getLearnDate() != null ? command.getLearnDate() : "--"));
            level.setText("Статус: " + (command.getStatus() != null ? command.getStatus() : "не установлен"));

            // Цвет статуса
            switch (command.getStatus() != null ? command.getStatus().toLowerCase() : "") {
                case "освоено":
                    statusIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
                    statusIndicator.setTextColor(Color.WHITE);
                    statusIndicator.setText("✅ Освоено");
                    break;
                case "в работе":
                    statusIndicator.setBackgroundColor(Color.parseColor("#FF9800"));
                    statusIndicator.setTextColor(Color.WHITE);
                    statusIndicator.setText("⏳ В работе");
                    break;
                default:
                    statusIndicator.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    statusIndicator.setTextColor(Color.GRAY);
                    statusIndicator.setText("➕ Новая");
                    break;
            }
        }
    }
}

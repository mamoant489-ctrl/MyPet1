package com.example.mypet;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CommandsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommandsAdapter adapter;
    private List<Command> commandsList;
    private TextView tvTotal, tvMastered, tvLearning;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commands);

        initViews();
        setupRecyclerView();
        setupFab();
        loadCommands();
    }
    public void GoToMenu(View view) {
        Intent intent = new Intent(this, BasicMenu.class);
        startActivity(intent);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvTotal = findViewById(R.id.tvTotal);
        tvMastered = findViewById(R.id.tvMastered);
        tvLearning = findViewById(R.id.tvLearning);
        fabAdd = findViewById(R.id.fabAdd);
        commandsList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new CommandsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> showAddCommandDialog());
    }

    private void showAddCommandDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_command, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etCommandName);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etLevel = dialogView.findViewById(R.id.etLevel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);


        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        etDate.setText(sdf.format(new Date()));

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Введите название команды", Toast.LENGTH_SHORT).show();
                return;
            }

            Command command = new Command(
                    UUID.randomUUID().toString(),
                    name,
                    etDate.getText().toString(),
                    etLevel.getText().toString().trim().isEmpty() ? "Начальный" : etLevel.getText().toString(),
                    "Новая"
            );

            commandsList.add(0, command);
            adapter.updateCommands(commandsList);
            updateStats();
            dialog.dismiss();
            Toast.makeText(this, "Команда добавлена!", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void loadCommands() {
        adapter.updateCommands(commandsList);
        updateStats();
    }

    private void updateStats() {
        int total = commandsList.size();
        long masteredCount = commandsList.stream()
                .filter(c -> "освоено".equalsIgnoreCase(c.getStatus()))
                .count();
        int learningCount = total - (int) masteredCount;

        tvTotal.setText(String.valueOf(total));
        tvMastered.setText(String.valueOf(masteredCount));
        tvLearning.setText(String.valueOf(learningCount));
    }
}

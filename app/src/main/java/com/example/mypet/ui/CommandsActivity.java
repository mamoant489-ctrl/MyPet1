package com.example.mypet.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mypet.adapters.CommandsAdapter;
import com.example.mypet.data.FirebasePaths;
import com.example.mypet.interfaces.CommandClickListener;
import com.example.mypet.models.Command;
import com.example.mypet.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommandsActivity extends AppCompatActivity implements CommandClickListener {

    private RecyclerView recyclerView;
    private CommandsAdapter adapter;
    private List<Command> commandsList = new ArrayList<>();
    private TextView tvTotal, tvMastered, tvLearning, tvEmpty;
    private FloatingActionButton fabAdd;

    private FirebaseUser currentUser;
    private String petId;
    private DatabaseReference commandsRef;
    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commands);

        initViews();
        setupFirebase();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvTotal = findViewById(R.id.tvTotal);
        tvMastered = findViewById(R.id.tvMastered);
        tvLearning = findViewById(R.id.tvLearning);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupFirebase() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Авторизуйтесь", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseDatabase.getInstance().getReference(FirebasePaths.USERS)
                .child("currentPetId")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        petId = snapshot.getValue(String.class);
                        commandsRef = FirebaseDatabase.getInstance()
                                        .getReference(FirebasePaths.USERS)
                                        .child(currentUser.getUid())
                                        .child("pets")
                                        .child(petId)
                                        .child("commands");
                        loadCommands();
                    } else {
                        showEmptyState(true);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки питомца", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Команды");
        }
    }

    private void setupRecyclerView() {
        adapter = new CommandsAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> showAddCommandDialog());
    }

    private void showAddCommandDialog() {
        if (commandsRef == null) {
            Toast.makeText(this, "Загружаем данные...", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_command, null);
        final EditText etCommandName = dialogView.findViewById(R.id.etCommandName);
        final MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        final String today = sdf.format(new Date());


        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();

        btnSave.setOnClickListener(v -> {
            String name = etCommandName.getText().toString().trim();
            if (name.isEmpty()) {
                etCommandName.requestFocus();
                etCommandName.setError("Название обязательно!");
                return;
            }

            String commandId = commandsRef.push().getKey();
            Command newCommand = new Command(commandId, name, today, "Новая");

            commandsRef.child(commandId).setValue(newCommand)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Команда добавлена", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }




    private void loadCommands() {
        if (commandsRef == null) return;

        if (listener != null) {
            commandsRef.removeEventListener(listener);
        }

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commandsList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Command command = child.getValue(Command.class);
                    if (command != null) {
                        command.setId(child.getKey());
                        commandsList.add(command);
                    }
                }
                adapter.updateList(commandsList);
                updateStats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommandsActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            }
        };
        commandsRef.addValueEventListener(listener);
    }

    private void updateStats() {
        int total = commandsList.size();
        int mastered = 0;

        for (Command cmd : commandsList) {
            if ("выучена".equalsIgnoreCase(cmd.getStatus())) {
                mastered++;
            }
        }
        int learning = total - mastered;

        tvTotal.setText(String.valueOf(total));
        tvMastered.setText(String.valueOf(mastered));
        tvLearning.setText(String.valueOf(learning));
        showEmptyState(total == 0);
    }


    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        showEmptyState(true);
    }

    @Override
    public void onCommandEdit(Command command) {
        String[] statuses = {"Новая", "В процессе", "Выучена"};

        new AlertDialog.Builder(this)
                .setTitle("Статус: " + command.getName())
                .setItems(statuses, (dialog, which) -> {
                    command.setStatus(statuses[which]);
                    if (commandsRef != null) {
                        commandsRef.child(command.getId()).setValue(command);
                    }
                })
                .show();
    }

    @Override
    public void onCommandDelete(Command command) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить?")
                .setMessage(command.getName())
                .setPositiveButton("Удалить", (dialog, which) -> {
                    if (commandsRef != null) {
                        commandsRef.child(command.getId()).removeValue();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null && commandsRef != null) {
            commandsRef.removeEventListener(listener);
        }
    }
}

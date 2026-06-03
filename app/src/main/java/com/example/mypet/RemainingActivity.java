package com.example.mypet;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RemainingActivity extends AppCompatActivity implements NotesAdapter.OnNoteChangedListener {
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<Note> notesList = new ArrayList<>();
    private FirebaseDatabase database;
    private ValueEventListener notesListener;
    private String currentUserId;
    private String petId;
    private static final String CHANNEL_ID = "notes_reminder_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        petId = getIntent().getStringExtra("petId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initRecyclerView();
        initFab();
        createNotificationChannel();
        database = FirebaseDatabase.getInstance();
        loadNotes();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(this, notesList, petId, this);
        recyclerView.setAdapter(adapter);
    }

    private void initFab() {
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddNoteDialog());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Напоминания о заметках";
            String description = "Уведомления о собачьих заметках";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void loadNotes() {
        DatabaseReference notesRef = database.getReference("users")
                .child(currentUserId).child("pets").child(petId).child("remaining").child("notes");
        notesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notesList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Note note = child.getValue(Note.class);
                    if (note != null) {
                        note.id = child.getKey();
                        notesList.add(note);
                    }
                }
                adapter.updateNotes(notesList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RemainingActivity.this, "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        notesRef.addValueEventListener(notesListener);
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.widget.EditText editText = new android.widget.EditText(this);
        editText.setHint("Введите заметку");

        String[] selectedDateTime = {null};

        builder.setTitle("Новая заметка")
                .setView(editText)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String text = editText.getText().toString().trim();
                    if (!text.isEmpty()) {
                        saveNote(text, selectedDateTime[0]);
                    }
                })
                .setNeutralButton("Напоминание", (dialog, which) -> showDateTimePicker(selectedDateTime))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showDateTimePicker(String[] selectedDateTime) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (view1, hourOfDay, minute) -> {
                                selectedDateTime[0] = String.format(Locale.getDefault(), "%d-%02d-%02d %02d:%02d", year, month + 1, dayOfMonth, hourOfDay, minute);
                                Toast.makeText(this, "Напоминание: " + selectedDateTime[0], Toast.LENGTH_SHORT).show();
                                scheduleReminder(selectedDateTime[0]);
                            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                    timePicker.show();
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void saveNote(String text, String reminderTime) {
        DatabaseReference notesRef = database.getReference("users")
                .child(currentUserId).child("pets").child(petId).child("remaining").child("notes");
        String noteId = notesRef.push().getKey();
        Note note = new Note(text, reminderTime);
        notesRef.child(noteId).setValue(note)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void scheduleReminder(String dateTimeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            long triggerTime = sdf.parse(dateTimeStr).getTime();

            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("note_time", dateTimeStr);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) (triggerTime % 100000),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
            Toast.makeText(this, "Напоминание установлено", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка напоминания: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNotesChanged() {
        loadNotes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notesListener != null && database != null) {
            database.getReference("users").child(currentUserId).child("pets")
                    .child(petId).child("remaining").child("notes").removeEventListener(notesListener);
        }
    }
}

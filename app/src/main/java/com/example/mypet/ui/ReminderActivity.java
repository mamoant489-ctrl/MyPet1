package com.example.mypet.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.util.Calendar;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mypet.R;
import com.example.mypet.adapters.ReminderAdapter;
import com.example.mypet.models.Reminder;
import com.example.mypet.notifications.ReminderReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReminderActivity extends AppCompatActivity
        implements ReminderAdapter.ReminderListener {

    private RecyclerView rvReminders;

    private FloatingActionButton fabAdd;

    private ReminderAdapter adapter;

    private List<Reminder> reminderList = new ArrayList<>();

    private DatabaseReference remindersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reminder);

        rvReminders = findViewById(R.id.rvReminders);

        fabAdd = findViewById(R.id.fabAddReminder);

        adapter = new ReminderAdapter(reminderList, this);

        rvReminders.setLayoutManager(
                new LinearLayoutManager(this));

        rvReminders.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        remindersRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(uid)
                .child("reminders");

        loadReminders();

        fabAdd.setOnClickListener(v ->
                showAddReminderDialog());

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    1);
        }
    }

    private void loadReminders() {

        remindersRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        reminderList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            Reminder reminder =
                                    ds.getValue(Reminder.class);

                            if (reminder != null) {
                                reminderList.add(reminder);
                            }
                        }

                        sortReminders();

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sortReminders() {

        Collections.sort(reminderList, (r1, r2) -> {

            if (r1.isCompleted() && !r2.isCompleted()) {
                return 1;
            }

            if (!r1.isCompleted() && r2.isCompleted()) {
                return -1;
            }

            return Long.compare(
                    r1.getReminderTime(),
                    r2.getReminderTime()
            );
        });
    }

    private void showAddReminderDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        final android.view.View view =
                getLayoutInflater().inflate(
                        R.layout.dialog_add_reminder,
                        null);

        builder.setView(view);

        AlertDialog dialog = builder.create();

        EditText etTitle =
                view.findViewById(R.id.etReminderTitle);

        EditText etDesc =
                view.findViewById(R.id.etReminderDesc);

        EditText etDate =
                view.findViewById(R.id.etReminderDate);

        EditText etTime =
                view.findViewById(R.id.etReminderTime);

        Calendar calendar = Calendar.getInstance();

        etDate.setFocusable(false);
        etTime.setFocusable(false);

        etDate.setOnClickListener(v -> {

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(
                            ReminderActivity.this,

                            (view1, selectedYear,
                             selectedMonth,
                             selectedDay) -> {

                                String formattedDate =
                                        String.format(
                                                Locale.getDefault(),
                                                "%02d.%02d.%04d",
                                                selectedDay,
                                                selectedMonth + 1,
                                                selectedYear
                                        );

                                etDate.setText(formattedDate);

                            },

                            year,
                            month,
                            day
                    );

            datePickerDialog.show();
        });

        etTime.setOnClickListener(v -> {

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog =
                    new TimePickerDialog(
                            ReminderActivity.this,

                            (view12, selectedHour,
                             selectedMinute) -> {

                                String formattedTime =
                                        String.format(
                                                Locale.getDefault(),
                                                "%02d:%02d",
                                                selectedHour,
                                                selectedMinute
                                        );

                                etTime.setText(formattedTime);

                            },

                            hour,
                            minute,
                            true
                    );

            timePickerDialog.show();
        });

        Spinner spinnerCategory =
                view.findViewById(R.id.spinnerCategory);

        Button btnSave =
                view.findViewById(R.id.btnSaveReminder);

        String[] categories = {
                "Вакцина",
                "Лекарство",
                "Прогулка",
                "Кормление",
                "Груминг"
        };

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories);

        spinnerCategory.setAdapter(spinnerAdapter);

        btnSave.setOnClickListener(v -> {

            try {

                String title =
                        etTitle.getText().toString().trim();

                String desc =
                        etDesc.getText().toString().trim();

                String date =
                        etDate.getText().toString().trim();

                String time =
                        etTime.getText().toString().trim();

                String category =
                        spinnerCategory
                                .getSelectedItem()
                                .toString();

                if (title.isEmpty()
                        || date.isEmpty()
                        || time.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Заполните все поля",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                SimpleDateFormat sdf =
                        new SimpleDateFormat(
                                "dd.MM.yyyy HH:mm",
                                Locale.getDefault());

                Date parsedDate =
                        sdf.parse(date + " " + time);

                long reminderTime =
                        parsedDate.getTime();

                String id =
                        remindersRef.push().getKey();

                Reminder reminder =
                        new Reminder(
                                id,
                                category + ": " + title,
                                desc,
                                reminderTime,
                                false,
                                ""
                        );

                remindersRef.child(id)
                        .setValue(reminder);

                scheduleNotification(
                        title,
                        reminderTime
                );

                dialog.dismiss();

            } catch (Exception e) {

                e.printStackTrace();

                Toast.makeText(
                        this,
                        "Ошибка даты: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        dialog.show();
    }

    private void scheduleNotification(String title,
                                      long reminderTime) {

        AlarmManager alarmManager =
                (AlarmManager)
                        getSystemService(ALARM_SERVICE);

        Intent intent =
                new Intent(this,
                        ReminderReceiver.class);

        intent.putExtra("title", title);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        (int) System.currentTimeMillis(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent);
    }

    @Override
    public void onDelete(Reminder reminder) {

        remindersRef.child(reminder.getId())
                .removeValue();
    }

    @Override
    public void onDone(Reminder reminder) {

        String completedDate =
                new SimpleDateFormat(
                        "dd.MM.yyyy",
                        Locale.getDefault())
                        .format(new Date());

        reminder.setCompleted(true);

        reminder.setCompletedAt(completedDate);

        remindersRef.child(reminder.getId())
                .setValue(reminder);
    }
}
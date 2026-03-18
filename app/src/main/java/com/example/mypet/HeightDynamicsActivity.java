package com.example.mypet;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class HeightDynamicsActivity extends AppCompatActivity {
    private LineChartView chartHeight;
    private RecyclerView rvHeightHistory;
    private HeightAdapter adapter;
    private DatabaseReference heightRef;
    private String userId, petId;
    private List<HeightRecord> heightRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_dynamics);

        initFirebase();
        initViews();
        loadHeightData();
    }

    private void initFirebase() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference petsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("pets");
        petsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                petId = snapshot.getChildren().iterator().next().getKey();
                heightRef = FirebaseDatabase.getInstance()
                        .getReference("users").child(userId).child("pets").child(petId)
                        .child("height_history");
                loadHeightData();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void initViews() {
        chartHeight = findViewById(R.id.chartHeightDetail);
        rvHeightHistory = findViewById(R.id.rvHeightHistory);
        rvHeightHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HeightAdapter(heightRecords, this::deleteHeightRecord);
        rvHeightHistory.setAdapter(adapter);
        findViewById(R.id.fabAddHeight).setOnClickListener(v -> showAddHeightDialog());
    }

    private void loadHeightData() {
        heightRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                heightRecords.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String date = child.getKey();
                    String height = child.getValue(String.class);
                    heightRecords.add(new HeightRecord(date, height));
                }
                adapter.notifyDataSetChanged();

                if (heightRecords.size() > 0) {
                    float[] values = new float[heightRecords.size()];
                    String[] dates = new String[heightRecords.size()];
                    for (int i = 0; i < heightRecords.size(); i++) {
                        values[i] = Float.parseFloat(heightRecords.get(i).height);
                        dates[i] = heightRecords.get(i).date;
                    }
                    chartHeight.setData(values, dates);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddHeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_measurement, null);
        EditText etValue = dialogView.findViewById(R.id.etValue);
        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        builder.setView(dialogView)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String value = etValue.getText().toString();
                    if (!value.isEmpty()) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String date = sdf.format(cal.getTime());
                        heightRef.child(date).setValue(value);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteHeightRecord(String date) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить запись?")
                .setMessage("Удалить измерение роста от " + date + "?")
                .setPositiveButton("Да", (dialog, which) -> heightRef.child(date).removeValue())
                .setNegativeButton("Нет", null)
                .show();
    }
}

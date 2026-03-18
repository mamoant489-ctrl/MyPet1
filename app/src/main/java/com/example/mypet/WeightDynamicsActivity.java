package com.example.mypet;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightDynamicsActivity extends AppCompatActivity {
    private LineChartView chartWeight;
    private RecyclerView rvWeightHistory;
    private WeightAdapter adapter;
    private DatabaseReference weightRef;
    private String userId, petId;
    private List<WeightRecord> weightRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_dynamics);

        initFirebase();
        chartWeight = findViewById(R.id.chartWeightDetail);
        rvWeightHistory = findViewById(R.id.rvWeightHistory);
        rvWeightHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WeightAdapter(weightRecords, this::deleteWeightRecord);
        rvWeightHistory.setAdapter(adapter);
        findViewById(R.id.fabAddWeight).setOnClickListener(v -> showAddWeightDialog());
        loadWeightData();
    }

    private void initFirebase() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference petsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("pets");
        petsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                petId = snapshot.getChildren().iterator().next().getKey();
                weightRef = FirebaseDatabase.getInstance()
                        .getReference("users").child(userId).child("pets").child(petId)
                        .child("weight_history");
                loadWeightData();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadWeightData() {
        weightRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                weightRecords.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String date = child.getKey();
                    String weight = child.getValue(String.class);
                    weightRecords.add(new WeightRecord(date, weight));
                }
                adapter.notifyDataSetChanged();

                if (weightRecords.size() > 0) {
                    float[] values = new float[weightRecords.size()];
                    String[] dates = new String[weightRecords.size()];
                    for (int i = 0; i < weightRecords.size(); i++) {
                        values[i] = Float.parseFloat(weightRecords.get(i).weight);
                        dates[i] = weightRecords.get(i).date;
                    }
                    chartWeight.setData(values, dates);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddWeightDialog() {
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
                        weightRef.child(date).setValue(value);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteWeightRecord(String date) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить запись?")
                .setPositiveButton("Да", (dialog, which) -> weightRef.child(date).removeValue())
                .setNegativeButton("Нет", null)
                .show();
    }
}

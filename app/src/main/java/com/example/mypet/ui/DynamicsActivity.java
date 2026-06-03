package com.example.mypet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mypet.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DynamicsActivity extends AppCompatActivity {

    private LineChart chartWeight, chartHeight;

    private TextView tvCurrentWeight;
    private TextView tvWeightDiff;

    private TextView tvCurrentHeight;
    private TextView tvHeightDiff;

    private LinearLayout cardWeight;
    private LinearLayout cardHeight;

    private ImageButton btnBack;

    private DatabaseReference weightRef;
    private DatabaseReference heightRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamics);

        initViews();
        initFirebase();

        loadWeightData();
        loadHeightData();

        cardWeight.setOnClickListener(v ->
                startActivity(
                        new Intent(this, WeightActivity.class)
                ));

        cardHeight.setOnClickListener(v ->
                startActivity(
                        new Intent(this, HeightActivity.class)
                ));

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {

        chartWeight = findViewById(R.id.chartWeight);
        chartHeight = findViewById(R.id.chartHeight);

        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvWeightDiff = findViewById(R.id.tvWeightDiff);

        tvCurrentHeight = findViewById(R.id.tvCurrentHeight);
        tvHeightDiff = findViewById(R.id.tvHeightDiff);

        cardWeight = findViewById(R.id.cardWeight);
        cardHeight = findViewById(R.id.cardHeight);

        btnBack = findViewById(R.id.btnBack);
    }

    private void initFirebase() {

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        String uid = user.getUid();

        weightRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(uid)
                .child("weight");

        heightRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(uid)
                .child("height");
    }

    private void loadWeightData() {

        weightRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<Entry> entries =
                        new ArrayList<>();

                ArrayList<Float> values =
                        new ArrayList<>();

                int index = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Float value =
                            ds.child("value")
                                    .getValue(Float.class);

                    if (value != null) {

                        values.add(value);

                        entries.add(
                                new Entry(index, value)
                        );

                        index++;
                    }
                }

                setupChart(chartWeight,
                        entries,
                        "Вес");

                updateWeightInfo(values);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadHeightData() {

        heightRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<Entry> entries =
                        new ArrayList<>();

                ArrayList<Float> values =
                        new ArrayList<>();

                int index = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Float value =
                            ds.child("value")
                                    .getValue(Float.class);

                    if (value != null) {

                        values.add(value);

                        entries.add(
                                new Entry(index, value)
                        );

                        index++;
                    }
                }

                setupChart(chartHeight,
                        entries,
                        "Рост");

                updateHeightInfo(values);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupChart(LineChart chart,
                            ArrayList<Entry> entries,
                            String label) {

        LineDataSet dataSet =
                new LineDataSet(entries, label);

        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);

        LineData data =
                new LineData(dataSet);

        chart.setData(data);

        Description description =
                new Description();

        description.setText("");

        chart.setDescription(description);

        chart.getAxisRight().setEnabled(false);

        chart.invalidate();
    }

    private void updateWeightInfo(ArrayList<Float> values) {

        if (values.isEmpty()) return;

        float current =
                values.get(values.size() - 1);

        tvCurrentWeight.setText(current + " кг");

        if (values.size() > 1) {

            float previous =
                    values.get(values.size() - 2);

            float diff =
                    current - previous;

            if (diff > 0) {

                tvWeightDiff.setText(
                        "+" + diff + " кг"
                );

                tvWeightDiff.setTextColor(
                        getColor(android.R.color.holo_green_dark)
                );

            } else if (diff < 0) {

                tvWeightDiff.setText(
                        diff + " кг"
                );

                tvWeightDiff.setTextColor(
                        getColor(android.R.color.holo_red_dark)
                );

            } else {

                tvWeightDiff.setText("Без изменений");
            }
        }
    }

    private void updateHeightInfo(ArrayList<Float> values) {

        if (values.isEmpty()) return;

        float current =
                values.get(values.size() - 1);

        tvCurrentHeight.setText(current + " см");

        if (values.size() > 1) {

            float previous =
                    values.get(values.size() - 2);

            float diff =
                    current - previous;

            if (diff > 0) {

                tvHeightDiff.setText(
                        "+" + diff + " см"
                );

                tvHeightDiff.setTextColor(
                        getColor(android.R.color.holo_green_dark)
                );

            } else if (diff < 0) {

                tvHeightDiff.setText(
                        diff + " см"
                );

                tvHeightDiff.setTextColor(
                        getColor(android.R.color.holo_red_dark)
                );

            } else {

                tvHeightDiff.setText("Без изменений");
            }
        }
    }
}
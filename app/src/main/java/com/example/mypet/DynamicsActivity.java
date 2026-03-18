package com.example.mypet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DynamicsActivity extends AppCompatActivity {

    private LineChartView chartWeight, chartHeight;
    private TextView tvCurrentWeight, tvWeightChange, tvCurrentHeight, tvHeightChange;
    private DatabaseReference weightRef, heightRef;
    private String userId, petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamics);

        initViews();
        initFirebase();
    }

    private void initViews() {
        chartWeight = findViewById(R.id.chartWeight);
        chartHeight = findViewById(R.id.chartHeight);
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvWeightChange = findViewById(R.id.tvWeightChange);
        tvCurrentHeight = findViewById(R.id.tvCurrentHeight);
        tvHeightChange = findViewById(R.id.tvHeightChange);


        chartWeight.setOnClickListener(v ->
                startActivity(new Intent(this, WeightDynamicsActivity.class)));
        chartHeight.setOnClickListener(v ->
                startActivity(new Intent(this, HeightDynamicsActivity.class)));
    }

    private void initFirebase() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference petsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("pets");

        petsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    petId = snapshot.getChildren().iterator().next().getKey();
                    weightRef = FirebaseDatabase.getInstance()
                            .getReference("users").child(userId).child("pets").child(petId)
                            .child("weight_history");
                    heightRef = FirebaseDatabase.getInstance()
                            .getReference("users").child(userId).child("pets").child(petId)
                            .child("height_history");


                    loadWeightData();
                    loadHeightData();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadWeightData() {
        weightRef.orderByKey().limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float[] values = new float[(int) snapshot.getChildrenCount()];
                String[] dates = new String[values.length];
                int i = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    values[i] = Float.parseFloat(child.getValue(String.class));
                    dates[i] = child.getKey();
                    i++;
                }
                if (chartWeight != null) {
                    chartWeight.setData(values, dates);
                    updateWeightStats(values);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadHeightData() {
        heightRef.orderByKey().limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float[] values = new float[(int) snapshot.getChildrenCount()];
                String[] dates = new String[values.length];
                int i = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    values[i] = Float.parseFloat(child.getValue(String.class));
                    dates[i] = child.getKey();
                    i++;
                }
                if (chartHeight != null) {
                    chartHeight.setData(values, dates);
                    updateHeightStats(values);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateWeightStats(float[] values) {
        if (values.length > 0 && tvCurrentWeight != null && tvWeightChange != null) {
            tvCurrentWeight.setText(String.format("%.1f кг", values[values.length-1]));
            if (values.length > 1) {
                float change = values[values.length-1] - values[values.length-2];
                tvWeightChange.setText(String.format("Δ%.1f %s",
                        Math.abs(change), change >= 0 ? "📈" : "📉"));
                tvWeightChange.setTextColor(change >= 0 ? Color.GREEN : Color.RED);
            }
        }
    }

    private void updateHeightStats(float[] values) {
        if (values.length > 0 && tvCurrentHeight != null && tvHeightChange != null) {
            tvCurrentHeight.setText(String.format("%.1f см", values[values.length-1]));
            if (values.length > 1) {
                float change = values[values.length-1] - values[values.length-2];
                tvHeightChange.setText(String.format("Δ%.1f %s",
                        Math.abs(change), change >= 0 ? "📈" : "📉"));
                tvHeightChange.setTextColor(change >= 0 ? Color.GREEN : Color.RED);
            }
        }
    }
}

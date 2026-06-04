package com.example.mypet.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypet.R;
import com.example.mypet.adapters.MeasurementAdapter;
import com.example.mypet.models.Measurement;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WeightActivity extends AppCompatActivity
        implements MeasurementAdapter.Listener {

    private LineChart chart;

    private RecyclerView rvMeasurements;

    private FloatingActionButton fabAdd;

    private ImageButton btnBack;

    private ArrayList<Measurement> list =
            new ArrayList<>();

    private MeasurementAdapter adapter;

    private DatabaseReference weightRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        initViews();
        initFirebase();
        setupRecycler();
        loadData();

        fabAdd.setOnClickListener(v -> showAddDialog());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {

        chart = findViewById(R.id.chart);
        rvMeasurements = findViewById(R.id.rvMeasurements);
        fabAdd = findViewById(R.id.fabAdd);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initFirebase() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();

        weightRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(uid)
                .child("weight");
    }

    private void setupRecycler() {

        adapter = new MeasurementAdapter(list, this, "кг");
        rvMeasurements.setLayoutManager(new LinearLayoutManager(this));
        rvMeasurements.setAdapter(adapter);
    }

    private void loadData() {

        weightRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        list.clear();

                        ArrayList<Entry> entries =
                                new ArrayList<>();

                        int index = 0;

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            Measurement measurement =
                                    ds.getValue(
                                            Measurement.class
                                    );

                            if (measurement != null) {

                                list.add(measurement);

                                entries.add(
                                        new Entry(
                                                index,
                                                measurement.getValue()
                                        )
                                );

                                index++;
                            }
                        }

                        adapter.notifyDataSetChanged();

                        setupChart(entries);
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                    }
                });
    }

    private void setupChart(
            ArrayList<Entry> entries) {

        LineDataSet dataSet =
                new LineDataSet(entries,
                        "Вес");

        dataSet.setLineWidth(3f);

        dataSet.setCircleRadius(5f);

        LineData data =
                new LineData(dataSet);

        chart.setData(data);

        Description description =
                new Description();

        description.setText("");

        chart.setDescription(description);

        chart.getAxisRight()
                .setEnabled(false);

        chart.invalidate();
    }

    private void showAddDialog() {

        EditText etValue =
                new EditText(this);

        etValue.setHint("Введите вес");

        new AlertDialog.Builder(this)

                .setTitle("Добавить вес")

                .setView(etValue)

                .setPositiveButton("Сохранить",
                        (dialog, which) -> {

                            String valueText =
                                    etValue.getText()
                                            .toString()
                                            .trim();

                            if (valueText.isEmpty()) {

                                Toast.makeText(this,
                                        "Введите значение",
                                        Toast.LENGTH_SHORT).show();

                                return;
                            }

                            float value =
                                    Float.parseFloat(valueText);

                            String id =
                                    weightRef.push().getKey();

                            String date =
                                    new SimpleDateFormat(
                                            "dd.MM.yyyy",
                                            Locale.getDefault()
                                    ).format(new Date());

                            Measurement measurement =
                                    new Measurement(
                                            id,
                                            value,
                                            date
                                    );

                            weightRef.child(id)
                                    .setValue(measurement);
                        })

                .setNegativeButton("Отмена",
                        null)

                .show();
    }

    @Override
    public void onDelete(
            Measurement measurement) {

        new AlertDialog.Builder(this)

                .setTitle("Удалить?")

                .setMessage(
                        measurement.getValue()
                                + " кг"
                )

                .setPositiveButton("Удалить",
                        (dialog, which) -> {

                            weightRef.child(
                                    measurement.getId()
                            ).removeValue();
                        })

                .setNegativeButton("Отмена",
                        null)

                .show();
    }

    @Override
    public void onEdit(
            Measurement measurement) {

        EditText etValue =
                new EditText(this);

        etValue.setText(
                String.valueOf(
                        measurement.getValue()
                )
        );

        new AlertDialog.Builder(this)

                .setTitle("Редактировать")

                .setView(etValue)

                .setPositiveButton("Сохранить",
                        (dialog, which) -> {

                            String valueText =
                                    etValue.getText()
                                            .toString()
                                            .trim();

                            if (valueText.isEmpty())
                                return;

                            float value =
                                    Float.parseFloat(valueText);

                            measurement.setValue(value);

                            weightRef.child(
                                    measurement.getId()
                            ).setValue(measurement);
                        })

                .setNegativeButton("Отмена",
                        null)

                .show();
    }
}
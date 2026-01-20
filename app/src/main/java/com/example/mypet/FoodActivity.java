package com.example.mypet;

import static com.example.mypet.R.*;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
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
import java.util.List;
import java.util.Locale;

public class FoodActivity extends AppCompatActivity implements MealClickListener {

    private TextView tvFoodName, tvDailyNorm, tvMealsPerDay;
    private TextView tvEatenToday, tvTreatsToday, tvProgressHint;
    private TextView tvEmptyMeals;
    private RecyclerView rvMeals;
    private Button btnSetupFood;
    private LinearProgressIndicator progressDaily;


    private FirebaseUser currentUser;
    private String currentPetId;
    private DatabaseReference mealsRef, foodSettingsRef;


    private List<Meal> mealsList = new ArrayList<>();
    private GroupedMealsAdapter groupedAdapter;
    private AlertDialog setupDialog, addDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_food);

        initViews();
        initFirebase();
        setupRecyclerView();
        setupClickListeners();
    }

    private void setupClickListeners() {
        btnSetupFood.setOnClickListener(v -> {
            if (foodSettingsRef == null || currentPetId == null) {
                Toast.makeText(this, "Загружаем данные питомца...", Toast.LENGTH_SHORT).show();
                return;
            }
            showFoodSettingsDialog();
        });


        findViewById(R.id.fabAddMeal).setOnClickListener(v -> {
            if (mealsRef == null || currentPetId == null) {
                Toast.makeText(this, "Загружаем данные питомца...", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddMealDialog();
        });
    }

    private void initViews() {
        tvFoodName = findViewById(id.tvFoodName);
        tvDailyNorm = findViewById(id.tvDailyNorm);
        tvMealsPerDay = findViewById(id.tvMealsPerDay);
        tvEatenToday = findViewById(id.tvEatenToday);
        tvTreatsToday = findViewById(id.tvTreatsToday);
        tvProgressHint = findViewById(id.tvProgressHint);
        tvEmptyMeals = findViewById(id.tvEmptyMeals);
        rvMeals = findViewById(id.rvMeals);
        btnSetupFood = findViewById(id.btnSetupFood);
        progressDaily = findViewById(id.progressDaily);
    }

    private void initFirebase() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Авторизуйтесь", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCurrentPetId();
    }

    private void loadCurrentPetId() {
        String uid = currentUser.getUid();
        DatabaseReference petsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets");

        petsRef.orderByKey().limitToLast(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            currentPetId = child.getKey();
                            if (currentPetId != null) {
                                setupDatabaseReferences();
                                loadFoodSettings();
                                loadMeals();
                            }
                            break;
                        }
                    } else {
                        tvProgressHint.setText("Сначала создайте профиль питомца");
                        btnSetupFood.setEnabled(false);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setupDatabaseReferences() {
        if (currentPetId == null) return;

        String uid = currentUser.getUid();
        mealsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets")
                .child(currentPetId)
                .child("meals");

        foodSettingsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets")
                .child(currentPetId)
                .child("foodSettings");
    }

    private void setupRecyclerView() {
        groupedAdapter = new GroupedMealsAdapter(mealsList, this);
        rvMeals.setLayoutManager(new LinearLayoutManager(this));
        rvMeals.setAdapter(groupedAdapter);
    }



    private void loadMeals() {
        if (mealsRef == null) return;

        mealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mealsList.clear();
                for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                    Meal meal = mealSnapshot.getValue(Meal.class);
                    if (meal != null) {
                        mealsList.add(meal);
                    }
                }
                mealsList.sort((m1, m2) -> m2.getDateTime().compareTo(m1.getDateTime()));
                groupedAdapter.notifyDataSetChanged();
                updateEmptyState();
                updateTodayStats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodActivity.this, "❌ " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadFoodSettings() {
        if (foodSettingsRef == null) return;

        foodSettingsRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        FoodSettings settings = snapshot.getValue(FoodSettings.class);
                        if (settings != null) {
                            tvFoodName.setText(settings.foodName);
                            tvDailyNorm.setText(settings.dailyNorm + "г");
                            tvMealsPerDay.setText(settings.mealsPerDay);
                        }
                    }
                });
    }


    private void showFoodSettingsDialog() {
        if (foodSettingsRef == null) {
            Toast.makeText(this, "Нет данных о питомце", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(layout.dialog_food_settings, null);
        EditText etFoodName = dialogView.findViewById(id.etFoodName);
        EditText etDailyNorm = dialogView.findViewById(id.etDailyNorm);
        EditText etMealsPerDay = dialogView.findViewById(id.etMealsPerDay);
        Button btnSave = dialogView.findViewById(id.btnSaveSettings);

        etFoodName.setText(tvFoodName.getText().toString().replace("—", ""));
        etDailyNorm.setText(tvDailyNorm.getText().toString().replace("г", "").replace("—", ""));
        etMealsPerDay.setText(tvMealsPerDay.getText().toString().replace("—", ""));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        setupDialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String foodName = etFoodName.getText().toString().trim();
            String dailyNorm = etDailyNorm.getText().toString().trim();
            String mealsPerDay = etMealsPerDay.getText().toString().trim();

            if (foodName.isEmpty() || dailyNorm.isEmpty() || mealsPerDay.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (foodSettingsRef == null) {
                Toast.makeText(this, "Ошибка подключения", Toast.LENGTH_SHORT).show();
                return;
            }

            FoodSettings settings = new FoodSettings(foodName, dailyNorm, mealsPerDay);
            foodSettingsRef.setValue(settings)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Сохранено!", Toast.LENGTH_SHORT).show();
                        setupDialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        setupDialog.show();
    }


    private void showAddMealDialog() {
        if (mealsRef == null || currentPetId == null) {
            Toast.makeText(this, "Нет данных о питомце", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_meal, null);


        Spinner spinnerType = dialogView.findViewById(R.id.rgMealType);
        String[] mealTypes = {"Еда", "Лакомство"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                mealTypes
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spinnerAdapter);

        EditText etTitle = dialogView.findViewById(R.id.etMealTitle);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Button btnSave = dialogView.findViewById(R.id.btnSaveMeal);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        addDialog = builder.create();

        btnSave.setOnClickListener(v -> {

            String selectedType = spinnerType.getSelectedItem().toString();
            String type = selectedType.equals("Еда") ? "еда" : "лакомство";

            String title = etTitle.getText().toString().trim();
            String comment = etComment.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();

            if (title.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Заполните название и количество", Toast.LENGTH_SHORT).show();
                return;
            }


            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String mealId = mealsRef.push().getKey();
            Meal meal = new Meal(
                    mealId,
                    type,
                    title,
                    comment.isEmpty() ? "—" : comment,
                    amount + (type.equals("еда") ? "г" : "шт"),
                    sdf.format(new Date())
            );

            mealsRef.child(mealId).setValue(meal)
                    .addOnSuccessListener(unused -> {
                        addDialog.dismiss();
                        Toast.makeText(this, "Приём пищи добавлен!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        addDialog.show();
    }



    private void updateTodayStats() {
        String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        int eatenToday = 0, treatsToday = 0;

        for (Meal meal : mealsList) {
            if (meal.getDateTime().startsWith(today)) {
                if ("еда".equals(meal.getType())) {
                    String grams = meal.getAmount().replaceAll("[^0-9]", "");
                    if (!grams.isEmpty()) eatenToday += Integer.parseInt(grams);
                } else if ("лакомство".equals(meal.getType())) {
                    treatsToday++;
                }
            }
        }

        tvEatenToday.setText(String.valueOf(eatenToday));
        tvTreatsToday.setText(String.valueOf(treatsToday));
        updateProgressBar(eatenToday);
    }

    private void updateProgressBar(int eatenToday) {
        String dailyNormText = tvDailyNorm.getText().toString().replace("г", "").replace("—", "0");
        try {
            int dailyNorm = Integer.parseInt(dailyNormText);
            if (dailyNorm > 0) {
                int progress = Math.min(100, (eatenToday * 100) / dailyNorm);
                progressDaily.setProgress(progress);
                tvProgressHint.setText(eatenToday + "/" + dailyNorm + "г");
            } else {
                tvProgressHint.setText("Заполните норму");
            }
        } catch (NumberFormatException ignored) {
            progressDaily.setProgress(0);
            tvProgressHint.setText("Заполните норму");
        }
    }

    private void updateEmptyState() {
        tvEmptyMeals.setVisibility(mealsList.isEmpty() ? View.VISIBLE : View.GONE);
        rvMeals.setVisibility(mealsList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDelete(Meal meal) {
        if (mealsRef == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Удалить?")
                .setMessage(meal.getTitle() + " - " + meal.getAmount())
                .setPositiveButton("Удалить", (dialog, which) ->
                        mealsRef.child(meal.getId()).removeValue())
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onEdit(Meal meal) {
        Toast.makeText(this, "Редактирование: " + meal.getTitle(), Toast.LENGTH_SHORT).show();
    }
}

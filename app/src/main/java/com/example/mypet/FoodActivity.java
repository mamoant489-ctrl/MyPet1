package com.example.mypet;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    // Views
    private TextView tvFoodName, tvDailyNorm, tvMealsPerDay;
    private TextView tvEatenToday, tvTreatsToday, tvProgressHint, tvEmptyMeals;
    private RecyclerView rvMeals;
    private Button btnSetupFood;
    private LinearProgressIndicator progressDaily;
    private FloatingActionButton fabAddMeal;
    private ImageButton btnBack;

    // Firebase
    private FirebaseUser currentUser;
    private String currentPetId;
    private DatabaseReference mealsRef, foodSettingsRef, petRef;

    // Data
    private List<Meal> mealsList = new ArrayList<>();
    private GroupedMealsAdapter groupedAdapter;
    private AlertDialog setupDialog, addDialog;
    private ValueEventListener mealsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        initViews();
        initFirebase();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        tvFoodName = findViewById(R.id.tvFoodName);
        tvDailyNorm = findViewById(R.id.tvDailyNorm);
        tvMealsPerDay = findViewById(R.id.tvMealsPerDay);
        tvEatenToday = findViewById(R.id.tvEatenToday);
        tvTreatsToday = findViewById(R.id.tvTreatsToday);
        tvProgressHint = findViewById(R.id.tvProgressHint);
        tvEmptyMeals = findViewById(R.id.tvEmptyMeals);
        rvMeals = findViewById(R.id.rvMeals);
        btnSetupFood = findViewById(R.id.btnSetupFood);
        progressDaily = findViewById(R.id.progressDaily);
        fabAddMeal = findViewById(R.id.fabAddMeal);
        btnBack = findViewById(R.id.imageButton);


        tvEatenToday.setText("0");
        tvTreatsToday.setText("0");
        tvProgressHint.setText("Заполните норму");
    }

    private void setupClickListeners() {

        btnSetupFood.setOnClickListener(v -> showFoodSettingsDialog());


        if (fabAddMeal != null) {
            fabAddMeal.setOnClickListener(v -> {
                if (mealsRef == null || currentPetId == null) {
                    Toast.makeText(this, "Загружаем данные питомца...", Toast.LENGTH_SHORT).show();
                } else {
                    showAddMealDialog();
                }
            });
        }


        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
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
                .getReference("Users").child(uid).child("pets");

        petsRef.orderByKey().limitToLast(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            currentPetId = child.getKey();
                            setupDatabaseReferences();
                            loadFoodSettings();
                            loadMeals();
                            break;
                        }
                    } else {
                        tvProgressHint.setText("Сначала создайте профиль питомца");
                        btnSetupFood.setEnabled(false);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void setupDatabaseReferences() {
        if (currentPetId == null) return;

        String uid = currentUser.getUid();
        petRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("pets").child(currentPetId);
        mealsRef = petRef.child("meals");
        foodSettingsRef = petRef.child("foodSettings");
    }

    private void setupRecyclerView() {
        groupedAdapter = new GroupedMealsAdapter(mealsList, this);
        rvMeals.setLayoutManager(new LinearLayoutManager(this));
        rvMeals.setAdapter(groupedAdapter);
    }

    private void loadMeals() {

        if (mealsListener != null) {
            mealsRef.removeEventListener(mealsListener);
        }

        mealsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mealsList.clear();
                for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                    Meal meal = mealSnapshot.getValue(Meal.class);
                    if (meal != null && meal.getId() != null) {
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
        };

        mealsRef.addValueEventListener(mealsListener);
    }

    private void loadFoodSettings() {
        if (foodSettingsRef == null) return;

        foodSettingsRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        FoodSettings settings = snapshot.getValue(FoodSettings.class);
                        if (settings != null) {
                            tvFoodName.setText(settings.foodName);
                            tvDailyNorm.setText(settings.dailyNorm + " г");
                            tvMealsPerDay.setText(settings.mealsPerDay);
                            updateProgressBar(0);
                        }
                    } else {
                        tvFoodName.setText("—");
                        tvDailyNorm.setText("—");
                        tvMealsPerDay.setText("—");
                    }
                });
    }

    private void showFoodSettingsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_food_settings, null);
        EditText etFoodName = dialogView.findViewById(R.id.etFoodName);
        EditText etDailyNorm = dialogView.findViewById(R.id.etDailyNorm);
        EditText etMealsPerDay = dialogView.findViewById(R.id.etMealsPerDay);
        Button btnSave = dialogView.findViewById(R.id.btnSaveSettings);


        etFoodName.setText(tvFoodName.getText().toString().replace("—", "").replace(" г", ""));
        etDailyNorm.setText(tvDailyNorm.getText().toString().replace(" г", "").replace("—", ""));
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

            FoodSettings settings = new FoodSettings(foodName, dailyNorm, mealsPerDay);
            foodSettingsRef.setValue(settings)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show();
                        setupDialog.dismiss();
                        loadFoodSettings();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        setupDialog.show();
    }

    private void showAddMealDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_meal, null);
        Spinner spinnerType = dialogView.findViewById(R.id.rgMealType);
        EditText etTitle = dialogView.findViewById(R.id.etMealTitle);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Button btnSave = dialogView.findViewById(R.id.btnSaveMeal);


        String[] mealTypes = {"Еда", "Лакомство"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, mealTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spinnerAdapter);

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
                    amount + (type.equals("еда") ? " г" : " шт"),
                    sdf.format(new Date())
            );

            mealsRef.child(mealId).setValue(meal)
                    .addOnSuccessListener(unused -> {
                        addDialog.dismiss();

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
                    if (!grams.isEmpty()) {
                        eatenToday += Integer.parseInt(grams);
                    }
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
        String dailyNormText = tvDailyNorm.getText().toString().replace(" г", "").replace("—", "0");
        try {
            int dailyNorm = Integer.parseInt(dailyNormText);
            if (dailyNorm > 0) {
                int progress = Math.min(100, (eatenToday * 100) / dailyNorm);
                progressDaily.setProgress(progress);
                tvProgressHint.setText(eatenToday + "/" + dailyNorm + " г");
            } else {
                tvProgressHint.setText("Заполните норму");
                progressDaily.setProgress(0);
            }
        } catch (NumberFormatException e) {
            progressDaily.setProgress(0);
            tvProgressHint.setText("Заполните норму");
        }
    }

    private void updateEmptyState() {
        boolean isEmpty = mealsList.isEmpty();
        tvEmptyMeals.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvMeals.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDelete(Meal meal) {
        if (mealsRef == null || meal.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Удалить приём пищи?")
                .setMessage(meal.getTitle() + " - " + meal.getAmount())
                .setPositiveButton("Удалить", (dialog, which) -> {
                    mealsRef.child(meal.getId()).removeValue()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "❌ Ошибка удаления", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onEdit(Meal meal) {
        Toast.makeText(this, "Редактирование: " + meal.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mealsListener != null && mealsRef != null) {
            mealsRef.removeEventListener(mealsListener);
        }
    }
}

package com.example.mypet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Profile extends AppCompatActivity {

    TextView currentDateTime;
    Calendar dateAndTime = Calendar.getInstance();
    private EditText etName, etAge, etBreed, etSex, etMark, etWeight, etHeight;
    private ImageButton btnSavePet;

    private CircularImageView ivProfile;
    private ImageButton btnAddPhoto;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private static final int REQ_READ_IMAGES = 101;

    private LinearLayout paramsContainer;
    private Button btnAddParam;

    private Button btnWeightHistory;
    private Button btnHeightHistory;
    private String currentPetId;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        currentDateTime = findViewById(R.id.etBirthDate);
        setInitialDateTime();

        etName   = findViewById(R.id.etName);
        etAge    = findViewById(R.id.etAge);
        etBreed  = findViewById(R.id.etBreed);
        etSex    = findViewById(R.id.etSex);
        etMark   = findViewById(R.id.etMark);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);

        btnSavePet = findViewById(R.id.btnSavePet);
        btnSavePet.setOnClickListener(v -> savePet());

        ivProfile = findViewById(R.id.ivProfile);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            ivProfile.setImageURI(imageUri);
                        }
                    }
                }
        );

        btnAddPhoto.setOnClickListener(v -> {
            if (checkAndRequestPermission()) {
                openGallery();
            }
        });


        paramsContainer = findViewById(R.id.paramsContainer);
        btnAddParam = findViewById(R.id.btnAddParam);

        btnAddParam.setOnClickListener(v -> addNewParamRow());

        btnWeightHistory = findViewById(R.id.btnWeightHistory);
        btnWeightHistory.setOnClickListener(v -> showWeightHistoryDialog());

        btnHeightHistory = findViewById(R.id.btnHeightHistory);
        btnHeightHistory.setOnClickListener(v -> showHeightHistoryDialog());
        loadLastPetIfExists();
    }


    public void setDate(View v) {
        new DatePickerDialog(
                Profile.this,
                d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void setInitialDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String formatted = sdf.format(dateAndTime.getTime());
        currentDateTime.setText(formatted);
    }

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
        }
    };


    public void GoToMenu(View view) {
        Intent intent = new Intent(this, BasicMenu.class);
        startActivity(intent);
    }


    private void savePet() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        String name   = etName.getText().toString().trim();
        String age    = etAge.getText().toString().trim();
        String breed  = etBreed.getText().toString().trim();
        String birth  = currentDateTime.getText().toString().trim();
        String sex    = etSex.getText().toString().trim();
        String mark   = etMark.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String height = etHeight.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите кличку", Toast.LENGTH_SHORT).show();
            return;
        }

        Pet pet = new Pet(name, age, breed, birth, sex, mark, weight, height);
        DatabaseReference petsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets");

        if (currentPetId != null) {
            petsRef.child(currentPetId).setValue(pet)
                    .addOnSuccessListener(unused -> {
                        saveWeightHistory(uid, currentPetId, weight);
                        saveHeightHistory(uid, currentPetId, height);
                        Toast.makeText(Profile.this, "Питомец обновлён", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(Profile.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            String petId = petsRef.push().getKey();
            if (petId == null) {
                Toast.makeText(this, "Ошибка создания питомца", Toast.LENGTH_SHORT).show();
                return;
            }
            currentPetId = petId;

            petsRef.child(petId).setValue(pet)
                    .addOnSuccessListener(unused -> {
                        saveWeightHistory(uid, petId, weight);  // первая запись в историю
                        saveHeightHistory(uid, petId, height);
                        Toast.makeText(Profile.this, "Питомец создан", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(Profile.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void loadLastPetIfExists() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        DatabaseReference petsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets");

        petsRef.orderByKey().limitToLast(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        return;
                    }

                    for (DataSnapshot child : snapshot.getChildren()) {
                        currentPetId = child.getKey();
                        Pet pet = child.getValue(Pet.class);
                        if (pet == null) return;

                        etName.setText(pet.name);
                        etAge.setText(pet.age);
                        etBreed.setText(pet.breed);
                        currentDateTime.setText(pet.birthDate);
                        etSex.setText(pet.sex);
                        etMark.setText(pet.mark);
                        etWeight.setText(pet.weight);
                        etHeight.setText(pet.height);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(Profile.this, "Ошибка загрузки питомца: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }


    private void saveHeightHistory(String uid, String petId, String heightValue) {
        if (heightValue == null || heightValue.isEmpty()) return;

        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets")
                .child(petId)
                .child("heightHistory");

        String id = historyRef.push().getKey();
        HistoryItem item = new HistoryItem(System.currentTimeMillis(), heightValue);
        historyRef.child(id).setValue(item);
    }


    private void saveWeightHistory(String uid, String petId, String weightValue) {
        if (weightValue == null || weightValue.isEmpty()) return;

        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets")
                .child(petId)
                .child("weightHistory");

        String id = historyRef.push().getKey();
        HistoryItem item = new HistoryItem(System.currentTimeMillis(), weightValue);
        historyRef.child(id).setValue(item);
    }

    private void showWeightHistoryDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || currentPetId == null) {
            Toast.makeText(this, "Нет данных для истории", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets")
                .child(currentPetId)
                .child("weightHistory");

        historyRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                new AlertDialog.Builder(Profile.this)
                        .setTitle("История веса")
                        .setMessage("Пока нет записей.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

            for (DataSnapshot child : snapshot.getChildren()) {
                HistoryItem item = child.getValue(HistoryItem.class);
                if (item == null) continue;

                String date = sdf.format(new Date(item.timestamp));
                sb.append("Дата изменения: ")
                        .append(date)
                        .append("\nЗапись: ")
                        .append(item.value)
                        .append("\n\n");
            }

            new AlertDialog.Builder(Profile.this)
                    .setTitle("История веса")
                    .setMessage(sb.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }).addOnFailureListener(e ->
                Toast.makeText(Profile.this, "Ошибка загрузки истории: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void showHeightHistoryDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || currentPetId == null) {
            Toast.makeText(this, "Нет данных для истории", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets")
                .child(currentPetId)
                .child("heightHistory");  // ← ИСПРАВЛЕНО: heightHistory вместо weightHistory

        historyRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                new AlertDialog.Builder(Profile.this)
                        .setTitle("История роста")
                        .setMessage("Пока нет записей.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

            for (DataSnapshot child : snapshot.getChildren()) {
                HistoryItem item = child.getValue(HistoryItem.class);
                if (item == null) continue;

                String date = sdf.format(new Date(item.timestamp));
                sb.append("Дата изменения: ")
                        .append(date)
                        .append("\nРост: ")
                        .append(item.value)
                        .append("\n\n");
            }

            new AlertDialog.Builder(Profile.this)
                    .setTitle("История роста")
                    .setMessage(sb.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }).addOnFailureListener(e ->
                Toast.makeText(Profile.this, "Ошибка загрузки истории: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }



    private boolean checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQ_READ_IMAGES);
                return false;
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQ_READ_IMAGES);
                return false;
            }
        }
        return true;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");              // только изображения [web:33]
        pickImageLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_READ_IMAGES) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this,
                        "Разрешение на доступ к фото не дано",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addNewParamRow() {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, dpToPx(8), 0, dpToPx(8));
        rowLayout.setLayoutParams(rowParams);

        // Поле ввода
        EditText etParam = new EditText(this);
        etParam.setHint("Новый параметр");
        etParam.setBackgroundColor(Color.parseColor("#E3BCA3"));
        etParam.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        etParam.setLayoutParams(etParams);

        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams btnsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonsLayout.setLayoutParams(btnsParams);
        buttonsLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        int btnSize = dpToPx(28);

        Button btnEdit = new Button(this);
        btnEdit.setText("✎");
        btnEdit.setBackgroundColor(Color.TRANSPARENT);
        btnEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        LinearLayout.LayoutParams editBtnParams = new LinearLayout.LayoutParams(
                btnSize,
                btnSize
        );
        btnEdit.setLayoutParams(editBtnParams);
        btnEdit.setPadding(0, 0, 0, 0);

        Button btnDelete = new Button(this);
        btnDelete.setText("✖");
        btnDelete.setBackgroundColor(Color.TRANSPARENT);
        btnDelete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        LinearLayout.LayoutParams delBtnParams = new LinearLayout.LayoutParams(
                btnSize,
                btnSize
        );
        delBtnParams.setMarginStart(dpToPx(4));
        btnDelete.setLayoutParams(delBtnParams);
        btnDelete.setPadding(0, 0, 0, 0);

        btnDelete.setOnClickListener(v -> paramsContainer.removeView(rowLayout));

        btnEdit.setOnClickListener(v -> {
            etParam.requestFocus();
            etParam.setSelection(etParam.getText().length());
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etParam, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        buttonsLayout.addView(btnEdit);
        buttonsLayout.addView(btnDelete);

        rowLayout.addView(etParam);
        rowLayout.addView(buttonsLayout);

        paramsContainer.addView(rowLayout);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }


}






package com.example.mypet.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.mypet.data.FirebasePaths;
import com.example.mypet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PetProfileActivity extends AppCompatActivity {


    private EditText etNickname, etBreed, etChip, etAge;
    private AutoCompleteTextView spGender;
    private LinearLayout llCustomParams;
    private TextView tvBirthDate;
    private Button btnSave;
    private ImageButton btnBack;

    private ImageView ivProfilePhoto;
    private FloatingActionButton ivAddParam;

    private boolean createNewPet = false;
    private DatabaseReference petRef;
    private String userId, petId;
    private Uri selectedPhotoUri;


    private ActivityResultLauncher<Intent> galleryLauncher;
    private List<Map<String, String>> customParams = new ArrayList<>();
    private boolean isPetIdReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_profile);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        createNewPet = getIntent().getBooleanExtra("newPet", false);

        setupGalleryLauncher();
        initPetAndProfile();
    }

    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedPhotoUri = result.getData().getData();
                        if (ivProfilePhoto != null) {
                            ivProfilePhoto.setImageURI(selectedPhotoUri);
                        }
                    }
                });
    }

    private void initPetAndProfile() {

        DatabaseReference petsRef =
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("Users")
                        .child(userId)
                        .child("pets");

        if(createNewPet){

            petId = petsRef.push().getKey();

            petRef = petsRef.child(petId);

            isPetIdReady = true;

            initViews();
            setupListeners();

            return;
        }

        petsRef.limitToFirst(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {

                        for (DataSnapshot child : snapshot.getChildren()) {
                            petId = child.getKey();
                            break;
                        }

                    } else {

                        petId = petsRef.push().getKey();
                    }

                    petRef = petsRef.child(petId);

                    isPetIdReady = true;

                    initViews();
                    loadProfile();
                    setupListeners();
                });
    }

    private void initViews() {
        etNickname = findViewById(R.id.etNickname);
        etBreed = findViewById(R.id.etBreed);
        etChip = findViewById(R.id.etChip);
        etAge = findViewById(R.id.etAge);
        spGender = findViewById(R.id.spGender);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        ivAddParam = findViewById(R.id.ivAddParam);
        llCustomParams = findViewById(R.id.llCustomParams);
        tvBirthDate = findViewById(R.id.etBirthDate);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBackToMenu);


        int brownColor = getResources().getColor(R.color.brown, getTheme());
        etNickname.setTextColor(brownColor);
        etBreed.setTextColor(brownColor);
        etChip.setTextColor(brownColor);
        etAge.setTextColor(brownColor);
        tvBirthDate.setTextColor(brownColor);
        spGender.setTextColor(brownColor);


        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.genders, android.R.layout.simple_dropdown_item_1line);
        spGender.setAdapter(genderAdapter);
        spGender.setThreshold(1);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> goToBasicMenu());


        ivProfilePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });


        tvBirthDate.setOnClickListener(v -> showDatePicker());


        ivAddParam.setOnClickListener(v -> showAddCustomParamDialog());


        btnSave.setOnClickListener(v -> saveProfileAndGoToMenu());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            String dateStr = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(cal.getTime());
            tvBirthDate.setText(dateStr);
            tvBirthDate.setTextColor(getResources().getColor(R.color.brown, getTheme()));
            updateAge(dateStr);
        }, cal.get(Calendar.YEAR) - 1, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateAge(String birthDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date birthDate = sdf.parse(birthDateStr);
            if (birthDate != null) {
                long ageInMonths = (new Date().getTime() - birthDate.getTime()) / (1000L * 60 * 60 * 24 * 30);
                int years = (int) (ageInMonths / 12);
                int months = (int) (ageInMonths % 12);
                etAge.setText(years + " г " + months + " м");
                etAge.setTextColor(getResources().getColor(R.color.brown, getTheme()));
            }
        } catch (Exception e) {
            etAge.setText("");
        }
    }

    private void showAddCustomParamDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_param, null);
        builder.setView(dialogView);

        EditText etKey = dialogView.findViewById(R.id.etKey);
        EditText etValue = dialogView.findViewById(R.id.etValue);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String key = etKey.getText().toString().trim();
            String value = etValue.getText().toString().trim();
            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                Map<String, String> param = new HashMap<>();
                param.put("key", key);
                param.put("value", value);
                customParams.add(param);
                addCustomParamView(key, value);
            }
        }).setNegativeButton("Отмена", null).show();
    }

    private void addCustomParamView(String key, String value) {
        View paramView = getLayoutInflater().inflate(R.layout.item_custom_param, llCustomParams, false);
        TextView tvKey = paramView.findViewById(R.id.tvKey);
        TextView tvValue = paramView.findViewById(R.id.tvValue);
        ImageView ivDelete = paramView.findViewById(R.id.ivDelete);

        tvKey.setText(key);
        tvValue.setText(value);
        ivDelete.setOnClickListener(v -> {
            llCustomParams.removeView(paramView);
            customParams.removeIf(param -> param.get("key").equals(key) && param.get("value").equals(value));
        });

        llCustomParams.addView(paramView);
    }

    private void loadProfile() {
        petRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                    if (data != null) {

                        etNickname.setText((String) data.get("name"));
                        etBreed.setText((String) data.get("breed"));
                        etChip.setText((String) data.get("mark"));
                        tvBirthDate.setText((String) data.get("birthDate"));
                        etAge.setText((String) data.get("age"));


                        String sex = (String) data.get("sex");
                        if (sex != null) {
                            spGender.setText(sex, false);
                        }


                        String photoUrl = (String) data.get("photoUrl");
                        if (photoUrl != null && ivProfilePhoto != null) {
                            Glide.with(PetProfileActivity.this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.usericon)
                                    .into(ivProfilePhoto);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PetProfileActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileAndGoToMenu() {
        if (!isPetIdReady || petRef == null) {
            Toast.makeText(this, "Загрузка данных питомца...", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> petData = new HashMap<>();
        petData.put("name", etNickname.getText().toString().trim());
        petData.put("breed", etBreed.getText().toString().trim());
        petData.put("sex", spGender.getText().toString().trim());
        petData.put("mark", etChip.getText().toString().trim());
        petData.put("birthDate", tvBirthDate.getText().toString());
        petData.put("age", etAge.getText().toString());
        petData.put("customParams", customParams);

        if (selectedPhotoUri != null) {
            uploadPhotoAndSave(petData);
        } else {
            saveToFirebase(petData);
        }
    }

    private void saveToFirebase(Map<String,Object> petData) {

        petRef.setValue(petData)

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Профиль сохранён",
                            Toast.LENGTH_SHORT
                    ).show();

                    goToBasicMenu();
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void uploadPhotoAndSave(Map<String, Object> petData) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("users/" + userId + "/pets/" + petId + "/profile.jpg");

        storageRef.putFile(selectedPhotoUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                petData.put("photoUrl", uri.toString());
                saveToFirebase(petData);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Ошибка фото: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            saveToFirebase(petData);
        });
    }

    private void goToBasicMenu() {
        Intent intent = new Intent(PetProfileActivity.this, BasicMenu.class);
        startActivity(intent);
        finish();
    }
}
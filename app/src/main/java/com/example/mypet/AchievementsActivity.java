package com.example.mypet;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AchievementsActivity extends AppCompatActivity implements AchievementsAdapter.OnAchievementChangedListener {

    private RecyclerView recyclerView;
    private AchievementsAdapter adapter;
    private List<Achievement> achievementsList;
    private DatabaseReference achievementsRef;
    private String userId, petId;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> cameraLauncher;
    private Uri selectedImageUri;
    private String currentEditingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        petId = getIntent().getStringExtra("petId");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        achievementsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pets").child(petId).child("achievements");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        achievementsList = new ArrayList<>();
        adapter = new AchievementsAdapter(achievementsList, this);
        recyclerView.setAdapter(adapter);

        listenToFirebaseChanges();

        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddEditDialog(null));


        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
            }
        });


        cameraLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                galleryLauncher.launch(cameraIntent);
            }
        });
    }

    private void listenToFirebaseChanges() {
        achievementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                achievementsList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Achievement achievement = child.getValue(Achievement.class);
                    if (achievement != null) {
                        achievement.setId(child.getKey());
                        achievementsList.add(achievement);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AchievementsActivity.this, "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEditDialog(String editingId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievement, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        ImageView ivPhoto = dialogView.findViewById(R.id.ivPhotoPreview);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvDate.setText(currentDate);

        tvDate.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
                cal.set(year, month, day);
                tvDate.setText(sdf.format(cal.getTime()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });


        ivPhoto.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Выбрать фото")
                    .setItems(new CharSequence[]{"Галерея", "Камера"}, (dialog, which) -> {
                        if (which == 0) {
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            galleryLauncher.launch(galleryIntent);
                        } else {
                            cameraLauncher.launch("android.permission.CAMERA");
                        }
                    }).show();
        });

        if (editingId != null) {
            achievementsRef.child(editingId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Achievement ach = snapshot.getValue(Achievement.class);
                    if (ach != null) {
                        etTitle.setText(ach.getTitle());
                        tvDate.setText(ach.getDate());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String date = tvDate.getText().toString();
            if (title.isEmpty()) {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedImageUri != null) {
                uploadPhotoAndSave(title, date, editingId);
            } else {
                saveAchievement(title, date, null, editingId);
            }
        }).setNegativeButton("Отмена", null).show();
    }

    private void uploadPhotoAndSave(String title, String date, String editingId) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("users/" + userId + "/pets/" + petId + "/achievements/" + System.currentTimeMillis() + ".jpg");
        storageRef.putFile(selectedImageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveAchievement(title, date, uri.toString(), editingId);
                });
            } else {
                Toast.makeText(this, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAchievement(String title, String date, String photoUrl, String editingId) {
        String id = editingId != null ? editingId : achievementsRef.push().getKey();
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("title", title);
        achievement.put("date", date);
        achievement.put("photoUrl", photoUrl);
        achievementsRef.child(id).setValue(achievement);
        currentEditingId = null;
        selectedImageUri = null;
    }

    @Override
    public void onAchievementChanged(String achievementId) {
        currentEditingId = achievementId;
        showAddEditDialog(achievementId);
    }

    @Override
    public void onDeleteAchievement(String achievementId) {
        new AlertDialog.Builder(this).setTitle("Удалить?")
                .setPositiveButton("Да", (d, w) -> achievementsRef.child(achievementId).removeValue())
                .setNegativeButton("Нет", null).show();
    }
}

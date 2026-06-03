package com.example.mypet.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypet.R;
import com.example.mypet.adapters.AchievementAdapter;
import com.example.mypet.models.Achievement;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AchievementsActivity extends AppCompatActivity
        implements AchievementAdapter.DeleteListener {

    private RecyclerView rvAchievements;
    private FloatingActionButton fabAdd;

    private List<Achievement> achievementsList;
    private AchievementAdapter adapter;

    private FirebaseUser currentUser;

    private DatabaseReference achievementsRef;

    private StorageReference storageRef;

    private Uri selectedImageUri;

    private String currentPetId;

    private ImageView currentPreview;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {

                            selectedImageUri =
                                    result.getData().getData();

                            if (currentPreview != null) {
                                currentPreview.setImageURI(
                                        selectedImageUri
                                );
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        rvAchievements =
                findViewById(R.id.rvAchievements);

        fabAdd =
                findViewById(R.id.fabAdd);

        achievementsList = new ArrayList<>();


        currentUser = FirebaseAuth.getInstance().getCurrentUser();



        if (currentUser == null) {

            Toast.makeText(this,
                    "Вы не авторизованы",
                    Toast.LENGTH_SHORT).show();

            finish();

            return;
        }

        loadCurrentPetId();


        storageRef =
                FirebaseStorage.getInstance()
                        .getReference()
                        .child("achievement_photos");


        adapter = new AchievementAdapter(
                achievementsList,
                this
        );

        rvAchievements.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvAchievements.setAdapter(adapter);


        fabAdd.setOnClickListener(v ->
                showAddDialog());
    }

    private void loadCurrentPetId() {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(currentUser.getUid())
                .child("pets")

                .orderByKey()
                .limitToLast(1)

                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                if (!snapshot.exists()) {

                                    Toast.makeText(
                                            AchievementsActivity.this,
                                            "Нет питомцев",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    finish();
                                    return;
                                }

                                for (DataSnapshot child : snapshot.getChildren()) {

                                    currentPetId = child.getKey();
                                    break;
                                }

                                achievementsRef =
                                        FirebaseDatabase.getInstance()
                                                .getReference()
                                                .child("Users")
                                                .child(currentUser.getUid())
                                                .child("pets")
                                                .child(currentPetId)
                                                .child("Achievements");

                                storageRef =
                                        FirebaseStorage.getInstance()
                                                .getReference()
                                                .child("achievement_photos")
                                                .child(currentUser.getUid())
                                                .child(currentPetId);

                                loadAchievements();
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                                Toast.makeText(
                                        AchievementsActivity.this,
                                        error.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
    }

    private void loadAchievements() {

        achievementsRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        achievementsList.clear();

                        for (DataSnapshot child
                                : snapshot.getChildren()) {

                            Achievement achievement =
                                    child.getValue(
                                            Achievement.class
                                    );

                            if (achievement != null) {

                                achievementsList.add(
                                        achievement
                                );
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                AchievementsActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void showAddDialog() {

        selectedImageUri = null;

        android.view.View dialogView =
                LayoutInflater.from(this)
                        .inflate(R.layout.dialog_achievement, null);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        Button btnPickImage = dialogView.findViewById(R.id.btnPickImage);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        ImageView ivPreview = dialogView.findViewById(R.id.ivPreview);

        currentPreview = ivPreview;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.setOnDismissListener(d -> {
            selectedImageUri = null;
            currentPreview = null;
        });

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {

            String title = etTitle.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Выберите фото", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadImage(title, dialog);
        });

        dialog.show();
    }

    private void uploadImage(String title, AlertDialog dialog) {

        if (selectedImageUri == null) {
            Toast.makeText(this, "Ошибка: изображение не выбрано", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageName = System.currentTimeMillis() + ".jpg";

        StorageReference imageRef = storageRef.child(imageName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    saveAchievement(title, uri.toString(), dialog);
                                })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Upload failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveAchievement(String title,
                                 String imageUrl,
                                 AlertDialog dialog) {

        String achievementId =
                achievementsRef.push().getKey();

        String currentDate =
                new SimpleDateFormat(
                        "dd.MM.yyyy",
                        Locale.getDefault()
                ).format(new Date());

        Achievement achievement =
                new Achievement(
                        achievementId,
                        title,
                        imageUrl,
                        currentDate
                );

        achievementsRef.child(achievementId)

                .setValue(achievement)

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Ачивка добавлена",
                            Toast.LENGTH_SHORT
                    ).show();

                    selectedImageUri = null;

                    dialog.dismiss();
                })

                .addOnFailureListener(e ->

                        Toast.makeText(
                                this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    @Override
    public void onDelete(Achievement achievement) {

        if (achievement == null
                || achievement.getId() == null) {
            return;
        }

        new AlertDialog.Builder(this)

                .setTitle("Удалить ачивку?")

                .setMessage(
                        achievement.getTitle()
                )

                .setPositiveButton(
                        "Удалить",
                        (dialog, which) ->

                                achievementsRef
                                        .child(
                                                achievement.getId()
                                        )
                                        .removeValue()
                )

                .setNegativeButton(
                        "Отмена",
                        null
                )

                .show();
    }
}
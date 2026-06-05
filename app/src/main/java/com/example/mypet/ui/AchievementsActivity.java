package com.example.mypet.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
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

    private String currentPetId;
    private ImageButton btnBack;

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
        initViews();
        setupClickListeners();

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

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
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

        android.view.View dialogView =
                LayoutInflater.from(this)
                        .inflate(R.layout.dialog_achievement, null);

        EditText etTitle =
                dialogView.findViewById(R.id.etTitle);

        Button btnSave =
                dialogView.findViewById(R.id.btnSave);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();

        btnSave.setOnClickListener(v -> {

            String title =
                    etTitle.getText().toString().trim();

            if (title.isEmpty()) {

                Toast.makeText(
                        this,
                        "Введите название",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            saveAchievement(title, dialog);
        });

        dialog.show();
    }


    private void saveAchievement(String title,
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
                        currentDate
                );

        achievementsRef.child(achievementId)

                .setValue(achievement)

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Достижение добавлено",
                            Toast.LENGTH_SHORT
                    ).show();

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

                .setTitle("Удалить достижение?")

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
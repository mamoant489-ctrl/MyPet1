package com.example.mypet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

public class BasicMenu extends AppCompatActivity {

    private CardView cardProfileHeader, cardCommands, cardEating, cardStats, cardReminders;
    private CardView cardAchievements, cardWalkSearch, cardWalkTracker, cardMood;
    private CircularImageView ivPetAvatar;
    private TextView tvPetName;

    private FirebaseUser currentUser;
    private String currentPetId;
    private DatabaseReference petsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_menu);

        initViews();
        initFirebase();
        setupAllClickListeners();
        loadPetProfile();
    }

    private void initViews() {
        cardProfileHeader = findViewById(R.id.cardProfileHeader);
        ivPetAvatar = findViewById(R.id.ivPetAvatar);
        tvPetName = findViewById(R.id.tvPetName);

        cardCommands = findViewById(R.id.cardCommands);
        cardEating = findViewById(R.id.cardEating);
        cardStats = findViewById(R.id.cardPhisicDynamic);
        cardReminders = findViewById(R.id.cardRemaining);
        cardAchievements = findViewById(R.id.cardAchivments);
        cardWalkSearch = findViewById(R.id.cardWalking);
        cardWalkTracker = findViewById(R.id.cardTracker);
        cardMood = findViewById(R.id.cardMood);
    }

    private void initFirebase() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Не авторизованы", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        petsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("pets");
    }

    private void loadPetProfile() {
        petsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {

                    DataSnapshot petSnapshot = snapshot.getChildren().iterator().next();
                    currentPetId = petSnapshot.getKey();


                    DataSnapshot profileSnapshot = petSnapshot.child("profile");
                    if (profileSnapshot.exists()) {
                        String nickname = profileSnapshot.child("nickname").getValue(String.class);
                        String photoUrl = profileSnapshot.child("photoUrl").getValue(String.class);


                        tvPetName.setText(nickname != null ? nickname : "Кличка питомца");


                        if (photoUrl != null) {
                            Glide.with(BasicMenu.this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.usericon)  // Иконка по умолчанию
                                    .circleCrop()
                                    .into(ivPetAvatar);
                        }
                    }
                } else {
                    tvPetName.setText("Создайте профиль питомца");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BasicMenu.this, "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAllClickListeners() {

        cardProfileHeader.setOnClickListener(v -> {
            Intent intent = new Intent(BasicMenu.this, PetProfileActivity.class);
            intent.putExtra("petId", currentPetId);
            startActivity(intent);
        });

        cardCommands.setOnClickListener(v -> startActivity(new Intent(this, CommandsActivity.class)));
        cardEating.setOnClickListener(v -> startActivity(new Intent(this, FoodActivity.class)));
        cardStats.setOnClickListener(v -> startActivity(new Intent(this, DynamicsActivity.class)));
        cardReminders.setOnClickListener(v -> startActivity(new Intent(this, RemainingActivity.class)));
        cardAchievements.setOnClickListener(v -> startActivity(new Intent(this, AchievementsActivity.class)));
        cardWalkSearch.setOnClickListener(v -> startActivity(new Intent(this, WalkingActivity.class)));
        cardWalkTracker.setOnClickListener(v -> startActivity(new Intent(this, TrackerActivity.class)));
        cardMood.setOnClickListener(v -> startActivity(new Intent(this, MoodActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPetProfile();
    }
}

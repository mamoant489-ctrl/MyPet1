package com.example.mypet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

public class BasicMenu extends AppCompatActivity {

    // UI элементы
    private CardView cardProfileHeader, cardCommands, cardEating, cardStats, cardReminders;
    private CardView cardAchievements, cardWalkSearch, cardWalkTracker, cardMood;
    private CircularImageView ivPetAvatar;
    private TextView tvPetName;

    // Firebase
    private FirebaseUser currentUser;
    private String currentPetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_menu);

        initViews();
        initFirebase();
        setupAllClickListeners();
        loadPetProfileData();
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
    }


    private void setupAllClickListeners() {

        cardProfileHeader.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
        cardCommands.setOnClickListener(v -> startActivity(new Intent(this, CommandsActivity.class)));
        cardEating.setOnClickListener(v -> startActivity(new Intent(this, FoodActivity.class)));
        cardStats.setOnClickListener(v -> startActivity(new Intent(this, PhisicDynamicActivity.class)));
        cardReminders.setOnClickListener(v -> startActivity(new Intent(this, RemainingActivity.class)));
        cardAchievements.setOnClickListener(v -> startActivity(new Intent(this, AchievementsActivity.class)));
        cardWalkSearch.setOnClickListener(v -> startActivity(new Intent(this, WalkingActivity.class)));
        cardWalkTracker.setOnClickListener(v -> startActivity(new Intent(this, TrackerActivity.class)));
        cardMood.setOnClickListener(v -> startActivity(new Intent(this, MoodActivity.class)));
    }

    private void loadPetProfileData() {
        if (currentUser == null) {
            tvPetName.setText("Авторизуйтесь");
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference petsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets")
                .orderByKey()
                .limitToLast(1).getRef();

        petsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        currentPetId = child.getKey();
                        Pet pet = child.getValue(Pet.class);

                        if (pet != null) {
                            tvPetName.setText(pet.name != null ? pet.name + " " : "Без имени");
                        }
                        break;
                    }
                } else {
                    tvPetName.setText("Создайте профиль");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvPetName.setText("Ошибка загрузки");
                Toast.makeText(BasicMenu.this, "❌ " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPetProfileData();
    }
}

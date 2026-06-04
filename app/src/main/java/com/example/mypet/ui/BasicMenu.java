package com.example.mypet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.mypet.data.FirebasePaths;
import com.example.mypet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypet.adapters.PetSwitcherAdapter;
import com.example.mypet.models.PetModel;

import java.util.ArrayList;

public class BasicMenu extends AppCompatActivity {

    private CardView cardProfileHeader, cardCommands, cardEating, cardStats, cardReminders;
    private CardView cardAchievements, cardWalkSearch, cardWalkTracker, cardMood;
    private CircularImageView ivPetAvatar;
    private TextView tvPetName;

    private FirebaseUser currentUser;
    private String currentPetId;
    private DatabaseReference petsRef;

    private DrawerLayout drawerLayout;

    private ImageButton btnMenu;

    private TextView tvPetAge;
    private TextView tvPetBirth;
    private TextView tvEmail;

    private Button btnAddPet;
    private Button btnLogout;

    private RecyclerView rvPets;

    private ArrayList<PetModel> pets =
            new ArrayList<>();

    private PetSwitcherAdapter petAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_menu);

        initViews();
        initFirebase();
        setupAllClickListeners();
        loadPetProfile();
        setupPetRecycler();
        loadAllPets();
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
        cardWalkTracker = findViewById(R.id.cardTracker);
        cardMood = findViewById(R.id.cardMood);

        drawerLayout = findViewById(R.id.drawerLayout);

        btnMenu = findViewById(R.id.btnMenu);

        tvPetAge = findViewById(R.id.tvPetAge);
        tvPetBirth = findViewById(R.id.tvPetBirth);

        tvEmail = findViewById(R.id.tvEmail);

        btnLogout = findViewById(R.id.btnLogout);

        rvPets = findViewById(R.id.rvPets);
    }

    private void initFirebase() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Не авторизованы", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvEmail.setText(currentUser.getEmail());

        petsRef = FirebaseDatabase.getInstance()
                .getReference(FirebasePaths.USERS).child(currentUser.getUid()).child("pets");
    }

    private void loadPetProfile() {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(currentUser.getUid())
                .child("pets")
                .limitToFirst(1)

                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            return;
                        }

                        DataSnapshot pet =
                                snapshot.getChildren()
                                        .iterator()
                                        .next();

                        String name =
                                pet.child("name")
                                        .getValue(String.class);

                        String age =
                                pet.child("age")
                                        .getValue(String.class);

                        String birthDate =
                                pet.child("birthDate")
                                        .getValue(String.class);

                        tvPetName.setText(
                                name == null
                                        ? "Питомец"
                                        : name
                        );

                        tvPetAge.setText(
                                age == null
                                        ? "-"
                                        : age
                        );

                        tvPetBirth.setText(
                                birthDate == null
                                        ? "-"
                                        : birthDate
                        );
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                    }
                });
    }


    private void setupPetRecycler() {

        petAdapter =
                new PetSwitcherAdapter(
                        pets,
                        pet -> {

                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child("Users")
                                    .child(currentUser.getUid())
                                    .child("currentPetId")
                                    .setValue(pet.getId())

                                    .addOnSuccessListener(unused -> {

                                        currentPetId =
                                                pet.getId();

                                        loadPetProfile();

                                        drawerLayout.closeDrawers();

                                        Toast.makeText(
                                                this,
                                                "Выбран: "
                                                        + pet.getName(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    });
                        });

        rvPets.setLayoutManager(
                new LinearLayoutManager(this));

        rvPets.setAdapter(petAdapter);
    }

    private void loadAllPets() {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(currentUser.getUid())
                .child("pets")

                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                pets.clear();

                                for(DataSnapshot ds :
                                        snapshot.getChildren()) {

                                    String id =
                                            ds.getKey();

                                    String name =
                                            ds.child("name")
                                                    .getValue(String.class);

                                    String age =
                                            ds.child("age")
                                                    .getValue(String.class);

                                    String photo =
                                            ds.child("photoUrl")
                                                    .getValue(String.class);

                                    pets.add(
                                            new PetModel(
                                                    id,
                                                    name,
                                                    age,
                                                    photo
                                            ));
                                }

                                petAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                            }
                        });
    }

    private void setupAllClickListeners() {

        cardProfileHeader.setOnClickListener(v -> {
            Intent intent = new Intent(BasicMenu.this, PetProfileActivity.class);
            intent.putExtra("petId", currentPetId);
            startActivity(intent);
        });

        btnMenu.setOnClickListener(v -> {drawerLayout.openDrawer(GravityCompat.START);
        });
        btnLogout.setOnClickListener(v -> {FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(BasicMenu.this, LoginActivity.class));
            finishAffinity();
        });

        cardCommands.setOnClickListener(v -> startActivity(new Intent(this, CommandsActivity.class)));
        cardEating.setOnClickListener(v -> startActivity(new Intent(this, FoodActivity.class)));
        cardStats.setOnClickListener(v -> startActivity(new Intent(this, DynamicsActivity.class)));
        cardReminders.setOnClickListener(v -> startActivity(new Intent(this, ReminderActivity.class)));
        cardAchievements.setOnClickListener(v -> startActivity(new Intent(this, AchievementsActivity.class)));
        cardWalkTracker.setOnClickListener(v -> startActivity(new Intent(this, TrackerActivity.class)));
        cardMood.setOnClickListener(v -> startActivity(new Intent(this, MoodActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPetProfile();
    }
}
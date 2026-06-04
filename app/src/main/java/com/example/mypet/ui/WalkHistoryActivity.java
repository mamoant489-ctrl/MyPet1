package com.example.mypet.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mypet.adapters.WalkAdapter;
import com.example.mypet.data.FirebasePaths;
import com.example.mypet.models.Walk;
import com.example.mypet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class WalkHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WalkAdapter adapter;
    private List<Walk> walks = new ArrayList<>();
    private DatabaseReference walksRef;
    private String userId, petId;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_history);

        initFirebase();
        setupRecyclerView();
        initViews();
        setupClickListeners();
    }

    private void initFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        userId = user.getUid();

        DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference(FirebasePaths.USERS).child(userId).child("pets");
        petsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    petId = snapshot.getChildren().iterator().next().getKey();
                    walksRef = FirebaseDatabase.getInstance().getReference(FirebasePaths.USERS).child(userId).child("pets").child(petId).child("walks");
                    loadWalks();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WalkAdapter(walks, this::deleteWalk);
        recyclerView.setAdapter(adapter);
    }

    private void loadWalks() {
        walksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                walks.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Walk walk = child.getValue(Walk.class);
                    if (walk != null) {
                        walk.setId(child.getKey());
                        walks.add(walk);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void deleteWalk(String walkId) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить прогулку?")
                .setPositiveButton("Да", (dialog, which) -> walksRef.child(walkId).removeValue())
                .setNegativeButton("Нет", null)
                .show();
    }
}

package com.example.mypet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mypet.R;

public class FirstEnter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first_enter);
    }
    public void GoToProfile(View view) {
        Intent intent = new Intent(this, PetProfileActivity.class);
        startActivity(intent);
    }

}
package com.example.mypet;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;


public class FirstEnter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first_enter);
    }
    public void GoToProfile(View view) {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

}
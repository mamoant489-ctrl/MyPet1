package com.example.mypet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Profile extends AppCompatActivity {


    TextView currentDateTime;
    Calendar dateAndTime = Calendar.getInstance();


    private EditText etName, etAge, etBreed, etSex, etMark, etWeight, etHeight;
    private Button btnSavePet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);


        currentDateTime = findViewById(R.id.etBirthDate);
        setInitialDateTime();


        etName   = findViewById(R.id.etName);
        etAge    = findViewById(R.id.etAge);
        etBreed  = findViewById(R.id.etBreed);
        etSex    = findViewById(R.id.etSex);
        etMark   = findViewById(R.id.etMark);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);


        btnSavePet = findViewById(R.id.btnSavePet);
        btnSavePet.setOnClickListener(v -> savePet());
    }


    public void setDate(View v) {
        new DatePickerDialog(
                Profile.this,
                d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void setInitialDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String formatted = sdf.format(dateAndTime.getTime());
        currentDateTime.setText(formatted);
    }

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
        }
    };


    public void GoToMenu(View view) {
        Intent intent = new Intent(this, BasicMenu.class);
        startActivity(intent);
    }


    private void savePet() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        String name   = etName.getText().toString().trim();
        String age    = etAge.getText().toString().trim();
        String breed  = etBreed.getText().toString().trim();
        String birth  = currentDateTime.getText().toString().trim();
        String sex    = etSex.getText().toString().trim();
        String mark   = etMark.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String height = etHeight.getText().toString().trim();


        if (name.isEmpty()) {
            Toast.makeText(this, "Введите кличку", Toast.LENGTH_SHORT).show();
            return;
        }

        Pet pet = new Pet(name, age, breed, birth, sex, mark, weight, height);

        DatabaseReference petsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("pets");

        String petId = petsRef.push().getKey();
        petsRef.child(petId).setValue(pet)
                .addOnSuccessListener(unused ->
                        Toast.makeText(Profile.this, "Питомец сохранён", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(Profile.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

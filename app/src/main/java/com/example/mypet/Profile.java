package com.example.mypet;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class Profile extends AppCompatActivity {

    TextView currentDateTime;
    Calendar dateAndTime=Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        currentDateTime = findViewById(R.id.etBirthDate);
        setInitialDateTime();
    }

    public void setDate(View v) {
        new DatePickerDialog(Profile.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }
    private void setInitialDateTime() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String formatted = sdf.format(dateAndTime.getTime());
        currentDateTime.setText(formatted);
    }
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
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
}


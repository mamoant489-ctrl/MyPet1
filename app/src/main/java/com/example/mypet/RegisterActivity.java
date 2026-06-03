package com.example.mypet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mypet.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    private FirebaseAuth auth;

    private static final String TAG = "REGISTER_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);

        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return insets;
                });

        binding.singUpBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser(){

        String email =
                binding.emailEt.getText().toString().trim();

        String password =
                binding.passwordEt.getText().toString().trim();

        String username =
                binding.usernameEt.getText().toString().trim();

        Log.d(TAG, "Кнопка регистрации нажата");

        if(email.isEmpty() ||
                password.isEmpty() ||
                username.isEmpty()){

            Toast.makeText(this,
                    "Заполните все поля",
                    Toast.LENGTH_SHORT).show();

            Log.e(TAG, "Пустые поля");

            return;
        }

        if(password.length() < 6){

            Toast.makeText(this,
                    "Пароль минимум 6 символов",
                    Toast.LENGTH_SHORT).show();

            Log.e(TAG, "Короткий пароль");

            return;
        }

        Log.d(TAG, "Начало регистрации Firebase");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        Log.d(TAG, "Firebase регистрация успешна");

                        if(auth.getCurrentUser() == null){

                            Log.e(TAG,
                                    "CurrentUser == null");

                            Toast.makeText(this,
                                    "Ошибка пользователя",
                                    Toast.LENGTH_SHORT).show();

                            return;
                        }

                        String uid = auth.getCurrentUser().getUid();

                        HashMap<String, String> userInfo = new HashMap<>();

                        userInfo.put("email", email);
                        userInfo.put("username", username);

                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Users")
                                .child(uid)
                                .setValue(userInfo)

                                .addOnSuccessListener(unused -> {

                                    Log.d(TAG, "Данные сохранены");

                                    Toast.makeText(RegisterActivity.this,
                                            "Регистрация успешна",
                                            Toast.LENGTH_SHORT).show();

                                    Intent intent =
                                            new Intent(RegisterActivity.this,
                                                    FirstEnter.class);

                                    startActivity(intent);

                                    finish();
                                })

                                .addOnFailureListener(e -> {

                                    Log.e(TAG,
                                            "Ошибка БД: " + e.getMessage());

                                    Toast.makeText(RegisterActivity.this,
                                            e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    }else{

                        Log.e(TAG,
                                "Ошибка регистрации: "
                                        + task.getException());

                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void GoToLogin(View view) {

        Intent intent =
                new Intent(this,
                        LoginActivity.class);

        startActivity(intent);
    }
}
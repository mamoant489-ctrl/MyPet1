package com.example.mypet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.mypet.data.FirebasePaths;
import com.example.mypet.databinding.ActivityRegisterBinding;
import com.example.mypet.R;
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

        setRegistrationInProgress(true);

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

                            setRegistrationInProgress(false);

                            return;
                        }

                        String uid = auth.getCurrentUser().getUid();

                        HashMap<String, String> userInfo = new HashMap<>();

                        userInfo.put("email", email);
                        userInfo.put("username", username);

                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child(FirebasePaths.USERS)
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

                                    setRegistrationInProgress(false);
                                });

                    }else{

                        Log.e(TAG,
                                "Ошибка регистрации: "
                                        + task.getException());

                        Toast.makeText(this,
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Ошибка регистрации",
                                Toast.LENGTH_LONG).show();

                        setRegistrationInProgress(false);
                    }
                });
    }

    private void setRegistrationInProgress(boolean inProgress) {
        binding.singUpBtn.setEnabled(!inProgress);
        binding.singUpBtn.setAlpha(inProgress ? 0.6f : 1f);
    }

    public void GoToLogin(View view) {

        Intent intent =
                new Intent(this,
                        LoginActivity.class);

        startActivity(intent);
    }
}

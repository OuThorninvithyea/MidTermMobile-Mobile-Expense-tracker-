package com.shawn.andrioduimidterm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.shawn.andrioduimidterm.database.AppDatabase;
import com.shawn.andrioduimidterm.database.User;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etPassword;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            // Check if email already exists
            User existingUser = db.userDao().getUserByEmail(email);
            if (existingUser != null) {
                runOnUiThread(() -> 
                    Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // Create new user
            User newUser = new User(fullName, email, password);
            long userId = db.userDao().insertUser(newUser);

            runOnUiThread(() -> {
                if (userId > 0) {
                    Toast.makeText(this, "Registration successful! Please login", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Registration failed. Please try again", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}

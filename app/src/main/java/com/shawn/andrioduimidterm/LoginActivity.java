package com.shawn.andrioduimidterm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.shawn.andrioduimidterm.database.AppDatabase;
import com.shawn.andrioduimidterm.database.User;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> login());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            User user = db.userDao().login(email, password);
            runOnUiThread(() -> {
                if (user != null) {
                    // Save session
                    SharedPreferences prefs = getSharedPreferences("ExpenseTracker", MODE_PRIVATE);
                    prefs.edit()
                            .putInt("user_id", user.getUser_id())
                            .putString("user_name", user.getFull_name())
                            .putString("user_email", user.getEmail())
                            .apply();

                    Toast.makeText(this, "Welcome " + user.getFull_name(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}

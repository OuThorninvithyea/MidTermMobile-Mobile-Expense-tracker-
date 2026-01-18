package com.shawn.andrioduimidterm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("ExpenseTracker", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);

            Intent intent;
            if (userId != -1) {
                // User is logged in
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // User not logged in
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000); // 2 second delay
    }
}

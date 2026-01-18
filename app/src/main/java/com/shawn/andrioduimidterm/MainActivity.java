package com.shawn.andrioduimidterm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shawn.andrioduimidterm.database.AppDatabase;
import com.shawn.andrioduimidterm.database.ExpenseEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int userId;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("ExpenseTracker", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(HomeFragment.newInstance(userId));
                return true;
            } else if (id == R.id.nav_charts) {
                switchFragment(new ChartsFragment());
                return true;
            } else if (id == R.id.nav_budget) {
                switchFragment(new BudgetFragment());
                return true;
            } else if (id == R.id.nav_settings) {
                switchFragment(new SettingsFragment());
                return true;
            }
            return false;
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddExpenseActivity.class));
        });

        // Set default fragment
        if (savedInstanceState == null) {
            switchFragment(HomeFragment.newInstance(userId));
        }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
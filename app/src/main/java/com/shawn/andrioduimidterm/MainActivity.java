package com.shawn.andrioduimidterm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
    private HomeFragment homeFragment;
    private ChartsFragment chartsFragment;
    private BudgetFragment budgetFragment;
    private SettingsFragment settingsFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply saved theme preference before setting content view
        SharedPreferences prefs = getSharedPreferences("ExpenseTracker", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize fragments once
        homeFragment = HomeFragment.newInstance(userId);
        chartsFragment = new ChartsFragment();
        budgetFragment = new BudgetFragment();
        settingsFragment = new SettingsFragment();

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (id == R.id.nav_charts) {
                switchFragment(chartsFragment);
                return true;
            } else if (id == R.id.nav_budget) {
                switchFragment(budgetFragment);
                return true;
            } else if (id == R.id.nav_settings) {
                switchFragment(settingsFragment);
                return true;
            }
            return false;
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddExpenseActivity.class));
        });

        // Set default fragment
        if (savedInstanceState == null) {
            switchFragment(homeFragment);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh home fragment data when returning from AddExpenseActivity
        if (homeFragment != null && currentFragment == homeFragment) {
            homeFragment.refreshData();
        }
    }

    private void switchFragment(Fragment fragment) {
        if (currentFragment == fragment) {
            return; // Already showing this fragment
        }
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        currentFragment = fragment;
    }
}
package com.shawn.andrioduimidterm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    
    private SwitchCompat switchDarkMode;
    private SharedPreferences prefs;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        prefs = requireActivity().getSharedPreferences("ExpenseTracker", requireContext().MODE_PRIVATE);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        
        // Load current theme preference
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);
        
        // Set up toggle listener
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            
            // Apply theme
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
        
        return view;
    }
}

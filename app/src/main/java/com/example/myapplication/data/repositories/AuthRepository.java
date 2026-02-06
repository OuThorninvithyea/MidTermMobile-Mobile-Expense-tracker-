package com.example.myapplication.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.models.LoginResult;
import com.example.myapplication.models.SignupResult;
import com.example.myapplication.models.User;

public class AuthRepository {
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private Context context;

    public AuthRepository(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("ExpenseTracker", Context.MODE_PRIVATE);
        this.dbHelper = new DatabaseHelper(context);
    }

    public LoginResult login(String username, String password) {
        User user = dbHelper.login(username, password);
        if (user != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("userId", user.id);
            editor.putString("username", user.username);
            editor.apply();
            return new LoginResult(true, user, null);
        }
        return new LoginResult(false, null, "Invalid username or password");
    }

    public SignupResult signup(String username, String password, String pet) {
        long userId = dbHelper.signup(username, password, pet);
        if (userId > 0) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("userId", (int) userId);
            editor.putString("username", username.trim());
            editor.apply();
            User user = new User((int) userId, username.trim());
            return new SignupResult(true, user, null);
        }
        if (userId == -2) {
            return new SignupResult(false, null, "Username already exists. Please choose a different username.");
        } else if (userId == -1) {
            return new SignupResult(false, null, "Database error occurred. Please try again.");
        }
        return new SignupResult(false, null, "Signup failed. Please try again.");
    }

    public User getCurrentUser() {
        int userId = prefs.getInt("userId", -1);
        String username = prefs.getString("username", null);
        
        if (userId > 0 && username != null) {
            if (dbHelper.checkUserExists(userId)) {
                return new User(userId, username);
            } else {
                logout();
                return null;
            }
        }
        return null;
    }

    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public boolean resetPassword(String username, String pet, String newPassword) {
        return dbHelper.resetPassword(username, pet, newPassword);
    }
    
    public boolean updateUsername(int userId, String newUsername) {
        boolean success = dbHelper.updateUsername(userId, newUsername);
        if (success) {
             prefs.edit().putString("username", newUsername).apply();
        }
        return success;
    }

    public boolean updatePassword(int userId, String currentPassword, String newPassword) {
        return dbHelper.updatePassword(userId, currentPassword, newPassword);
    }
    
    public void resetDatabase() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        dbHelper.resetDatabase(context);
    }
}

package com.example.myapplication.services;

import android.content.Context;
import com.example.myapplication.data.DataManager;
import com.example.myapplication.models.LoginResult;
import com.example.myapplication.models.SignupResult;
import com.example.myapplication.models.User;

/**
 * AuthService
 * 
 * Service layer for Authentication.
 * Handles business logic such as input validation before interacting with the Repository (DataManager).
 */
public class AuthService {
    private DataManager dataManager;

    public AuthService(Context context) {
        this.dataManager = DataManager.getInstance(context);
    }

    public LoginResult login(String username, String password) {
        return dataManager.login(username, password);
    }

    /**
     * Registers a new user with input validation.
     * 
     * @param username The desired username
     * @param password The password (must be >= 3 chars)
     * @param pet Security answer
     * @return SignupResult containing success status or error message
     */
    public SignupResult signup(String username, String password, String pet) {
        // Validation logic moved from DataManager (Business Logic)
        if (username == null || username.trim().isEmpty()) {
            return new SignupResult(false, null, "Username is required");
        }
        if (password == null || password.length() < 3) {
            return new SignupResult(false, null, "Password must be at least 3 characters");
        }
        if (pet == null || pet.trim().isEmpty()) {
            return new SignupResult(false, null, "Security answer is required");
        }
        return dataManager.signup(username, password, pet);
    }

    public boolean resetPassword(String username, String pet, String newPassword) {
        return dataManager.resetPassword(username, pet, newPassword);
    }

    public User getCurrentUser() {
        return dataManager.getCurrentUser();
    }

    public void logout() {
        dataManager.logout();
    }
    
    public boolean updateUsername(String newUsername) {
        return dataManager.updateUsername(newUsername);
    }
    
    public boolean updatePassword(String currentPassword, String newPassword) {
        return dataManager.updatePassword(currentPassword, newPassword);
    }

    public void resetDatabase() {
        dataManager.resetDatabase();
    }
}

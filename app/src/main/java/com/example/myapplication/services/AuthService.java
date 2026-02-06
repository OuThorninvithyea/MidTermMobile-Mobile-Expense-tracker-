package com.example.myapplication.services;

import android.content.Context;
import com.example.myapplication.data.repositories.AuthRepository;
import com.example.myapplication.models.LoginResult;
import com.example.myapplication.models.SignupResult;
import com.example.myapplication.models.User;

/**
 * AuthService
 * 
 * Service layer for Authentication.
 * Handles business logic such as input validation before interacting with the Repository.
 */
public class AuthService {
    private AuthRepository authRepository;

    public AuthService(Context context) {
        this.authRepository = new AuthRepository(context);
    }

    public LoginResult login(String username, String password) {
        return authRepository.login(username, password);
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
        if (username == null || username.trim().isEmpty()) {
            return new SignupResult(false, null, "Username is required");
        }
        if (password == null || password.length() < 3) {
            return new SignupResult(false, null, "Password must be at least 3 characters");
        }
        if (pet == null || pet.trim().isEmpty()) {
            return new SignupResult(false, null, "Security answer is required");
        }
        return authRepository.signup(username, password, pet);
    }

    public boolean resetPassword(String username, String pet, String newPassword) {
        return authRepository.resetPassword(username, pet, newPassword);
    }

    public User getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public void logout() {
        authRepository.logout();
    }
    
    public boolean updateUsername(String newUsername) {
        User currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            return authRepository.updateUsername(currentUser.id, newUsername);
        }
        return false;
    }
    
    public boolean updatePassword(String currentPassword, String newPassword) {
        User currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            return authRepository.updatePassword(currentUser.id, currentPassword, newPassword);
        }
        return false;
    }

    public void resetDatabase() {
        authRepository.resetDatabase();
    }
}

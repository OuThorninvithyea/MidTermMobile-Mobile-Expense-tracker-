package com.example.myapplication.handlers;

import android.content.Context;
import com.example.myapplication.models.LoginResult;
import com.example.myapplication.models.SignupResult;
import com.example.myapplication.models.User;
import com.example.myapplication.services.AuthService;

/**
 * AuthHandler
 * 
 * Presentation Layer Handler for Authentication.
 * Acts as an intermediary between UI components (Activities/Fragments) and the Service Layer (AuthService).
 * Decouples the UI from business logic and data access.
 */
public class AuthHandler {
    private AuthService authService;

    public AuthHandler(Context context) {
        this.authService = new AuthService(context);
    }

    public LoginResult handleLogin(String username, String password) {
        return authService.login(username, password);
    }

    public SignupResult handleSignup(String username, String password, String pet) {
        return authService.signup(username, password, pet);
    }

    public boolean handleResetPassword(String username, String pet, String newPassword) {
        return authService.resetPassword(username, pet, newPassword);
    }

    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public void handleLogout() {
        authService.logout();
    }
    
    public boolean handleUpdateUsername(String newUsername) {
        return authService.updateUsername(newUsername);
    }
    
    public boolean handleUpdatePassword(String currentPassword, String newPassword) {
        return authService.updatePassword(currentPassword, newPassword);
    }

    public void handleResetDatabase() {
        authService.resetDatabase();
    }
}

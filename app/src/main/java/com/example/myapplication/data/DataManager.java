package com.example.myapplication.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import com.example.myapplication.models.*;

/**
 * DataManager
 * 
 * CORE REPOSITORY (Data Access Layer).
 * 
 * Role:
 * - Provides low-level data access to `DatabaseHelper` (SQLite) and `SharedPreferences`.
 * - DOES NOT contain business logic (validation, comparisons, etc. are moved to Services).
 * - Implements Singleton pattern to ensure a single data source entry point.
 * 
 * Architecture:
 * - Accessed ONLY by Services (AuthService, ExpenseService, etc.).
 * - Should NOT be accessed directly by Activities/Fragments.
 */
public class DataManager {
    private static DataManager instance;
    private DatabaseHelper dbHelper;
    private static final String KEY_CATEGORIES = "categories_list";
    private static final Set<String> DEFAULT_CATEGORIES = new HashSet<>(Arrays.asList(
        "Food", "Transport", "Shopping", "Bills", "Entertainment", "Others"
    ));

    private SharedPreferences prefs;
    private Context context;

    /**
     * Private constructor to enforce Singleton pattern.
     * Initializes the database helper and shared preferences.
     * 
     * @param context Application context
     */
    private DataManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("ExpenseTracker", Context.MODE_PRIVATE);
        
        // Initialize database helper
        try {
            this.dbHelper = new DatabaseHelper(context);
            android.util.Log.d("DataManager", "DatabaseHelper initialized");
        } catch (Exception e) {
            android.util.Log.e("DataManager", "Error initializing DatabaseHelper: " + e.getMessage(), e);
            // Try to recover by deleting and recreating
            try {
                context.deleteDatabase("expense_tracker.db");
                this.dbHelper = new DatabaseHelper(context);
            } catch (Exception e2) {
                android.util.Log.e("DataManager", "Failed to recover database: " + e2.getMessage(), e2);
            }
        }
    }
    
    // Public method to reset database
    public void resetDatabase() {
        android.util.Log.d("DataManager", "Resetting database via DataManager...");
        // Clear SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        // Reset database
        dbHelper.resetDatabase(context);
        
        android.util.Log.d("DataManager", "Database reset completed");
    }

    /**
     * Public method to get the singleton instance of DataManager.
     * Uses double-checked locking (if synchronized) or simple null check here for thread safety context.
     *
     * @param context Application context needed for database and prefs initialization
     * @return The single instance of DataManager
     */
    /**
     * Public method to get the singleton instance of DataManager.
     * Ensures only one instance of DataManager is created (Singleton Pattern).
     * 
     * @param context Application context needed for database and prefs initialization
     * @return The single instance of DataManager
     */
    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            // Use application context to avoid memory leaks if activity context is passed
            instance = new DataManager(context.getApplicationContext());
        }
        return instance;
    }

    // Authentication methods
    /**
     * Authenticates a user with the provided credentials.
     *
     * @param username The username input
     * @param password The password input
     * @return LoginResult containing success status, user object, or error message
     */
    public LoginResult login(String username, String password) {
        android.util.Log.d("DataManager", "Login attempt for: " + username);
        User user = dbHelper.login(username, password);
        if (user != null) {
            // Save login session to SharedPreferences to keep user logged in
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("userId", user.id);
            editor.putString("username", user.username);
            editor.apply();
            android.util.Log.d("DataManager", "Login success, saving user to prefs");
            return new LoginResult(true, user, null);
        }
        android.util.Log.e("DataManager", "Login failed for: " + username);
        return new LoginResult(false, null, "Invalid username or password");
    }

    /**
     * Register a new user.
     * 
     * @param username Desired username
     * @param password Password (will be hashed)
     * @param pet Security answer for password recovery
     * @return SignupResult containing success status and user data or error message
     */
    public SignupResult signup(String username, String password, String pet) {
        android.util.Log.d("DataManager", "Signup attempt for: " + username);
        
        // Input validation is now handled in AuthService or DB layer


        long userId = dbHelper.signup(username, password, pet);
        if (userId > 0) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("userId", (int) userId);
            editor.putString("username", username.trim());
            editor.apply();
            android.util.Log.d("DataManager", "Signup success, user ID: " + userId);
            User user = new User((int) userId, username.trim());
            return new SignupResult(true, user, null);
        }
        android.util.Log.e("DataManager", "Signup failed: Database returned userId: " + userId);
        // Check if it's a duplicate username or other error
        if (userId == -2) {
            return new SignupResult(false, null, "Username already exists. Please choose a different username.");
        } else if (userId == -1) {
            return new SignupResult(false, null, "Database error occurred. Please try again.");
        }
        return new SignupResult(false, null, "Signup failed. Please try again.");
    }
// ...
    public User getCurrentUser() {
        int userId = prefs.getInt("userId", -1);
        String username = prefs.getString("username", null);
        
        if (userId > 0 && username != null) {
            // Verify user actually exists in DB (in case of DB reset)
            if (dbHelper.checkUserExists(userId)) {
                return new User(userId, username);
            } else {
                // User in prefs but not in DB - likely DB was reset
                android.util.Log.w("DataManager", "User found in prefs but not in DB. Logging out.");
                logout(); // Clear invalid prefs
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

    // Expense Methods
    public long addExpense(String category, double amount, String note, String date, String imageUri) {
        User user = getCurrentUser();
        if (user == null) return -1;
        return dbHelper.addExpense(user.id, category, amount, note, date, imageUri);
    }

    public List<Expense> getExpenses() {
        User user = getCurrentUser();
        List<Expense> expenses = new ArrayList<>();
        if (user == null) return expenses;

        String json = dbHelper.getExpenses(user.id);
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Expense expense = new Expense(
                    obj.getInt("id"),
                    obj.getString("category"),
                    obj.getDouble("amount"),
                    obj.optString("note", ""),
                    obj.optString("date", ""),
                    obj.optString("imageUri", null)
                );
                expenses.add(expense);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return expenses;
    }

    public boolean updateExpense(int expenseId, String category, double amount, String note, String date, String imageUri) {
         return dbHelper.updateExpense(expenseId, category, amount, note, date, imageUri);
    }
    
    public boolean deleteExpense(int expenseId) {
        return dbHelper.deleteExpense(expenseId);
    }

    public boolean clearExpenses() {
        User user = getCurrentUser();
        if (user == null) return false;
        return dbHelper.clearExpenses(user.id);
    }

    // Budget Methods
    public boolean setBudget(String category, double limit) {
        User user = getCurrentUser();
        if (user == null) return false;
        return dbHelper.setBudget(user.id, category, limit);
    }

    public List<Budget> getBudgets() {
        User user = getCurrentUser();
        List<Budget> budgets = new ArrayList<>();
        if (user == null) return budgets;

        String json = dbHelper.getBudgets(user.id);
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Budget budget = new Budget(
                    obj.getString("category"),
                    obj.getDouble("limit")
                );
                budgets.add(budget);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return budgets;
    }

    public boolean deleteBudget(String category) {
        User user = getCurrentUser();
        if (user == null) return false;
        return dbHelper.deleteBudget(user.id, category);
    }

    public boolean resetPassword(String username, String pet, String newPassword) {
        return dbHelper.resetPassword(username, pet, newPassword);
    }
    
    public boolean updateUsername(String newUsername) {
        User user = getCurrentUser();
        if (user == null) return false;
        boolean success = dbHelper.updateUsername(user.id, newUsername);
        if (success) {
             prefs.edit().putString("username", newUsername).apply();
        }
        return success;
    }

    public boolean updatePassword(String currentPassword, String newPassword) {
        User user = getCurrentUser();
        if (user == null) return false;
        return dbHelper.updatePassword(user.id, currentPassword, newPassword);
    }

    public List<String> getCategories() {
        Set<String> categories = prefs.getStringSet(KEY_CATEGORIES, null);
        if (categories == null) {
            categories = DEFAULT_CATEGORIES;
            prefs.edit().putStringSet(KEY_CATEGORIES, categories).apply();
        }
        return new ArrayList<>(categories);
    }
    
    public boolean addCategory(String category) {
        // Need to fetch fresh set to allow modification and saving
        Set<String> oldCategories = prefs.getStringSet(KEY_CATEGORIES, null);
        Set<String> categories = oldCategories != null ? new HashSet<>(oldCategories) : new HashSet<>(DEFAULT_CATEGORIES);
        
        if (categories.add(category)) {
            prefs.edit().putStringSet(KEY_CATEGORIES, categories).apply();
            return true;
        }
        return false;
    }

    public boolean deleteCategory(String category) {
        Set<String> oldCategories = prefs.getStringSet(KEY_CATEGORIES, null);
        Set<String> categories = oldCategories != null ? new HashSet<>(oldCategories) : new HashSet<>(DEFAULT_CATEGORIES);

        if (categories.remove(category)) {
            prefs.edit().putStringSet(KEY_CATEGORIES, categories).apply();
            return true;
        }
        return false;
    }





}

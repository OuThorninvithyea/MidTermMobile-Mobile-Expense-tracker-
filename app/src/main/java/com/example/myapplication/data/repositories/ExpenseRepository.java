package com.example.myapplication.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.models.Expense;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpenseRepository {
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private static final String KEY_CATEGORIES = "categories_list";
    private static final Set<String> DEFAULT_CATEGORIES = new HashSet<>(Arrays.asList(
        "Food", "Transport", "Shopping", "Bills", "Entertainment", "Others"
    ));

    public ExpenseRepository(Context context) {
        this.prefs = context.getSharedPreferences("ExpenseTracker", Context.MODE_PRIVATE);
        this.dbHelper = new DatabaseHelper(context);
    }

    public long addExpense(int userId, String category, double amount, String note, String date, String imageUri) {
        return dbHelper.addExpense(userId, category, amount, note, date, imageUri);
    }

    public List<Expense> getExpenses(int userId) {
        List<Expense> expenses = new ArrayList<>();
        String json = dbHelper.getExpenses(userId);
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

    public boolean clearExpenses(int userId) {
        return dbHelper.clearExpenses(userId);
    }

    // Category Management
    public List<String> getCategories() {
        Set<String> categories = prefs.getStringSet(KEY_CATEGORIES, null);
        if (categories == null) {
            categories = DEFAULT_CATEGORIES;
            prefs.edit().putStringSet(KEY_CATEGORIES, categories).apply();
        }
        return new ArrayList<>(categories);
    }
    
    public boolean addCategory(String category) {
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

package com.example.myapplication.data.repositories;

import android.content.Context;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.models.Budget;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class BudgetRepository {
    private DatabaseHelper dbHelper;

    public BudgetRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public boolean setBudget(int userId, String category, double limit) {
        return dbHelper.setBudget(userId, category, limit);
    }

    public List<Budget> getBudgets(int userId) {
        List<Budget> budgets = new ArrayList<>();
        String json = dbHelper.getBudgets(userId);
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

    public boolean deleteBudget(int userId, String category) {
        return dbHelper.deleteBudget(userId, category);
    }
}

package com.example.myapplication.services;

import android.content.Context;
import com.example.myapplication.data.DataManager;
import com.example.myapplication.models.Budget;
import java.util.List;

/**
 * BudgetService
 * 
 * Service layer for Budget Management.
 * Handles business logic related to budgets.
 * Currently primarily delegates to Repository, but scalable for future complex budget rules.
 */
public class BudgetService {
    private DataManager dataManager;

    public BudgetService(Context context) {
        this.dataManager = DataManager.getInstance(context);
    }

    public boolean setBudget(String category, double limit) {
        return dataManager.setBudget(category, limit);
    }

    public List<Budget> getBudgets() {
        return dataManager.getBudgets();
    }

    public boolean deleteBudget(String category) {
        return dataManager.deleteBudget(category);
    }
}

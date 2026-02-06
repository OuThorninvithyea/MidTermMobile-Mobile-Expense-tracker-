package com.example.myapplication.handlers;

import android.content.Context;
import com.example.myapplication.models.Budget;
import com.example.myapplication.services.BudgetService;
import java.util.List;

/**
 * BudgetHandler
 * 
 * Presentation Layer Handler for Budget Management.
 * Acts as the bridge between UI (Fragments) and BudgetService.
 */
public class BudgetHandler {
    private BudgetService budgetService;

    public BudgetHandler(Context context) {
        this.budgetService = new BudgetService(context);
    }

    public boolean handleSetBudget(String category, double limit) {
        return budgetService.setBudget(category, limit);
    }

    public List<Budget> getBudgets() {
        return budgetService.getBudgets();
    }

    public boolean handleDeleteBudget(String category) {
        return budgetService.deleteBudget(category);
    }
}

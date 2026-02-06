package com.example.myapplication.handlers;

import android.content.Context;
import com.example.myapplication.models.Expense;
import com.example.myapplication.models.BudgetCheckResult;
import com.example.myapplication.services.ExpenseService;
import java.util.List;

/**
 * ExpenseHandler
 * 
 * Presentation Layer Handler for Expense Management.
 * Acts as the bridge between UI (Fragments) and ExpenseService.
 * Manages UI-related data flow for expenses and categories.
 */
public class ExpenseHandler {
    private ExpenseService expenseService;

    public ExpenseHandler(Context context) {
        this.expenseService = new ExpenseService(context);
    }

    public long handleAddExpense(String category, double amount, String note, String date, String imageUri) {
        return expenseService.addExpense(category, amount, note, date, imageUri);
    }

    public List<Expense> getExpenses() {
        return expenseService.getExpenses();
    }

    public boolean handleUpdateExpense(int expenseId, String category, double amount, String note, String date, String imageUri) {
        return expenseService.updateExpense(expenseId, category, amount, note, date, imageUri);
    }

    public boolean handleDeleteExpense(int expenseId) {
        return expenseService.deleteExpense(expenseId);
    }

    public boolean handleClearExpenses() {
        return expenseService.clearExpenses();
    }

    public List<String> getCategories() {
        return expenseService.getCategories();
    }

    public boolean handleAddCategory(String category) {
        return expenseService.addCategory(category);
    }

    public boolean handleDeleteCategory(String category) {
        return expenseService.deleteCategory(category);
    }

    public BudgetCheckResult checkBudget(String category, double amount) {
        return expenseService.checkBudget(category, amount);
    }

    public BudgetCheckResult checkBudgetOnUpdate(String category, double newAmount, int expenseId) {
        return expenseService.checkBudgetOnUpdate(category, newAmount, expenseId);
    }
}

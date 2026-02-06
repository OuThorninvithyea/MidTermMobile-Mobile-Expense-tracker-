package com.example.myapplication.services;

import android.content.Context;
import com.example.myapplication.data.DataManager;
import com.example.myapplication.models.Expense;
import com.example.myapplication.models.BudgetCheckResult;
import java.util.List;

public class ExpenseService {
    private DataManager dataManager;

    public ExpenseService(Context context) {
        this.dataManager = DataManager.getInstance(context);
    }

    public long addExpense(String category, double amount, String note, String date, String imageUri) {
        return dataManager.addExpense(category, amount, note, date, imageUri);
    }

    public List<Expense> getExpenses() {
        return dataManager.getExpenses();
    }

    public boolean updateExpense(int expenseId, String category, double amount, String note, String date, String imageUri) {
        return dataManager.updateExpense(expenseId, category, amount, note, date, imageUri);
    }

    public boolean deleteExpense(int expenseId) {
        return dataManager.deleteExpense(expenseId);
    }

    public boolean clearExpenses() {
        return dataManager.clearExpenses();
    }

    public List<String> getCategories() {
        return dataManager.getCategories();
    }

    public boolean addCategory(String category) {
        return dataManager.addCategory(category);
    }

    public boolean deleteCategory(String category) {
        return dataManager.deleteCategory(category);
    }

    public BudgetCheckResult checkBudget(String category, double amount) {
        // Business logic moved from DataManager
        List<com.example.myapplication.models.Budget> budgets = dataManager.getBudgets();
        
        com.example.myapplication.models.Budget budget = null;
        for (com.example.myapplication.models.Budget b : budgets) {
            if (b.category.equals(category)) {
                budget = b;
                break;
            }
        }
        
        if (budget == null) {
            return new BudgetCheckResult(false, 0, 0, 0);
        }
        
        List<Expense> expenses = dataManager.getExpenses();
        double totalSpent = 0;
        for (Expense expense : expenses) {
            if (expense.category.equals(category)) {
                totalSpent += expense.amount;
            }
        }
        
        double newTotal = totalSpent + amount;
        boolean exceedsBudget = newTotal >= budget.limit;
        
        return new BudgetCheckResult(exceedsBudget, budget.limit, totalSpent, newTotal);
    }

    public BudgetCheckResult checkBudgetOnUpdate(String category, double newAmount, int expenseId) {
        // Business logic moved from DataManager
        List<com.example.myapplication.models.Budget> budgets = dataManager.getBudgets();
        
        com.example.myapplication.models.Budget budget = null;
        for (com.example.myapplication.models.Budget b : budgets) {
            if (b.category.equals(category)) {
                budget = b;
                break;
            }
        }
        
        if (budget == null) {
            return new BudgetCheckResult(false, 0, 0, 0);
        }
        
        List<Expense> expenses = dataManager.getExpenses();
        double totalSpent = 0;
        for (Expense expense : expenses) {
            if (expense.category.equals(category) && expense.id != expenseId) {
                totalSpent += expense.amount;
            }
        }
        
        double newTotal = totalSpent + newAmount;
        boolean exceedsBudget = newTotal >= budget.limit;
        
        return new BudgetCheckResult(exceedsBudget, budget.limit, totalSpent, newTotal);
    }
}

package com.example.myapplication.services;

import android.content.Context;
import com.example.myapplication.data.repositories.AuthRepository;
import com.example.myapplication.data.repositories.BudgetRepository;
import com.example.myapplication.data.repositories.ExpenseRepository;
import com.example.myapplication.models.Expense;
import com.example.myapplication.models.BudgetCheckResult;
import com.example.myapplication.models.User;
import java.util.List;
import java.util.ArrayList;

public class ExpenseService {
    private ExpenseRepository expenseRepository;
    private AuthRepository authRepository;
    private BudgetRepository budgetRepository;

    public ExpenseService(Context context) {
        this.expenseRepository = new ExpenseRepository(context);
        this.authRepository = new AuthRepository(context);
        this.budgetRepository = new BudgetRepository(context);
    }

    public long addExpense(String category, double amount, String note, String date, String imageUri) {
        User currentUser = authRepository.getCurrentUser();
        if (currentUser == null) return -1;
        return expenseRepository.addExpense(currentUser.id, category, amount, note, date, imageUri);
    }

    public List<Expense> getExpenses() {
        User currentUser = authRepository.getCurrentUser();
        if (currentUser == null) return new ArrayList<>();
        return expenseRepository.getExpenses(currentUser.id);
    }

    public boolean updateExpense(int expenseId, String category, double amount, String note, String date, String imageUri) {
        return expenseRepository.updateExpense(expenseId, category, amount, note, date, imageUri);
    }

    public boolean deleteExpense(int expenseId) {
        return expenseRepository.deleteExpense(expenseId);
    }

    public boolean clearExpenses() {
        User currentUser = authRepository.getCurrentUser();
        if (currentUser == null) return false;
        return expenseRepository.clearExpenses(currentUser.id);
    }

    public List<String> getCategories() {
        return expenseRepository.getCategories();
    }

    public boolean addCategory(String category) {
        return expenseRepository.addCategory(category);
    }

    public boolean deleteCategory(String category) {
        return expenseRepository.deleteCategory(category);
    }

    public BudgetCheckResult checkBudget(String category, double amount) {
        User currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
             return new BudgetCheckResult(false, 0, 0, 0);
        }
        
        List<com.example.myapplication.models.Budget> budgets = budgetRepository.getBudgets(currentUser.id);
        
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
        
        List<Expense> expenses = expenseRepository.getExpenses(currentUser.id);
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
        User currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
             return new BudgetCheckResult(false, 0, 0, 0);
        }
        
        List<com.example.myapplication.models.Budget> budgets = budgetRepository.getBudgets(currentUser.id);
        
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
        
        List<Expense> expenses = expenseRepository.getExpenses(currentUser.id);
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

package com.example.myapplication.services;

import android.content.Context;
import com.example.myapplication.data.repositories.AuthRepository;
import com.example.myapplication.data.repositories.BudgetRepository;
import com.example.myapplication.models.Budget;
import com.example.myapplication.models.User;
import java.util.List;
import java.util.ArrayList;

/**
 * BudgetService
 * 
 * Service layer for Budget Management.
 * Handles business logic related to budgets.
 * Delegates to BudgetRepository but fetches user context first.
 */
public class BudgetService {
    private BudgetRepository budgetRepository;
    private AuthRepository authRepository;

    public BudgetService(Context context) {
        this.budgetRepository = new BudgetRepository(context);
        this.authRepository = new AuthRepository(context);
    }

    public boolean setBudget(String category, double limit) {
        User currentUser = authRepository.getCurrentUser();
        if (currentUser == null) return false;
        return budgetRepository.setBudget(currentUser.id, category, limit);
    }

    public List<Budget> getBudgets() {
        User currentUser = authRepository.getCurrentUser();
        if (currentUser == null) return new ArrayList<>();
        return budgetRepository.getBudgets(currentUser.id);
    }

    public boolean deleteBudget(String category) {
        User currentUser = authRepository.getCurrentUser();
        if (currentUser == null) return false;
        return budgetRepository.deleteBudget(currentUser.id, category);
    }
}

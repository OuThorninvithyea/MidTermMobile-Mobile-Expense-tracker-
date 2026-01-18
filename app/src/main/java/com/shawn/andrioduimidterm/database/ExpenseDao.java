package com.shawn.andrioduimidterm.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    long insertExpense(ExpenseEntity expense);

    @Update
    void updateExpense(ExpenseEntity expense);

    @Delete
    void deleteExpense(ExpenseEntity expense);

    @Query("SELECT * FROM expense WHERE user_id = :userId ORDER BY created_at DESC")
    List<ExpenseEntity> getAllExpenses(int userId);

    @Query("SELECT * FROM expense WHERE user_id = :userId AND (expense_name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY created_at DESC")
    List<ExpenseEntity> searchExpenses(int userId, String query);

    @Query("SELECT SUM(amount) FROM expense WHERE user_id = :userId")
    double getTotalExpenses(int userId);

    @Query("SELECT SUM(amount) FROM expense WHERE user_id = :userId AND expense_date = :date")
    double getDailyTotal(int userId, String date);

    @Query("SELECT SUM(amount) FROM expense WHERE user_id = :userId AND expense_date LIKE :month || '%'")
    double getMonthlyTotal(int userId, String month);

    @Query("SELECT SUM(amount) FROM expense WHERE user_id = :userId AND expense_date LIKE :year || '%'")
    double getYearlyTotal(int userId, String year);
}

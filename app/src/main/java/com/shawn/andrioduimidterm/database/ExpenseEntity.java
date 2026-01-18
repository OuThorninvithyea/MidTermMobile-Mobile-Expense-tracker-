package com.shawn.andrioduimidterm.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "expense",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE))
public class ExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    private int expense_id;
    private int user_id;
    private String expense_name;
    private String category;
    private double amount;
    private String expense_date;
    private String created_at;

    public ExpenseEntity(int user_id, String expense_name, String category, 
                        double amount, String expense_date, String created_at) {
        this.user_id = user_id;
        this.expense_name = expense_name;
        this.category = category;
        this.amount = amount;
        this.expense_date = expense_date;
        this.created_at = created_at;
    }

    // Getters and Setters
    public int getExpense_id() { return expense_id; }
    public void setExpense_id(int expense_id) { this.expense_id = expense_id; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public String getExpense_name() { return expense_name; }
    public void setExpense_name(String expense_name) { this.expense_name = expense_name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getExpense_date() { return expense_date; }
    public void setExpense_date(String expense_date) { this.expense_date = expense_date; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

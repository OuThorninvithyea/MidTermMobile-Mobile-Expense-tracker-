package com.shawn.andrioduimidterm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.shawn.andrioduimidterm.database.AppDatabase;
import com.shawn.andrioduimidterm.database.ExpenseEntity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private EditText etAmount, etNote;
    private ChipGroup chipGroup;
    private AppDatabase db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        db = AppDatabase.getInstance(this);
        SharedPreferences prefs = getSharedPreferences("ExpenseTracker", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        chipGroup = findViewById(R.id.chipGroup);

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedChipId = chipGroup.getCheckedChipId();
        if (selectedChipId == -1) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        Chip selectedChip = findViewById(selectedChipId);
        String category = selectedChip.getText().toString();
        // Remove emoji from category
        category = category.replaceAll("[^a-zA-Z ]", "").trim();

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String expenseName = note.isEmpty() ? category + " expense" : note;
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());

        ExpenseEntity expense = new ExpenseEntity(
                userId,
                expenseName,
                category,
                amount,
                currentDate,
                createdAt
        );

        new Thread(() -> {
            long id = db.expenseDao().insertExpense(expense);
            runOnUiThread(() -> {
                if (id > 0) {
                    Toast.makeText(this, "Expense Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}

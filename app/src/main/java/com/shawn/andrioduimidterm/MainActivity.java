package com.shawn.andrioduimidterm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shawn.andrioduimidterm.database.AppDatabase;
import com.shawn.andrioduimidterm.database.ExpenseEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> expenseList;
    private AppDatabase db;
    private int userId;
    private TextView tvTotalSpent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getInstance(this);
        SharedPreferences prefs = getSharedPreferences("ExpenseTracker", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            // Not logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvTotalSpent = findViewById(R.id.totalSpent);

        expenseList = new ArrayList<>();
        adapter = new ExpenseAdapter(expenseList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddExpenseActivity.class));
        });

        loadExpenses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
    }

    private void loadExpenses() {
        new Thread(() -> {
            List<ExpenseEntity> entities = db.expenseDao().getAllExpenses(userId);
            double total = db.expenseDao().getTotalExpenses(userId);
            
            expenseList.clear();
            for (ExpenseEntity entity : entities) {
                String emoji = getCategoryEmoji(entity.getCategory());
                String time = formatDate(entity.getExpense_date());
                String amount = String.format(Locale.US, "-$%.2f", entity.getAmount());
                expenseList.add(new Expense(
                    entity.getExpense_name(),
                    entity.getCategory(),
                    time,
                    amount,
                    emoji
                ));
            }

            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                tvTotalSpent.setText(String.format(Locale.US, "$%.2f", total));
            });
        }).start();
    }

    private String getCategoryEmoji(String category) {
        switch (category.toLowerCase()) {
            case "food": return "🍕";
            case "transport": return "🚗";
            case "shopping": return "🛍️";
            case "bills": return "💸";
            case "entertainment": return "🎵";
            default: return "📝";
        }
    }

    private String formatDate(String date) {
        // Simple date formatting - you can enhance this
        return date;
    }
}
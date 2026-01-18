package com.shawn.andrioduimidterm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.shawn.andrioduimidterm.database.AppDatabase;
import com.shawn.andrioduimidterm.database.ExpenseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> expenseList;
    private AppDatabase db;
    private int userId;
    private TextView tvTotalSpent;

    public static HomeFragment newInstance(int userId) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt("user_id", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt("user_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = AppDatabase.getInstance(requireContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);

        expenseList = new ArrayList<>();
        adapter = new ExpenseAdapter(expenseList);
        recyclerView.setAdapter(adapter);

        loadExpenses();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only reload if we're coming back from adding an expense
        // The fragment is now cached, so we don't need to reload on every resume
    }

    public void refreshData() {
        loadExpenses();
    }

    private void loadExpenses() {
        new Thread(() -> {
            List<ExpenseEntity> entities = db.expenseDao().getAllExpenses(userId);
            double total = db.expenseDao().getTotalExpenses(userId);
            
            expenseList.clear();
            for (ExpenseEntity entity : entities) {
                String emoji = getCategoryEmoji(entity.getCategory());
                String time = entity.getExpense_date(); // Using raw date for now
                String amount = String.format(Locale.US, "-$%.2f", entity.getAmount());
                expenseList.add(new Expense(
                    entity.getExpense_name(),
                    entity.getCategory(),
                    time,
                    amount,
                    emoji
                ));
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    tvTotalSpent.setText(String.format(Locale.US, "$%.2f", total));
                });
            }
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
}

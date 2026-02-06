package com.example.myapplication.ui.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.R;
import com.example.myapplication.handlers.ExpenseHandler;
import com.example.myapplication.models.Expense;
import com.example.myapplication.adapters.CategoryBreakdownAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AnalyticsFragment
 * 
 * This fragment is responsible for displaying financial analytics to the user.
 * It shows the total expenses, transaction count, and a breakdown of expenses by category.
 * Users can search/filter expenses and sort them by different criteria (amount, name, percentage).
 * 
 * Key features:
 * - Total expense calculation
 * - Category-wise breakdown with percentages
 * - Search functionality
 * - Multi-criteria sorting
 */
public class AnalyticsFragment extends Fragment {
    private RecyclerView rvCategoryBreakdown;
    private TextView tvTotalExpenses, tvTransactionCount;
    private TextInputEditText etSearch;
    private MaterialButton btnSort;
    private ExpenseHandler expenseHandler;
    private CategoryBreakdownAdapter adapter;
    private List<CategoryBreakdownAdapter.CategoryBreakdown> allBreakdowns;
    private String currentSortType = "amount_desc"; // Default: highest amount first
    private String searchQuery = "";

    /**
     * Called to have the fragment instantiate its user interface view.
     * Use this method to inflate the layout (fragment_analytics.xml).
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view.
     * This is where we initialize views, setup listeners, and load data.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize DataManager to access database
        expenseHandler = new ExpenseHandler(requireContext());
        
        // Bind UI components
        rvCategoryBreakdown = view.findViewById(R.id.rvCategoryBreakdown);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        tvTransactionCount = view.findViewById(R.id.tvTransactionCount);
        etSearch = view.findViewById(R.id.etSearchAnalytics);
        btnSort = view.findViewById(R.id.btnSortAnalytics);

        // Setup search functionality
        // We add a text watcher to filter the list in real-time as the user types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update query and reload list whenever text changes
                searchQuery = s.toString().toLowerCase().trim();
                loadAnalytics();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup sort button to show a popup menu with sorting options
        btnSort.setOnClickListener(v -> showSortMenu());

        // Initial data load
        loadAnalytics();
    }

    /**
     * Loads, processes, and displays the analytics data.
     * This method acts as the pipeline for data transformation:
     * 1. Fetch raw expenses from DataManager
     * 2. Calculate totals and aggregation by category
     * 3. Convert to breakdown objects
     * 4. Filter based on search query
     * 5. Sort based on selected criteria
     * 6. Update the UI
     */
    private void loadAnalytics() {
        // Step 1: Fetch raw data
        List<Expense> expenses = expenseHandler.getExpenses();

        
        // Step 2: Aggregate data
        double total = 0;
        Map<String, Double> categoryTotals = new HashMap<>();
        
        // Loop through all expenses to sum up amount and group by category
        for (Expense expense : expenses) {
            total += expense.amount;
            // Add amount to existing category total or initialize if new category
            categoryTotals.put(expense.category, 
                categoryTotals.getOrDefault(expense.category, 0.0) + expense.amount);
        }

        tvTotalExpenses.setText(String.format(Locale.getDefault(), "$%.2f", total));
        tvTransactionCount.setText(expenses.size() + " transactions");

        // Step 3: Create breakdown objects
        // We convert the map entries into a list of CategoryBreakdown objects for the adapter
        allBreakdowns = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            double percentage = total > 0 ? (entry.getValue() / total) * 100 : 0;
            allBreakdowns.add(new CategoryBreakdownAdapter.CategoryBreakdown(entry.getKey(), entry.getValue(), percentage));
        }

        // Step 4: Filter
        // Apply search query to filter the list
        List<CategoryBreakdownAdapter.CategoryBreakdown> filteredBreakdowns = filterBreakdowns(allBreakdowns);
        
        // Step 5: Sort
        // Order the list based on the current sort criteria
        List<CategoryBreakdownAdapter.CategoryBreakdown> sortedBreakdowns = sortBreakdowns(filteredBreakdowns);

        // Step 6: Update UI
        // Initialize or update the RecyclerView adapter
        if (adapter == null) {
            adapter = new CategoryBreakdownAdapter(sortedBreakdowns);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
            rvCategoryBreakdown.setLayoutManager(layoutManager);
            rvCategoryBreakdown.setAdapter(adapter);
        } else {
            // If adapter exists, just update the data to avoid re-creating views
            adapter.updateBreakdowns(sortedBreakdowns);
        }
    }

    /**
     * Filters the list of breakdowns based on the user's search query.
     * Matches against category name, amount, or percentage.
     * 
     * @param breakdowns The original list of breakdowns
     * @return A new list containing only the matching items
     */
    private List<CategoryBreakdownAdapter.CategoryBreakdown> filterBreakdowns(List<CategoryBreakdownAdapter.CategoryBreakdown> breakdowns) {
        // If search is empty, return all items
        if (searchQuery.isEmpty()) {
            return new ArrayList<>(breakdowns);
        }

        List<CategoryBreakdownAdapter.CategoryBreakdown> filtered = new ArrayList<>();
        for (CategoryBreakdownAdapter.CategoryBreakdown breakdown : breakdowns) {
            // Check if query matches category name (case-insensitive)
            if (breakdown.category != null && breakdown.category.toLowerCase().contains(searchQuery)) {
                filtered.add(breakdown);
            } 
            // Check if query matches the formatted amount string
            else if (String.format(Locale.getDefault(), "%.2f", breakdown.amount).contains(searchQuery)) {
                filtered.add(breakdown);
            } 
            // Check if query matches the formatted percentage string
            else if (String.format(Locale.getDefault(), "%.1f", breakdown.percentage).contains(searchQuery)) {
                filtered.add(breakdown);
            }
        }
        return filtered;
    }

    /**
     * Sorts the list of breakdowns based on the `currentSortType`.
     * 
     * @param breakdowns The list to sort
     * @return The sorted list
     */
    private List<CategoryBreakdownAdapter.CategoryBreakdown> sortBreakdowns(List<CategoryBreakdownAdapter.CategoryBreakdown> breakdowns) {
        List<CategoryBreakdownAdapter.CategoryBreakdown> sorted = new ArrayList<>(breakdowns);
        
        switch (currentSortType) {
            case "amount_desc":
                // Compare amounts: B - A for descending (Highest first)
                Collections.sort(sorted, (a, b) -> Double.compare(b.amount, a.amount));
                break;
            case "amount_asc":
                // Compare amounts: A - B for ascending (Lowest first)
                Collections.sort(sorted, (a, b) -> Double.compare(a.amount, b.amount));
                break;
            case "name_asc":
                // Compare strings alphabetically
                Collections.sort(sorted, (a, b) -> {
                    String c1 = a.category != null ? a.category : "";
                    String c2 = b.category != null ? b.category : "";
                    return c1.compareToIgnoreCase(c2);
                });
                break;
            case "name_desc":
                // Compare strings reverse alphabetically
                Collections.sort(sorted, (a, b) -> {
                    String c1 = a.category != null ? a.category : "";
                    String c2 = b.category != null ? b.category : "";
                    return c2.compareToIgnoreCase(c1);
                });
                break;
            case "percentage_desc":
                Collections.sort(sorted, (a, b) -> Double.compare(b.percentage, a.percentage));
                break;
            case "percentage_asc":
                Collections.sort(sorted, (a, b) -> Double.compare(a.percentage, b.percentage));
                break;
        }
        
        return sorted;
    }

    private void showSortMenu() {
        PopupMenu popupMenu = new PopupMenu(requireContext(), btnSort);
        popupMenu.getMenu().add("Amount (High to Low)");
        popupMenu.getMenu().add("Amount (Low to High)");
        popupMenu.getMenu().add("Category (A-Z)");
        popupMenu.getMenu().add("Category (Z-A)");
        popupMenu.getMenu().add("Percentage (High to Low)");
        popupMenu.getMenu().add("Percentage (Low to High)");
        
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title = item.getTitle().toString();
                if (title.equals("Amount (High to Low)")) {
                    currentSortType = "amount_desc";
                } else if (title.equals("Amount (Low to High)")) {
                    currentSortType = "amount_asc";
                } else if (title.equals("Category (A-Z)")) {
                    currentSortType = "name_asc";
                } else if (title.equals("Category (Z-A)")) {
                    currentSortType = "name_desc";
                } else if (title.equals("Percentage (High to Low)")) {
                    currentSortType = "percentage_desc";
                } else if (title.equals("Percentage (Low to High)")) {
                    currentSortType = "percentage_asc";
                }
                loadAnalytics();
                return true;
            }
        });
        
        popupMenu.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAnalytics();
    }
}

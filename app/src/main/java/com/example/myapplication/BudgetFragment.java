package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * BudgetFragment
 * 
 * This fragment manages the user interface for the Budget feature.
 * It allows users to:
 * 1. View their list of budgets.
 * 2. See visual progress (progress bars) for each budget.
 * 3. Add new budgets for specific categories.
 * 4. Edit or delete existing budgets.
 * 
 * It acts as the "View" in the MVC/MVVM pattern, displaying data from DataManager.
 */
public class BudgetFragment extends Fragment {
    // UI Components
    private RecyclerView rvBudgets; // List view for budget items
    private MaterialButton btnAddBudget; // Floating action button to add new budget
    private TextView tvEmptyState; // Text shown when no budgets exist
    
    // Data & Logic
    private DataManager dataManager; // Repository for accessing database
    private BudgetAdapter adapter; // Adapter to bind data to RecyclerView
    private List<BudgetAdapter.BudgetItem> budgetItems; // List of data objects to display

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    /**
     * Called immediately after the view is created.
     * Use this for:
     * 1. Initializing UI references (findViewById).
     * 2. Setting up the RecyclerView (LayoutManager and Adapter).
     * 3. Setting up button click listeners.
     * 4. Loading initial data.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get singleton instance of DataManager
        dataManager = DataManager.getInstance(requireContext());
        
        // Initialize UI components
        rvBudgets = view.findViewById(R.id.rvBudgets);
        btnAddBudget = view.findViewById(R.id.btnAddBudget);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        // Setup RecyclerView
        // LinearLayoutManager arranges items in a vertical list
        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Initialize the list and adapter
        budgetItems = new ArrayList<>();
        adapter = new BudgetAdapter(budgetItems, new BudgetAdapter.OnBudgetClickListener() {
            // Implement interface callbacks for item interactions
            @Override
            public void onEditClick(DataManager.Budget budget) {
                showEditBudgetDialog(budget);
            }

            @Override
            public void onDeleteClick(DataManager.Budget budget) {
                showDeleteConfirmation(budget);
            }
        });
        rvBudgets.setAdapter(adapter);
        
        // Click listener for "Add Budget" button
        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());

        // Initial data fetch
        loadBudgets();
    }

    /**
     * Fetches budget data and expense data to calculate progress.
     * 
     * Logic:
     * 1. Get all budgets and all expenses.
     * 2. Aggregate expenses by category into a Map (Category -> Total Spent).
     * 3. Merge budget info with spent info into `BudgetItem` objects.
     * 4. Update the adapter to refresh the UI.
     * 5. Toggle empty state visibility.
     */
    private void loadBudgets() {
        List<DataManager.Budget> budgets = dataManager.getBudgets();
        List<DataManager.Expense> expenses = dataManager.getExpenses();
        
        // Step 1: Calculate spent amounts per category
        // We use a HashMap for O(1) lookups during the merge step
        Map<String, Double> categoryTotals = new HashMap<>();
        for (DataManager.Expense expense : expenses) {
            double currentTotal = categoryTotals.getOrDefault(expense.category, 0.0);
            categoryTotals.put(expense.category, currentTotal + expense.amount);
        }
        
        // Step 2: Create display items merging Budget limit + Spent amount
        budgetItems.clear();
        for (DataManager.Budget budget : budgets) {
            double spent = categoryTotals.getOrDefault(budget.category, 0.0);
            budgetItems.add(new BudgetAdapter.BudgetItem(budget, spent));
        }
        
        // Step 3: Update adapter
        adapter.updateBudgets(budgetItems);
        
        // Step 4: Handle empty state UI
        if (budgets.isEmpty()) {
            rvBudgets.setVisibility(View.GONE);
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        } else {
            rvBudgets.setVisibility(View.VISIBLE);
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.GONE);
            }
        }
    }

    private void showAddBudgetDialog() {
        showBudgetDialog(null);
    }

    private void showEditBudgetDialog(DataManager.Budget budget) {
        showBudgetDialog(budget);
    }

    /**
     * Shows a dialog to add or edit a budget.
     * This method builds a complex custom dialog programmatically.
     * 
     * Key parts:
     * - Grid layout for category selection
     * - "Others" category handling with custom input
     * - Input validation before saving
     * 
     * @param existingBudget The budget object if editing, or null if adding new.
     */
    private void showBudgetDialog(DataManager.Budget existingBudget) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_budget, null);
        
        TextInputEditText etAmount = dialogView.findViewById(R.id.etBudgetAmount);
        TextInputEditText etCustomCategory = dialogView.findViewById(R.id.etCustomCategoryBudget);
        com.google.android.material.textfield.TextInputLayout tilCustomCategory = dialogView.findViewById(R.id.tilCustomCategoryBudget);
        GridLayout gridCategories = dialogView.findViewById(R.id.gridBudgetCategories);
        
        List<String> categoryList = dataManager.getCategories();
        String[] categories = categoryList.toArray(new String[0]);
        
        // Setup icon map with defaults
        Map<String, String> iconMap = new HashMap<>();
        iconMap.put("Food", "🍔");
        iconMap.put("Transport", "🚗");
        iconMap.put("Shopping", "🛍️");
        iconMap.put("Bills", "📜");
        iconMap.put("Entertainment", "🍿");
        iconMap.put("Others", "✨");
        
        String[] selectedCategory = {existingBudget != null ? existingBudget.category : categories[0]};
        String[] customCategoryName = {""};
        TextView[] othersCategoryLabel = {null};
        
        // Pre-fill amount if editing
        if (existingBudget != null) {
            etAmount.setText(String.format(Locale.getDefault(), "%.2f", existingBudget.limit));
        }
        
        // Create category cards dynamically in a Grid
        // We iterate through predefined categories and create a visual card for each
        for (int i = 0; i < categories.length; i++) {
            MaterialCardView card = new MaterialCardView(requireContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            // width = 0 and columnSpec with weight 1f distributes columns equally
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 3, 1f); // 3 columns
            params.setMargins(8, 8, 8, 8);
            card.setLayoutParams(params);
            card.setRadius(24);
            card.setCardElevation(2);

            TextView tvIcon = new TextView(requireContext());
            // Use default icon for unknown categories
            String icon = iconMap.getOrDefault(categories[i], "🏷️");
            tvIcon.setText(icon);
            tvIcon.setTextSize(24);
            tvIcon.setPadding(24, 24, 24, 8);
            tvIcon.setGravity(android.view.Gravity.CENTER);

            TextView tvLabel = new TextView(requireContext());
            tvLabel.setText(categories[i]);
            tvLabel.setTextSize(10);
            tvLabel.setGravity(android.view.Gravity.CENTER);
            tvLabel.setPadding(8, 0, 8, 16);
            tvLabel.setTextColor(requireContext().getColor(android.R.color.darker_gray));

            // Store reference to "Others" category label to update it dynamically with custom text
            if (categories[i].equals("Others")) {
                othersCategoryLabel[0] = tvLabel;
            }

            android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.addView(tvIcon);
            layout.addView(tvLabel);
            card.addView(layout);

            final String category = categories[i];
            // Set click listener for each category card
            card.setOnClickListener(v -> {
                if (category.equals("Others")) {
                    // Logic for "Others": allow custom text input
                    selectedCategory[0] = "Others";
                    // Show the custom category input field
                    tilCustomCategory.setVisibility(View.VISIBLE);
                    etCustomCategory.requestFocus();
                    updateCategorySelection(gridCategories, categories, selectedCategory[0], othersCategoryLabel[0], customCategoryName[0]);
                } else {
                    // Logic for standard categories
                    selectedCategory[0] = category;
                    customCategoryName[0] = ""; // Clear custom category when selecting a predefined one
                    // Hide the custom category input field
                    tilCustomCategory.setVisibility(View.GONE);
                    etCustomCategory.setText("");
                    // Reset "Others" label if it was showing custom name
                    if (othersCategoryLabel[0] != null) {
                        othersCategoryLabel[0].setText("Others");
                    }
                    updateCategorySelection(gridCategories, categories, selectedCategory[0], othersCategoryLabel[0], customCategoryName[0]);
                }
            });

            gridCategories.addView(card);
        }
        
        // Handle case where we are editing an existing budget
        // We need to pre-select the correct category or setup the "Others" field if it's a custom category
        if (existingBudget != null) {
            boolean isPredefinedCategory = false;
            for (String cat : categories) {
                if (cat.equals(existingBudget.category)) {
                    isPredefinedCategory = true;
                    break;
                }
            }
            if (!isPredefinedCategory) {
                // If not in standard list, it must be valid custom category via "Others"
                selectedCategory[0] = "Others";
                customCategoryName[0] = existingBudget.category;
                tilCustomCategory.setVisibility(View.VISIBLE);
                etCustomCategory.setText(existingBudget.category);
                if (othersCategoryLabel[0] != null) {
                    othersCategoryLabel[0].setText(existingBudget.category);
                }
            }
        }
        
        // Update visual state of selection
        updateCategorySelection(gridCategories, categories, selectedCategory[0], othersCategoryLabel[0], customCategoryName[0]);

        // Disable category selection if editing (category cannot be changed)
        if (existingBudget != null) {
            for (int i = 0; i < gridCategories.getChildCount(); i++) {
                gridCategories.getChildAt(i).setEnabled(false);
                gridCategories.getChildAt(i).setAlpha(0.6f);
            }
            // Also disable custom category input when editing
            tilCustomCategory.setEnabled(false);
            etCustomCategory.setEnabled(false);
        }
        
        // Listen for custom category input changes
        etCustomCategory.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                customCategoryName[0] = s.toString().trim();
                // Update the "Others" label to show custom name
                if (othersCategoryLabel[0] != null && selectedCategory[0].equals("Others")) {
                    if (!customCategoryName[0].isEmpty()) {
                        othersCategoryLabel[0].setText(customCategoryName[0]);
                    } else {
                        othersCategoryLabel[0].setText("Others");
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        String dialogTitle = existingBudget != null ? "Edit Budget" : "Set Budget";
        String buttonText = existingBudget != null ? "Update" : "Set";

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(buttonText, (d, w) -> {
                String amountStr = etAmount.getText().toString().trim();

                if (amountStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a budget amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(requireContext(), "Budget amount must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validate custom category if "Others" is selected
                    if (selectedCategory[0].equals("Others") && customCategoryName[0].isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
                        etCustomCategory.requestFocus();
                        return;
                    }

                    // Use custom category name if "Others" is selected, otherwise use selected category
                    String categoryToSave = selectedCategory[0].equals("Others") ? customCategoryName[0] : selectedCategory[0];
                    
                    if (dataManager.setBudget(categoryToSave, amount)) {
                        loadBudgets();
                        String message = existingBudget != null ? "Budget updated successfully" : "Budget set successfully";
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        String message = existingBudget != null ? "Failed to update budget" : "Failed to set budget";
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .create();

        dialog.show();
    }

    /**
     * Updates the visual state of category cards to show selection.
     * Highlights the selected card and resets others.
     * 
     * @param gridCategories The GridLayout containing cards
     * @param categories Array of category names
     * @param selected The currently selected category (can be "Others")
     * @param othersLabel Reference to the TextView for "Others" to update text
     * @param customName Custom name if "Others" is selected
     */
    private void updateCategorySelection(GridLayout gridCategories, String[] categories, String selected, TextView othersLabel, String customName) {
        for (int i = 0; i < gridCategories.getChildCount(); i++) {
            MaterialCardView card = (MaterialCardView) gridCategories.getChildAt(i);
            // Access hierarchy: Card -> LinearLayout -> TextView (Label is at index 1)
            TextView tvLabel = (TextView) ((android.widget.LinearLayout) card.getChildAt(0)).getChildAt(1);
            
            // Check if this is the "Others" category
            boolean isOthersCategory = (tvLabel == othersLabel);
            
            // Update "Others" label if custom name is set and it's selected
            if (isOthersCategory) {
                if (selected.equals("Others") && !customName.isEmpty()) {
                    tvLabel.setText(customName);
                } else {
                    tvLabel.setText("Others");
                }
            }
            
            // Determine if this category is selected
            boolean isSelected = false;
            if (isOthersCategory) {
                isSelected = selected.equals("Others");
            } else {
                // For other categories, compare with the original category name
                String originalCategoryName = categories[i];
                isSelected = originalCategoryName.equals(selected);
            }
            
            // Get theme-aware colors
            int surfaceColor = getMaterialColor("colorSurface");
            int primaryColor = getMaterialColor("colorPrimary");
            int primaryContainerColor = getMaterialColor("colorPrimaryContainer");
            
            // Apply selection style
            if (isSelected) {
                card.setCardBackgroundColor(primaryContainerColor);
                card.setStrokeWidth(4);
                card.setStrokeColor(primaryColor);
            } else {
                card.setCardBackgroundColor(surfaceColor);
                card.setStrokeWidth(0);
            }
        }
    }

    /**
     * Helper method to resolve theme colors attributes to integer color values.
     * Useful for supporting both Light and Dark themes dynamically.
     */
    private int getThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        if (requireContext().getTheme().resolveAttribute(attr, typedValue, true)) {
            if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && 
                typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                return typedValue.data;
            } else {
                return ContextCompat.getColor(requireContext(), typedValue.resourceId);
            }
        }
        // Fallback to a default color if attribute not found
        return 0xFF000000; // Black as fallback
    }
    
    private int getMaterialColor(String attrName) {
        int attrId = requireContext().getResources().getIdentifier(
            attrName, "attr", requireContext().getPackageName());
        if (attrId == 0) {
            // Try Material library package
            attrId = requireContext().getResources().getIdentifier(
                attrName, "attr", "com.google.android.material");
        }
        if (attrId != 0) {
            return getThemeColor(attrId);
        }
        // Fallback colors
        switch (attrName) {
            case "colorSurface":
                return getThemeColor(android.R.attr.colorBackground);
            case "colorPrimary":
                return getThemeColor(android.R.attr.colorPrimary);
            case "colorOnSurfaceVariant":
                return getThemeColor(android.R.attr.textColorSecondary);
            case "colorPrimaryContainer":
                return getThemeColor(android.R.attr.colorPrimary);
            default:
                return 0xFF000000;
        }
    }

    private void showDeleteConfirmation(DataManager.Budget budget) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Budget")
            .setMessage("Are you sure you want to delete the budget for " + budget.category + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (dataManager.deleteBudget(budget.category)) {
                    loadBudgets();
                    Toast.makeText(requireContext(), "Budget deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete budget", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgets();
    }
}

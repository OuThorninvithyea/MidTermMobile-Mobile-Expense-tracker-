package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Budget;
import java.util.List;
import java.util.Locale;

/**
 * BudgetAdapter
 * 
 * RecyclerView Adapter for displaying a list of budgets.
 * 
 * Features:
 * - Displays Category name and dynamic icon
 * - Visual ProgressBar showing spent vs limit
 * - Dynamic color coding based on percentage used:
 *   - < 80% : Blue (Safe)
 *   - >= 80% : Orange (Warning)
 *   - >= 100% : Red (Over budget)
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private List<BudgetItem> budgets;
    private OnBudgetClickListener listener;

    /**
     * Interface for handling click events on budget items.
     * Implemented by the Fragment to handle actions.
     */
    public interface OnBudgetClickListener {
        void onEditClick(Budget budget);
        void onDeleteClick(Budget budget);
    }

    public BudgetAdapter(List<BudgetItem> budgets, OnBudgetClickListener listener) {
        this.budgets = budgets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (item_budget.xml)
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetItem budgetItem = budgets.get(position);
        holder.bind(budgetItem);
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    /**
     * Updates the dataset and refreshes the RecyclerView.
     * @param newBudgets New list of budget items
     */
    public void updateBudgets(List<BudgetItem> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class holding references to UI views for efficient recycling.
     */
    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategory, tvCategoryIcon, tvSpent, tvLimit, tvWarning;
        private ProgressBar progressBar;
        private android.widget.ImageButton btnMenu;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
            tvCategoryIcon = itemView.findViewById(R.id.tvBudgetCategoryIcon);
            tvSpent = itemView.findViewById(R.id.tvBudgetSpent);
            tvLimit = itemView.findViewById(R.id.tvBudgetLimit);
            tvWarning = itemView.findViewById(R.id.tvBudgetWarning);
            progressBar = itemView.findViewById(R.id.progressBudget);
            btnMenu = itemView.findViewById(R.id.btnMenuBudget);
        }

        /**
         * Binds data to the views.
         * Contains logic for progress calculation and dynamic coloring.
         * 
         * @param budgetItem The data item to display
         */
        public void bind(BudgetItem budgetItem) {
            Budget budget = budgetItem.budget;
            double spent = budgetItem.spent;
            double limit = budget.limit;
            
            // Calculate percentage used (0 to 100+)
            double percentage = limit > 0 ? (spent / limit) * 100 : 0;
            
            tvCategory.setText(budget.category);
            tvCategoryIcon.setText(getCategoryIcon(budget.category));
            tvSpent.setText(String.format(Locale.getDefault(), "$%.2f", spent));
            tvLimit.setText(String.format(Locale.getDefault(), "/ $%.2f", limit));
            
            // Set basic progress bar value (capped at 100 for visual bar)
            int progress = (int) Math.min(percentage, 100);
            progressBar.setProgress(progress);
            
            // Dynamic Color Logic:
            // - Over 100% -> Red (Critical)
            // - Over 80% -> Orange (Warning)
            // - Under 80% -> Blue (Normal)
            if (percentage >= 100) {
                int redColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark);
                progressBar.getProgressDrawable().setColorFilter(
                    redColor,
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                tvWarning.setText("🚨 Budget Exceeded!");
                tvWarning.setVisibility(View.VISIBLE);
                tvWarning.setTextColor(redColor);
            } else if (percentage >= 80) {
                int orangeColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark);
                progressBar.getProgressDrawable().setColorFilter(
                    orangeColor,
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                tvWarning.setText(String.format(Locale.getDefault(), "⚠️ %.0f%% of limit reached", percentage));
                tvWarning.setVisibility(View.VISIBLE);
                tvWarning.setTextColor(orangeColor);
            } else {
                int blueColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark);
                progressBar.getProgressDrawable().setColorFilter(
                    blueColor,
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                tvWarning.setVisibility(View.GONE);
            }

            // Setup 3-dot menu button
            btnMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                popupMenu.getMenu().add("Edit");
                popupMenu.getMenu().add("Delete");
                
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (listener != null) {
                            if (item.getTitle().toString().equals("Edit")) {
                                listener.onEditClick(budget);
                            } else if (item.getTitle().toString().equals("Delete")) {
                                listener.onDeleteClick(budget);
                            }
                        }
                        return true;
                    }
                });
                
                popupMenu.show();
            });
        }

        private String getCategoryIcon(String category) {
            switch (category) {
                case "Food": return "🍔";
                case "Transport": return "🚗";
                case "Shopping": return "🛍️";
                case "Bills": return "📜";
                case "Entertainment": return "🍿";
                case "Others": return "✨";
                default: return "📦";
            }
        }
    }

    public static class BudgetItem {
        public Budget budget;
        public double spent;

        public BudgetItem(Budget budget, double spent) {
            this.budget = budget;
            this.spent = spent;
        }
    }
}

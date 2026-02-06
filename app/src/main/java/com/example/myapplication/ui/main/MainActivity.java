package com.example.myapplication.ui.main;

// Import the Intent class to facilitate launching separate activities within the app.
import android.content.Intent;
// Import SharedPreferences to retrieve lightweight data like boolean flags or simple settings.
import android.content.SharedPreferences;
// Import the Bundle class to handle data passed between components or saved instance states.
import android.os.Bundle;
// Import AppCompatActivity to provide compatibility support for newer Android features on older devices.
import androidx.appcompat.app.AppCompatActivity;
// Import AppCompatDelegate to manage global app configuration, such as day/night modes.
import androidx.appcompat.app.AppCompatDelegate;
// Import Fragment class to allow modular UI sections within this activity.
import androidx.fragment.app.Fragment;
// Import BottomNavigationView to provide the bottom navigation bar UI component.
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.myapplication.R;
import com.example.myapplication.handlers.AuthHandler;
import com.example.myapplication.ui.auth.LoginActivity;

/**
 * MainActivity is the central hub of the application after login.
 * It hosts the BottomNavigationView and manages the swapping of Fragments.
 *
 * Responsibilities:
 * 1. Validates session (redirects to LoginActivity if not logged in).
 * 2. Sets up the bottom navigation menu.
 * 3. Handles switching between Home, Analytics, Add, Budget, and Settings fragments.
 */
// Define the MainActivity class, extending AppCompatActivity to inherit standard Android lifecycle and UI behavior.
public class MainActivity extends AppCompatActivity {
    // Declare a public variable for the BottomNavigationView so it can be accessed or referenced if needed.
    public BottomNavigationView bottomNavigation;
    // Declare a private DataManager instance to handle user session and database interactions.
    private AuthHandler authHandler;

    // The onCreate method is the entry point where the activity initializes its UI and variables.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass's onCreate method to ensure standard initialization runs first.
        super.onCreate(savedInstanceState);
        
        // Load the shared preferences file named "AppSettings" with a private mode (0) so only this app can read it.
        SharedPreferences prefs = getSharedPreferences("AppSettings", 0);
        // Retrieve the "dark_mode" integer setting. If not found, default to following the system's night mode setting.
        int darkMode = prefs.getInt("dark_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        // Apply the retrieved dark mode setting to the entire application via the delegate.
        AppCompatDelegate.setDefaultNightMode(darkMode);
        
        // Set the UI layout for this activity from the XML resource file 'activity_main'.
        setContentView(R.layout.activity_main);

        // Get the singleton instance of DataManager, passing the current context (this activity) to it.
        authHandler = new AuthHandler(this);

        // Security check: Verify if a user is currently logged into the session.
        if (authHandler.getCurrentUser() == null) {
            // If no user is logged in, create an Intent to navigate to the LoginActivity.
            startActivity(new Intent(this, LoginActivity.class));
            // Call finish() to close MainActivity so the user cannot return to it by pressing the back button.
            finish();
            // Return immediately to stop further execution of this method since we are leaving the activity.
            return;
        }

        // Find the BottomNavigationView UI element defined in the XML layout by its ID 'bottomNavigation'.
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Set a listener to handle events when an item in the bottom navigation menu is selected.
        bottomNavigation.setOnItemSelectedListener(item -> {
            // Initialize a variable to hold the fragment we want to display.
            Fragment selectedFragment = null;
            // Get the unique ID of the selected menu item.
            int itemId = item.getItemId();
            
            // Check if the selected item is the Home button.
            if (itemId == R.id.nav_home) {
                // Instantiate the HomeFragment to show the main dashboard.
                selectedFragment = new HomeFragment();
            // Check if the selected item is the Analytics button.
            } else if (itemId == R.id.nav_analytics) {
                // Instantiate the AnalyticsFragment to show charts and stats.
                selectedFragment = new AnalyticsFragment();
            // Check if the selected item is the Add Expense button.
            } else if (itemId == R.id.nav_add) {
                // Instantiate the AddExpenseFragment to allow entering new transactions.
                selectedFragment = new AddExpenseFragment();
            // Check if the selected item is the Budget button.
            } else if (itemId == R.id.nav_budget) {
                // Instantiate the BudgetFragment to show budget tracking.
                selectedFragment = new BudgetFragment();
            // Check if the selected item is the Settings button.
            } else if (itemId == R.id.nav_settings) {
                // Instantiate the SettingsFragment to allow configuration changes.
                selectedFragment = new SettingsFragment();
            }

            // If a valid fragment was selected (i.e., not null), perform the replacement transaction.
            if (selectedFragment != null) {
                // Begin a FragmentTransaction using the FragmentManager.
                getSupportFragmentManager().beginTransaction()
                    // Replace the content of the container view (ID: fragmentContainer) with the new selectedFragment.
                    .replace(R.id.fragmentContainer, selectedFragment)
                    // Commit the transaction to apply the changes immediately.
                    .commit();
                // Return true to indicate the item selection event was handled successfully.
                return true;
            }
            // Return false if no valid selection was made (though logically existing logic handles all cases).
            return false;
        });

        // initial loading check: Only load the default fragment if this is the first creation (not a rotation/recreation).
        if (savedInstanceState == null) {
            // Programmatically select the 'nav_home' item to trigger the listener and load the HomeFragment by default.
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }
}
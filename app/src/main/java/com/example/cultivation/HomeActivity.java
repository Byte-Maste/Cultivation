package com.example.cultivation;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity implements DashboardNavigator {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_scan) {
                selectedFragment = new ScanFragment();
            } else if (itemId == R.id.nav_farm) {
                selectedFragment = new FarmFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Load Home Fragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    @Override
    public void openDashboard(String plotName, String variety, String plantingDate) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString("plotName", plotName);
        args.putString("variety", variety);
        args.putString("plantingDate", plantingDate);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Allow going back to Farm List
                .commit();
    }
}
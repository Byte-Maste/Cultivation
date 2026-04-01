package com.example.cultivation.miller;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.cultivation.ProfileFragment;
import com.example.cultivation.R;
import com.example.cultivation.DashboardFragment;
import com.example.cultivation.DashboardNavigator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MillerHomeActivity extends AppCompatActivity implements DashboardNavigator {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miller_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_find_farmers) {
                selectedFragment = new FindFarmersFragment();
            } else if (itemId == R.id.nav_connections) {
                selectedFragment = new ConnectionsFragment();
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

        // Default Load
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FindFarmersFragment())
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
                .addToBackStack(null)
                .commit();
    }
}

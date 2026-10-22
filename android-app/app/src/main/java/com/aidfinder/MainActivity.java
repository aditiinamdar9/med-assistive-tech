package com.aidfinder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aidfinder.auth.UserSession;
import com.aidfinder.ui.HomeFragment;
import com.aidfinder.ui.ProfileFragment;
import com.aidfinder.ui.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String intentUser = getIntent().getStringExtra("USERNAME");
        if (intentUser != null && !intentUser.trim().isEmpty()) {
            username = intentUser.trim();
            UserSession.saveUsername(this, username);
        } else {
            username = UserSession.getUsername(this);
            if (username.isEmpty()) {
                username = "Guest";
            }
        }

        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                loadFragment(HomeFragment.newInstance(username));
                return true;
            } else if (itemId == R.id.navigation_search) {
                loadFragment(new SearchFragment());
                return true;
            } else if (itemId == R.id.navigation_profile) {
                loadFragment(ProfileFragment.newInstance(username));
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
    }

    public void selectTab(int menuId) {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(menuId);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}

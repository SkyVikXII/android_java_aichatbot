package com.nhom4.aichatbot;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nhom4.aichatbot.Fragments.FragmentCharacter;
import com.nhom4.aichatbot.Fragments.FragmentChat;
import com.nhom4.aichatbot.Fragments.FragmentSetting;
import com.nhom4.aichatbot.Fragments.HomeFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        ints();

    }

    private void ints() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        loadFragment(new HomeFragment());
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_character) {
                fragment = new FragmentCharacter();
            } else if (itemId == R.id.nav_chat) {
                fragment = new FragmentChat();
            } else if (itemId == R.id.nav_settings) {
                fragment = new FragmentSetting();
            }

            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }
}
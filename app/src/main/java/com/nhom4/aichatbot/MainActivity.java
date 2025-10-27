package com.nhom4.aichatbot;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nhom4.aichatbot.Database.CharacterDbHelper;
import com.nhom4.aichatbot.Database.EndpointDbHelper;
import com.nhom4.aichatbot.Database.ModelDbHelper;
import com.nhom4.aichatbot.Database.PromptDbHelper;
import com.nhom4.aichatbot.Firebase.CharacterFirebaseHelper;
import com.nhom4.aichatbot.Firebase.EndpointFirebaseHelper;
import com.nhom4.aichatbot.Firebase.ModelFirebaseHelper;
import com.nhom4.aichatbot.Firebase.PromptFirebaseHelper;
import com.nhom4.aichatbot.Fragments.FragmentCharacter;
import com.nhom4.aichatbot.Fragments.FragmentChat;
import com.nhom4.aichatbot.Fragments.FragmentSetting;
import com.nhom4.aichatbot.Fragments.HomeFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ModelFirebaseHelper modelFirebaseHelper;
    ModelDbHelper modelDbHelper;
    EndpointFirebaseHelper endpointFirebaseHelper;
    EndpointDbHelper endpointDbHelper;
    PromptFirebaseHelper promptFirebaseHelper;
    PromptDbHelper promptDbHelper;
    CharacterFirebaseHelper characterFirebaseHelper;
    CharacterDbHelper characterDbHelper;

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
        intsdb();
    }

    private void intsdb() {
        endpointDbHelper = new EndpointDbHelper(getBaseContext());
        endpointFirebaseHelper = new EndpointFirebaseHelper();
        modelDbHelper = new ModelDbHelper(getBaseContext());
        modelFirebaseHelper = new ModelFirebaseHelper();
        promptFirebaseHelper = new PromptFirebaseHelper();
        promptDbHelper = new PromptDbHelper(getBaseContext());
        characterDbHelper = new CharacterDbHelper(getBaseContext());
        characterFirebaseHelper = new CharacterFirebaseHelper();
        syncAllData();
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
    private void syncAllData(){
        modelFirebaseHelper.syncUserModels(modelDbHelper, new ModelFirebaseHelper.SyncCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getBaseContext(), "Không thể tải du lieu tu server", Toast.LENGTH_SHORT).show();
            }
        });
        endpointFirebaseHelper.syncSystemEndpoints(endpointDbHelper, new EndpointFirebaseHelper.SyncCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getBaseContext(), "Không thể tải du lieu tu server", Toast.LENGTH_SHORT).show();
            }
        });
        endpointFirebaseHelper.syncUserEndpoints(endpointDbHelper, new EndpointFirebaseHelper.SyncCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getBaseContext(), "Không thể tải du lieu tu server", Toast.LENGTH_SHORT).show();
            }
        });
        promptFirebaseHelper.syncUserPrompts(promptDbHelper, new PromptFirebaseHelper.SyncCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getBaseContext(), "Không thể tải du lieu tu server", Toast.LENGTH_SHORT).show();
            }
        });
        characterFirebaseHelper.syncUserCharacters(characterDbHelper, new CharacterFirebaseHelper.SyncCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getBaseContext(), "Không thể tải du lieu tu server", Toast.LENGTH_SHORT).show();
            }
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
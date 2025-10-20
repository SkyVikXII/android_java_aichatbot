package com.nhom4.aichatbot;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_SAVE_LOGIN = "save_login";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save user login credentials
    public void saveUserLogin(String username, String password, boolean saveLogin) {
        editor.putBoolean(KEY_SAVE_LOGIN, saveLogin);

        if (saveLogin) {
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_PASSWORD, password);
        } else {
            editor.remove(KEY_USERNAME);
            editor.remove(KEY_PASSWORD);
        }
        editor.apply();
    }

    // Get saved username
    public String getSavedUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    // Get saved password
    public String getSavedPassword() {
        return sharedPreferences.getString(KEY_PASSWORD, "");
    }

    // Check if save login is enabled
    public boolean isSaveLoginEnabled() {
        return sharedPreferences.getBoolean(KEY_SAVE_LOGIN, false);
    }

    // Clear all saved data (for logout)
    public void clearUserData() {
        editor.clear();
        editor.apply();
    }
}

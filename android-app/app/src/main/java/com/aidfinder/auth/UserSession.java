package com.aidfinder.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    private static final String PREF_NAME = "AidFinderSession";
    private static final String KEY_USERNAME = "current_username";

    public static void saveUsername(Context context, String username) {
        if (context == null || username == null) return;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_USERNAME, username.trim()).apply();
    }

    public static String getUsername(Context context) {
        if (context == null) return "";
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_USERNAME, "");
    }

    public static void clearSession(Context context) {
        if (context == null) return;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().apply();
    }
}

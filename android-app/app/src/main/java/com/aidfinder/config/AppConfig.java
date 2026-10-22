package com.aidfinder.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.aidfinder.BuildConfig;

/**
 * AppConfig manages runtime configuration and user preferences for the Android app,
 * such as custom AI base URL overrides and app tokens stored in SharedPreferences.
 */
public class AppConfig {

    private static final String PREF_NAME = "aid_finder_config";
    private static final String KEY_AI_BASE_URL = "ai_base_url";
    private static final String KEY_APP_TOKEN = "app_token";

    private static AppConfig instance;
    private final SharedPreferences prefs;

    private AppConfig(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AppConfig getInstance(Context context) {
        if (instance == null) {
            instance = new AppConfig(context);
        }
        return instance;
    }

    public String getBaseUrl() {
        return prefs.getString(KEY_AI_BASE_URL, BuildConfig.AI_BASE_URL);
    }

    public void setBaseUrl(String url) {
        prefs.edit().putString(KEY_AI_BASE_URL, url).apply();
    }

    public String getAppToken() {
        return prefs.getString(KEY_APP_TOKEN, BuildConfig.APP_TOKEN);
    }

    public void setAppToken(String token) {
        prefs.edit().putString(KEY_APP_TOKEN, token).apply();
    }

    public void resetToDefaults() {
        prefs.edit().clear().apply();
    }
}

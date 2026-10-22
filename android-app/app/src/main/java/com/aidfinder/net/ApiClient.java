package com.aidfinder.net;

import android.content.Context;

import com.aidfinder.BuildConfig;
import com.aidfinder.config.AppConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Builds the one Retrofit client the app uses.
 *
 * The base URL and app token come from AppConfig (supporting runtime overrides
 * and falling back to BuildConfig).
 */
public class ApiClient {

    private static AiApi api;
    private static String currentBaseUrl;

    public static synchronized AiApi get() {
        return get(null);
    }

    public static synchronized AiApi get(Context context) {
        String baseUrl = context != null
                ? AppConfig.getInstance(context).getBaseUrl()
                : BuildConfig.AI_BASE_URL;
        String appToken = context != null
                ? AppConfig.getInstance(context).getAppToken()
                : BuildConfig.APP_TOKEN;

        if (api == null || !baseUrl.equals(currentBaseUrl)) {
            currentBaseUrl = baseUrl;
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient http = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(45, TimeUnit.SECONDS)   // model calls are slow
                    .addInterceptor(logging)
                    .addInterceptor(chain -> chain.proceed(
                            chain.request().newBuilder()
                                    .addHeader("X-App-Token", appToken)
                                    .build()))
                    .build();

            api = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(http)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AiApi.class);
        }
        return api;
    }
}

package com.aidfinder.net;

import com.aidfinder.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Builds the one Retrofit client the app uses.
 *
 * The base URL comes from BuildConfig, so a debug build talks to your laptop
 * and a release build talks to your hosted service without changing any code.
 */
public class ApiClient {

    private static AiApi api;

    public static synchronized AiApi get() {
        if (api == null) {
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
                                    .addHeader("X-App-Token", BuildConfig.APP_TOKEN)
                                    .build()))
                    .build();

            api = new Retrofit.Builder()
                    .baseUrl(BuildConfig.AI_BASE_URL)
                    .client(http)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AiApi.class);
        }
        return api;
    }
}

package com.aidfinder.catalog;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the product catalog on the device.
 *
 * This class is the app's source of truth for product data. The AI service
 * only ever returns product ids - we look the real product up here. That is
 * what stops a made-up product from ever reaching a user.
 */
public class CatalogRepository {

    private static final String TAG = "CatalogRepository";
    private static CatalogRepository instance;

    private final List<Product> products;
    private final Map<String, Product> byId = new HashMap<>();

    private CatalogRepository(Context context) {
        this.products = load(context);
        for (Product p : products) {
            byId.put(p.id, p);
        }
    }

    public static synchronized CatalogRepository get(Context context) {
        if (instance == null) {
            instance = new CatalogRepository(context.getApplicationContext());
        }
        return instance;
    }

    private List<Product> load(Context context) {
        try (Reader reader = new InputStreamReader(
                context.getAssets().open("catalog.json"), StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, new TypeToken<List<Product>>() {}.getType());
        } catch (Exception e) {
            Log.e(TAG, "Could not read catalog.json", e);
            return new ArrayList<>();
        }
    }

    public List<Product> all() {
        return Collections.unmodifiableList(products);
    }

    /** Returns null if the id is not one of ours. Callers must handle null. */
    public Product findById(String id) {
        return byId.get(id);
    }
}

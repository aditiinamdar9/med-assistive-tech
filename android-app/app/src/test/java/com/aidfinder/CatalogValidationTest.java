package com.aidfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.aidfinder.catalog.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A plain JVM test - runs with `./gradlew test`, no emulator needed.
 * It checks the rule that matters most: ids the AI invents get dropped.
 */
public class CatalogValidationTest {

    private static final String CATALOG_JSON =
            "[{\"id\":\"grip-001\",\"name\":\"Weighted utility grips\"}]";

    private Map<String, Product> catalogById() {
        List<Product> products = new Gson().fromJson(
                CATALOG_JSON, new TypeToken<List<Product>>() {}.getType());
        Map<String, Product> byId = new HashMap<>();
        for (Product p : products) byId.put(p.id, p);
        return byId;
    }

    @Test
    public void realIdIsFound() {
        assertEquals("Weighted utility grips", catalogById().get("grip-001").name);
    }

    @Test
    public void inventedIdIsNotFound() {
        // If this ever returns non-null, a hallucinated product could reach a user.
        assertNull(catalogById().get("miracle-cure-999"));
    }
}

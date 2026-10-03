package com.aidfinder;

import com.aidfinder.catalog.Product;

/** A product from our own catalog, plus the plain-English reason the AI gave. */
public class RecommendationResult {
    public final Product product;
    public final String why;

    public RecommendationResult(Product product, String why) {
        this.product = product;
        this.why = why;
    }
}

package com.aidfinder.auth.model;

/** A product a user starred from their search results, plus the reason shown at the time. */
public class Favorite {
    private String productId;
    private String productName;
    private String why;

    public Favorite() {
    }

    public Favorite(String productId, String productName, String why) {
        this.productId = productId;
        this.productName = productName;
        this.why = why;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        this.why = why;
    }
}

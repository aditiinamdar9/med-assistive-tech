package com.aidfinder.auth.model;

/** One dated log entry a user wrote about a specific device, plus how helpful they rated it that time. */
public class JournalEntry {
    private long id;
    private String productId;
    private String productName;
    private String entryText;
    private int rating; // 1-5, set fresh on every entry - never pre-filled from a prior one
    private long createdAtMillis;

    public JournalEntry() {
    }

    public JournalEntry(long id, String productId, String productName, String entryText,
                         int rating, long createdAtMillis) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.entryText = entryText;
        this.rating = rating;
        this.createdAtMillis = createdAtMillis;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getEntryText() {
        return entryText;
    }

    public void setEntryText(String entryText) {
        this.entryText = entryText;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public void setCreatedAtMillis(long createdAtMillis) {
        this.createdAtMillis = createdAtMillis;
    }
}

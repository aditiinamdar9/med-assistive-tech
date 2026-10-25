package com.aidfinder.net.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecommendRequest {
    @SerializedName("user_text")
    public String userText;

    public List<CatalogItemDto> catalog;

    @SerializedName("user_context")
    public UserContextDto userContext;

    public RecommendRequest(String userText, List<CatalogItemDto> catalog) {
        this(userText, catalog, null);
    }

    public RecommendRequest(String userText, List<CatalogItemDto> catalog, UserContextDto userContext) {
        this.userText = userText;
        this.catalog = catalog;
        // Omit the field entirely rather than send an all-empty object.
        this.userContext = (userContext != null && !userContext.isBlank()) ? userContext : null;
    }
}

package com.aidfinder.net.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecommendRequest {
    @SerializedName("user_text")
    public String userText;

    public List<CatalogItemDto> catalog;

    public RecommendRequest(String userText, List<CatalogItemDto> catalog) {
        this.userText = userText;
        this.catalog = catalog;
    }
}

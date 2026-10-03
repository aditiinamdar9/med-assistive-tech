package com.aidfinder.net.dto;

/** Trimmed product data sent to the AI service. Price and buy links stay on the device. */
public class CatalogItemDto {
    public String id;
    public String name;
    public String category;
    public String tags;
    public String description;

    public CatalogItemDto(String id, String name, String category, String tags, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.tags = tags;
        this.description = description;
    }
}

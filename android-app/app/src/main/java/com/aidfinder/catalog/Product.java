package com.aidfinder.catalog;

/**
 * One item in the aid catalog. Loaded from assets/catalog.json.
 * Field names must match the JSON keys exactly - Gson maps them by name.
 */
public class Product {
    public String id;
    public String name;
    public String category;      // "physical" or "mental"
    public String tags;          // comma-separated difficulties, not conditions
    public String description;   // plain English, no medical words
    public double price;
    public String buyUrl;
}

# Gson needs the DTO field names kept, or JSON parsing silently returns nulls
# in release builds while working fine in debug. This is a classic one to hit.
-keep class com.aidfinder.net.dto.** { *; }
-keep class com.aidfinder.catalog.Product { *; }

package com.travelapi.patterns;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AppConfigManager {

    private final Map<String, String> configProperties;

    public AppConfigManager() {
        this.configProperties = new HashMap<>();
        initializeDefaultConfig();
    }

    private void initializeDefaultConfig() {
        configProperties.put("app.name", "Travel Booking API");
        configProperties.put("app.version", "1.0.0");
        configProperties.put("app.max.bookings.per.customer", "50");
        configProperties.put("app.default.currency", "USD");
        configProperties.put("app.booking.advance.days", "365");
    }

    public String getProperty(String key) {
        return configProperties.get(key);
    }

    public String getProperty(String key, String defaultValue) {
        return configProperties.getOrDefault(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        configProperties.put(key, value);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = configProperties.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Map<String, String> getAllProperties() {
        return new HashMap<>(configProperties);
    }
}

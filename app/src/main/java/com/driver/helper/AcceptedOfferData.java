package com.driver.helper;

import java.util.HashMap;
import java.util.Map;

/** Validates server data before it is used to open pickup navigation. */
final class AcceptedOfferData {
    static HashMap<String, String> navigationData(Map<String, String> data) {
        if (data == null || empty(data.get("ride_id")) || empty(data.get("user_id"))) return null;
        if (!coordinate(data.get("pickup_lat"), 90) || !coordinate(data.get("pickup_lng"), 180)
                || !coordinate(data.get("drop_lat"), 90) || !coordinate(data.get("drop_lng"), 180)) return null;
        HashMap<String, String> ride = new HashMap<>(data);
        ride.put("onride_id", data.get("ride_id"));
        ride.put("rideuserid", data.get("user_id"));
        // Acceptance always starts at pickup, never at an externally supplied ride phase.
        ride.remove("from");
        return ride;
    }

    private static boolean empty(String value) { return value == null || value.trim().isEmpty(); }
    private static boolean coordinate(String value, int limit) {
        try {
            double number = Double.parseDouble(value);
            return !Double.isNaN(number) && !Double.isInfinite(number) && Math.abs(number) <= limit;
        } catch (Exception invalid) { return false; }
    }
}

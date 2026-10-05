package com.driver.helper;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
public class AcceptedOfferDataTest {
    private Map<String, String> offer() {
        Map<String, String> data = new HashMap<>();
        data.put("ride_id", "ride-123"); data.put("user_id", "passenger-456");
        data.put("pickup_lat", "-6.8"); data.put("pickup_lng", "39.2");
        data.put("drop_lat", "-6.7"); data.put("drop_lng", "39.3");
        data.put("onride_otp", "1234"); return data;
    }
    @Test public void mapsAcceptanceToPickupWithoutChangingSource() {
        Map<String, String> source = offer(); source.put("from", "onride");
        Map<String, String> ride = AcceptedOfferData.navigationData(source);
        assertEquals("ride-123", ride.get("onride_id"));
        assertEquals("passenger-456", ride.get("rideuserid"));
        assertEquals("1234", ride.get("onride_otp"));
        assertNull(ride.get("from")); assertEquals("onride", source.get("from"));
    }
    @Test public void rejectsMissingIdentifiers() {
        Map<String, String> data = offer(); data.remove("ride_id");
        assertNull(AcceptedOfferData.navigationData(data));
        data = offer(); data.put("user_id", " "); assertNull(AcceptedOfferData.navigationData(data));
    }
    @Test public void rejectsInvalidCoordinates() {
        for (String invalid : new String[]{"NaN", "Infinity", "91", "bad", "", null}) {
            Map<String, String> data = offer(); data.put("pickup_lat", invalid);
            assertNull(AcceptedOfferData.navigationData(data));
        }
    }
    @Test public void acceptsZeroAndBoundaryCoordinates() {
        Map<String, String> data = offer(); data.put("pickup_lat", "0"); data.put("drop_lng", "-180");
        assertNotNull(AcceptedOfferData.navigationData(data));
    }
    @Test public void handlesNullPayload() { assertNull(AcceptedOfferData.navigationData(null)); }
}

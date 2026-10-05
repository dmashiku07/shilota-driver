package com.driver.helper;
import org.junit.Test;
import static org.junit.Assert.*;
public class RequestLocationLabelTest {
    @Test public void resolvesPlusCodesAndCoordinates() {
        assertTrue(RequestLocationLabel.needsResolution("75PW+2F Dar es Salaam"));
        assertTrue(RequestLocationLabel.needsResolution("6G6775PW+2F"));
        assertTrue(RequestLocationLabel.needsResolution("-6.8, 39.2"));
    }
    @Test public void preservesPassengerLandmarks() {
        assertFalse(RequestLocationLabel.needsResolution("Mlimani City, Ubungo"));
        assertFalse(RequestLocationLabel.needsResolution("Sam Nujoma Road"));
    }
    @Test public void fallbackRemovesCodeWithoutInventingStreet() {
        assertEquals("Dar es Salaam", RequestLocationLabel.areaFallback("75PW+2F Dar es Salaam"));
        assertEquals("", RequestLocationLabel.areaFallback("-6.8, 39.2"));
    }
    @Test public void readableAddressDeduplicatesAndSkipsCodes() {
        assertEquals("Masaki, Dar es Salaam", RequestLocationLabel.readableParts("75PW+2F", "Masaki", "masaki", "Dar es Salaam", "12", null));
    }
}

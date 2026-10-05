package com.driver.helper;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Keeps meaningful place names, excluding Plus Codes and coordinate-only labels. */
public final class RequestLocationLabel {
    private static final Pattern PLUS_CODE = Pattern.compile("(?i)(?<![A-Z0-9])[23456789CFGHJMPQRVWX]{2,8}\\+[23456789CFGHJMPQRVWX]{2,3}(?![A-Z0-9])");
    private static final Pattern COORDINATES = Pattern.compile("^\\s*-?\\d+(?:\\.\\d+)?\\s*,\\s*-?\\d+(?:\\.\\d+)?\\s*$");

    public static boolean needsResolution(String value) {
        return value == null || value.trim().isEmpty() || PLUS_CODE.matcher(value).find()
                || COORDINATES.matcher(value).matches();
    }

    public static String readableParts(String... values) {
        Set<String> seen = new LinkedHashSet<>();
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (value == null || needsResolution(value)) continue;
            String clean = value.trim();
            if (clean.matches("[\\d\\s-]+") || !seen.add(clean.toLowerCase(Locale.ROOT))) continue;
            if (result.length() > 0) result.append(", ");
            result.append(clean);
        }
        return result.toString();
    }

    public static String areaFallback(String value) {
        if (value == null || COORDINATES.matcher(value).matches()) return "";
        return PLUS_CODE.matcher(value).replaceAll("").replaceAll("^[,\\s]+|[,\\s]+$", "").trim();
    }
}

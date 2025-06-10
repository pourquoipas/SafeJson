/*
 * Copyright (c) 2025 Gianluca Terenziani
 *
 * Questo file è parte di SafeJson.
 * SafeJson è distribuito sotto i termini della licenza
 * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 *
 * Dovresti aver ricevuto una copia della licenza insieme a questo progetto.
 * In caso contrario, la puoi trovare su: http://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package net.gnius.safejson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * A wrapper class for org.json.JSONObject and org.json.JSONArray
 * to provide null-safe navigation and typed accessors.
 * <p>
 * It allows for chained 'get' calls without throwing NullPointerExceptions.
 * If a path is invalid or a value is null, subsequent operations will yield
 * a 'missing' or 'null' representation, and getter methods will return null
 * or default values.
 * </p>
 */
public class SafeJson {

    private final Object value;
    private final boolean isPresent;

    /**
     * Represents an instance where the path was not found or the value is missing.
     */
    private static final SafeJson MISSING_INSTANCE = new SafeJson(null, false);

    // Default date formats for parsing. ISO 8601 formats are prioritized.
    // SimpleDateFormat is not thread-safe, so new instances are created or used locally.
    private static final List<String> DEFAULT_DATE_PATTERNS = Arrays.asList(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", // ISO8601 with milliseconds and timezone (e.g., +01:00)
            "yyyy-MM-dd'T'HH:mm:ssXXX",     // ISO8601 with seconds and timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // ISO8601 with milliseconds, UTC (Zulu)
            "yyyy-MM-dd'T'HH:mm:ss'Z'",     // ISO8601 with seconds, UTC (Zulu)
            "yyyy-MM-dd",                   // ISO8601 date only
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy"
    );

    /**
     * Internal constructor.
     *
     * @param value     The actual JSON value (JSONObject, JSONArray, String, Number, Boolean, JSONObject.NULL).
     * @param isPresent True if the path to this value was valid, false otherwise.
     */
    private SafeJson(Object value, boolean isPresent) {
        if (!isPresent) {
            this.value = null; // Standardize MISSING_INSTANCE to have null value
            this.isPresent = false;
        } else {
            this.value = value; // Can be JSONObject.NULL
            this.isPresent = true;
        }
    }

    /**
     * Parses a JSON string into a SafeJson object.
     *
     * @param jsonString The JSON string to parse.
     * @return A SafeJson object wrapping the parsed JSON, or MISSING_INSTANCE if parsing fails.
     */
    public static SafeJson parse(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return MISSING_INSTANCE;
        }
        try {
            Object json = new JSONTokener(jsonString).nextValue();
            return new SafeJson(json, true);
        } catch (JSONException e) {
            // Optionally log the exception
            // System.err.println("Failed to parse JSON string: " + e.getMessage());
            return MISSING_INSTANCE;
        }
    }

    /**
     * Creates a SafeJson wrapper around an existing JSONObject.
     *
     * @param jsonObject The JSONObject to wrap.
     * @return A SafeJson object. If jsonObject is null, returns MISSING_INSTANCE.
     */
    public SafeJson(JSONObject jsonObject) {
        this(jsonObject, jsonObject != null);
    }

    /**
     * Creates a SafeJson wrapper around an existing JSONArray.
     *
     * @param jsonArray The JSONArray to wrap.
     * @return A SafeJson object. If jsonArray is null, returns MISSING_INSTANCE.
     */
    public SafeJson(JSONArray jsonArray) {
        this(jsonArray, jsonArray != null);
    }

    /**
     * Creates an empty SafeJson object wrapping an empty JSONObject.
     * @return A new SafeJson instance representing an empty JSON object.
     */
    public static SafeJson emptyObject() {
        return new SafeJson(new JSONObject(), true);
    }

    /**
     * Creates an empty SafeJson object wrapping an empty JSONArray.
     * @return A new SafeJson instance representing an empty JSON array.
     */
    public static SafeJson emptyArray() {
        return new SafeJson(new JSONArray(), true);
    }


    // --- Navigation Methods ---

    /**
     * Gets a value from the wrapped JSONObject by key.
     *
     * @param key The key.
     * @return A SafeJson object wrapping the value, or MISSING_INSTANCE if not found or not a JSONObject.
     */
    public SafeJson get(String key) {
        if (!isPresent || !(this.value instanceof JSONObject)) {
            return MISSING_INSTANCE;
        }
        JSONObject currentJson = (JSONObject) this.value;
        if (currentJson.has(key)) {
            // JSONObject.get() returns the actual value, which could be JSONObject.NULL
            return new SafeJson(currentJson.get(key), true);
        } else {
            return MISSING_INSTANCE;
        }
    }

    /**
     * Gets an element from the wrapped JSONArray by index.
     *
     * @param index The index.
     * @return A SafeJson object wrapping the element, or MISSING_INSTANCE if out of bounds or not a JSONArray.
     */
    public SafeJson get(int index) {
        if (!isPresent || !(this.value instanceof JSONArray)) {
            return MISSING_INSTANCE;
        }
        JSONArray currentArray = (JSONArray) this.value;
        if (index >= 0 && index < currentArray.length()) {
            // JSONArray.get() returns the actual value, which could be JSONObject.NULL
            return new SafeJson(currentArray.get(index), true);
        } else {
            return MISSING_INSTANCE;
        }
    }

    /**
     * Convenience method to get a value from a JSONObject (by key) and then an element from a JSONArray (by index).
     * Equivalent to {@code this.get(key).get(index)}.
     *
     * @param key   The key for the JSONObject.
     * @param index The index for the JSONArray.
     * @return A SafeJson object, or MISSING_INSTANCE.
     */
    public SafeJson get(String key, int index) {
        SafeJson intermediate = get(key);
        // If intermediate is MISSING_INSTANCE, its get(index) will also return MISSING_INSTANCE
        return intermediate.get(index);
    }

    // --- State Checking Methods ---

    /**
     * Checks if the current SafeJson represents a missing path or an explicit JSON null value.
     *
     * @return True if missing or JSON null, false otherwise.
     */
    public boolean isNull() {
        return !isPresent || JSONObject.NULL.equals(this.value);
    }

    /**
     * Checks if the wrapped value is a JSONObject.
     * @return True if it is a JSONObject and the path was valid, false otherwise.
     */
    public boolean isJsonObject() {
        return isPresent && this.value instanceof JSONObject;
    }

    /**
     * Checks if the wrapped value is a JSONArray.
     * @return True if it is a JSONArray and the path was valid, false otherwise.
     */
    public boolean isJsonArray() {
        return isPresent && this.value instanceof JSONArray;
    }

    /**
     * Checks if the wrapped value is a String.
     * @return True if it is a String and the path was valid, false otherwise.
     */
    public boolean isString() {
        return isPresent && this.value instanceof String;
    }

    /**
     * Checks if the wrapped value is a Number (Integer, Long, Double, BigDecimal, etc.).
     * @return True if it is a Number and the path was valid, false otherwise.
     */
    public boolean isNumber() {
        return isPresent && this.value instanceof Number;
    }

    /**
     * Checks if the wrapped value is an Integer.
     * @return True if it is an Integer and the path was valid, false otherwise.
     */
    public boolean isInteger() {
        return isPresent && this.value instanceof Integer;
    }

    /**
     * Checks if the wrapped value is a Long or an Integer (which can be treated as Long).
     * @return True if it is a Long or Integer and the path was valid, false otherwise.
     */
    public boolean isLong() {
        return isPresent && (this.value instanceof Long || this.value instanceof Integer);
    }

    /**
     * Checks if the wrapped value is a Double or BigDecimal (common floating-point types in org.json).
     * @return True if it is a Double or BigDecimal and the path was valid, false otherwise.
     */
    public boolean isDouble() {
        return isPresent && (this.value instanceof Double || this.value instanceof BigDecimal);
    }

    /**
     * Checks if the wrapped value is a BigDecimal.
     * @return True if it is a BigDecimal and the path was valid, false otherwise.
     */
    public boolean isBigDecimal() {
        return isPresent && this.value instanceof BigDecimal;
    }

    /**
     * Checks if the wrapped value is a Boolean.
     * @return True if it is a Boolean and the path was valid, false otherwise.
     */
    public boolean isBoolean() {
        return isPresent && this.value instanceof Boolean;
    }

    /**
     * Checks if the wrapped string value can be parsed into a Date.
     * Uses a default list of common date patterns or custom patterns if provided.
     *
     * @param customPatterns Optional custom date patterns to try first.
     * @return True if the value is a string and can be parsed into a Date.
     */
    public boolean isDate(String... customPatterns) {
        return getDate(customPatterns) != null;
    }

    // --- Value Retrieval Methods ---

    /**
     * Gets the underlying raw Java object.
     *
     * @return The object (e.g., JSONObject, JSONArray, String, Number, Boolean),
     * or null if this SafeJson represents a missing path or an explicit JSON null.
     */
    public Object getValue() {
        if (isNull()) {
            return null;
        }
        return this.value;
    }

    /**
     * Gets the value as a String. If the underlying value is not a String,
     * its {@code toString()} representation is returned.
     * For JSONObject and JSONArray, this will be their JSON string representation.
     *
     * @return The string representation, or null if missing/JSON null.
     */
    public String getAsString() {
        if (isNull()) {
            return null;
        }
        return String.valueOf(this.value);
    }

    /**
     * Gets the value as a String, only if it is inherently a String.
     *
     * @return The String value, or null if not a String or if missing/JSON null.
     */
    public String getString() {
        if (isString()) {
            return (String) this.value;
        }
        return null;
    }

    /**
     * Gets the value as an Integer. Attempts conversion if the value is a Number or a parsable String.
     *
     * @return The Integer value, or null if not convertible or if missing/JSON null.
     */
    public Integer getInteger() {
        if (isNull()) return null;
        if (this.value instanceof Integer) return (Integer) this.value;
        if (this.value instanceof Number) return ((Number) this.value).intValue();
        if (this.value instanceof String) {
            try {
                return new BigDecimal((String) this.value).intValueExact();
            } catch (NumberFormatException | ArithmeticException e) { /* fall through */ }
        }
        return null;
    }

    /**
     * Gets the value as a Long. Attempts conversion if the value is a Number or a parsable String.
     *
     * @return The Long value, or null if not convertible or if missing/JSON null.
     */
    public Long getLong() {
        if (isNull()) return null;
        if (this.value instanceof Long) return (Long) this.value;
        if (this.value instanceof Number) return ((Number) this.value).longValue();
        if (this.value instanceof String) {
            try {
                return new BigDecimal((String) this.value).longValueExact();
            } catch (NumberFormatException | ArithmeticException e) { /* fall through */ }
        }
        return null;
    }

    /**
     * Gets the value as a Double. Attempts conversion if the value is a Number (including BigDecimal) or a parsable String.
     *
     * @return The Double value, or null if not convertible or if missing/JSON null.
     */
    public Double getDouble() {
        if (isNull()) return null;
        if (this.value instanceof Double) return (Double) this.value;
        if (this.value instanceof BigDecimal) return ((BigDecimal) this.value).doubleValue();
        if (this.value instanceof Number) return ((Number) this.value).doubleValue();
        if (this.value instanceof String) {
            try {
                return Double.parseDouble((String) this.value);
            } catch (NumberFormatException e) { /* fall through */ }
        }
        return null;
    }

    /**
     * Gets the value as a BigDecimal (referred to as 'Decimal' in the request).
     * Attempts conversion if the value is a Number or a parsable String.
     *
     * @return The BigDecimal value, or null if not convertible or if missing/JSON null.
     */
    public BigDecimal getBigDecimal() {
        if (isNull()) return null;
        if (this.value instanceof BigDecimal) return (BigDecimal) this.value;
        if (this.value instanceof String) {
            try {
                return new BigDecimal((String) this.value);
            } catch (NumberFormatException e) { /* fall through */ }
        }
        // For other numbers (Integer, Long, Double), convert via string to preserve precision as much as possible
        // for types like Double, though direct construction from Double is also an option.
        if (this.value instanceof Number) {
            try {
                return new BigDecimal(this.value.toString());
            } catch (NumberFormatException e) { /* fall through */ }
        }
        return null;
    }

    /**
     * Gets the value as a Boolean. Converts "true" or "false" strings (case-insensitive).
     *
     * @return The Boolean value, or null if not convertible or if missing/JSON null.
     */
    public Boolean getBoolean() {
        if (isNull()) return null;
        if (this.value instanceof Boolean) return (Boolean) this.value;
        if (this.value instanceof String) {
            String sVal = ((String) this.value).toLowerCase();
            if ("true".equals(sVal)) return true;
            if ("false".equals(sVal)) return false;
        }
        return null;
    }

    /**
     * Gets the value as a Date if it's a string parsable by the defined date formats.
     *
     * @param customPatterns Optional custom date patterns to try first.
     * Patterns should conform to SimpleDateFormat.
     * @return The Date object, or null if not a parsable string or if missing/JSON null.
     */
    public Date getDate(String... customPatterns) {
        if (!isString()) return null;
        String strValue = (String) this.value;

        List<String> patternsToTry = new ArrayList<>();
        if (customPatterns != null && customPatterns.length > 0) {
            patternsToTry.addAll(Arrays.asList(customPatterns));
        }
        patternsToTry.addAll(DEFAULT_DATE_PATTERNS);

        for (String pattern : patternsToTry) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                // For ISO8601 'Z', SimpleDateFormat interprets it as a literal 'Z' unless handled.
                // Often, it's better to use java.time for modern date/time handling,
                // but for compatibility with java.util.Date and SimpleDateFormat:
                if (pattern.endsWith("'Z'")) {
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                return sdf.parse(strValue);
            } catch (ParseException e) {
                // Try next format
            }
        }
        return null;
    }

    /**
     * Gets the underlying JSONObject if this SafeJson wraps one.
     * @return The JSONObject, or null if not a JSONObject or if missing/JSON null.
     */
    public JSONObject getJsonObject() {
        if (isJsonObject()) {
            return (JSONObject) this.value;
        }
        return null;
    }

    /**
     * Gets the underlying JSONArray if this SafeJson wraps one.
     * @return The JSONArray, or null if not a JSONArray or if missing/JSON null.
     */
    public JSONArray getJsonArray() {
        if (isJsonArray()) {
            return (JSONArray) this.value;
        }
        return null;
    }

    // --- Setter Methods ---

    /**
     * Helper to unwrap SafeJson instances or convert Java null to JSONObject.NULL for setters.
     */
    private Object prepareValueForSetting(Object val) {
        if (val instanceof SafeJson) {
            SafeJson sjVal = (SafeJson) val;
            return sjVal.isPresent ? sjVal.value : JSONObject.NULL;
        }
        if (val == null) {
            return JSONObject.NULL;
        }
        // org.json.JSONObject/JSONArray are quite flexible with what they accept in put(),
        // including collections, maps, arrays, primitives, etc.
        return val;
    }

    /**
     * Puts a key-value pair into the wrapped JSONObject.
     * If this SafeJson does not wrap a JSONObject, this operation does nothing.
     *
     * @param key   The key.
     * @param value The value to set (can be a primitive, String, Map, Collection, SafeJson, etc.).
     * @return This SafeJson instance for chaining.
     */
    public SafeJson put(String key, Object value) {
        if (isJsonObject()) {
            Object processedValue = prepareValueForSetting(value);
            ((JSONObject) this.value).put(key, processedValue);
        }
        // If not a JSONObject, or if missing, it's a no-op for safety.
        // Could throw an exception or log a warning if strict behavior is desired.
        return this;
    }

    /**
     * Adds a value to the end of the wrapped JSONArray.
     * If this SafeJson does not wrap a JSONArray, this operation does nothing.
     *
     * @param value The value to add.
     * @return This SafeJson instance for chaining.
     */
    public SafeJson add(Object value) {
        if (isJsonArray()) {
            Object processedValue = prepareValueForSetting(value);
            ((JSONArray) this.value).put(processedValue);
        }
        return this;
    }

    /**
     * Puts a value into the wrapped JSONArray at a specific index.
     * If this SafeJson does not wrap a JSONArray, or if the index is out of bounds,
     * this operation does nothing.
     *
     * @param index The index at which to set the value.
     * @param value The value to set.
     * @return This SafeJson instance for chaining.
     */
    public SafeJson put(int index, Object value) {
        if (isJsonArray()) {
            JSONArray arr = (JSONArray) this.value;
            if (index >= 0 && index < arr.length()) { // Only allow update, not auto-extending
                Object processedValue = prepareValueForSetting(value);
                try {
                    arr.put(index, processedValue);
                } catch (JSONException e) {
                    // Should not happen if index is checked, but org.json might have other reasons
                    // System.err.println("Error putting value into JSONArray at index " + index + ": " + e.getMessage());
                }
            }
        }
        return this;
    }

    // --- Utility Methods ---

    /**
     * Returns the number of elements if this wraps a JSONObject (number of keys)
     * or a JSONArray (number of elements).
     * @return The size, or 0 if not applicable or if missing/null.
     */
    public int size() {
        if (isJsonObject()) {
            return ((JSONObject) this.value).length();
        }
        if (isJsonArray()) {
            return ((JSONArray) this.value).length();
        }
        return 0;
    }

    /**
     * Checks if the wrapped JSONObject or JSONArray is empty, or if this SafeJson is missing/null.
     * @return True if empty, missing, or null. False for primitives or non-empty collections.
     */
    public boolean isEmpty() {
        if (isNull()) return true; // Missing or explicit null is considered empty of data.
        if (isJsonObject()) {
            return ((JSONObject) this.value).length() == 0;
        }
        if (isJsonArray()) {
            return ((JSONArray) this.value).length() == 0;
        }
        // Primitives are not considered "empty" in this context unless they are null (handled by isNull()).
        return false;
    }


    @Override
    public String toString() {
        if (!isPresent) {
            return "SafeJson[MISSING]";
        }
        if (JSONObject.NULL.equals(value)) {
            return "SafeJson[JSON_NULL]";
        }
        if (value == null) { // Should ideally not happen if isPresent is true due to constructor logic
            return "SafeJson[JAVA_NULL_UNEXPECTED]";
        }
        // For JSONObject and JSONArray, their own toString methods provide the JSON string.
        return "SafeJson[" + value.toString() + "]";
    }

    /**
     * Provides the JSON string representation of the wrapped object.
     * If the wrapped object is a JSONObject or JSONArray, it's pretty-printed with an indent factor of 2.
     * Otherwise, it returns the string representation via getAsString().
     *
     * @return The formatted JSON string or string representation, or "null" if missing/JSON null.
     */
    public String toJsonString() {
        return toJsonString(0);
    }

    /**
     * Provides the JSON string representation of the wrapped object with a specific indent factor for pretty-printing.
     *
     * @param indentFactor The number of spaces to use for indentation (0 for compact).
     * @return The formatted JSON string or string representation, or "null" if missing/JSON null.
     */
    public String toJsonString(int indentFactor) {
        if (isNull()) {
            return "null"; // Standard JSON representation of null
        }
        try {
            if (this.value instanceof JSONObject) {
                return ((JSONObject) this.value).toString(indentFactor);
            }
            if (this.value instanceof JSONArray) {
                return ((JSONArray) this.value).toString(indentFactor);
            }
        } catch (JSONException e) {
            // Should not happen with valid JSONObject/JSONArray
            return String.valueOf(this.value); // Fallback
        }
        // For primitives, strings, etc., just use their string value.
        // JSON spec requires strings to be quoted. org.json.JSONObject.valueToString handles this.
        return JSONObject.valueToString(this.value);
    }

}

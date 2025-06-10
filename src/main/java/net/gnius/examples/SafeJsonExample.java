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
package net.gnius.examples;

import net.gnius.safejson.SafeJson;
import org.json.JSONObject;

public class SafeJsonExample {

    // --- Main method for demonstration ---
    public static void main(String[] args) {
        String jsonInput = "{\n" +
                "\"key_one\": \"value_one\",\n" +
                "\"key_two\": 123,\n" +
                "\"key_three\": \"2025-06-24\",\n" +
                "\"key_four\": null,\n" +
                "\"key_five\": 54.321,\n" +
                "\"key_array\": [ {\n" +
                "\"subkey\": \"a\",\n" +
                "\"value\": 10 }, {\n" +
                "\"subkey\": \"b\",\n" +
                "\"value\": 20}, {\n" +
                "\"different_subkey\": \"prova\" }],\n" +
                "\"boolean_true\": true,\n" +
                "\"boolean_false\": \"false\",\n" +
                "\"empty_obj\": {},\n" +
                "\"empty_arr\": []\n" +
                "}";

        SafeJson root = SafeJson.parse(jsonInput);

        System.out.println("Root is JSONObject: " + root.isJsonObject()); // true
        System.out.println("key_one: " + root.get("key_one").getString()); // value_one
        System.out.println("key_two (Long): " + root.get("key_two").getLong()); // 123
        System.out.println("key_three (Date): " + root.get("key_three").getDate()); // Should parse
        System.out.println("key_four isNull: " + root.get("key_four").isNull()); // true
        System.out.println("key_four value: " + root.get("key_four").getValue()); // null
        System.out.println("key_five (BigDecimal): " + root.get("key_five").getBigDecimal()); // 54.321
        System.out.println("key_five (Double): " + root.get("key_five").getDouble()); // 54.321

        SafeJson keyArray = root.get("key_array");
        System.out.println("key_array isArray: " + keyArray.isJsonArray()); // true
        System.out.println("key_array size: " + keyArray.size()); // 3

        // Accessing array elements
        System.out.println("key_array[0].subkey: " + keyArray.get(0).get("subkey").getString()); // "a"
        System.out.println("key_array[1].value (Integer): " + keyArray.get(1).get("value").getInteger()); // 20

        // Chained get with key and index
        System.out.println("key_array[2].different_subkey: " + root.get("key_array", 2).get("different_subkey").getString()); // "prova"

        // Null-safe chaining for non-existent paths
        SafeJson nonExistent = root.get("non_existent_key").get("no_key", 10).get("unknown");
        System.out.println("non_existent.isNull(): " + nonExistent.isNull()); // true
        System.out.println("non_existent.getValue(): " + nonExistent.getValue()); // null
        System.out.println("non_existent.getString(): " + nonExistent.getString()); // null

        System.out.println("key_array[2].subkey (non-existent): " + root.get("key_array", 2).get("subkey").getString()); // null
        System.out.println("key_array[2].subkey isNull: " + root.get("key_array", 2).get("subkey").isNull()); // true

        // Boolean tests
        System.out.println("boolean_true: " + root.get("boolean_true").getBoolean()); // true
        System.out.println("boolean_false (from string): " + root.get("boolean_false").getBoolean()); // true (parsed "false")

        // Setters
        System.out.println("\n--- Modifying JSON ---");
        SafeJson modifiable = SafeJson.parse(jsonInput); // Create a new modifiable copy
        modifiable.get("key_one").put("new_subkey", "new_value_for_key_one"); // This won't work as key_one is a String
        // Let's modify an object
        modifiable.put("new_top_level_key", "hello world");
        modifiable.get("key_array").get(0).put("value", 1000); // Change value of key_array[0].value
        modifiable.get("key_array").add(new JSONObject().put("added", true)); // Add new object to array

        System.out.println("Modified JSON: \n" + modifiable.toJsonString(2));

        SafeJson itemToModify = modifiable.get("key_array").get(0);
        if(itemToModify.isJsonObject()){
            itemToModify.put("another_new_field", 123.456);
        }
        System.out.println("After further modification on item: \n" + modifiable.toJsonString(2));

        // Test empty objects/arrays
        System.out.println("empty_obj is empty: " + root.get("empty_obj").isEmpty()); // true
        System.out.println("empty_obj size: " + root.get("empty_obj").size());       // 0
        System.out.println("empty_arr is empty: " + root.get("empty_arr").isEmpty()); // true
        System.out.println("empty_arr size: " + root.get("empty_arr").size());       // 0
        System.out.println("key_one isEmpty (String): " + root.get("key_one").isEmpty()); // false (unless string is empty)
        System.out.println("non_existent isEmpty: " + nonExistent.isEmpty()); // true
    }
}

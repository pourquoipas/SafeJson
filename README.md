## SafeJson: Null-Safe JSON Navigation for Java

`SafeJson` is a Java utility class designed to simplify working with JSON objects and arrays, particularly when dealing with potentially missing keys or null values. It wraps the `org.json.JSONObject` and `org.json.JSONArray` classes to provide a fluent, null-safe API for navigating JSON structures and retrieving typed values.

### Features

* **Null-Safe Chaining:** Traverse nested JSON structures with chained `get()` calls without worrying about `NullPointerExceptions`. If a key or index doesn't exist, or if a value is `null`, subsequent operations gracefully handle the missing data.
* **Typed Getters:** Retrieve values as specific Java types (String, Integer, Long, Double, BigDecimal, Boolean, Date, JSONObject, JSONArray) with built-in parsing and conversion.
* **Type Checking:** Easily check the type of a JSON value (`isString()`, `isJsonObject()`, `isNumber()`, etc.).
* **Fluent Setters:** Modify JSON objects and arrays using `put()` and `add()` methods that return the `SafeJson` instance for continued chaining.
* **Easy Initialization:** Parse JSON strings or wrap existing `org.json.JSONObject` or `org.json.JSONArray` instances.
* **Clear Representation of Missing/Null Data:** Differentiates between a path that doesn't exist and an explicit JSON `null` value, both handled safely.

### Why Use SafeJson?

Working directly with `org.json.JSONObject` often requires repetitive checks for key existence (`has()`) and null values to avoid `JSONExceptions` or `NullPointerExceptions`. `SafeJson` abstracts these checks away, leading to cleaner and more readable code.

```java
// Traditional org.json approach
JSONObject json = ...;
String value = null;
if (json.has("data")) {
    Object dataObj = json.get("data");
    if (dataObj instanceof JSONObject) {
        JSONObject dataJson = (JSONObject) dataObj;
        if (dataJson.has("items")) {
            Object itemsObj = dataJson.get("items");
            if (itemsObj instanceof JSONArray) {
                JSONArray itemsArray = (JSONArray) itemsObj;
                if (itemsArray.length() > 0) {
                    Object firstItemObj = itemsArray.get(0);
                    if (firstItemObj instanceof JSONObject) {
                        JSONObject firstItemJson = (JSONObject) firstItemObj;
                        if (firstItemJson.has("name")) {
                            value = firstItemJson.optString("name", null);
                        }
                    }
                }
            }
        }
    }
}

// With SafeJson
SafeJson safeJson = SafeJson.parse(...);
String value = safeJson.get("data").get("items").get(0).get("name").getString();
// 'value' will be the string if the path exists, or null otherwise. No exceptions.
```

### Basic Usage

#### 1. Parsing JSON

```java
String jsonString = "{\"name\": \"John Doe\", \"age\": 30, \"city\": null, \"details\": {\"status\": \"active\"}}";
SafeJson root = SafeJson.parse(jsonString);
```

#### 2. Navigating and Getting Values

```java
String name = root.get("name").getString(); // "John Doe"
Integer age = root.get("age").getInteger(); // 30
String city = root.get("city").getString(); // null (because value is JSON null)
boolean cityIsNull = root.get("city").isNull(); // true

String status = root.get("details").get("status").getString(); // "active"

// Non-existent path
String street = root.get("address").get("street").getString(); // null
boolean streetIsNull = root.get("address").get("street").isNull(); // true (because path is missing)
Object rawValue = root.get("address").get("street").getValue(); // null
```

#### 3. Working with Arrays

```java
String jsonArrayString = "[{\"item\": \"Book\"}, {\"item\": \"Pen\"}]";
SafeJson array = SafeJson.parse(jsonArrayString);

String firstItem = array.get(0).get("item").getString(); // "Book"
int arraySize = array.size(); // 2
```

#### 4. Type Checking

```java
if (root.get("age").isNumber()) {
    // ...
}
if (root.get("details").isJsonObject()) {
    // ...
}
```

#### 5. Default Values (Implicitly Null)

If a path doesn't exist or the value is null, typed getters return `null` (for objects) or their default wrapper equivalent won't be directly available (e.g. you'd get `null` for `getInteger()` not `0`). You can handle this with standard Java null checks or `Optional`.

```java
String nonExistentValue = root.get("nonExistent").getString(); // returns null
Integer nonExistentInt = root.get("nonExistent").getInteger(); // returns null
```

#### 6. Modifying JSON

```java
SafeJson newJson = SafeJson.emptyObject();
newJson.put("greeting", "Hello");
newJson.put("count", 100);

SafeJson items = SafeJson.emptyArray();
items.add("item1");
items.add(SafeJson.emptyObject().put("id", 1).put("name", "SubItem"));

newJson.put("items", items);

System.out.println(newJson.toJsonString(2));
/*
Output:
{
  "greeting": "Hello",
  "count": 100,
  "items": [
    "item1",
    {
      "name": "SubItem",
      "id": 1
    }
  ]
}
*/
```

### Maven Project Structure and Setup

To use `SafeJson` in your Maven project and separate the main class logic from test/demonstration code:

1.  **Dependency:** `SafeJson` relies on the `org.json` library. Add it to your `pom.xml`:

    ```xml
    <dependencies>
        <dependency>
            <groupId>org.json</groupId>
            <artifactId>json</artifactId>
            <version>20231013</version> </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.10.0</version> <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    ```

2.  **Project Directory Structure:**

    ```
    your-project/
    ├── pom.xml
    └── src/
        ├── main/
        │   └── java/
        │       └── com/yourpackage/   (or your chosen package structure)
        │           └── SafeJson.java
        └── test/
            └── java/
                └── com/yourpackage/   (mirroring main package structure)
                    └── SafeJsonTest.java
    ```

3.  **`SafeJson.java`:**
    Place the `SafeJson.java` class (as provided previously, without the `main` method) in `src/main/java/com/yourpackage/SafeJson.java`.

4.  **`SafeJsonTest.java`:**
    Create a JUnit test class. Move the demonstration code from the original `main` method into test methods here.

    ```java
    // src/test/java/com/yourpackage/SafeJsonTest.java
    package com.yourpackage;

    import org.json.JSONObject; // If needed for constructing test inputs directly
    import org.junit.jupiter.api.Test;
    import static org.junit.jupiter.api.Assertions.*;
    import java.math.BigDecimal; // Added for testBasicNavigationAndGetters

    public class SafeJsonTest {

        private final String sampleJsonInput = "{\n" +
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

        @Test
        void testBasicNavigationAndGetters() {
            SafeJson root = SafeJson.parse(sampleJsonInput);

            assertTrue(root.isJsonObject());
            assertEquals("value_one", root.get("key_one").getString());
            assertEquals(Long.valueOf(123), root.get("key_two").getLong());
            assertNotNull(root.get("key_three").getDate("yyyy-MM-dd"));
            assertTrue(root.get("key_four").isNull());
            assertNull(root.get("key_four").getValue());
            assertEquals(0, new BigDecimal("54.321").compareTo(root.get("key_five").getBigDecimal()));
        }

        @Test
        void testArrayAccess() {
            SafeJson root = SafeJson.parse(sampleJsonInput);
            SafeJson keyArray = root.get("key_array");

            assertTrue(keyArray.isJsonArray());
            assertEquals(3, keyArray.size());
            assertEquals("a", keyArray.get(0).get("subkey").getString());
            assertEquals(Integer.valueOf(20), keyArray.get(1).get("value").getInteger());
            assertEquals("prova", root.get("key_array", 2).get("different_subkey").getString());
        }

        @Test
        void testNullSafeChainingForNonExistentPaths() {
            SafeJson root = SafeJson.parse(sampleJsonInput);
            SafeJson nonExistent = root.get("non_existent_key").get("no_key", 10).get("unknown");

            assertTrue(nonExistent.isNull());
            assertNull(nonExistent.getValue());
            assertNull(nonExistent.getString());

            assertNull(root.get("key_array", 2).get("subkey").getString());
            assertTrue(root.get("key_array", 2).get("subkey").isNull());
        }

        @Test
        void testBooleanValues() {
            SafeJson root = SafeJson.parse(sampleJsonInput);
            assertEquals(Boolean.TRUE, root.get("boolean_true").getBoolean());
            // The current getBoolean() implementation parses "true" and "false" strings correctly.
            assertEquals(Boolean.FALSE, root.get("boolean_false").getBoolean());
        }
        
        @Test
        void testIsEmptyAndSize() {
            SafeJson root = SafeJson.parse(sampleJsonInput);
            assertTrue(root.get("empty_obj").isEmpty());
            assertEquals(0, root.get("empty_obj").size());
            assertTrue(root.get("empty_arr").isEmpty());
            assertEquals(0, root.get("empty_arr").size());
            
            assertFalse(root.get("key_one").isEmpty()); // String "value_one" is not empty
            
            // Test for an empty string value within the JSON
            SafeJson emptyStringJson = SafeJson.parse("{\"empty_string_val\": \"\"}");
            // SafeJson.isEmpty() on a node wrapping "" (empty string) will be false, because the node is present and not a JSON null.
            // To check if the *string content* is empty:
            String retrievedEmptyString = emptyStringJson.get("empty_string_val").getString();
            assertNotNull(retrievedEmptyString); 
            assertTrue(retrievedEmptyString.isEmpty()); 
            // The SafeJson node itself is not "empty" in the sense of missing or being a collection with no elements.
            assertFalse(emptyStringJson.get("empty_string_val").isEmpty());


            SafeJson nonExistent = root.get("non_existent_key");
            assertTrue(nonExistent.isEmpty()); // Missing is considered empty
        }

        @Test
        void testSetters() {
            SafeJson modifiable = SafeJson.parse(sampleJsonInput);
            
            modifiable.get("key_one").put("new_subkey", "new_value_for_key_one"); 
            assertEquals("value_one", modifiable.get("key_one").getString()); // Unchanged

            modifiable.put("new_top_level_key", "hello world");
            assertEquals("hello world", modifiable.get("new_top_level_key").getString());

            modifiable.get("key_array").get(0).put("value", 1000);
            assertEquals(Integer.valueOf(1000), modifiable.get("key_array").get(0).get("value").getInteger());

            SafeJson newArrayItem = SafeJson.emptyObject().put("added", true);
            modifiable.get("key_array").add(newArrayItem);
            assertEquals(4, modifiable.get("key_array").size());
            assertTrue(modifiable.get("key_array").get(3).get("added").getBoolean());
        }
         @Test
        void testDateParsing() {
           SafeJson dateJson = SafeJson.parse("{\"event_date\": \"2024-07-15T10:30:00Z\"}");
           assertNotNull(dateJson.get("event_date").getDate());
   
           SafeJson simpleDateJson = SafeJson.parse("{\"simple_date\": \"2023/12/25\"}");
           assertNotNull(simpleDateJson.get("simple_date").getDate("yyyy/MM/dd"));
       }
    }
    ```

   **Note on `SafeJsonTest.java`:**
   * The test `testBooleanValues` for `"boolean_false"` string was updated to reflect the current `getBoolean()` implementation which correctly parses `"true"`/`"false"` strings.
   * The `testIsEmptyAndSize` has a comment regarding string emptiness. `SafeJson.isEmpty()` is primarily for `null`/`MISSING_INSTANCE` or empty `JSONObject`/`JSONArray`. If you need to check if a *retrieved string value* is empty, you'd do `safeJson.get("myString").getString().isEmpty()` after checking for null.
   * The setter test for `modifiable.get("key_one").put(...)` correctly shows that this won't modify the string "value_one" itself into an object, as `get("key_one")` returns a `SafeJson` wrapping a string, and its `put` method (intended for `SafeJson` wrapping `JSONObject`) will be a no-op.

By structuring your project this way, `SafeJson` becomes a reusable library component, and its functionality can be thoroughly verified with unit tests.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/pourquoipas/SafeJson)
[![License: MIT](https://img.shields.io/badge/License-CC_BY--NC--SA--4.0-green)](http://creativecommons.org/licenses/by-nc-sa/4.0/)

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

## Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue for bugs, feature requests, or improvements.

---

## License

This project is licensed under the CC BY-NC-SA 4.0 License. See the `LICENSE` file for details.

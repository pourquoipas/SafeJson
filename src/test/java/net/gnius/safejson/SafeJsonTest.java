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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
// Assicurati che SafeJson sia accessibile, ad es. import com.yourpackage.SafeJson;

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

        assertTrue(root.isJsonObject(), "L'oggetto JSON radice analizzato ('root') dovrebbe essere un JSONObject.");
        assertEquals("value_one", root.get("key_one").getString(), "Il valore per 'key_one' dovrebbe essere 'value_one'.");
        assertEquals(Long.valueOf(123), root.get("key_two").getLong(), "Il valore per 'key_two' dovrebbe essere Long 123.");
        assertNotNull(root.get("key_three").getDate("yyyy-MM-dd"), "Il valore per 'key_three' ('2025-06-24') dovrebbe essere analizzato come una Data valida.");
        assertTrue(root.get("key_four").isNull(), "Il valore per 'key_four' (che è JSON null) dovrebbe essere considerato nullo da SafeJson (isNull() deve essere true).");
        assertNull(root.get("key_four").getValue(), "getValue() per 'key_four' (JSON null) dovrebbe restituire Java null.");
        // Per BigDecimal, compareTo è spesso più robusto di equals() se la scala può variare.
        assertEquals(0, new BigDecimal("54.321").compareTo(root.get("key_five").getBigDecimal()), "Il valore BigDecimal per 'key_five' non corrisponde a 54.321 (controllo tramite compareTo).");
    }

    @Test
    void testArrayAccess() {
        SafeJson root = SafeJson.parse(sampleJsonInput);
        SafeJson keyArray = root.get("key_array");

        assertTrue(keyArray.isJsonArray(), "L'elemento 'key_array' dovrebbe essere un JSONArray.");
        assertEquals(3, keyArray.size(), "La dimensione di 'key_array' dovrebbe essere 3.");
        assertEquals("a", keyArray.get(0).get("subkey").getString(), "Il valore di 'key_array[0].subkey' dovrebbe essere 'a'.");
        assertEquals(Integer.valueOf(20), keyArray.get(1).get("value").getInteger(), "Il valore di 'key_array[1].value' dovrebbe essere Integer 20.");
        assertEquals("prova", root.get("key_array", 2).get("different_subkey").getString(), "Il valore di 'key_array[2].different_subkey' usando get(key, index) dovrebbe essere 'prova'.");
    }

    @Test
    void testNullSafeChainingForNonExistentPaths() {
        SafeJson root = SafeJson.parse(sampleJsonInput);
        SafeJson nonExistent = root.get("non_existent_key").get("no_key", 10).get("unknown");

        assertTrue(nonExistent.isNull(), "Una catena di get() su un percorso non esistente ('non_existent_key.no_key[10].unknown') dovrebbe risultare in isNull() true.");
        assertNull(nonExistent.getValue(), "getValue() su un percorso non esistente ('non_existent_key.no_key[10].unknown') dovrebbe restituire null.");
        assertNull(nonExistent.getString(), "getString() su un percorso non esistente ('non_existent_key.no_key[10].unknown') dovrebbe restituire null.");

        SafeJson nonExistentSubKey = root.get("key_array").get(2).get("subkey");
        assertNull(nonExistentSubKey.getString(), "L'accesso a una sottochiave non esistente ('subkey') in 'key_array[2]' dovrebbe restituire una stringa nulla.");
        assertTrue(nonExistentSubKey.isNull(), "L'accesso a una sottochiave non esistente ('subkey') in 'key_array[2]' dovrebbe risultare in isNull() true.");
    }

    @Test
    void testBooleanValues() {
        SafeJson root = SafeJson.parse(sampleJsonInput);
        assertEquals(Boolean.TRUE, root.get("boolean_true").getBoolean(), "Il valore per 'boolean_true' (true booleano JSON) dovrebbe essere Boolean TRUE.");
        assertEquals(Boolean.FALSE, root.get("boolean_false").getBoolean(), "Il valore per 'boolean_false' (stringa \"false\" JSON) dovrebbe essere analizzato come Boolean FALSE.");
    }

    @Test
    void testIsEmptyAndSize() {
        SafeJson root = SafeJson.parse(sampleJsonInput);
        SafeJson emptyObjNode = root.get("empty_obj");
        SafeJson emptyArrNode = root.get("empty_arr");

        assertTrue(emptyObjNode.isEmpty(), "isEmpty() per un nodo SafeJson che wrappa un JSONObject vuoto ('empty_obj') dovrebbe essere true.");
        assertEquals(0, emptyObjNode.size(), "La dimensione di un nodo SafeJson che wrappa un JSONObject vuoto ('empty_obj') dovrebbe essere 0.");

        assertTrue(emptyArrNode.isEmpty(), "isEmpty() per un nodo SafeJson che wrappa un JSONArray vuoto ('empty_arr') dovrebbe essere true.");
        assertEquals(0, emptyArrNode.size(), "La dimensione di un nodo SafeJson che wrappa un JSONArray vuoto ('empty_arr') dovrebbe essere 0.");

        SafeJson keyOneNode = root.get("key_one");
        assertFalse(keyOneNode.isEmpty(), "isEmpty() per un nodo SafeJson che wrappa una stringa non vuota ('key_one') dovrebbe essere false (il nodo è presente e non è una collezione vuota).");

        SafeJson emptyStringJson = SafeJson.parse("{\"empty_string_val\": \"\"}");
        SafeJson emptyStringNode = emptyStringJson.get("empty_string_val");
        String retrievedEmptyString = emptyStringNode.getString();

        assertNotNull(retrievedEmptyString, "getString() per 'empty_string_val' non dovrebbe restituire null, dato che la chiave esiste con una stringa vuota.");
        assertTrue(retrievedEmptyString.isEmpty(), "La stringa recuperata da 'empty_string_val' dovrebbe essere essa stessa vuota (String.isEmpty()).");
        assertFalse(emptyStringNode.isEmpty(), "SafeJson.isEmpty() per un nodo che wrappa una stringa vuota ('empty_string_val') dovrebbe essere false (il nodo è presente, il valore non è una collezione).");

        SafeJson nonExistentNode = root.get("non_existent_key");
        assertTrue(nonExistentNode.isEmpty(), "isEmpty() per un percorso non esistente (che restituisce MISSING_INSTANCE) dovrebbe essere true.");
    }

    @Test
    void testSetters() {
        SafeJson modifiable = SafeJson.parse(sampleJsonInput); // Crea una copia modificabile

        // Tentativo di put su un valore stringa: non dovrebbe avere effetto sulla stringa stessa.
        modifiable.get("key_one").put("new_subkey", "new_value_for_key_one");
        assertEquals("value_one", modifiable.get("key_one").getString(), "Il tentativo di put() su un SafeJson che wrappa una Stringa ('key_one') non dovrebbe cambiare il valore originale della stringa.");

        // Aggiunta di una nuova chiave/valore all'oggetto radice
        modifiable.put("new_top_level_key", "hello world");
        assertEquals("hello world", modifiable.get("new_top_level_key").getString(), "Il valore per 'new_top_level_key' dopo put() sull'oggetto radice dovrebbe essere 'hello world'.");

        // Modifica di un valore in un oggetto dentro un array
        modifiable.get("key_array").get(0).put("value", 1000);
        assertEquals(Integer.valueOf(1000), modifiable.get("key_array").get(0).get("value").getInteger(), "Il valore di 'key_array[0].value' dopo put() dovrebbe essere 1000.");

        // Aggiunta di un nuovo oggetto all'array
        SafeJson newArrayItem = SafeJson.emptyObject().put("added", true);
        modifiable.get("key_array").add(newArrayItem);
        assertEquals(4, modifiable.get("key_array").size(), "La dimensione di 'key_array' dopo add() dovrebbe essere 4.");
        assertTrue(modifiable.get("key_array").get(3).get("added").getBoolean(), "Il campo 'added' nel nuovo elemento (indice 3) dell'array 'key_array' dovrebbe essere true.");
    }

    @Test
    void testDateParsing() {
        SafeJson dateJsonRoot = SafeJson.parse("{\"event_date\": \"2024-07-15T10:30:00Z\", \"another_date\": \"2023-10-20\"}");
        SafeJson eventDateNode = dateJsonRoot.get("event_date");
        SafeJson anotherDateNode = dateJsonRoot.get("another_date");

        assertNotNull(eventDateNode.getDate(), "La stringa data ISO8601 'event_date' ('2024-07-15T10:30:00Z') dovrebbe essere analizzata come un oggetto Date non nullo usando i formati di default.");
        assertNotNull(anotherDateNode.getDate("yyyy-MM-dd"), "La stringa data semplice 'another_date' ('2023-10-20') con formato personalizzato 'yyyy-MM-dd' dovrebbe essere analizzata come un oggetto Date non nullo.");
    }
}

package de.tlongo.serveranalytics.services.logfileservice;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Created by tomas on 10/13/14.
 */
public class JsonBuilder {
    JsonObject json;

    private static Gson gson = new Gson();

    private JsonBuilder() {
        json = new JsonObject();
    }

    public JsonBuilder property(String key, String value) {
        json.addProperty(key, value);

        return this;
    }

    public JsonBuilder property(String key, Number value) {
        json.addProperty(key, value);

        return this;
    }

    public JsonBuilder property(String key, Boolean value) {
        json.addProperty(key, value);

        return this;
    }

    public JsonBuilder property(String key, Character value) {
        json.addProperty(key, value);

        return this;
    }

    public JsonBuilder property(String key, JsonArray value) {
        json.add(key, value);

        return this;
    }

    public JsonBuilder property(String key, JsonBuilder builder) {
        property(key, builder.create());

        return this;
    }

    private void property(String key, JsonObject json) {
        this.json.add(key, json);
    }

    public JsonObject create() {
        return json;
    }

    public String string() {
        return gson.toJson(json);
    }

    public static JsonBuilder jsonDocument() {
        return new JsonBuilder();
    }

    public static JsonArray array(Object... values) {
        JsonArray array = new JsonArray();

        for (Object value : values) {
            if (value instanceof Number) {
                array.add(new JsonPrimitive((Number)value));
            } else if (value instanceof String) {
                array.add(new JsonPrimitive((String)value));
            } else if (value instanceof Number) {
                array.add(new JsonPrimitive((Character)value));
            } else if (value instanceof Boolean) {
                array.add(new JsonPrimitive((Boolean)value));
            } else if (value instanceof Character) {
                array.add(new JsonPrimitive((Character)value));
            }
        }

        return array;
    }
}

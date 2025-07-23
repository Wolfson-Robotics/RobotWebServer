package org.wolfsonrobotics.RobotWebServer.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.function.Function;

public class GsonHelper {

    public static JsonObject singletonObj(String key, Object val) {
        return new Gson().toJsonTree(Collections.singletonMap(key, val)).getAsJsonObject();
    }
    public static JsonObject fromMap(Map<?, ?> map) {
        return new Gson().toJsonTree(map).getAsJsonObject();
    }

    public static void put(JsonObject obj, String key, Object val) {
        obj.add(key, new Gson().toJsonTree(val));
    }
    public static void put(JsonObject obj, String key, List<String> val) {
        val.forEach(v -> put(obj, key, v));
    }
    public static void add(JsonArray arr, List<String> val) {
        JsonArray list = new JsonArray();
        addAll(list, val);
        arr.add(list);
    }
    public static void addAll(JsonArray arr, List<String> val) {
        val.forEach(arr::add);
    }

    public static boolean isString(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }
    public static boolean isString(JsonObject obj, String key) {
        return isString(obj.get(key));
    }
    public static boolean isJSONArr(JsonElement element) {
        return element != null && element.isJsonArray();
    }
    public static boolean isJSONArr(JsonObject obj, String key) {
        return isJSONArr(obj.get(key));
    }

    public static Object getAsObj(JsonElement element) {
        if (element == null) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString();
            }
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (primitive.isNumber()) {
                Number number = primitive.getAsNumber();
                if (number.toString().contains(".")) {
                    return number.doubleValue();
                }
                return number.longValue();
            }
            return null;
        }
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        if (element.isJsonArray()) {
            return element.getAsJsonArray();
        }
        return null;
    }
    public static Object getAsObj(JsonObject obj, String key) {
        return getAsObj(obj.get(key));
    }


    public static boolean contains(JsonArray arr, String val) {
        return arr.contains(new JsonPrimitive(val));
    }
    public static boolean contains(JsonArray arr, boolean val) {
        return arr.contains(new JsonPrimitive(val));
    }
    public static boolean contains(JsonArray arr, Number val) {
        return arr.contains(new JsonPrimitive(val));
    }
    public static boolean contains(JsonArray arr, char val) {
        return arr.contains(new JsonPrimitive(val));
    }

    public static <T> T opt(JsonObject obj, Function<JsonElement, T> fn, String key, T val) {
        JsonElement elem = obj.get(key);
        return (elem != null && !elem.isJsonNull()) ? fn.apply(elem) : val;
    }

    public static String optString(JsonObject obj, String key, String defaultVal) {
        return opt(obj, JsonElement::getAsString, key, defaultVal);
    }
    public static String optString(JsonObject obj, String key) {
        return optString(obj, key, "");
    }
    public static JsonArray optJSONArray(JsonObject obj, String key, JsonArray defaultVal) {
        return opt(obj, JsonElement::getAsJsonArray, key, defaultVal);
    }
    public static JsonArray optJSONArray(JsonObject obj, String key) {
        return optJSONArray(obj, key, new JsonArray());
    }


    public static JsonObject getPostPayloadJSON(Map<String, String> map) {
        return new Gson().fromJson(map.get("postData"), JsonObject.class);
    }
}

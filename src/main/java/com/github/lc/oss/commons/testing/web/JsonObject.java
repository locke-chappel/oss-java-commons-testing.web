package com.github.lc.oss.commons.testing.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;

public class JsonObject extends HashMap<String, Object> {
    private static final long serialVersionUID = -7286486270904246512L;

    public JsonObject() {
    }

    public JsonObject(Map<String, ?> src) {
        if (src != null) {
            src.entrySet().forEach(e -> this.put(e.getKey(), e.getValue()));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object put(String key, Object value) {
        if (value instanceof Map) {
            return super.put(key, new JsonObject((Map<String, Object>) value));
        } else if (value instanceof Enum) {
            return super.put(key, ((Enum<?>) value).name());
        } else if (value instanceof List) {
            List l = (List) value;
            if (!l.isEmpty()) {
                Object sample = l.iterator().next();
                if (sample instanceof Map) {
                    List<JsonObject> list = new ArrayList<>();
                    for (Object item : l) {
                        JsonObject child = new JsonObject((Map<String, Object>) item);
                        list.add(child);
                    }
                    return super.put(key, list);
                }
            }
        }
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        for (Entry<? extends String, ? extends Object> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    public JsonObject getChild(String id) {
        Object v = this.get(id);
        if (v == null) {
            return null;
        }

        Assertions.assertTrue(v instanceof JsonObject, id + " is not a JsonObject.");
        return (JsonObject) v;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getArray(String id) {
        Object v = this.get(id);
        if (v == null) {
            return null;
        }

        Assertions.assertTrue(v instanceof List, id + " is not an array.");
        return (List<T>) v;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String id) {
        Object v = this.get(id);
        if (v == null) {
            return null;
        }
        Assertions.assertFalse(v instanceof JsonObject, id + " is a JsonObject not a property.");
        return (T) v;
    }

    public boolean getBoolean(String id) {
        return this.getProperty(id);
    }

    public int getInt(String id) {
        Number n = this.getProperty(id);
        return n.intValue();
    }

    public long getLong(String id) {
        Number n = this.getProperty(id);
        return n.longValue();
    }

    public String getString(String id) {
        return this.getProperty(id);
    }
}

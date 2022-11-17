package com.github.lc.oss.commons.testing.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.lc.oss.commons.testing.AbstractTest;

public class JsonObjectTest extends AbstractTest {
    @Test
    public void test_constructors() {
        JsonObject j = new JsonObject();
        Assertions.assertTrue(j.isEmpty());

        Map<String, Object> map = new HashMap<>();
        map.put("a", null);
        j = new JsonObject(map);
        Assertions.assertEquals(1, j.size());
        Assertions.assertNull(j.get("a"));

        j = new JsonObject((Map<String, ?>) null);
        Assertions.assertTrue(j.isEmpty());
    }

    @Test
    public void test_putAndGet() {
        JsonObject j = new JsonObject();
        Assertions.assertTrue(j.isEmpty());

        Map<String, Object> map = new HashMap<>();
        map.put("a", null);
        map.put("b", 1);
        map.put("bl", 1l);
        map.put("c", true);
        map.put("d", "test");
        map.put("e", Arrays.asList(10, 20));
        map.put("f", new ArrayList<>());

        Map<String, Object> mapB = new HashMap<>();
        map.put("g", mapB);

        Map<String, Object> mapC = new HashMap<>();
        map.put("h", Arrays.asList(mapC));

        j.putAll(map);
        Assertions.assertEquals(map.size(), j.size());
        Assertions.assertSame(map.get("a"), j.get("a"));
        Assertions.assertSame(map.get("b"), j.get("b"));
        Assertions.assertSame(map.get("c"), j.get("c"));
        Assertions.assertSame(map.get("d"), j.get("d"));
        Assertions.assertSame(map.get("e"), j.get("e"));
        Assertions.assertSame(map.get("f"), j.get("f"));
        Object g = j.get("g");
        Assertions.assertTrue(g instanceof JsonObject);
        Assertions.assertTrue(((JsonObject) g).isEmpty());
        Object h = j.get("h");
        Assertions.assertTrue(h instanceof List);

        Assertions.assertTrue(j.getBoolean("c"));
        JsonObject child = j.getChild("g");
        Assertions.assertTrue(child.isEmpty());
        Assertions.assertEquals(1, j.getInt("b"));
        Assertions.assertEquals(1l, j.getLong("bl"));
        Assertions.assertEquals("test", j.getString("d"));
        Assertions.assertNull(j.getProperty("junk"));
        Assertions.assertNull(j.getChild("junk"));
    }

    @Test
    public void test_mapOfMaps() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        Map<String, List<String>> m1 = new HashMap<>();
        m1.put("l1", list);
        Map<String, Map<String, List<String>>> m2 = new HashMap<>();
        m2.put("m1", m1);
        Map<String, Map<String, Map<String, List<String>>>> m3 = new HashMap<>();
        m3.put("m2", m2);

        JsonObject j3 = new JsonObject(m3);
        Assertions.assertNotNull(j3);
        JsonObject j2 = j3.getChild("m2");
        Assertions.assertNotNull(j2);
        JsonObject j1 = j2.getChild("m1");
        Assertions.assertNotNull(j1);
        List<String> jl = j1.getArray("l1");
        Assertions.assertEquals(2, jl.size());
        Assertions.assertTrue(jl.contains("a"));
        Assertions.assertTrue(jl.contains("b"));
        Assertions.assertFalse(jl.contains("A"));

        List<JsonObject> jnull = j1.getArray("objects");
        Assertions.assertNull(jnull);
    }
}

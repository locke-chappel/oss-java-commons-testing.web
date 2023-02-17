package io.github.lc.oss.commons.testing.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;
import org.springframework.http.HttpStatus;

import io.github.lc.oss.commons.testing.AbstractMockTest;

public class AbstractRestTestTest extends AbstractMockTest {
    private static enum TestEnum {
        A,
        B
    }

    private RestService restService;

    private AbstractRestTest test;

    @BeforeEach
    public void init() {
        this.test = new AbstractRestTest() {
        };
        this.restService = Mockito.mock(RestService.class);
        this.setField("restService", this.restService, this.test);
    }

    /*
     * These methods are all pass-through calls to the service, just run to make
     * sure they call it (by writing a mock statement we are actually asserting that
     * the method is being called, even if the data being passed around is junk - it
     * doesn't actually matter in this case since the it's call itself we are
     * testing).
     */
    @Test
    public void test_codeCoverage() {
        Mockito.when(this.restService.basicAuthHeader(null, null)).thenReturn(null);
        Assertions.assertNull(this.test.basicAuthHeader(null, null));

        Mockito.when(this.restService.call(null, null, null, null, null)).thenReturn(null);
        Assertions.assertNull(this.test.call(null, null, null, null, null));

        Mockito.when(this.restService.callJson(null, null, null, null, null)).thenReturn(null);
        Assertions.assertNull(this.test.callJson(null, null, null, null, null));

        Mockito.when(this.restService.fromJson(null)).thenReturn(null);
        Assertions.assertNull(this.test.fromJson(null));

        Mockito.when(this.restService.getJson(null)).thenReturn(null);
        Assertions.assertNull(this.test.getJson(null));

        Mockito.when(this.restService.getJson(null, (Map<String, String>) null)).thenReturn(null);
        Assertions.assertNull(this.test.getJson(null, (Map<String, String>) null));

        Mockito.when(this.restService.getJson(null, HttpStatus.NO_CONTENT)).thenReturn(null);
        Assertions.assertNull(this.test.getJson(null, HttpStatus.NO_CONTENT));

        Mockito.when(this.restService.getJson(null, null, HttpStatus.NO_CONTENT)).thenReturn(null);
        Assertions.assertNull(this.test.getJson(null, null, HttpStatus.NO_CONTENT));

        Mockito.when(this.restService.postJson(null, null)).thenReturn(null);
        Assertions.assertNull(this.test.postJson(null, null));

        Mockito.when(this.restService.postJson(null, null, null)).thenReturn(null);
        Assertions.assertNull(this.test.postJson(null, null, null));

        Mockito.when(this.restService.postJson(null, null, null, null)).thenReturn(null);
        Assertions.assertNull(this.test.postJson(null, null, null, null));

        Mockito.when(this.restService.putJson(null, null)).thenReturn(null);
        Assertions.assertNull(this.test.putJson(null, null));

        Mockito.when(this.restService.putJson(null, null, null)).thenReturn(null);
        Assertions.assertNull(this.test.putJson(null, null, null));

        Mockito.when(this.restService.putJson(null, null, null, null)).thenReturn(null);
        Assertions.assertNull(this.test.putJson(null, null, null, null));

        Mockito.when(this.restService.deleteJson(null)).thenReturn(null);
        Assertions.assertNull(this.test.deleteJson(null));

        Mockito.when(this.restService.deleteJson(null, null)).thenReturn(null);
        Assertions.assertNull(this.test.deleteJson(null, null));

        Mockito.when(this.restService.deleteJson(null, null, null)).thenReturn(null);
        Assertions.assertNull(this.test.deleteJson(null, null, null));

        Mockito.when(this.restService.toJson(null)).thenReturn(null);
        Assertions.assertNull(this.test.toJson(null));
    }

    @Test
    public void test_assertJsonNotNull() {
        JsonObject object = new JsonObject();
        object.put("id", "parent");

        try {
            this.test.assertJsonNotNull(null, null);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("Object cannot be null ==> expected: not <null>", ex.getMessage());
        }

        try {
            this.test.assertJsonNotNull(object, "junk");
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }
        String result = this.test.assertJsonNotNull(object, "id");
        Assertions.assertEquals("parent", result);
    }

    @Test
    public void test_assertJsonNull() {
        JsonObject object = new JsonObject();
        object.put("id", "parent");

        try {
            this.test.assertJsonNull(null, null);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("Object cannot be null ==> expected: not <null>", ex.getMessage());
        }

        try {
            this.test.assertJsonNull(object, "id");
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: <null> but was: <parent>", ex.getMessage());
        }
        this.test.assertJsonNull(object, "junk");
    }

    @Test
    public void test_assertJsonObject() {
        JsonObject object = new JsonObject();

        JsonObject child = new JsonObject();
        child.put("id", "child");
        object.put("child", child);

        try {
            this.test.assertJsonObject(null, null);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("Parent cannot be null ==> expected: not <null>", ex.getMessage());
        }

        try {
            this.test.assertJsonObject(object, null);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }

        JsonObject result = this.test.assertJsonObject(object, "child");
        Assertions.assertEquals(child.get("id"), result.get("id"));
    }

    @Test
    public void test_assertJsonOptional() {
        JsonObject object = new JsonObject();
        object.put("value", 5);
        object.put("other", null);

        try {
            this.test.assertJsonOptional(object, "value", "a");
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: <a> but was: <5>", ex.getMessage());
        }

        try {
            this.test.assertJsonOptional(object, "value", null);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: <null> but was: <5>", ex.getMessage());
        }

        Object result = this.test.assertJsonOptional(object, "value", 5);
        Assertions.assertEquals(5, result);

        result = this.test.assertJsonOptional(object, "other", null);
        Assertions.assertNull(result);
    }

    @Test
    public void test_assertJsonProperty() {
        JsonObject object = new JsonObject();
        object.put("id", "parent");
        object.put("value", 5);
        object.put("enum", TestEnum.B);

        try {
            this.test.assertJsonProperty(null, null, null);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("Object cannot be null ==> expected: not <null>", ex.getMessage());
        }

        try {
            this.test.assertJsonProperty(object, "random", null);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected cannot be null ==> expected: not <null>", ex.getMessage());
        }

        try {
            this.test.assertJsonProperty(object, "id", "value");
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: <value> but was: <parent>", ex.getMessage());
        }

        Object result = this.test.assertJsonProperty(object, "id", "parent");
        Assertions.assertEquals(object.get("id"), result);

        result = this.test.assertJsonProperty(object, "value", 5);
        Assertions.assertEquals(object.get("value"), result);

        result = this.test.assertJsonProperty(object, "enum", TestEnum.B);
        Assertions.assertEquals(object.get("enum"), result);
    }

    @Test
    public void test_assertJsonMessage() {
        JsonObject body = new JsonObject();
        JsonObject m1 = new JsonObject();
        m1.put("category", "C");
        m1.put("severity", "S");
        m1.put("number", 1);
        JsonObject m2 = new JsonObject();
        m2.put("category", "D");
        m2.put("severity", "S");
        m2.put("number", 1);
        JsonObject m3 = new JsonObject();
        m3.put("category", "C");
        m3.put("severity", "T");
        m3.put("number", 1);
        m3.put("text", "message");
        body.put("messages", Arrays.asList(m1, m2, m3));

        this.test.assertJsonMessage(body, "C", "S", 1);
        this.test.assertJsonMessage(body, "D", "S", 1);
        this.test.assertJsonMessage(body, "C", "T", 1, "message");

        try {
            this.test.assertJsonMessage(body, "C", "S", 2);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("C.S.2 not found ==> expected: not <null>", ex.getMessage());
        }

        try {
            this.test.assertJsonMessage(body, "C", "S", 1, "junk");
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("C.S.1 not found ==> expected: not <null>", ex.getMessage());
        }
    }

    @Test
    public void test_assertJsonArray() {
        JsonObject object = new JsonObject();
        object.put("null", (List<JsonObject>) null);
        object.put("empty", new ArrayList<Integer>());
        object.put("3", Arrays.asList("1", "2", "3"));

        try {
            this.test.assertJsonArray(null, null, 0);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("Object cannot be null ==> expected: not <null>", ex.getMessage());
        }

        try {
            this.test.assertJsonArray(object, "junk", 0);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }

        try {
            this.test.assertJsonArray(object, "null", 0);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }

        try {
            this.test.assertJsonArray(object, "empty", 1);
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: <1> but was: <0>", ex.getMessage());
        }

        List<Integer> resultI = this.test.assertJsonArray(object, "empty", 0);
        Assertions.assertNotNull(resultI);
        Assertions.assertTrue(resultI.isEmpty());

        List<String> resultS = this.test.assertJsonArray(object, "3", 3);
        Assertions.assertNotNull(resultS);
        Assertions.assertEquals(3, resultS.size());
        Assertions.assertTrue(resultS.contains("1"));
        Assertions.assertTrue(resultS.contains("2"));
        Assertions.assertTrue(resultS.contains("3"));
    }
}

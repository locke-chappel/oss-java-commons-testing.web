package com.github.lc.oss.commons.testing.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import com.github.lc.oss.commons.testing.AbstractTest;

public class AbstractWebTestTest extends AbstractTest {
    private static class TestClass extends AbstractWebTest {

    }

    @Test
    public void test_getUrl_noAppUrl() {
        AbstractWebTest test = new TestClass();

        String result = test.getUrl("/");
        Assertions.assertEquals("/", result);
    }

    @Test
    public void test_getUrl() {
        AbstractWebTest test = new TestClass();
        this.setField("appUrl", "http://localhost:8080/", test);

        String result = test.getUrl("/api/v1/resource");
        Assertions.assertEquals("http://localhost:8080/api/v1/resource", result);
    }

    @Test
    public void test_prefixUrl() {
        AbstractWebTest test = new TestClass();

        String result = test.prefixUrl(null, null);
        Assertions.assertNull(result);

        result = test.prefixUrl("", null);
        Assertions.assertNull(result);

        result = test.prefixUrl(null, "");
        Assertions.assertEquals("", result);

        result = test.prefixUrl("", "");
        Assertions.assertEquals("", result);

        result = test.prefixUrl("/", "/");
        Assertions.assertEquals("/", result);

        result = test.prefixUrl("http://localhost", "/");
        Assertions.assertEquals("http://localhost/", result);

        result = test.prefixUrl("http://localhost/", "/");
        Assertions.assertEquals("http://localhost/", result);

        result = test.prefixUrl("http://localhost", "/index");
        Assertions.assertEquals("http://localhost/index", result);

        result = test.prefixUrl("http://localhost/", "/index");
        Assertions.assertEquals("http://localhost/index", result);

        result = test.prefixUrl("http://localhost:8080/", "/api/v1/resource");
        Assertions.assertEquals("http://localhost:8080/api/v1/resource", result);

        result = test.prefixUrl("http://localhost:8080/", "api/v1/resource");
        Assertions.assertEquals("http://localhost:8080/api/v1/resource", result);

        result = test.prefixUrl("http://localhost:8080", "api/v1/resource");
        Assertions.assertEquals("http://localhost:8080/api/v1/resource", result);
    }

    @Test
    public void test_assertHeader() {
        AbstractWebTest test = new TestClass();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Test", "test");

        test.assertHeader("X-Test", "test", headers);
    }
}

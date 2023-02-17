package io.github.lc.oss.commons.testing.web;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.lc.oss.commons.testing.AbstractTest;

public class TestRestTemplateErrorHandlerTest extends AbstractTest {
    @Test
    public void test_hasError() {
        TestRestTemplateErrorHandler handler = new TestRestTemplateErrorHandler();
        try {
            Assertions.assertFalse(handler.hasError(null));
        } catch (IOException e) {
            Assertions.fail("Unexpectged exception");
        }
    }

    @Test
    public void test_handleError() {
        TestRestTemplateErrorHandler handler = new TestRestTemplateErrorHandler();
        try {
            handler.handleError(null);
        } catch (IOException e) {
            Assertions.fail("Unexpectged exception");
        }
    }
}

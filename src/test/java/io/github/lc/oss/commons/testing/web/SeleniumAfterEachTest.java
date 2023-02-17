package io.github.lc.oss.commons.testing.web;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.WebDriver;

import io.github.lc.oss.commons.testing.AbstractTest;

public class SeleniumAfterEachTest extends AbstractTest {
    private static class CallHelper {
        public boolean wasCalled = false;
    }

    private SeleniumAfterEach sae = new SeleniumAfterEach();

    @Test
    public void test_afterEach_success() {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        AbstractSeleniumTest test = Mockito.mock(AbstractSeleniumTest.class);
        WebDriver driver = Mockito.mock(WebDriver.class);

        final CallHelper driverQuit = new CallHelper();

        Mockito.when(context.getTestInstance()).thenReturn(Optional.of(test));
        Mockito.when(context.getExecutionException()).thenReturn(Optional.empty());
        Mockito.when(test.getDriver()).thenReturn(driver);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Assertions.assertFalse(driverQuit.wasCalled);
                driverQuit.wasCalled = true;
                return null;
            }
        }).when(driver).quit();

        Assertions.assertFalse(driverQuit.wasCalled);
        try {
            this.sae.afterEach(context);
        } catch (Exception e) {
            Assertions.fail("Unexpected exception");
        }
        Assertions.assertTrue(driverQuit.wasCalled);
    }

    @Test
    public void test_afterEach_failure() {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        AbstractSeleniumTest test = Mockito.mock(AbstractSeleniumTest.class);
        WebDriver driver = Mockito.mock(WebDriver.class);
        Method method = this.getClass().getMethods()[0];

        final CallHelper driverQuit = new CallHelper();
        final CallHelper testScreen = new CallHelper();

        Mockito.when(context.getTestInstance()).thenReturn(Optional.of(test));
        Mockito.when(context.getExecutionException()).thenReturn(Optional.of(new RuntimeException()));
        Mockito.when(test.getDriver()).thenReturn(driver);
        Mockito.when(context.getTestMethod()).thenReturn(Optional.of(method));
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Assertions.assertFalse(testScreen.wasCalled);
                testScreen.wasCalled = true;
                return null;
            }
        }).when(test).screenShot(ArgumentMatchers.notNull());
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Assertions.assertFalse(driverQuit.wasCalled);
                driverQuit.wasCalled = true;
                return null;
            }
        }).when(driver).quit();

        Assertions.assertFalse(driverQuit.wasCalled);
        Assertions.assertFalse(testScreen.wasCalled);
        try {
            this.sae.afterEach(context);
        } catch (Exception e) {
            Assertions.fail("Unexpected exception");
        }
        Assertions.assertTrue(driverQuit.wasCalled);
        Assertions.assertTrue(testScreen.wasCalled);
    }
}

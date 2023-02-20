package io.github.lc.oss.commons.testing.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Quotes;
import org.opentest4j.AssertionFailedError;

import io.github.lc.oss.commons.serialization.Message;
import io.github.lc.oss.commons.testing.AbstractMockTest;
import io.github.lc.oss.commons.util.PathNormalizer;

public class AbstractSeleniumTestTest extends AbstractMockTest {
    private static class TestClass extends AbstractSeleniumTest {
        private WebDriver driver = Mockito.mock(ChromeDriver.class);

        @Override
        protected WebDriver getDriver() {
            return this.driver;
        }
    }

    private String getScreenShotPath() {
        return new PathNormalizer().dir(System.getProperty("java.io.tmpdir"));
    }

    private String getScreenShotName() {
        return "selenium-test-screenshot";
    }

    @AfterEach
    public void cleanup() {
        try {
            Files.deleteIfExists(Paths.get(this.getScreenShotPath() + this.getScreenShotName() + ".png"));
        } catch (IOException ex) {
            Assertions.fail("Error delete test screenshot", ex);
        }
    }

    @Test
    public void test_getDriver() {
        AbstractSeleniumTest test = new AbstractSeleniumTest() {
        };

        Assertions.assertNull(test.getDriver());
    }

    @Test
    public void test_getExpectedBrowserErrors() {
        AbstractSeleniumTest test = new TestClass();
        this.setField("loggingPrefix", "[app-name]", test);
        Set<String> result = test.getExpectedBrowserErrors();
        Set<String> resultAgain = test.getExpectedBrowserErrors();
        Assertions.assertSame(result, resultAgain);
        Assertions.assertTrue(result.contains("Failed to load resource: the server responded with a status of 409"));
        Assertions.assertTrue(result.contains("[app-name]"));
        try {
            result.add("test");
            Assertions.fail("Expected exception");
        } catch (UnsupportedOperationException ex) {
            // Pass
        }
    }

    @Test
    public void test_getChromiumOptions() {
        AbstractSeleniumTest test = new TestClass();
        ChromeOptions result = test.getChromiumOptions();
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_getChromiumOptions_withBlankPath() {
        AbstractSeleniumTest test = new TestClass();
        this.setField("chromiumPath", "  \t \r \n \t ", test);
        this.setField("sandbox", true, test);
        this.setField("chromiumAllowCors", true, test);
        this.setField("headless", true, test);
        ChromeOptions result = test.getChromiumOptions();
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_getChromiumOptions_withPath() {
        AbstractSeleniumTest test = new TestClass();
        this.setField("chromiumPath", "/path/to/chromium", test);
        this.setField("sandbox", true, test);
        ChromeOptions result = test.getChromiumOptions();
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_checkLogs_none() {
        TestInfo testInfo = Mockito.mock(TestInfo.class);
        Options options = Mockito.mock(Options.class);
        Logs logs = Mockito.mock(Logs.class);
        List<LogEntry> entries = new ArrayList<>();
        LogEntries logEntries = new LogEntries(entries);
        AbstractSeleniumTest test = new TestClass();

        Mockito.when(test.getDriver().manage()).thenReturn(options);
        Mockito.when(options.logs()).thenReturn(logs);
        Mockito.when(logs.get(LogType.BROWSER)).thenReturn(logEntries);

        test.checkLogs(testInfo);
    }

    @Test
    public void test_checkLogs_nonErrorLogs() {
        TestInfo testInfo = Mockito.mock(TestInfo.class);
        Options options = Mockito.mock(Options.class);
        Logs logs = Mockito.mock(Logs.class);
        List<LogEntry> entries = new ArrayList<>();
        entries.add(new LogEntry(Level.INFO, System.currentTimeMillis(), "[App] App Message"));
        entries.add(new LogEntry(Level.INFO, System.currentTimeMillis(), "Failed to load resource: the server responded with a status of 409"));
        entries.add(new LogEntry(Level.INFO, System.currentTimeMillis(), ""));
        LogEntries logEntries = new LogEntries(entries);
        AbstractSeleniumTest test = new TestClass();
        this.setField("loggingPrefix", "[App]", test);

        Mockito.when(test.getDriver().manage()).thenReturn(options);
        Mockito.when(options.logs()).thenReturn(logs);
        Mockito.when(logs.get(LogType.BROWSER)).thenReturn(logEntries);

        test.checkLogs(testInfo);
    }

    @Test
    public void test_checkLogs_withErrors() {
        TestInfo testInfo = Mockito.mock(TestInfo.class);
        Options options = Mockito.mock(Options.class);
        Logs logs = Mockito.mock(Logs.class);
        List<LogEntry> entries = new ArrayList<>();
        entries.add(new LogEntry(Level.INFO, System.currentTimeMillis(), "Boom!"));
        LogEntries logEntries = new LogEntries(entries);
        AbstractSeleniumTest test = new TestClass();
        this.setField("loggingPrefix", "[App]", test);

        Mockito.when(test.getDriver().manage()).thenReturn(options);
        Mockito.when(options.logs()).thenReturn(logs);
        Mockito.when(logs.get(LogType.BROWSER)).thenReturn(logEntries);

        try {
            test.checkLogs(testInfo);
            Assertions.fail("Expected error");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("Browser errors detected during null:\nBoom! ==> expected: <true> but was: <false>", ex.getMessage());
        }
    }

    @Test
    public void test_assertById() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        WebElement result = test.assertById("id");
        Assertions.assertSame(element, result);
    }

    @Test
    public void test_assertByCssSelector() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.cssSelector("selector..."))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        WebElement result = test.assertByCssSelector("selector...");
        Assertions.assertSame(element, result);
    }

    @Test
    public void test_clickBanner() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.cssSelector("div#lib\\.notitifcations\\.banner > div.message.success > span.close"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        test.clickBanner(Message.Severities.Success);
    }

    @Test
    public void test_clickCheckbox() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.cssSelector("label[for='id']"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        test.clickCheckbox("id");
    }

    @Test
    public void test_clickById() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        test.clickById("id");
    }

    @Test
    public void test_findById() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);

        WebElement result = test.findById("id", false);
        Assertions.assertSame(element, result);
    }

    @Test
    public void test_findById_clickable() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        WebElement result = test.findById("id");
        Assertions.assertSame(element, result);
    }

    @Test
    public void test_findById_presentOnly() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);

        WebElement result = test.findById("id", false);
        Assertions.assertSame(element, result);
    }

    @Test
    public void test_navigate() {
        AbstractSeleniumTest test = new TestClass();

        Mockito.when(((JavascriptExecutor) test.getDriver()).executeScript("return document.readyState;")).thenReturn("complete");

        test.navigate("/index");
    }

    @Test
    public void test_waitFornavigate() {
        AbstractSeleniumTest test = new TestClass();

        Mockito.when(test.getDriver().getCurrentUrl()).thenReturn("/index");
        Mockito.when(((JavascriptExecutor) test.getDriver()).executeScript("return $$._inProgress.IsRunning();")).thenReturn(false);

        test.waitForNavigate("/index");
    }

    @Test
    public void test_waitForScipt_string() {
        AbstractSeleniumTest test = new TestClass();

        Mockito.when(((JavascriptExecutor) test.getDriver()).executeScript("return $$._inProgress.IsRunning();")).thenReturn("false");

        test.waitForScript();
    }

    @Test
    public void test_waitForScipt_boolean() {
        AbstractSeleniumTest test = new TestClass();

        Mockito.when(((JavascriptExecutor) test.getDriver()).executeScript("return $$._inProgress.IsRunning();")).thenReturn(false);

        test.waitForScript();
    }

    @Test
    public void test_assertTextContent() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);
        Mockito.when(element.getText()).thenReturn("value");

        test.assertTextContent("id", "value");
    }

    @Test
    public void test_assertTextContentContains() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.doAnswer(new Answer<String>() {
            private int count = 0;

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if (this.count == 0) {
                    this.count++;
                    return null;
                } else if (this.count == 1) {
                    this.count++;
                    return "different";
                } else {
                    return "value";
                }
            }
        }).when(element).getText();

        test.assertTextContentContains(element, "alu");
    }

    @Test
    public void test_assertTextValue() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);
        Mockito.when(element.getAttribute("value")).thenReturn("value");

        test.assertTextValue("id", "value");
    }

    @Test
    public void test_assertTextValueContains() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.doAnswer(new Answer<String>() {
            private int count = 0;

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if (this.count == 0) {
                    this.count++;
                    return null;
                } else if (this.count == 1) {
                    this.count++;
                    return "different";
                } else {
                    return "value";
                }
            }
        }).when(element).getAttribute("value");

        test.assertTextValueContains(element, "al");
    }

    @Test
    public void test_findAllByAttribute() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        List<WebElement> elements = Arrays.asList(element);

        Mockito.when(test.getDriver().findElements(By.cssSelector("div[att='v']"))).thenReturn(elements);

        List<WebElement> result = test.findAllByAttribute("div", "att", "v");
        Assertions.assertSame(elements, result);
    }

    @Test
    public void test_findByAttribute() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.cssSelector("div[att='v']"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        WebElement result = test.findByAttribute("div", "att", "v");
        Assertions.assertSame(element, result);
    }

    @Test
    public void test_findByAttribute_v2() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.cssSelector("div[att='v']"))).thenReturn(element);

        WebElement result = test.findByAttribute("div", "att", "v", false);
        Assertions.assertSame(element, result);
    }

    @Test
    public void test_findByCssSelector() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.cssSelector("div.one"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        WebElement result = test.findByCssSelector("div.one");
        Assertions.assertSame(element, result);
    }

    @Test
    public void test_findParent() {
        AbstractSeleniumTest test = new TestClass();

        WebElement child = Mockito.mock(WebElement.class);
        WebElement parent = Mockito.mock(WebElement.class);

        Mockito.when(child.findElement(By.xpath("./.."))).thenReturn(parent);

        WebElement result = test.findParent(child);
        Assertions.assertSame(parent, result);
    }

    @Test
    public void test_findTableRows() {
        AbstractSeleniumTest test = new TestClass();

        WebElement table = Mockito.mock(WebElement.class);
        WebElement row = Mockito.mock(WebElement.class);
        WebElement cell = Mockito.mock(WebElement.class);

        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(table);
        Mockito.when(table.isDisplayed()).thenReturn(true);
        Mockito.when(table.isEnabled()).thenReturn(true);

        Mockito.when(table.findElements(By.cssSelector("tbody > tr"))).thenReturn(Arrays.asList(row));
        Mockito.when(row.findElements(By.tagName("td"))).thenReturn(Arrays.asList(cell));

        List<List<WebElement>> result = test.findTableRows("id");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        List<WebElement> r = result.iterator().next();
        Assertions.assertNotNull(r);
        Assertions.assertEquals(1, r.size());
        WebElement c = r.iterator().next();
        Assertions.assertSame(cell, c);
    }

    @Test
    public void test_focusById() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);

        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when((JavascriptExecutor) test.getDriver()).executeScript("arguments[0].focus();", element);

        test.focusById("id");
    }

    @Test
    public void test_focus() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when((JavascriptExecutor) test.getDriver()).executeScript("arguments[0].focus();", element);

        test.focus(element);
    }

    @Test
    public void test_screenShot_nullPath() {
        AbstractSeleniumTest test = new TestClass();
        this.setField("screenShotPath", null, test);

        test.screenShot(this.getScreenShotName());
    }

    @Test
    public void test_screenShot_blankPath() {
        AbstractSeleniumTest test = new TestClass();
        this.setField("screenShotPath", "", test);

        test.screenShot(this.getScreenShotName());
    }

    @Test
    public void test_screenShot() {
        AbstractSeleniumTest test = new TestClass();
        this.setField("screenShotPath", this.getScreenShotPath(), test);

        Mockito.when(((ChromeDriver) test.getDriver()).getScreenshotAs(OutputType.BYTES)).thenReturn(new byte[] { 0x00 });

        test.screenShot(this.getScreenShotName());
    }

    @Test
    public void test_scrollTo() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when((JavascriptExecutor) test.getDriver()).executeScript("arguments[0].scrollIntoView({ block : \"center\", inline : \"center\" });", element);

        test.scrollTo(element);
    }

    @Test
    public void test_selectValue() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        List<WebElement> options = new ArrayList<>();
        WebElement option = Mockito.mock(WebElement.class);
        Mockito.when(option.isEnabled()).thenReturn(true);
        options.add(option);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);
        Mockito.when(element.getTagName()).thenReturn("select");
        Mockito.when(element.findElements(By.xpath(".//option[@value = " + Quotes.escape("value") + "]"))).thenReturn(options);

        test.selectValue("id", "value");
    }

    @Test
    public void test_sendKeys() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        test.sendKeys("id", "text");
    }

    @Test
    public void test_sendKeys_append() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        test.sendKeys("id", "text", true);
    }

    @Test
    public void test_sendKeys_null() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        test.sendKeys("id", null, true);
    }

    @Test
    public void test_sendKeys_blank() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("id"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        test.sendKeys("id", " \t \r \n \t ", true);
    }

    @Test
    public void test_sendKeysToBody() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.cssSelector("body"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        test.sendKeysToBody((CharSequence) Keys.ESCAPE);
    }

    @Test
    public void test_assertMessage() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("lib.notitifcations.banner"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        WebElement msg1 = Mockito.mock(WebElement.class);
        WebElement msg2 = Mockito.mock(WebElement.class);
        WebElement msg3 = Mockito.mock(WebElement.class);
        Mockito.when(element.findElements(By.cssSelector("div.message.Error"))).thenReturn(Arrays.asList(msg1, msg2, msg3));
        Mockito.when(msg1.getText()).thenReturn("a");
        Mockito.when(msg2.getText()).thenReturn("e");
        Mockito.when(msg3.getText()).thenReturn(null);
        WebElement span = Mockito.mock(WebElement.class);
        Mockito.when(msg2.findElement(ArgumentMatchers.notNull())).thenReturn(span);

        test.assertMessage("Error", "e");
    }

    @Test
    public void test_assertMessage_notFound() {
        AbstractSeleniumTest test = new TestClass() {
            @Override
            protected int getSeleniumDefaultWaitLimit() {
                return 1;
            }
        };

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("lib.notitifcations.banner"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        WebElement msg1 = Mockito.mock(WebElement.class);
        Mockito.when(element.findElements(By.cssSelector("div.message.Error"))).thenReturn(Arrays.asList(msg1));
        Mockito.when(msg1.getText()).thenReturn("a");

        try {
            test.assertMessage("Error", "e");
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("No 'Error' message with text 'e' was found", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessage_otherAssertionFailure() {
        AbstractSeleniumTest test = new TestClass();

        WebElement element = Mockito.mock(WebElement.class);
        Mockito.when(test.getDriver().findElement(By.id("lib.notitifcations.banner"))).thenReturn(element);
        Mockito.when(element.isDisplayed()).thenReturn(true);
        Mockito.when(element.isEnabled()).thenReturn(true);

        WebElement msg1 = Mockito.mock(WebElement.class);
        Mockito.when(element.findElements(By.cssSelector("div.message.Error"))).thenReturn(Arrays.asList(msg1));
        Mockito.when(msg1.getText()).thenThrow(new AssertionFailedError("BOOM!"));

        try {
            test.assertMessage("Error", "e");
            Assertions.fail("Expected exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("BOOM!", ex.getMessage());
        }
    }

    @Test
    public void test_waitUntil_retry_fail() {
        AbstractSeleniumTest test = new TestClass() {
            @Override
            protected int getSeleniumDefaultRetryWaitLimit() {
                return 1;
            }
        };

        long start = 0;
        long end = 0;
        try {
            start = System.currentTimeMillis();
            test.waitUntil(ExpectedConditions.urlToBe("junk"), 1, 3);
            Assertions.fail("Expected exception");
        } catch (TimeoutException ex) {
            end = System.currentTimeMillis();
            Assertions.assertTrue(ex.getMessage().startsWith("Expected condition failed: waiting for url to be \"junk\". Current url: \"null\""));
        }

        // test should abort in ~3 seconds, allow for a compute buffer of ~2x that
        Assertions.assertTrue(end - start < 6000);
    }

    @Test
    public void test_waitUntil_retry() {
        AbstractSeleniumTest test = new TestClass() {
            @Override
            protected int getSeleniumDefaultRetryWaitLimit() {
                return 1;
            }
        };

        final long start = System.currentTimeMillis();
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if (System.currentTimeMillis() - start >= 2000) {
                    return "junk";
                }
                return "not junk";
            }
        }).when(test.getDriver()).getCurrentUrl();

        final long testStart = System.currentTimeMillis();
        test.waitUntil(ExpectedConditions.urlToBe("junk"), 1, 10);
        final long delta = System.currentTimeMillis() - testStart;

        Assertions.assertTrue(delta >= 2000);
        Assertions.assertTrue(delta < 3000);
    }

    @Test
    public void test_defaultWaitValues() {
        AbstractSeleniumTest test = new TestClass();

        Assertions.assertEquals(15, test.getSeleniumDefaultWaitLimit());
        Assertions.assertEquals(5, test.getSeleniumDefaultRetryWaitLimit());
    }
}

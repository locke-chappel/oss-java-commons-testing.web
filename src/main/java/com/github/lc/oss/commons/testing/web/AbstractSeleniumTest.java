package com.github.lc.oss.commons.testing.web;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Value;

import com.github.lc.oss.commons.serialization.Message;
import com.github.lc.oss.commons.util.IoTools;

@ExtendWith(SeleniumAfterEach.class)
public abstract class AbstractSeleniumTest extends AbstractWebTest {
    private static final int DEFAULT_MAX_WAIT_SECONDS = 15;
    private static final int DEFAULT_RETRY_WAIT_SECONDS = 5;

    @Value("${testing.chromium.acceptInsecureCerts:false}")
    private boolean chromiumAcceptInsecureCerts;
    @Value("${testing.chromium.chromiumAllowCors:false}")
    private boolean chromiumAllowCors;
    @Value("${testing.chromium.driver.port:12000}")
    private int chromiumDriverPort;
    @Value("${testing.chromium.path:}")
    private String chromiumPath;
    @Value("#{pathNormalizer.dir('${testing.chromium.screenshot.path:}')}")
    private String screenShotPath;
    @Value("${testing.chromium.headless:true}")
    private boolean headless;
    @Value("${testing.chromium.windowsize:1280,720}")
    private String windowSize;
    @Value("${testing.chromium.retryCount:0}")
    private int retryCount;
    @Value("${testing.chromium.sandbox:true}")
    private boolean sandbox;
    @Value("${application.ui.logging.prefix:}")
    private String loggingPrefix;
    protected Set<String> defaultExpectedErrors = null;
    private WebDriver driver;

    protected Set<String> getExpectedBrowserErrors() {
        if (this.defaultExpectedErrors == null) {
            this.defaultExpectedErrors = Collections.unmodifiableSet(new HashSet<>(Arrays.asList( //
                    /* All app message are not considered errors */
                    this.loggingPrefix, //
                    /*
                     * sub-optimal browser design, google it (409 is handled via the app but still
                     * logged...)
                     */
                    "Failed to load resource: the server responded with a status of 409")));
        }
        return this.defaultExpectedErrors;
    }

    protected ChromeOptions getChromiumOptions() {
        ChromeOptions opts = new ChromeOptions();
        opts.setHeadless(this.headless);
        opts.addArguments("--window-size=" + this.windowSize);
        opts.addArguments("--disable-extensions");
        opts.addArguments("--disable-gpu");
        opts.addArguments("--disable-dev-shm-usage");
        opts.addArguments("--incognito");
        opts.setAcceptInsecureCerts(this.chromiumAcceptInsecureCerts);
        if (!this.sandbox) {
            opts.addArguments("--no-sandbox");
        }
        if (this.chromiumAllowCors) {
            opts.addArguments("--disable-web-security");
        }
        opts.addArguments("disable-infobars");
        opts.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        opts.setExperimentalOption("useAutomationExtension", false);
        if (this.chromiumPath != null && !"".equals(this.chromiumPath.trim())) {
            opts.setBinary(this.chromiumPath);
        }
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.INFO);
        opts.setCapability("goog:loggingPrefs", logPrefs);
        return opts;
    }

    @BeforeEach
    public void launchChromium() {
        this.driver = new ChromeDriver(new ChromeDriverService.Builder().usingPort(this.chromiumDriverPort).build(), this.getChromiumOptions());
    }

    @AfterEach
    public void checkLogs(TestInfo testInfo) {
        String appLog = this.getBrowserLogsAsString(e -> e.contains(this.loggingPrefix));

        String browserLog = this.getBrowserLogsAsString(e -> this.getExpectedBrowserErrors().stream().noneMatch(s -> e.contains(s)));

        if (!appLog.trim().equals("")) {
            System.err.println("### Application Browser Console Log");
            System.err.println(appLog);
            System.err.println("### End Application Browser Console Log");
            Assertions.assertFalse(appLog.contains(this.loggingPrefix + " [ERROR]"), "Application errors detected during " + testInfo.getDisplayName());
        }

        Assertions.assertTrue(browserLog.trim().equals(""), "Browser errors detected during " + testInfo.getDisplayName() + ":\n" + browserLog);
    }

    protected WebElement assertById(String id) {
        return this.assertById(id, true);
    }

    protected WebElement assertById(String id, boolean clickable) {
        WebElement element = this.findById(id, clickable);
        Assertions.assertNotNull(element);
        return element;
    }

    protected WebElement assertByCssSelector(String selector) {
        return this.assertByCssSelector(selector, true);
    }

    protected WebElement assertByCssSelector(String selector, boolean clickable) {
        WebElement element = this.findByCssSelector(selector, clickable);
        Assertions.assertNotNull(element);
        return element;
    }

    protected void assertMessage(String severity, String text) {
        this.assertMessage(severity, text, this.getSeleniumDefaultWaitLimit());
    }

    protected void assertMessage(String severity, String text, long maxWait) {
        try {
            this.waitUntil(() -> {
                WebElement banner = this.assertById("lib.notitifcations.banner");
                List<WebElement> messages = banner.findElements(By.cssSelector("div.message." + severity));
                Set<WebElement> found = messages.stream(). //
                        filter(e -> e.getText() != null). //
                        filter(e -> e.getText().contains(text)). //
                        collect(Collectors.toSet());
                if (found.size() < 1) {
                    return false;
                }
                found.forEach(e -> e.findElement(By.cssSelector("span.awesome.bold.close")).click());
                return true;
            }, maxWait * 1000);
        } catch (AssertionFailedError ex) {
            if ("Waited too long, aborting ==> expected: <true> but was: <false>".equals(ex.getMessage())) {
                throw new AssertionFailedError(String.format("No '%s' message with text '%s' was found", severity, text));
            }
            throw ex;
        }
    }

    protected void assertTextContent(String id, String text) {
        WebElement e = this.assertById(id);
        this.assertTextContent(e, text);
    }

    protected void assertTextContent(WebElement element, String text) {
        this.waitUntil(() -> text.equals(this.getTextContent(element)));
    }

    protected void assertTextContentContains(WebElement element, String contains) {
        this.waitUntil(() -> {
            String text = this.getTextContent(element);
            if (text == null) {
                return false;
            }
            return text.contains(contains);
        });
    }

    protected void assertTextValue(String id, String text) {
        WebElement e = this.assertById(id);
        this.assertTextValue(e, text);
    }

    protected void assertTextValue(WebElement element, String text) {
        this.waitUntil(() -> text.equals(this.getTextValue(element)));
    }

    protected void assertTextValueContains(WebElement element, String contains) {
        this.waitUntil(() -> {
            String text = this.getTextValue(element);
            if (text == null) {
                return false;
            }
            return text.contains(contains);
        });
    }

    protected void clickBanner(Message.Severity severity) {
        this.clickBanner("lib\\.notitifcations\\.banner", severity);
    }

    protected void clickBanner(String bannerId, Message.Severity severity) {
        this.clickByCssSelector("div#" + bannerId + " > div.message." + severity.name().toLowerCase() + " > span.close");
    }

    protected void clickByCssSelector(String selector) {
        WebElement element = this.assertByCssSelector(selector, true);
        this.click(element);
    }

    protected void clickById(String id) {
        this.clickById(id, true);
    }

    protected void clickById(String id, boolean clickable) {
        WebElement element = this.assertById(id, clickable);
        this.click(element);
    }

    /**
     * Specialized click "by id" for custom labeled check boxes. For stock HTML
     * check box use the normal clickById;
     *
     * @param id
     */
    protected void clickCheckbox(String id) {
        WebElement e = this.findByCssSelector("label[for='" + id + "']");
        this.click(e);
    }

    protected void click(WebElement element) {
        this.scrollTo(element);
        element.click();
    }

    protected List<WebElement> findAllByAttribute(String tag, String attribute, String value) {
        return this.findAllByCssSelector(tag + "[" + attribute + "='" + value + "']");
    }

    protected List<WebElement> findAllByCssSelector(String selector) {
        return this.waitUntil(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(selector)));
    }

    protected WebElement findByAttribute(String tag, String attribute, String value) {
        return this.findByAttribute(tag, attribute, value, true);
    }

    protected WebElement findByAttribute(String tag, String attribute, String value, boolean clickable) {
        return this.findByCssSelector(tag + "[" + attribute + "='" + value + "']", clickable);
    }

    protected WebElement findByCssSelector(String selector) {
        return this.findByCssSelector(selector, true);
    }

    protected WebElement findByCssSelector(String selector, boolean clickable) {
        if (clickable) {
            return this.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
        } else {
            return this.waitUntil(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
        }
    }

    protected WebElement findById(String id) {
        return this.findById(id, true);
    }

    protected WebElement findById(String id, boolean clickable) {
        if (clickable) {
            return this.waitUntil(ExpectedConditions.elementToBeClickable(By.id(id)));
        } else {
            return this.waitUntil(ExpectedConditions.presenceOfElementLocated(By.id(id)));
        }
    }

    protected WebElement findParent(WebElement child) {
        return child.findElement(By.xpath("./.."));
    }

    protected List<List<WebElement>> findTableRows(String id) {
        return this.findTableRows(this.assertById(id));
    }

    protected List<List<WebElement>> findTableRows(WebElement table) {
        List<List<WebElement>> rows = new ArrayList<>();
        List<WebElement> body = table.findElements(By.cssSelector("tbody > tr"));
        for (WebElement tr : body) {
            rows.add(tr.findElements(By.tagName("td")));
        }
        return rows;
    }

    protected void focusById(String id) {
        WebElement element = this.assertById(id);
        this.focus(element);
    }

    protected void focus(WebElement element) {
        ((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].focus();", element);
    }

    protected List<String> getBrowserLogs(Predicate<String> filter) {
        return StreamSupport.stream(this.getDriver().manage().logs().get(LogType.BROWSER).spliterator(), true). //
                map(e -> e.getMessage()). //
                filter(filter). //
                collect(Collectors.toList());
    }

    protected String getBrowserLogsAsString(Predicate<String> filter) {
        return this.getBrowserLogs(filter).stream().collect(Collectors.joining("\n"));
    }

    protected String getTextContent(WebElement element) {
        return element.getText();
    }

    protected String getTextValue(WebElement element) {
        return element.getAttribute("value");
    }

    protected void navigate(String url) {
        this.navigateAbsoluteUrl(this.getUrl(url));
    }

    protected void navigateAbsoluteUrl(String url) {
        this.getDriver().get(url);
        /*
         * note: don't call waitForNavigate here, redirect behaviors may not be
         * accounted for (i.e. we requested "/" but landed on "/login")
         */
        this.waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public @Nullable Boolean apply(@Nullable WebDriver arg0) {
                return ((JavascriptExecutor) AbstractSeleniumTest.this.getDriver()).executeScript("return document.readyState;").equals("complete");
            }
        });
    }

    protected void screenShot(String name) {
        if (this.getScreenShotPath() == null || this.getScreenShotPath().trim().equals("")) {
            return;
        }

        byte[] img = ((TakesScreenshot) this.getDriver()).getScreenshotAs(OutputType.BYTES);
        IoTools.writeToFile(img, this.getScreenShotPath() + name + ".png");
    }

    protected void scrollTo(WebElement element) {
        ((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView({ block : \"center\", inline : \"center\" });", element);
    }

    protected void selectValue(String id, String value) {
        this.selectValue(this.assertById(id), value);
    }

    protected void selectValue(WebElement element, String value) {
        Select s = new Select(element);
        s.selectByValue(value);
    }

    protected void sendKeys(String id, CharSequence text) {
        this.sendKeys(id, text, false);
    }

    protected void sendKeys(String id, CharSequence text, boolean append) {
        this.sendKeys(this.assertById(id), text, append);
    }

    protected void sendKeys(WebElement element, CharSequence text, boolean append) {
        if (text == null || text.toString().trim().equals("")) {
            element.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.BACK_SPACE));
        } else if (!append) {
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"), text);
        } else {
            element.sendKeys(text);
        }
    }

    protected void sendKeysToBody(CharSequence... keys) {
        WebElement body = this.assertByCssSelector("body");
        body.sendKeys(keys);
    }

    protected void waitForNavigate(String url) {
        this.waitUntil(ExpectedConditions.urlToBe(this.getUrl(url)));
        this.waitFor(1000);
        this.waitForScript();
    }

    protected void waitForScript() {
        this.waitForScript(this.getSeleniumDefaultWaitLimit() * 1000);
    }

    protected void waitForScript(int timeout) {
        this.waitUntil(() -> {
            Object result = ((JavascriptExecutor) this.getDriver()).executeScript("return $$._inProgress.IsRunning();");
            if (result instanceof String) {
                result = Boolean.parseBoolean((String) result);
            }
            return Boolean.FALSE.equals(result);
        }, timeout);
    }

    protected <T> T waitUntil(ExpectedCondition<T> condition) {
        return this.waitUntil(condition, this.getSeleniumDefaultWaitLimit());
    }

    protected <T> T waitUntil(ExpectedCondition<T> condition, int timeout) {
        return this.waitUntil(condition, timeout, this.getRetryCount());
    }

    protected <T> T waitUntil(ExpectedCondition<T> condition, int timeout, int retry) {
        try {
            WebDriverWait wait = new WebDriverWait(this.getDriver(), Duration.ofSeconds(timeout));
            return wait.until(condition);
        } catch (TimeoutException ex) {
            if (retry > 0) {
                return this.waitUntil(condition, this.getSeleniumDefaultRetryWaitLimit(), --retry);
            }
            throw ex;
        }
    }

    protected int getSeleniumDefaultWaitLimit() {
        return AbstractSeleniumTest.DEFAULT_MAX_WAIT_SECONDS;
    }

    protected int getSeleniumDefaultRetryWaitLimit() {
        return AbstractSeleniumTest.DEFAULT_RETRY_WAIT_SECONDS;
    }

    protected WebDriver getDriver() {
        return this.driver;
    }

    protected int getRetryCount() {
        return this.retryCount;
    }

    protected String getScreenShotPath() {
        return this.screenShotPath;
    }
}

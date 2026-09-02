package com.websummoner.websummoner.misc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.time.Duration;
import java.util.function.Function;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestBase {

    private static final Logger LOG = LoggerFactory.getLogger(TestBase.class);

    /** The fixtures are static HTML, so this only ever absorbs browser start-up jitter. */
    private static final Duration ELEMENT_TIMEOUT = Duration.ofSeconds(15);

    @RegisterExtension
    final WebDriverExtension webDriver = new WebDriverExtension(getCapabilitiesProcessor());

    public WebDriver getDriver() {
        return webDriver.getDriver();
    }

    public void waitUntilElementIsPresent(By by) {
        new WebDriverWait(getDriver(), ELEMENT_TIMEOUT).until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public String getPageTitle() {
        new WebDriverWait(getDriver(), ELEMENT_TIMEOUT).until(ExpectedConditions.not(ExpectedConditions.titleIs("")));
        return getDriver().getTitle();
    }

    public void openPage(Page page) {
        String pageUrl = webDriver.getPageUrl(page);
        LOG.info("Opening page at: {}", pageUrl);
        getDriver().get(pageUrl);
    }

    public void fail(String message, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        fail(String.format("%s: %s", message, sw));
    }

    public void fail(String msg) {
        Assertions.fail(msg);
    }

    public <T> void assertThat(T actual, org.hamcrest.Matcher<? super T> matcher) {
        MatcherAssert.assertThat(actual, matcher);
    }

    public <T> void assertThat(String reason, T actual, org.hamcrest.Matcher<? super T> matcher) {
        MatcherAssert.assertThat(reason, actual, matcher);
    }

    protected Function<MutableCapabilities, MutableCapabilities> getCapabilitiesProcessor() {
        return Function.identity();
    }

    /** The browser under test, for the few cases where behaviour is driver-specific. */
    protected String browserName() {
        return TestProperties.getBrowserName();
    }

    protected String getLocalHost() {
        try {
            if (System.getProperty("os.name").startsWith("Mac")) {
                return InetAddress.getLocalHost().getHostAddress();
            } else {
                return InetAddress.getLocalHost().getHostName();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

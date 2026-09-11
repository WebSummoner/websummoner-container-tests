package com.websummoner.websummoner.misc;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Function;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 5 replacement for the old JUnit 4 WebDriverRule: creates a
 * RemoteWebDriver before each test, saves a screenshot on failure and quits
 * the driver afterwards.
 */
public class WebDriverExtension implements BeforeEachCallback, AfterEachCallback, TestExecutionExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebDriverExtension.class);
    private static final String CHROME = "chrome";
    private static final String YANDEX = "yandex";
    private static final String OPERA = "opera";
    private static final String BRAVE = "brave";
    private static final String SAFARI = "safari";
    private static final String EDGE = "MicrosoftEdge";

    private WebDriver driver;

    private final Function<MutableCapabilities, MutableCapabilities> capabilitiesProcessor;

    public WebDriverExtension(Function<MutableCapabilities, MutableCapabilities> capabilitiesProcessor) {
        this.capabilitiesProcessor = capabilitiesProcessor;
    }

    public WebDriver getDriver() {
        return driver;
    }

    String getPageUrl(Page page) {
        return String.format("%s/%s", TestProperties.getBaseUrl(), page.getName());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        driver = new RemoteWebDriver(getConnectionUrl(), capabilitiesProcessor.apply(getCapabilities()));
        // Generous but bounded: a cold browser container on a loaded host
        // can take well over five seconds to paint. Tests that care about
        // page-load timeouts set their own (see TestTimeouts).
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        settleStartPage();
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        // A failed assumption lands here too, and a skip is not a failure.
        if (driver != null && !(throwable instanceof TestAbortedException)) {
            takeScreenshot(driver);
        }
        throw throwable;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (WebDriverException e) {
                // WebKit ends the session then closes the socket without a
                // response, so quit throws on a teardown that worked. Anywhere
                // else a failing quit is real.
                if (!SAFARI.equals(TestProperties.getBrowserName())) {
                    throw e;
                }
                LOG.info("Ignoring WebKit quit teardown error: session is already gone");
            } finally {
                driver = null;
            }
        }
    }

    /**
     * Yandex loads its own start page (https://ya.ru/) shortly after launch, on
     * top of whatever was navigated to first. Spend that race on a blank page
     * here, so the first navigation a test makes is not the one that gets
     * stolen. It runs before any test page loads, so there is no alert or
     * history for it to disturb.
     *
     * <p>Scoped to Yandex on purpose: it is a workaround for one browser's
     * behaviour, and WebKit in particular does not tolerate the extra
     * navigation ("A previous interaction is still underway").
     */
    private void settleStartPage() {
        if (!YANDEX.equals(TestProperties.getBrowserName())) {
            return;
        }
        try {
            driver.get("about:blank");
        } catch (RuntimeException e) {
            LOG.debug("Could not pre-navigate to about:blank", e);
        }
    }

    private URL getConnectionUrl() throws MalformedURLException {
        return new URL(TestProperties.getConnectionUrl());
    }

    MutableCapabilities getCapabilities() {
        switch (TestProperties.getBrowserName()) {
            case CHROME -> {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("no-sandbox");
                return options;
            }
            case EDGE -> {
                // Edge is Chromium but needs its own class: arguments travel
                // under ms:edgeOptions, which ChromeOptions never writes.
                EdgeOptions options = new EdgeOptions();
                options.addArguments("no-sandbox");
                return options;
            }
            case YANDEX, OPERA, BRAVE -> {
                // Selenium 4 dropped the dedicated Opera class, and there was
                // never one for Yandex or Brave. All three are Chromium, so
                // ChromeOptions with an explicit binary and the real
                // browserName is what their in-container drivers accept.
                ChromeOptions options = new ChromeOptions();
                options.setCapability("browserName", TestProperties.getBrowserName());
                options.setCapability("browserVersion", TestProperties.getBrowserVersion());
                options.setBinary(binaryFor(TestProperties.getBrowserName()));
                options.addArguments("no-sandbox");
                return options;
            }
            default -> {
                MutableCapabilities caps = new MutableCapabilities();
                caps.setCapability("browserName", TestProperties.getBrowserName());
                caps.setCapability("browserVersion", TestProperties.getBrowserVersion());
                return caps;
            }
        }
    }

    private static String binaryFor(String browserName) {
        return switch (browserName) {
            case OPERA -> "/usr/bin/opera";
            case BRAVE -> "/usr/bin/brave-browser";
            default -> "/usr/bin/yandex-browser";
        };
    }

    private void takeScreenshot(WebDriver driver) {
        // Catch everything: this runs while a test is already failing, and an
        // exception here would replace the real failure in the report.
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path target = Path.of("target", "failure-screenshot-" + System.currentTimeMillis() + ".png");
            Files.createDirectories(target.getParent());
            Files.write(target, png);
            LOG.info("Failure screenshot saved to {}", target);
        } catch (Exception e) {
            LOG.warn("Failed to save failure screenshot", e);
        }
    }
}

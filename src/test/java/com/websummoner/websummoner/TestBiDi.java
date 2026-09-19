package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.bidi.log.ConsoleLogEntry;
import org.openqa.selenium.bidi.module.LogInspector;
import org.openqa.selenium.remote.RemoteWebDriver;

@Tag("bidi")
@DisplayName("WebDriver BiDi")
public class TestBiDi extends TestBase {

    private static final String PROBE = "websummoner-bidi-probe";

    @Override
    protected Function<MutableCapabilities, MutableCapabilities> getCapabilitiesProcessor() {
        return caps -> {
            caps.setCapability("webSocketUrl", true);
            return caps;
        };
    }

    /** Skips rather than fails when the driver ignored the capability. */
    private String webSocketUrl() {
        Object raw = ((RemoteWebDriver) getDriver()).getCapabilities().getCapability("webSocketUrl");
        assumeTrue(raw instanceof String, browserName() + " returned no webSocketUrl");
        return (String) raw;
    }

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("webSocketUrl points at the hub, not at the browser container")
    public void testWebSocketUrlIsRewritten() {
        String url = webSocketUrl();
        String sessionId = ((RemoteWebDriver) getDriver()).getSessionId().toString();
        assertThat(url, startsWith("ws://"));
        assertThat(url, containsString("/bidi/" + sessionId));
        assertThat("container address leaked to the client", url, not(containsString(":4444/session/")));
    }

    @Test
    @DisplayName("Streams a console message over the proxied socket")
    public void testConsoleEntryOverBiDi() throws Exception {
        webSocketUrl();
        try (LogInspector inspector = new LogInspector(getDriver())) {
            CompletableFuture<ConsoleLogEntry> received = new CompletableFuture<>();
            inspector.onConsoleEntry(received::complete);
            ((JavascriptExecutor) getDriver()).executeScript("console.log(arguments[0])", PROBE);
            assertThat(received.get(20, TimeUnit.SECONDS).getText(), containsString(PROBE));
        }
    }
}

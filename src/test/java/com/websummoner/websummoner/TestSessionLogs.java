package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.MutableCapabilities;

@Tag("logs")
@DisplayName("Session logs")
public class TestSessionLogs extends TestBase {

    private static final String LOG_NAME = "websummoner-session.log";

    @Override
    protected Function<MutableCapabilities, MutableCapabilities> getCapabilitiesProcessor() {
        return caps -> {
            caps.setCapability("websummoner:options", Map.of("enableLog", true, "logName", LOG_NAME));
            return caps;
        };
    }

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Streams container output while the session is live")
    public void testLiveLogListing() throws Exception {
        HttpResponse<String> listing = http("GET", "/logs/?json", null);
        assertThat(listing.statusCode(), equalTo(200));
    }

    @Test
    @DisplayName("Writes the log file when the session ends")
    public void testLogFileWrittenOnQuit() throws Exception {
        getDriver().quit();

        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            HttpResponse<String> file = http("GET", "/logs/" + LOG_NAME, null);
            assertThat(file.statusCode(), equalTo(200));
            assertThat(file.body(), is(not(emptyString())));
        });
    }
}

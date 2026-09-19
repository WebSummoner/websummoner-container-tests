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

@Tag("har")
@DisplayName("HAR capture")
public class TestHarCapture extends TestBase {

    private static final String HAR_NAME = "websummoner-session.har";

    @Override
    protected Function<MutableCapabilities, MutableCapabilities> getCapabilitiesProcessor() {
        return caps -> {
            caps.setCapability("websummoner:options", Map.of("enableHAR", true, "harName", HAR_NAME));
            return caps;
        };
    }

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Writes the page's requests to a HAR file when the session ends")
    public void testHarWrittenOnQuit() throws Exception {
        getDriver().quit();

        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            HttpResponse<String> har = http("GET", "/har/" + HAR_NAME, null);
            assertThat(har.statusCode(), equalTo(200));
            assertThat(har.body(), containsString("\"version\":\"1.2\""));
            assertThat(har.body(), containsString("first.html"));
        });
    }
}

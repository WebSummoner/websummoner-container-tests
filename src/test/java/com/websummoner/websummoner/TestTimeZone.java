package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;

@Tag("timezone")
@DisplayName("Time zone")
public class TestTimeZone extends TestBase {

    private static final String ZONE = "Asia/Tokyo";
    private static final long OFFSET_MINUTES = -540;

    @Override
    protected Function<MutableCapabilities, MutableCapabilities> getCapabilitiesProcessor() {
        return caps -> {
            caps.setCapability("websummoner:options", Map.of("timeZone", ZONE));
            return caps;
        };
    }

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Container runs in the requested zone")
    public void testTimeZoneApplied() {
        Object offset = ((JavascriptExecutor) getDriver()).executeScript("return new Date().getTimezoneOffset()");
        assertThat(((Number) offset).longValue(), equalTo(OFFSET_MINUTES));
    }

    @Test
    @DisplayName("Intl reports the requested zone")
    public void testIntlZone() {
        Object zone = ((JavascriptExecutor) getDriver())
                .executeScript("return Intl.DateTimeFormat().resolvedOptions().timeZone");
        assertThat(String.valueOf(zone), equalTo(ZONE));
    }
}

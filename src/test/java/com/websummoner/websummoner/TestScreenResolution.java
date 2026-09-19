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

@Tag("resolution")
@DisplayName("Screen resolution")
public class TestScreenResolution extends TestBase {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 1024;

    @Override
    protected Function<MutableCapabilities, MutableCapabilities> getCapabilitiesProcessor() {
        return caps -> {
            caps.setCapability("websummoner:options", Map.of("screenResolution", WIDTH + "x" + HEIGHT + "x24"));
            return caps;
        };
    }

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Virtual display matches the requested size")
    public void testScreenSize() {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        assertThat(((Number) js.executeScript("return screen.width")).intValue(), equalTo(WIDTH));
        assertThat(((Number) js.executeScript("return screen.height")).intValue(), equalTo(HEIGHT));
    }
}

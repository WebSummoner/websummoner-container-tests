package com.websummoner.websummoner;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

@Tag("screenshots")
@DisplayName("Screenshots")
public class TestScreenshot extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Captures a viewport screenshot as PNG bytes")
    public void testScreenshot() throws Exception {
        WebDriver driver = getDriver();
        try {
            takeScreenshot(driver);
        } catch (Exception e) {
            fail("Screenshots are not supported", e);
        }
    }

    private byte[] takeScreenshot(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }
}

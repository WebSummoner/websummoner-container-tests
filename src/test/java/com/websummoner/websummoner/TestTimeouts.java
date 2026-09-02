package com.websummoner.websummoner;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Tag("timeouts")
@DisplayName("Timeouts")
public class TestTimeouts extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Applies a page-load timeout")
    public void testPageLoadTimeout() throws Exception {
        try {
            WebDriver driver = getDriver();
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(1));
            openPage(Page.SECOND);
        } catch (Exception e) {
            fail("Setting page load timeout is not supported", e);
        }
    }

    @Test
    @DisplayName("Applies an implicit wait timeout")
    public void testImplicitTimeout() throws Exception {
        try {
            WebDriver driver = getDriver();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        } catch (Exception e) {
            fail("Implicitly waiting is not supported", e);
        }
    }

    @Test
    @DisplayName("Applies a script timeout")
    public void testScriptTimeout() throws Exception {
        try {
            WebDriver driver = getDriver();
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));
        } catch (Exception e) {
            fail("Setting script timeout is not supported", e);
        }
    }
}

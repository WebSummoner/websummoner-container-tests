package com.websummoner.websummoner;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

@Tag("alerts")
@DisplayName("Alerts")
public class TestAlert extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.ALERT);
    }

    @Test
    @DisplayName("Accepts and dismisses a JavaScript alert")
    public void testAlertClose() throws Exception {
        try {
            WebDriver driver = getDriver();
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            fail("Closing alerts is not supported", e);
        }
    }
}

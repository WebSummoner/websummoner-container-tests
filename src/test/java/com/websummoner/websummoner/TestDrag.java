package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@Tag("actions")
@DisplayName("Drag and drop")
public class TestDrag extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.DRAG);
        waitUntilElementIsPresent(By.cssSelector("#custom-handle"));
    }

    @Test
    @DisplayName("Drags an element with the W3C Actions API")
    public void testDrag() throws Exception {
        try {
            WebDriver driver = getDriver();
            WebElement button = driver.findElement(By.cssSelector("#custom-handle"));
            assertThat(button.getText(), equalTo("10"));

            Actions drag = new Actions(driver);
            drag.clickAndHold(button).moveByOffset(100, 0).build().perform();

            assertThat(button.getText(), equalTo("20"));
        } catch (Exception e) {
            fail("Drag functionality is not supported", e);
        }
    }
}

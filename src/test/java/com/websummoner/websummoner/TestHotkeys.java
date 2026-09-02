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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Tag("keyboard")
@DisplayName("Keyboard input")
public class TestHotkeys extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.HOTKEYS);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Sends a key chord to the focused element")
    public void testHotkeys() {
        WebDriver driver = getDriver();
        String someKeys = Keys.chord(Keys.CONTROL, "c");
        WebElement divTag = driver.findElement(By.cssSelector("#test-id"));
        assertThat(divTag.getText(), equalTo("initial"));
        driver.findElement(By.tagName("html")).sendKeys(someKeys);
        assertThat(divTag.getText(), equalTo("new"));
    }
}

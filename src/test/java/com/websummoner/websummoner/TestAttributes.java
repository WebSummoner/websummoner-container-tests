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

@Tag("elements")
@DisplayName("Element attributes")
public class TestAttributes extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.SECOND);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Reads an element's size and page position")
    public void testGetAttributes() throws Exception {
        WebDriver driver = getDriver();
        WebElement div = driver.findElement(By.cssSelector("#test-id"));
        assertThat(div.getSize().getWidth(), equalTo(100));
        assertThat(div.getSize().getHeight(), equalTo(100));
        assertThat(div.getLocation().getX(), equalTo(100));
        assertThat(div.getLocation().getY(), equalTo(100));
    }
}

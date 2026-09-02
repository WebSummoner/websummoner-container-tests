package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Tag("locators")
@DisplayName("Element locators")
public class TestFindElement extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Finds an element by CSS selector")
    public void testFindByCSSSelector() throws Exception {
        WebDriver driver = getDriver();
        List<WebElement> elementsByCSSSelector = driver.findElements(By.cssSelector("div#test-id"));
        assertThat(elementsByCSSSelector, hasSize(1));
        assertThat(elementsByCSSSelector.get(0).getTagName(), equalToIgnoringCase("div"));
        assertThat(elementsByCSSSelector.get(0).getText(), equalTo("foo"));
    }

    @Test
    @DisplayName("Finds an element by id")
    public void testFindById() throws Exception {
        WebDriver driver = getDriver();
        List<WebElement> elementsById = driver.findElements(By.cssSelector("#test-id"));
        assertThat(elementsById, hasSize(1));
        assertThat(elementsById.get(0).getTagName(), equalToIgnoringCase("div"));
        assertThat(elementsById.get(0).getText(), equalTo("foo"));
    }

    @Test
    @DisplayName("Finds an element by class name")
    public void testFindByClass() throws Exception {
        WebDriver driver = getDriver();
        List<WebElement> elementsByClass = driver.findElements(By.className("test-class"));
        assertThat(elementsByClass, hasSize(1));
        assertThat(elementsByClass.get(0).getTagName(), equalToIgnoringCase("span"));
        assertThat(elementsByClass.get(0).getText(), equalTo("bar"));
    }

    @Test
    @DisplayName("Finds an element by XPath")
    public void testFindByXPath() throws Exception {
        WebDriver driver = getDriver();
        List<WebElement> elementsByXPath =
                driver.findElements(By.xpath("/html/body/span[contains(@class, 'test-class')]"));
        assertThat(elementsByXPath, hasSize(1));
        assertThat(elementsByXPath.get(0).getTagName(), equalToIgnoringCase("span"));
        assertThat(elementsByXPath.get(0).getText(), equalTo("bar"));
    }
}

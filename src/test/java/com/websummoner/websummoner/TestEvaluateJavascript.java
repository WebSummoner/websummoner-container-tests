package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Tag("javascript")
@DisplayName("JavaScript execution")
public class TestEvaluateJavascript extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
        WebDriver driver = getDriver();
        assertThat("Javascript execution is not supported", driver, is(instanceOf(JavascriptExecutor.class)));
    }

    @Test
    @DisplayName("Executes a synchronous script and returns its value")
    public void testEvaluateJavascript() throws Exception {
        try {
            WebDriver driver = getDriver();

            WebElement element = driver.findElement(By.cssSelector("div#test-id"));
            assertThat(element.getText(), equalTo("foo"));

            JavascriptExecutor javaScriptExecutor = (JavascriptExecutor) driver;
            javaScriptExecutor.executeScript(getScript());
            assertThat(element.getText(), equalTo("bar"));

        } catch (Exception e) {
            fail("Synchronous javascript execution is not supported", e);
        }
    }

    @Test
    @DisplayName("Executes an asynchronous script and awaits its callback")
    public void testEvaluateJavascriptAsync() throws Exception {
        try {
            WebDriver driver = getDriver();

            WebElement element = driver.findElement(By.cssSelector("div#test-id"));
            assertThat(element.getText(), equalTo("foo"));

            JavascriptExecutor javaScriptExecutor = (JavascriptExecutor) driver;
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(1));
            String result = String.valueOf(javaScriptExecutor.executeAsyncScript(getAsyncScript()));
            assertThat(element.getText(), equalTo("baz"));
            assertThat(result, equalTo("works"));
        } catch (Exception e) {
            fail("Asynchronous javascript execution is not supported");
        }
    }

    @Test
    @DisplayName("Scrolls the page from injected JavaScript")
    public void testScroll() throws Exception {
        try {
            WebDriver driver = getDriver();
            JavascriptExecutor javaScriptExecutor = (JavascriptExecutor) driver;
            javaScriptExecutor.executeScript("scroll(0, 500);");
        } catch (Exception e) {
            fail("Window scrolling is not supported", e);
        }
    }

    private String getScript() {
        return "var div = document.getElementById('test-id'); div.textContent = 'bar';";
    }

    private String getAsyncScript() {
        return "var callback = arguments[arguments.length - 1];" + " var div = document.getElementById('test-id');"
                + " div.textContent = 'baz'; callback('works');";
    }
}

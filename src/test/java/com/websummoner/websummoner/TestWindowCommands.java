package com.websummoner.websummoner;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;

@Tag("windows")
@DisplayName("Windows and frames")
public class TestWindowCommands extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    /** WebKit ends the session on close(), so nothing can be asserted afterwards. */
    @Test
    @DisplayName("Closes a browser window")
    public void testWindowCloseIsSupported() throws Exception {
        Assumptions.assumeFalse("safari".equals(browserName()), "WebKitWebDriver ends the session on close()");
        try {
            WebDriver driver = getDriver();
            // Count the change rather than assume the session starts with one
            // window: Opera reports its own interface (Speed Dial, address-bar
            // dropdown) as browser windows, so a fresh session already has
            // several handles.
            int before = driver.getWindowHandles().size();
            WebElement link = driver.findElement(By.cssSelector("#test-link")); // This link opens new window
            link.click();
            await().until(() -> getDriver().getWindowHandles().size() == before + 1);
            getDriver().close();
            await().until(() -> getDriver().getWindowHandles().size() == before);
        } catch (Exception e) {
            fail("WebDriver.close() is not supported", e);
        }
    }

    @Test
    @DisplayName("Switches between two browser windows")
    public void testSwitchWindows() throws Exception {
        try {
            WebDriver driver = getDriver();
            int before = driver.getWindowHandles().size();
            WebElement link = driver.findElement(By.cssSelector("#test-link")); // This link opens new window
            link.click();
            await().until(() -> driver.getWindowHandles().size() == before + 1);
            List<String> windowNames = driver.getWindowHandles().stream()
                    .map(wh -> {
                        driver.switchTo().window(wh);
                        return getPageTitle();
                    })
                    .collect(Collectors.toList());
            // hasItems, not containsInAnyOrder: browser-chrome windows may also
            // be present (see above).
            assertThat(windowNames, hasItems("first", "second"));
        } catch (Exception e) {
            fail("Switching between windows is not supported", e);
        }
    }

    @Test
    @DisplayName("Switches into and out of an iframe")
    public void testSwitchFrames() throws Exception {
        try {
            openPage(Page.FRAMES);
            WebDriver driver = getDriver();
            // Search from the driver once switched, rather than from
            // switchTo().activeElement(): the active element is only incidental
            // to frame switching, and using it as a search root breaks on
            // WebKitWebDriver with a StaleElementReferenceException.
            WebElement firstFrameElement = driver.findElement(By.cssSelector("#first"));
            driver.switchTo().frame(firstFrameElement);
            assertThat(driver.findElements(By.cssSelector("span.test-class")), hasSize(1));

            driver.switchTo().defaultContent();
            WebElement secondFrameElement = driver.findElement(By.cssSelector("#second"));
            driver.switchTo().frame(secondFrameElement);
            assertThat(driver.findElements(By.cssSelector("div#test-id")), hasSize(1));
            assertThat(driver.findElements(By.cssSelector("span.test-class")), is(empty()));
        } catch (Exception e) {
            fail("Switching between frames is not supported", e);
        }
    }

    @Test
    @DisplayName("Navigates back and forward through history")
    public void testBackAndForward() throws Exception {
        WebDriver driver = getDriver();
        assertThat(getPageTitle(), equalTo("first"));
        openPage(Page.SECOND);
        assertThat(getPageTitle(), equalTo("second"));
        driver.navigate().back();
        assertThat(getPageTitle(), equalTo("first"));
        driver.navigate().forward();
        assertThat(getPageTitle(), equalTo("second"));
    }
}

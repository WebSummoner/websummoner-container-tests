package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

@Tag("cookies")
@DisplayName("Cookies")
public class TestCookies extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Writes a cookie and reads it back")
    public void testCookie() throws Exception {
        final String COOKIE_NAME = "test-cookie";
        final String COOKIE_VALUE = "test-cookie";
        WebDriver driver = getDriver();
        try {
            driver.manage().deleteAllCookies();
            // sameSite must be set explicitly. Chromium and Gecko apply a
            // default when it is missing; WebKitWebDriver drops the cookie
            // instead, silently and with a success response.
            Cookie cookie = new Cookie.Builder(COOKIE_NAME, COOKIE_VALUE)
                    .path("/")
                    .sameSite("Lax")
                    .build();
            driver.manage().addCookie(cookie);
            Cookie fetchedCookie = driver.manage().getCookieNamed(COOKIE_NAME);
            assertThat(fetchedCookie.getValue(), equalTo(COOKIE_VALUE));
            driver.manage().deleteCookie(fetchedCookie);
            assertThat(driver.manage().getCookies(), is(empty()));
        } catch (Exception e) {
            fail("Cookies are not supported", e);
        }
    }

    /**
     * Setting a cookie from the page rather than through the driver's cookie
     * endpoint. This is the portable way to seed a cookie: WebKitWebDriver
     * accepts {@code addCookie}, reports success and stores nothing, so
     * {@link #testCookie()} cannot pass on the WebKit image. Reading is fine on
     * every driver, so a cookie written here is visible to the driver
     * afterwards.
     */
    @Test
    @DisplayName("Writes a cookie from page JavaScript and reads it through the driver")
    public void testCookieViaJavascript() throws Exception {
        final String COOKIE_NAME = "js-cookie";
        final String COOKIE_VALUE = "js-value";
        WebDriver driver = getDriver();
        try {
            driver.manage().deleteAllCookies();
            ((JavascriptExecutor) driver)
                    .executeScript(String.format("document.cookie = '%s=%s; path=/'", COOKIE_NAME, COOKIE_VALUE));

            Cookie fetchedCookie = driver.manage().getCookieNamed(COOKIE_NAME);
            assertThat("driver cannot see the cookie the page set", fetchedCookie, is(notNullValue()));
            assertThat(fetchedCookie.getValue(), equalTo(COOKIE_VALUE));

            String fromPage = (String) ((JavascriptExecutor) driver).executeScript("return document.cookie");
            assertThat(fromPage, containsString(COOKIE_NAME + "=" + COOKIE_VALUE));
        } catch (Exception e) {
            fail("Setting cookies from page JavaScript is not supported", e);
        }
    }
}

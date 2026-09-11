package com.websummoner.websummoner.misc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;

/**
 * Unit tests for the browser-capability routing — the one piece of real logic
 * in {@link WebDriverExtension} that does not need a live grid. Selenium 4
 * dropped the dedicated Opera classes and routes Opera/Yandex through
 * {@link ChromeOptions}, so this guards against that regressing.
 */
class WebDriverExtensionTest {

    private final WebDriverExtension extension = new WebDriverExtension(Function.identity());

    @AfterEach
    void clearBrowserName() {
        System.clearProperty("grid.browser.name");
    }

    @Test
    void pageUrlJoinsBaseUrlAndPageName() {
        assertThat(extension.getPageUrl(Page.FIRST), containsString("first.html"));
    }

    @Test
    void chromeRoutesThroughChromeOptions() {
        System.setProperty("grid.browser.name", "chrome");
        assertThat(extension.getCapabilities(), instanceOf(ChromeOptions.class));
    }

    @Test
    void yandexRoutesThroughChromeOptions() {
        System.setProperty("grid.browser.name", "yandex");
        assertThat(extension.getCapabilities(), instanceOf(ChromeOptions.class));
    }

    @Test
    void operaRoutesThroughChromeOptions() {
        System.setProperty("grid.browser.name", "opera");
        assertThat(extension.getCapabilities(), instanceOf(ChromeOptions.class));
    }

    @Test
    void braveRoutesThroughChromeOptions() {
        System.setProperty("grid.browser.name", "brave");
        assertThat(extension.getCapabilities(), instanceOf(ChromeOptions.class));
    }

    @Test
    void edgeRoutesThroughEdgeOptions() {
        System.setProperty("grid.browser.name", "MicrosoftEdge");
        assertThat(extension.getCapabilities(), instanceOf(EdgeOptions.class));
    }

    @Test
    void unknownBrowserFallsBackToPlainCapabilities() {
        System.setProperty("grid.browser.name", "firefox");
        MutableCapabilities caps = extension.getCapabilities();
        assertThat(caps, is(instanceOf(MutableCapabilities.class)));
        assertThat(caps.getBrowserName(), is("firefox"));
    }
}

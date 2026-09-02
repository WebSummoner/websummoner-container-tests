package com.websummoner.websummoner.misc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the property-reading defaults. No browser or grid required —
 * these exercise the pure {@link TestProperties} logic in isolation, restoring
 * any system property they touch so the integration tests are unaffected.
 */
class TestPropertiesTest {

    @Test
    void returnsDefaultWhenPropertyUnset() {
        withPropertyCleared("grid.browser.name", () -> assertThat(TestProperties.getBrowserName(), is("chrome")));
    }

    @Test
    void returnsOverrideWhenPropertySet() {
        withProperty("grid.browser.name", "firefox", () -> assertThat(TestProperties.getBrowserName(), is("firefox")));
    }

    @Test
    void propFallsBackToSuppliedDefault() {
        withPropertyCleared(
                "some.absent.key",
                () -> assertThat(TestProperties.prop("some.absent.key", "fallback"), is("fallback")));
    }

    private static void withProperty(String key, String value, Runnable body) {
        String previous = System.getProperty(key);
        System.setProperty(key, value);
        try {
            body.run();
        } finally {
            restore(key, previous);
        }
    }

    private static void withPropertyCleared(String key, Runnable body) {
        String previous = System.getProperty(key);
        System.clearProperty(key);
        try {
            body.run();
        } finally {
            restore(key, previous);
        }
    }

    private static void restore(String key, String previous) {
        if (previous == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previous);
        }
    }
}

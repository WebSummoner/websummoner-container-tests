package com.websummoner.websummoner.misc;

/**
 * Test configuration read from system properties — the same -D flags the
 * images tool passes when validating a freshly built browser image.
 */
interface TestProperties {

    static String prop(String name, String defaultValue) {
        String value = System.getProperty(name);
        return value != null ? value : defaultValue;
    }

    // Hosted copy of the pages/ directory from this repository.
    static String getBaseUrl() {
        return prop("pages.base.url", "https://websummoner.github.io/websummoner-container-tests/pages");
    }

    static String getConnectionUrl() {
        return prop("grid.connection.url", "http://localhost:4444/wd/hub");
    }

    static String getBrowserName() {
        return prop("grid.browser.name", "chrome");
    }

    static String getBrowserVersion() {
        return prop("grid.browser.version", "152.0");
    }
}

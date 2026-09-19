package com.websummoner.websummoner.misc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.function.Function;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestBase {

    private static final Logger LOG = LoggerFactory.getLogger(TestBase.class);

    /** The fixtures are static HTML, so this only ever absorbs browser start-up jitter. */
    private static final Duration ELEMENT_TIMEOUT = Duration.ofSeconds(15);

    @RegisterExtension
    final WebDriverExtension webDriver = new WebDriverExtension(getCapabilitiesProcessor());

    public WebDriver getDriver() {
        return webDriver.getDriver();
    }

    public void waitUntilElementIsPresent(By by) {
        new WebDriverWait(getDriver(), ELEMENT_TIMEOUT).until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public String getPageTitle() {
        new WebDriverWait(getDriver(), ELEMENT_TIMEOUT).until(ExpectedConditions.not(ExpectedConditions.titleIs("")));
        return getDriver().getTitle();
    }

    public void openPage(Page page) {
        String pageUrl = webDriver.getPageUrl(page);
        LOG.info("Opening page at: {}", pageUrl);
        getDriver().get(pageUrl);
    }

    public void fail(String message, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        fail(String.format("%s: %s", message, sw));
    }

    public void fail(String msg) {
        Assertions.fail(msg);
    }

    public <T> void assertThat(T actual, org.hamcrest.Matcher<? super T> matcher) {
        MatcherAssert.assertThat(actual, matcher);
    }

    public <T> void assertThat(String reason, T actual, org.hamcrest.Matcher<? super T> matcher) {
        MatcherAssert.assertThat(reason, actual, matcher);
    }

    /** The hub root, for the endpoints that sit outside /wd/hub. */
    protected String hubUrl() {
        return TestProperties.getConnectionUrl().replaceAll("/wd/hub/?$", "");
    }

    protected String sessionId() {
        return ((org.openqa.selenium.remote.RemoteWebDriver) getDriver())
                .getSessionId()
                .toString();
    }

    protected HttpResponse<String> http(String method, String path, String body) throws Exception {
        HttpRequest.BodyPublisher payload =
                body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder(URI.create(hubUrl() + path))
                .method(method, payload)
                .timeout(Duration.ofSeconds(30))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected Function<MutableCapabilities, MutableCapabilities> getCapabilitiesProcessor() {
        return Function.identity();
    }

    /** The browser under test, for the few cases where behaviour is driver-specific. */
    protected String browserName() {
        return TestProperties.getBrowserName();
    }

    /** An address a browser container can reach back on: a host name may not resolve there. */
    protected String getLocalHost() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            if (!local.isLoopbackAddress() && local instanceof Inet4Address) {
                return local.getHostAddress();
            }
            for (NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nic.isUp() || nic.isLoopback()) {
                    continue;
                }
                for (InetAddress address : Collections.list(nic.getInetAddresses())) {
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
            return local.getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

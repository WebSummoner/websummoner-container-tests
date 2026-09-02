package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.proxy.CaptureType;
import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import de.sstoehr.harreader.model.HarEntry;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;

@Tag("proxy")
@DisplayName("HTTP proxy")
public class TestProxy extends TestBase {

    private static final BrowserUpProxy proxy = new BrowserUpProxyServer();

    @BeforeAll
    public static void initProxy() {
        if (!proxy.isStarted()) {
            proxy.start();
            proxy.setHarCaptureTypes(CaptureType.REQUEST_HEADERS);
        }
    }

    @BeforeEach
    public void startHar() {
        if (proxy.isStarted()) {
            proxy.newHar();
        }
    }

    @Test
    @DisplayName("Routes browser traffic through a manual proxy and captures a HAR")
    public void testProxy() {
        openPage(Page.FIRST);
        List<HarEntry> proxyLogEntries = proxy.getHar().getLog().getEntries();
        assertThat(proxyLogEntries, is(not(empty())));
    }

    @AfterEach
    public void clearHar() {
        if (proxy.isStarted()) {
            proxy.endHar();
        }
    }

    @AfterAll
    public static void shutdownProxy() {
        if (proxy.isStarted()) {
            proxy.stop();
        }
    }

    @Override
    protected Function<MutableCapabilities, MutableCapabilities> getCapabilitiesProcessor() {
        return caps -> {
            Proxy seleniumProxy = new Proxy();
            seleniumProxy
                    .setProxyType(Proxy.ProxyType.MANUAL)
                    .setHttpProxy(getProxyString())
                    .setSslProxy(getProxyString());
            caps.setCapability("proxy", seleniumProxy);
            // The proxy MITMs TLS with its own generated CA, which no browser
            // trusts. Firefox enforces the W3C default (acceptInsecureCerts
            // false) and aborts the navigation; Chromium-based drivers happen
            // to be lenient. Opt in explicitly so the test is browser-agnostic.
            caps.setCapability("acceptInsecureCerts", true);
            return caps;
        };
    }

    private String getProxyString() {
        // The browser runs in a container, so the proxy address must be one
        // the container can reach — override with -Dproxy.host when the
        // default hostname does not resolve from inside Docker (e.g. pass the
        // docker bridge gateway address).
        String host = System.getProperty("proxy.host", getLocalHost());
        return String.format("%s:%d", host, proxy.getPort());
    }
}

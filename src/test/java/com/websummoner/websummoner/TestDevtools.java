package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

@Tag("devtools")
@DisplayName("Chrome DevTools Protocol")
public class TestDevtools extends TestBase {

    private String cdpUrl() {
        Object raw = ((RemoteWebDriver) getDriver()).getCapabilities().getCapability("se:cdp");
        assumeTrue(raw instanceof String, browserName() + " advertises no se:cdp");
        return (String) raw;
    }

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("se:cdp points at the hub, not at the browser container")
    public void testCdpUrlIsRewritten() {
        String url = cdpUrl();
        assertThat(url, startsWith("ws://"));
        assertThat(url, containsString("/devtools/" + sessionId()));
    }

    @Test
    @DisplayName("Answers a CDP command through the proxied socket")
    public void testCdpCommand() throws Exception {
        String url = cdpUrl();
        CompletableFuture<String> reply = new CompletableFuture<>();
        WebSocket socket = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(url), new WebSocket.Listener() {
                    private final StringBuilder frame = new StringBuilder();

                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                        frame.append(data);
                        if (last) {
                            reply.complete(frame.toString());
                        }
                        ws.request(1);
                        return null;
                    }
                })
                .get(20, TimeUnit.SECONDS);
        try {
            socket.sendText("{\"id\":1,\"method\":\"Browser.getVersion\"}", true);
            assertThat(reply.get(20, TimeUnit.SECONDS), containsString("product"));
        } finally {
            socket.abort();
        }
    }
}

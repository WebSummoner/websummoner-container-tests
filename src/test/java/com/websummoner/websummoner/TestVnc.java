package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.MutableCapabilities;

@Tag("vnc")
@DisplayName("VNC")
public class TestVnc extends TestBase {

    @Override
    protected Function<MutableCapabilities, MutableCapabilities> getCapabilitiesProcessor() {
        return caps -> {
            caps.setCapability("websummoner:options", Map.of("enableVNC", true));
            return caps;
        };
    }

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Proxies the RFB handshake from the container")
    public void testRfbHandshake() throws Exception {
        CompletableFuture<String> greeting = new CompletableFuture<>();
        String url = hubUrl().replaceFirst("^http", "ws") + "/vnc/" + sessionId();
        WebSocket socket = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                // x/net/websocket rejects a handshake without an Origin.
                .header("Origin", hubUrl())
                .buildAsync(URI.create(url), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onBinary(WebSocket ws, ByteBuffer data, boolean last) {
                        greeting.complete(StandardCharsets.US_ASCII.decode(data).toString());
                        ws.request(1);
                        return null;
                    }
                })
                .get(20, TimeUnit.SECONDS);
        try {
            assertThat(greeting.get(20, TimeUnit.SECONDS), startsWith("RFB "));
        } finally {
            socket.abort();
        }
    }
}

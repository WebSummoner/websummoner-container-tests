package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@Tag("clipboard")
@DisplayName("Clipboard")
public class TestClipboard extends TestBase {

    private static final String TEXT = "websummoner-clipboard";

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    @Test
    @DisplayName("Writes to the container clipboard and reads it back")
    public void testClipboardRoundTrip() throws Exception {
        HttpResponse<String> write = http("POST", "/clipboard/" + sessionId(), TEXT);
        assertThat(write.statusCode(), equalTo(200));

        HttpResponse<String> read = http("GET", "/clipboard/" + sessionId(), null);
        assertThat(read.statusCode(), equalTo(200));
        assertThat(read.body(), containsString(TEXT));
    }

    @Test
    @DisplayName("Unknown session is rejected")
    public void testUnknownSession() throws Exception {
        HttpResponse<String> response = http("GET", "/clipboard/does-not-exist", null);
        assertThat(response.statusCode(), not(equalTo(200)));
    }
}

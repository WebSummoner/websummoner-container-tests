package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

@Tag("download")
@DisplayName("File download")
public class TestFileDownload extends TestBase {

    private static final String FILE = "websummoner-download.txt";
    private static final String CONTENT = "downloaded-by-websummoner";

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.FIRST);
        waitUntilElementIsPresent(By.cssSelector("#test-id"));
    }

    private void downloadFromPage() {
        ((JavascriptExecutor) getDriver())
                .executeScript(
                        "const a = document.createElement('a');"
                                + "a.href = URL.createObjectURL(new Blob([arguments[1]], {type: 'text/plain'}));"
                                + "a.download = arguments[0];"
                                + "document.body.appendChild(a); a.click();",
                        FILE,
                        CONTENT);
    }

    @Test
    @DisplayName("Lists and serves a file the browser downloaded")
    public void testDownloadedFileIsServed() throws Exception {
        downloadFromPage();

        Awaitility.await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            HttpResponse<String> listing = http("GET", "/download/" + sessionId() + "/", null);
            assertThat(listing.statusCode(), equalTo(200));
            assertThat(listing.body(), containsString(FILE));
        });

        HttpResponse<String> file = http("GET", "/download/" + sessionId() + "/" + FILE, null);
        assertThat(file.statusCode(), equalTo(200));
        assertThat(file.body(), equalTo(CONTENT));
    }
}

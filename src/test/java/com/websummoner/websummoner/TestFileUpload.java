package com.websummoner.websummoner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Uploading through a RemoteWebDriver sends the file to the grid, which unpacks
 * it inside the browser container and hands the driver a container-local path.
 * This is what the hub's -enable-file-upload flag serves.
 */
@Tag("file-upload")
@DisplayName("File upload")
public class TestFileUpload extends TestBase {

    @BeforeEach
    public void before() throws Exception {
        openPage(Page.UPLOAD);
        waitUntilElementIsPresent(By.id("file-input"));
    }

    @Test
    @DisplayName("Uploads a local file through the grid into the browser container")
    public void testFileUpload() throws Exception {
        Path file = Files.createTempFile("websummoner-upload", ".txt");
        Files.write(file, "summoned".getBytes(StandardCharsets.UTF_8));

        ((RemoteWebDriver) getDriver()).setFileDetector(new LocalFileDetector());
        getDriver()
                .findElement(By.id("file-input"))
                .sendKeys(file.toAbsolutePath().toString());

        assertThat(
                getDriver().findElement(By.id("file-name")).getText(),
                equalTo(file.getFileName().toString()));
    }

    /**
     * Upstream Selenoid could not upload a file whose name was not ASCII
     * (aerokube/selenoid#669: "unable to unzip 'file'"), because the archive was
     * handed to the driver to unpack. WebSummoner extracts it in the hub
     * instead, so the encoding never reaches the driver, and the file lands in
     * the container with correct UTF-8 bytes on every image.
     *
     * <p>The last step is still the driver's: `sendKeys` has to accept the
     * container path. geckodriver and WebKitWebDriver do. Every
     * chromedriver-family driver rejects it with
     * "invalid argument: File not found : " — note the empty name, which is the
     * mangled path, not a missing file. Verified on Chrome, Edge, Brave, Yandex
     * and Opera against a file that `test -f` confirms is present. Skipped
     * there rather than asserted, because nothing in WebSummoner can fix it.
     */
    @Test
    @DisplayName("Uploads a file whose name is not ASCII")
    public void testFileUploadNonAsciiName() throws Exception {
        Assumptions.assumeFalse(
                isChromiumFamily(),
                "chromedriver cannot pass a non-ASCII path to sendKeys — see aerokube/selenoid#669");

        Path dir = Files.createTempDirectory("websummoner-upload-utf8");
        Path file = dir.resolve("PDF_ùâàæ.pdf");
        Files.write(file, "summoned".getBytes(StandardCharsets.UTF_8));

        ((RemoteWebDriver) getDriver()).setFileDetector(new LocalFileDetector());
        getDriver()
                .findElement(By.id("file-input"))
                .sendKeys(file.toAbsolutePath().toString());

        assertThat(
                getDriver().findElement(By.id("file-name")).getText(),
                equalTo(file.getFileName().toString()));
    }

    private boolean isChromiumFamily() {
        return switch (browserName()) {
            case "chrome", "MicrosoftEdge", "brave", "yandex", "opera" -> true;
            default -> false;
        };
    }
}

package com.websummoner.websummoner;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

/**
 * Checks a recording really has sound: a silent MP4 plays back fine and hides
 * the regression. Needs {@code -Dselenoid.video.dir}; the session quits inside
 * the test because the file is only finalised then.
 */
@EnabledIfSystemProperty(named = "selenoid.video.dir", matches = ".+")
public class TestVideoCapture {

    @Test
    @Timeout(120)
    public void recordedVideoContainsAudioStream() throws Exception {
        assumeTrue(hasFfprobe(), "ffprobe not on PATH, cannot inspect streams");

        // Settings go in the selenoid:options vendor object, not top-level keys.
        ChromeOptions options = new ChromeOptions();
        options.setCapability("selenoid:options", Map.of("enableVideo", true, "videoFrameRate", 12));
        RemoteWebDriver driver = new RemoteWebDriver(
                URI.create(System.getProperty("grid.connection.url", "http://localhost:4444/wd/hub"))
                        .toURL(),
                options);
        SessionId sessionId = driver.getSessionId();
        driver.get("about:blank");
        driver.quit();

        Path video = Path.of(System.getProperty("selenoid.video.dir"), sessionId + ".mp4");
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(2))
                .until(() -> Files.exists(video));

        boolean hasAudio = await().atMost(Duration.ofSeconds(15)).until(streamPresent(video), Boolean::booleanValue);
        assertTrue(hasAudio, "Recorded video has no audio stream: " + video);
    }

    private static Callable<Boolean> streamPresent(Path video) {
        return () -> {
            String out = new String(Runtime.getRuntime()
                    .exec(new String[] {
                        "ffprobe",
                        "-v",
                        "error",
                        "-select_streams",
                        "a",
                        "-show_entries",
                        "stream=codec_type",
                        "-of",
                        "csv=p=0",
                        video.toString()
                    })
                    .getInputStream()
                    .readAllBytes());
            return out.contains("audio");
        };
    }

    private static boolean hasFfprobe() {
        try {
            return new String(Runtime.getRuntime()
                            .exec(new String[] {"ffprobe", "-version"})
                            .getInputStream()
                            .readAllBytes())
                    .contains("ffprobe");
        } catch (Exception e) {
            return false;
        }
    }
}

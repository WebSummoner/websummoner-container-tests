package com.websummoner.websummoner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.websummoner.websummoner.misc.Page;
import com.websummoner.websummoner.misc.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

/**
 * Guards audio output in the image. An AudioContext only reaches "running" with
 * a working output device, so it catches a broken PulseAudio setup without
 * needing the recorder.
 */
public class TestAudioPlayback extends TestBase {

    /**
     * Brave ignores --autoplay-policy, so unblock audio the way a user does.
     * The click also proves the device works rather than the flag.
     */
    @BeforeEach
    public void allowAudioWithAUserGesture() {
        openPage(Page.FIRST);
        getDriver().findElement(By.tagName("h1")).click();
    }

    @Test
    public void audioOutputDeviceIsUsable() {
        Object state = ((JavascriptExecutor) getDriver()).executeScript("""
                const ctx = new AudioContext();
                const osc = ctx.createOscillator();
                osc.connect(ctx.destination);
                osc.start();
                ctx.resume();
                return new Promise((resolve) => {
                    const started = Date.now();
                    (function poll() {
                        if (ctx.state === 'running' || Date.now() - started > 5000) {
                            resolve(ctx.state);
                        } else {
                            setTimeout(poll, 100);
                        }
                    })();
                });""");
        assertEquals("running", state, () -> "AudioContext never started, audio output is broken: " + state);
    }

    @Test
    public void audioContextIsWritableToMediaStream() {
        // Recording pipelines need a MediaStreamDestination; a broken stack gives
        // zero channels.
        Object channels = ((JavascriptExecutor) getDriver()).executeScript("""
                const ctx = new AudioContext();
                const dest = ctx.createMediaStreamDestination();
                const osc = ctx.createOscillator();
                osc.connect(dest);
                osc.start();
                ctx.resume();
                return new Promise((resolve) => {
                    const started = Date.now();
                    (function poll() {
                        if (ctx.state === 'running' || Date.now() - started > 5000) {
                            resolve(ctx.destination.channelCount + '/' + dest.stream.getAudioTracks().length);
                        } else {
                            setTimeout(poll, 100);
                        }
                    })();
                });""");
        assertEquals("2/1", channels, () -> "Audio routing to MediaStream is broken: " + channels);
    }
}

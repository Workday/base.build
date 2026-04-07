package build.base.telemetry.foundation;

import build.base.telemetry.Commenced;
import build.base.telemetry.Diagnostic;
import build.base.telemetry.Fatal;
import build.base.telemetry.Location;
import build.base.telemetry.Telemetry;
import build.base.telemetry.Warning;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for {@link TelemetryRecorder} default methods and concrete implementations.
 */
class TelemetryRecorderTests {

    private static final URI URI = java.net.URI.create("test://recorder");

    /**
     * BUG-11: {@code diagnostic(Location, format, args)} had a stray {@code null} as the third argument,
     * routing the real format to the args array and calling {@code String.format(null, ...)} → NPE.
     */
    @Test
    void diagnosticWithLocationShouldNotThrowNPE() {
        final var recorder = NoOpTelemetryRecorder.of(URI);

        assertThatNoException()
            .isThrownBy(() -> {
                final Diagnostic d = recorder.diagnostic((Location) null, "msg %s", "arg");
                assertThat(d.message()).isEqualTo("msg arg");
            });
    }

    /**
     * BUG-9: {@code commence(format, args)} formatted the description once into {@code description},
     * then passed {@code description} + original {@code args} to {@code Commenced.create()}, which
     * formatted it a second time. When the original arg itself contains format specifiers (e.g. "%d"),
     * the second {@code String.format} call interprets the arg as a new format specifier and throws.
     */
    @Test
    void commenceShouldNotThrowWhenArgContainsFormatSpecifier() {
        final var recorder = NoOpTelemetryRecorder.of(URI);

        // "Status: %s" with arg "%d" → first format → "Status: %d"
        // Second (buggy) format: String.format("Status: %d", "%d") → IllegalFormatConversionException
        assertThatNoException()
            .isThrownBy(() -> recorder.commence("Status: %s", "%d"));
    }

    /**
     * BUG-9: Verify the formatted message is correct (not double-formatted) by inspecting
     * the output written to a stream.
     */
    @Test
    void commenceShouldFormatDescriptionExactlyOnce() {
        final var out = new ByteArrayOutputStream();
        final var recorder = PrintStreamTelemetryRecorder.of(URI, new PrintStream(out), null);

        recorder.commence("Processing %s items", "100");

        assertThat(out.toString()).contains("Processing 100 items");
    }

    /**
     * BUG-14: {@link PrintStreamTelemetryRecorder} routed {@link Fatal} and {@link Warning}
     * to {@code outputStream} instead of {@code errorStream}.
     */
    @Test
    void fatalShouldWriteToErrorStream() {
        final var out = new ByteArrayOutputStream();
        final var err = new ByteArrayOutputStream();
        final var recorder = PrintStreamTelemetryRecorder.of(URI,
            new PrintStream(out), new PrintStream(err));

        recorder.fatal((Throwable) null, "fatal message");

        assertThat(err.toString()).contains("fatal message");
        assertThat(out.toString()).doesNotContain("fatal message");
    }

    /**
     * BUG-14: {@link Warning} should go to {@code errorStream}.
     */
    @Test
    void warnShouldWriteToErrorStream() {
        final var out = new ByteArrayOutputStream();
        final var err = new ByteArrayOutputStream();
        final var recorder = PrintStreamTelemetryRecorder.of(URI,
            new PrintStream(out), new PrintStream(err));

        recorder.warn("warning message");

        assertThat(err.toString()).contains("warning message");
        assertThat(out.toString()).doesNotContain("warning message");
    }
}

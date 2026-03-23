package build.base.telemetry.ansi;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link ANSIBasedTelemetryRecorder}.
 *
 * @author brian.oliver
 * @since May-2025
 */
class ANSIBasedTelemetryRecorderTests {

    @Test
    void shouldUseTelemetryRecorder()
        throws InterruptedException {

        try (final var telemetryRecorder = ANSIBasedTelemetryRecorder.create()) {
            try (final var meter = telemetryRecorder.commence(100, "This should be fun!");
                 final var anotherMeter = telemetryRecorder.commence(50, "A second meter")) {

                for (int i = 1; i <= 100; i++) {
                    meter.progress();

                    Thread.sleep(100);

                    if (i % 2 == 0) {
                        anotherMeter.progress();
                        telemetryRecorder.advice("This is going well!");
                    }
                }
            }
        }
    }
}

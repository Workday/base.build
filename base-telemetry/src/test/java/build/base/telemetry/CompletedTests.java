package build.base.telemetry;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Completed}.
 */
class CompletedTests {

    private static final URI URI = java.net.URI.create("test://uri");

    /**
     * BUG-8: Verify that {@link Completed#toString()} correctly formats durations greater than one day.
     * The original arithmetic subtracted adjusted hours (not total hours) from total minutes,
     * producing wildly inflated minute values.
     */
    @Test
    void toStringShouldFormatMultiDayDurationCorrectly() {
        final var commenced = Instant.now().minus(Duration.ofDays(2).plusHours(1).plusMinutes(5).plusSeconds(30));
        final var completed = Completed.create(URI, null, commenced, "task");

        final var text = completed.toString();

        assertThat(text)
            .contains("2 days")
            .contains("1 hrs")
            .contains("5 mins")
            .contains("30 secs")
            .doesNotContain("2885 mins");
    }

    /**
     * BUG-8: Verify a simple sub-minute duration formats without days/hours/minutes noise.
     */
    @Test
    void toStringShouldFormatSubMinuteDurationCorrectly() {
        final var commenced = Instant.now().minus(Duration.ofSeconds(45));
        final var completed = Completed.create(URI, null, commenced, "task");

        final var text = completed.toString();

        assertThat(text)
            .contains("45 secs")
            .doesNotContain("days")
            .doesNotContain("hrs")
            .doesNotContain("mins");
    }
}

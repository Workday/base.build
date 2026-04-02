package build.base.telemetry;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for the {@code null} format-string handling across all telemetry factory methods.
 *
 * BUG-2: Every {@code create()} factory called {@code String.format(format, args)} unconditionally,
 * throwing NPE when {@code format} was {@code null}.
 */
class TelemetryFactoryTests {

    private static final URI URI = java.net.URI.create("test://uri");

    @Test
    void progressCreateWithNullFormatShouldProduceEmptyMessage() {
        assertThatNoException()
            .isThrownBy(() -> {
                final var p = Progress.create(URI, NamedUnit.none(), 5, 10, null);
                assertThat(p.message()).isEmpty();
            });
    }

    @Test
    void commencedCreateWithNullFormatShouldProduceEmptyMessage() {
        assertThatNoException()
            .isThrownBy(() -> {
                final var c = Commenced.create(URI, (String) null);
                assertThat(c.message()).isEmpty();
            });
    }

    @Test
    void commencedCreateWithLocationsAndNullFormatShouldProduceEmptyMessage() {
        assertThatNoException()
            .isThrownBy(() -> {
                final var c = Commenced.create(URI, Stream.empty(), null);
                assertThat(c.message()).isEmpty();
            });
    }

    @Test
    void completedCreateWithNullFormatShouldProduceEmptyMessage() {
        assertThatNoException()
            .isThrownBy(() -> {
                final var c = Completed.create(URI, null, Instant.now(), null);
                assertThat(c.message()).isEmpty();
            });
    }
}

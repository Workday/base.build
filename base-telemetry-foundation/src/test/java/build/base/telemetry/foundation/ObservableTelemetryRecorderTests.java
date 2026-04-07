package build.base.telemetry.foundation;

import build.base.telemetry.Information;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ObservableTelemetryRecorder}.
 */
class ObservableTelemetryRecorderTests {

    private static final URI URI_A = java.net.URI.create("test://a");
    private static final URI URI_B = java.net.URI.create("test://b");

    /**
     * BUG-13: {@link ObservableTelemetryRecorder#uri()} delegated to the wrapped recorder's URI,
     * ignoring the URI stored in {@code this.uri}. When constructed with a different URI,
     * the provided URI was silently discarded.
     */
    @Test
    void uriShouldReturnTheURIPassedAtConstruction() {
        final var inner = NoOpTelemetryRecorder.of(URI_B);
        final var observable = ObservableTelemetryRecorder.of(URI_A, inner);

        assertThat(observable.uri()).isEqualTo(URI_A);
    }

    /**
     * BUG-12: {@link ObservableTelemetryRecorder#stream()} was not synchronized, so concurrent
     * access from a recording thread and a reading thread could produce ConcurrentModificationException.
     * Verify that a concurrent record + stream does not throw.
     */
    @Test
    void streamShouldNotThrowDuringConcurrentRecording() throws InterruptedException {
        final var observable = ObservableTelemetryRecorder.of(URI_A, NoOpTelemetryRecorder.of(URI_A));

        final var thread = Thread.ofVirtual().start(() -> {
            for (int i = 0; i < 1000; i++) {
                observable.info("message %d", i);
            }
        });

        // Continuously read the stream while recording is happening
        for (int i = 0; i < 500; i++) {
            observable.stream().filter(t -> t instanceof Information).count();
        }

        thread.join();
    }

    /**
     * BUG-12: {@link ObservableTelemetryRecorder#hasObserved(Class)} was not synchronized either.
     */
    @Test
    void hasObservedShouldNotThrowDuringConcurrentRecording() throws InterruptedException {
        final var observable = ObservableTelemetryRecorder.of(URI_A, NoOpTelemetryRecorder.of(URI_A));

        final var thread = Thread.ofVirtual().start(() -> {
            for (int i = 0; i < 1000; i++) {
                observable.info("message %d", i);
            }
        });

        for (int i = 0; i < 500; i++) {
            observable.hasObserved(Information.class);
        }

        thread.join();
    }
}

package build.base.assertion;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

/**
 * Tests for {@link CompletableFutureAssertionTests}.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
class CompletableFutureAssertionTests {

    /**
     * Ensure that a {@link CompletableFuture} completed with a {@code null} value completes.
     */
    @Test
    void shouldCompleteWithNull() {
        final var completed = new CompletableFuture<>();
        completed.complete(null);

        CompletableFutureAssertion.assertThat(completed)
            .isCompleted();
    }
}

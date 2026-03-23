package build.base.assertion;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

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

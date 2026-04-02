package build.base.retryable;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Retryable}s.
 *
 * @author brian.oliver
 */
class RetryableTests {

    /**
     * Ensure we can create a {@link RetryableConstant} for a value.
     */
    @Test
    void shouldCreateConstantRetryables() {
        final Retryable<Integer> retryable = Retryable.of(42);

        assertThat(retryable)
            .isInstanceOf(RetryableConstant.class);

        assertThat(retryable.get())
            .isEqualTo(42);
    }

    /**
     * Ensure we can create a {@link RetryableConstant} for a <code>null</code>.
     */
    @Test
    void shouldCreateNullConstantRetryables() {
        final Retryable<Integer> retryable = Retryable.of((Integer) null);

        assertThat(retryable)
            .isInstanceOf(RetryableConstant.class);

        assertThat(retryable.get())
            .isNull();
    }

    /**
     * Ensure we can create a {@link RetryableConstant} for a {@link Supplier}.
     */
    @Test
    void shouldCreateRetryableSuppliers() {
        final Supplier<Integer> supplier = () -> 42;
        final var retryable = Retryable.of(supplier);

        assertThat(retryable)
            .isInstanceOf(RetryableSupplier.class);

        assertThat(retryable.get())
            .isEqualTo(42);
    }
}

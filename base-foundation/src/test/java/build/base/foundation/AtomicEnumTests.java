package build.base.foundation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AtomicEnum}.
 *
 * @author andrew.wilson
 * @since Dec-2020
 */
class AtomicEnumTests {

    /**
     * A testing {@link AtomicEnum}.
     */
    enum TestAtomicEnum {
        OPTION1,
        OPTION2,
        OPTION3;
    }

    /**
     * Ensure {@link AtomicEnum#is(Enum)} compares {@link Enum} values.
     */
    @Test
    void shouldExecuteIs() {
        assertThat(AtomicEnum.of(TestAtomicEnum.OPTION1)
            .is(TestAtomicEnum.OPTION1))
            .isTrue();
    }

    /**
     * Ensure {@link AtomicEnum#get()} returns an {@link Enum} value.
     */
    @Test
    void shouldExecuteGet() {
        assertThat(AtomicEnum.of(TestAtomicEnum.OPTION1).get())
            .isEqualTo(TestAtomicEnum.OPTION1);
    }

    /**
     * Ensure {@link AtomicEnum#set(Enum)} sets an {@link Enum} value.
     */
    @Test
    void shouldExecuteSet() {
        final var atomicEnum = AtomicEnum.of(TestAtomicEnum.OPTION1);

        assertThat(atomicEnum.set(TestAtomicEnum.OPTION2))
            .isEqualTo(TestAtomicEnum.OPTION1);

        assertThat(atomicEnum.set(TestAtomicEnum.OPTION3))
            .isEqualTo(TestAtomicEnum.OPTION2);
    }

    /**
     * Ensure {@link AtomicEnum#compareAndSet(Enum, Enum)} mutates the {@link AtomicEnum} if compare is true.
     */
    @Test
    void shouldExecuteCompareAndSetTrueIfSame() {
        final var atomicEnum = AtomicEnum.of(TestAtomicEnum.OPTION1);

        assertThat(atomicEnum.compareAndSet(TestAtomicEnum.OPTION1, TestAtomicEnum.OPTION2))
            .isTrue();

        assertThat(atomicEnum.get())
            .isEqualTo(TestAtomicEnum.OPTION2);
    }

    /**
     * Ensure {@link AtomicEnum#compareAndSet(Enum, Enum)} fails to mutate if the expected {@link Enum} value is different.
     */
    @Test
    void shouldExecuteCompareAndSetFalseIfDifferent() {
        final var atomicEnum = AtomicEnum.of(TestAtomicEnum.OPTION1);

        assertThat(atomicEnum.compareAndSet(TestAtomicEnum.OPTION2, TestAtomicEnum.OPTION3))
            .isFalse();

        assertThat(atomicEnum.get())
            .isEqualTo(TestAtomicEnum.OPTION1);
    }

    /**
     * Ensure {@link AtomicEnum#compute(Function)} computes a value.
     */
    @Test
    void shouldExecuteCompute() {
        final var atomicEnum = AtomicEnum.of(TestAtomicEnum.OPTION1);
        final var result = new AtomicReference<TestAtomicEnum>();

        final var compute = atomicEnum.compute(a -> {
            result.set(a);
            return TestAtomicEnum.OPTION2;
        });

        assertThat(atomicEnum.get())
            .isEqualTo(TestAtomicEnum.OPTION2);

        assertThat(result.get())
            .isEqualTo(TestAtomicEnum.OPTION1);

        assertThat(compute)
            .isEqualTo(TestAtomicEnum.OPTION1);
    }

    /**
     * Ensure {@link AtomicEnum#compute(Function)} with {@code null} returns the {@link Enum} value.
     */
    @Test
    void shouldExecuteComputeWithNullReturnsSame() {
        final var atomicEnum = AtomicEnum.of(TestAtomicEnum.OPTION1);

        assertThat(atomicEnum.compute(null))
            .isEqualTo(TestAtomicEnum.OPTION1);

        assertThat(atomicEnum.get())
            .isEqualTo(TestAtomicEnum.OPTION1);
    }

    /**
     * Ensure {@link AtomicEnum#consume(Consumer)} consumes the {@link Enum} value.
     */
    @Test
    void shouldExecuteConsume() {
        final var atomicEnum = AtomicEnum.of(TestAtomicEnum.OPTION1);
        final var result = new AtomicReference<>();

        atomicEnum.consume(result::set);

        assertThat(result.get())
            .isEqualTo(TestAtomicEnum.OPTION1);
    }

    /**
     * Ensure {@link AtomicEnum#map(Function)} maps the {@link Enum} value.
     */
    @Test
    void shouldExecuteMap() {
        final var atomicEnum = AtomicEnum.of(TestAtomicEnum.OPTION1);

        final var reference = new AtomicReference<>();

        final var result = atomicEnum.map(v -> {
            reference.set(v);
            return "result";
        });

        assertThat(result)
            .isEqualTo("result");

        assertThat(reference.get())
            .isEqualTo(TestAtomicEnum.OPTION1);
    }
}

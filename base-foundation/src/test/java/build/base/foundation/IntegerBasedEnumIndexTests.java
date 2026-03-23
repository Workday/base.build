package build.base.foundation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IntegerBasedEnumIndex}es.
 *
 * @author brian.oliver
 * @since Feb-2025
 */
class IntegerBasedEnumIndexTests {

    /**
     * Ensure consecutive zero-based {@link Enum} values can be indexed and found.
     */
    @Test
    void shouldIndexAndAccessConsecutiveZeroBasedValues() {
        IntegerBasedEnumIndex.createIndex(ConsecutiveZeroBased.class, ConsecutiveZeroBased::index);

        assertThat(IntegerBasedEnumIndex
            .getValue(ConsecutiveZeroBased.class, 0))
            .isEqualTo(ConsecutiveZeroBased.RED);

        assertThat(IntegerBasedEnumIndex
            .getValue(ConsecutiveZeroBased.class, 1))
            .isEqualTo(ConsecutiveZeroBased.GREEN);

        assertThat(IntegerBasedEnumIndex
            .getValue(ConsecutiveZeroBased.class, 2))
            .isEqualTo(ConsecutiveZeroBased.BLUE);
    }

    /**
     * Ensure non-consecutive zero-based {@link Enum} values can be indexed and found.
     */
    @Test
    void shouldIndexAndAccessNonConsecutiveZeroBasedValues() {
        IntegerBasedEnumIndex.createIndex(NonConsecutiveZeroBased.class, NonConsecutiveZeroBased::index);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutiveZeroBased.class, 0))
            .isEqualTo(NonConsecutiveZeroBased.RED);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutiveZeroBased.class, 3))
            .isEqualTo(NonConsecutiveZeroBased.GREEN);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutiveZeroBased.class, 7))
            .isEqualTo(NonConsecutiveZeroBased.BLUE);
    }

    /**
     * Ensure consecutive negative {@link Enum} values can be indexed and found.
     */
    @Test
    void shouldIndexAndAccessConsecutiveNegativeBasedValues() {
        IntegerBasedEnumIndex.createIndex(ConsecutiveNegativeBased.class, ConsecutiveNegativeBased::index);

        assertThat(IntegerBasedEnumIndex
            .getValue(ConsecutiveNegativeBased.class, -5))
            .isEqualTo(ConsecutiveNegativeBased.RED);

        assertThat(IntegerBasedEnumIndex
            .getValue(ConsecutiveNegativeBased.class, -4))
            .isEqualTo(ConsecutiveNegativeBased.GREEN);

        assertThat(IntegerBasedEnumIndex
            .getValue(ConsecutiveNegativeBased.class, -3))
            .isEqualTo(ConsecutiveNegativeBased.BLUE);
    }

    /**
     * Ensure non-consecutive negative {@link Enum} values can be indexed and found.
     */
    @Test
    void shouldIndexAndAccessNonConsecutiveNegativeBasedValues() {
        IntegerBasedEnumIndex.createIndex(NonConsecutiveNegativeBased.class, NonConsecutiveNegativeBased::index);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutiveNegativeBased.class, -7))
            .isEqualTo(NonConsecutiveNegativeBased.RED);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutiveNegativeBased.class, -3))
            .isEqualTo(NonConsecutiveNegativeBased.GREEN);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutiveNegativeBased.class, -1))
            .isEqualTo(NonConsecutiveNegativeBased.BLUE);
    }

    /**
     * Ensure consecutive positive {@link Enum} values can be indexed and found.
     */
    @Test
    void shouldIndexAndAccessConsecutivePositiveBasedValues() {
        IntegerBasedEnumIndex.createIndex(ConsecutivePositiveBased.class, ConsecutivePositiveBased::index);

        assertThat(IntegerBasedEnumIndex
            .getValue(ConsecutivePositiveBased.class, 1))
            .isEqualTo(ConsecutivePositiveBased.RED);

        assertThat(IntegerBasedEnumIndex
            .getValue(ConsecutivePositiveBased.class, 2))
            .isEqualTo(ConsecutivePositiveBased.GREEN);

        assertThat(IntegerBasedEnumIndex
            .getValue(ConsecutivePositiveBased.class, 3))
            .isEqualTo(ConsecutivePositiveBased.BLUE);
    }

    /**
     * Ensure non-consecutive negative {@link Enum} values can be indexed and found.
     */
    @Test
    void shouldIndexAndAccessNonConsecutivePositiveBasedValues() {
        IntegerBasedEnumIndex.createIndex(NonConsecutivePositiveBased.class, NonConsecutivePositiveBased::index);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutivePositiveBased.class, 1))
            .isEqualTo(NonConsecutivePositiveBased.RED);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutivePositiveBased.class, 3))
            .isEqualTo(NonConsecutivePositiveBased.GREEN);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutivePositiveBased.class, 7))
            .isEqualTo(NonConsecutivePositiveBased.BLUE);
    }

    /**
     * Ensure non-consecutive negative and positive {@link Enum} values can be indexed and found.
     */
    @Test
    void shouldIndexAndAccessNonConsecutiveNegativeAndPositiveBasedValues() {
        IntegerBasedEnumIndex.createIndex(NonConsecutive.class, NonConsecutive::index);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutive.class, -10))
            .isEqualTo(NonConsecutive.RED);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutive.class, 0))
            .isEqualTo(NonConsecutive.GREEN);

        assertThat(IntegerBasedEnumIndex
            .getValue(NonConsecutive.class, 10))
            .isEqualTo(NonConsecutive.BLUE);
    }

    /**
     * An {@link Enum} with consecutive zero-based index values.
     */
    public enum ConsecutiveZeroBased {
        RED(0),
        GREEN(1),
        BLUE(2);

        ConsecutiveZeroBased(final int index) {
            this.index = index;
        }

        private final int index;

        int index() {
            return this.index;
        }
    }

    /**
     * An {@link Enum} with non-consecutive zero-based index values.
     */
    public enum NonConsecutiveZeroBased {
        RED(0),
        GREEN(3),
        BLUE(7);

        NonConsecutiveZeroBased(final int index) {
            this.index = index;
        }

        private final int index;

        int index() {
            return this.index;
        }
    }

    /**
     * An {@link Enum} with consecutive negative-based index values.
     */
    public enum ConsecutiveNegativeBased {
        RED(-5),
        GREEN(-4),
        BLUE(-3);

        ConsecutiveNegativeBased(final int index) {
            this.index = index;
        }

        private final int index;

        int index() {
            return this.index;
        }
    }

    /**
     * An {@link Enum} with non-consecutive negative-based index values.
     */
    public enum NonConsecutiveNegativeBased {
        RED(-7),
        GREEN(-3),
        BLUE(-1);

        NonConsecutiveNegativeBased(final int index) {
            this.index = index;
        }

        private final int index;

        int index() {
            return this.index;
        }
    }

    /**
     * An {@link Enum} with consecutive positive-based index values.
     */
    public enum ConsecutivePositiveBased {
        RED(1),
        GREEN(2),
        BLUE(3);

        ConsecutivePositiveBased(final int index) {
            this.index = index;
        }

        private final int index;

        int index() {
            return this.index;
        }
    }

    /**
     * An {@link Enum} with non-consecutive positive-based index values.
     */
    public enum NonConsecutivePositiveBased {
        RED(1),
        GREEN(3),
        BLUE(7);

        NonConsecutivePositiveBased(final int index) {
            this.index = index;
        }

        private final int index;

        int index() {
            return this.index;
        }
    }

    /**
     * An {@link Enum} with non-consecutive negative and positive-based index values.
     */
    public enum NonConsecutive {
        RED(-10),
        GREEN(0),
        BLUE(10);

        NonConsecutive(final int index) {
            this.index = index;
        }

        private final int index;

        int index() {
            return this.index;
        }
    }
}

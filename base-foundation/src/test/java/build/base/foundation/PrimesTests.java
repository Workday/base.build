package build.base.foundation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Primes}.
 *
 * @author brian.oliver
 */
class PrimesTests {

    /**
     * Ensure the {@link Primes} has a minimum.
     */
    @Test
    void shouldDefineMinimumPrime() {
        assertThat(Primes.minimum())
            .isEqualTo(2);
    }

    /**
     * Ensure the {@link Primes} has a maximum.
     */
    @Test
    void shouldDefineMaximumPrime() {
        assertThat(Primes.maximum())
            .isEqualTo(9973);
    }

    /**
     * Ensure an {@link java.util.stream.IntStream} of {@link Primes} can be acquired.
     */
    @Test
    void shouldDefinePrimeStream() {
        assertThat(Primes.stream())
            .hasSize(1229);
    }

    /**
     * Ensure an {@link Iterable} of {@link Primes} can be acquired.
     */
    @Test
    void shouldDefinePrimeIterable() {
        assertThat(Primes.get().iterator().hasNext())
            .isTrue();
    }

    /**
     * Ensure the {@link Iterable} of {@link Primes} eventually throws {@link NoSuchElementException}.
     */
    @Test
    void shouldThrowNoSuchElementExceptionUponIteratorExhaustion() {
        final Iterator<Integer> iter = Primes.get().iterator();
        while (iter.hasNext()) {
            iter.next();
        }
        assertThrows(NoSuchElementException.class, iter::next);
    }

    /**
     * Ensure numbers closest to primes are obtained.
     */
    @Test
    void shouldObtainClosestPrime() {
        assertThat(Primes.closestTo(-1))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.closestTo(0))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.closestTo(1))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.closestTo(Primes.minimum()))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.closestTo(Primes.maximum()))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.closestTo(Primes.maximum() - 1))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.closestTo(Primes.maximum() + 1))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.closestTo(Primes.maximum() + 2))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.closestTo(2))
            .isEqualTo(2);

        assertThat(Primes.closestTo(3))
            .isEqualTo(3);

        assertThat(Primes.closestTo(5))
            .isEqualTo(5);

        assertThat(Primes.closestTo(4))
            .isEqualTo(5);

        assertThat(Primes.closestTo(9))
            .isEqualTo(11);

        assertThat(Primes.closestTo(8))
            .isEqualTo(7);

        assertThat(Primes.closestTo(10))
            .isEqualTo(11);
    }

    /**
     * Ensure prime numbers before numbers are obtained.
     */
    @Test
    void shouldObtainPreviousPrime() {
        assertThat(Primes.before(-1))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.before(0))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.before(1))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.before(Primes.minimum()))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.before(Primes.maximum() + 1))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.before(Primes.maximum() + 2))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.before(2))
            .isEqualTo(2);

        assertThat(Primes.before(3))
            .isEqualTo(2);

        assertThat(Primes.before(5))
            .isEqualTo(3);

        assertThat(Primes.before(4))
            .isEqualTo(3);

        assertThat(Primes.before(9))
            .isEqualTo(7);

        assertThat(Primes.before(8))
            .isEqualTo(7);

        assertThat(Primes.before(10))
            .isEqualTo(7);
    }

    /**
     * Ensure prime numbers before numbers are obtained.
     */
    @Test
    void shouldObtainNextPrime() {
        assertThat(Primes.after(-1))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.after(0))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.after(1))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.after(Primes.maximum() - 1))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.after(Primes.maximum()))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.after(Primes.maximum() + 1))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.after(Primes.maximum() + 2))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.after(2))
            .isEqualTo(3);

        assertThat(Primes.after(3))
            .isEqualTo(5);

        assertThat(Primes.after(5))
            .isEqualTo(7);

        assertThat(Primes.after(4))
            .isEqualTo(5);

        assertThat(Primes.after(9))
            .isEqualTo(11);

        assertThat(Primes.after(8))
            .isEqualTo(11);

        assertThat(Primes.after(10))
            .isEqualTo(11);
    }

    /**
     * Ensure floor prime numbers are obtained.
     */
    @Test
    void shouldObtainFloorPrime() {
        assertThat(Primes.floor(-1))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.floor(0))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.floor(1))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.floor(Primes.minimum()))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.floor(Primes.maximum() - 1))
            .isEqualTo(Primes.before(Primes.maximum()));

        assertThat(Primes.floor(Primes.maximum()))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.floor(Primes.maximum() + 1))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.floor(Primes.maximum() + 2))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.floor(2))
            .isEqualTo(2);

        assertThat(Primes.floor(3))
            .isEqualTo(3);

        assertThat(Primes.floor(5))
            .isEqualTo(5);

        assertThat(Primes.floor(4))
            .isEqualTo(3);

        assertThat(Primes.floor(9))
            .isEqualTo(7);

        assertThat(Primes.floor(8))
            .isEqualTo(7);

        assertThat(Primes.floor(10))
            .isEqualTo(7);
    }

    /**
     * Ensure ceil prime numbers are obtained.
     */
    @Test
    void shouldObtainCeilPrime() {
        assertThat(Primes.ceil(-1))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.ceil(0))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.ceil(1))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.ceil(Primes.minimum()))
            .isEqualTo(Primes.minimum());

        assertThat(Primes.ceil(Primes.maximum() - 1))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.ceil(Primes.maximum()))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.ceil(Primes.maximum() + 1))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.ceil(Primes.maximum() + 2))
            .isEqualTo(Primes.maximum());

        assertThat(Primes.ceil(2))
            .isEqualTo(2);

        assertThat(Primes.ceil(3))
            .isEqualTo(3);

        assertThat(Primes.ceil(5))
            .isEqualTo(5);

        assertThat(Primes.ceil(4))
            .isEqualTo(5);

        assertThat(Primes.ceil(9))
            .isEqualTo(11);

        assertThat(Primes.ceil(8))
            .isEqualTo(11);

        assertThat(Primes.ceil(10))
            .isEqualTo(11);
    }

    /**
     * Ensure the proper index is returned.
     */
    @Test
    void shouldObtainProperIndexForKnownPrime() {
        assertThat(Primes.index(Primes.minimum()))
            .isEqualTo(0);
    }

    /**
     * Ensure an {@link IllegalArgumentException} is thrown for bogus index request.
     */
    @Test
    void shouldThrowIAEForUnknownValue() {
        assertThrows(IllegalArgumentException.class, () -> Primes.index(4));

    }

    /**
     * Ensure that the random prime is larger than the specified value.
     */
    @Test
    void shouldReturnRandomLargePrime() {
        assertThat(Primes.randomAfter(2000))
            .isGreaterThan(2000);
    }

    /**
     * Ensure that Primes.maximum() is returned when asking for an out-of-range random prime.
     */
    @Test
    void shouldReturnMaxOnRandomLargerThenMax() {
        assertThat(Primes.randomAfter(Primes.maximum()))
            .isEqualTo(Primes.maximum());
    }
}

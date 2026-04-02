package build.base.foundation;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class for tests of {@link AbstractWeakHasherMap} based maps.
 *
 * @author mark.falco
 * @author brian.oliver
 * @since March-2022
 */
public abstract class AbstractHasherMapTests {

    /**
     * Construct a new map for testing.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return the map
     */
    abstract protected <K, V> Map<K, V> makeMap();

    /**
     * Verify that we can put.
     */
    @Test
    void shouldPut() {
        final Map<Integer, Integer> map = makeMap();
        final var n = 1;

        map.put(n, n + 1);
        assertTrue(map.containsKey(n));
    }

    @Test
    void shouldGet() {
        final Map<Integer, Integer> map = makeMap();
        final var n = 1;

        map.put(n, n + 1);
        assertThat(map.get(n))
            .isEqualTo(n + 1);
    }

    public volatile int blackhole;

    /**
     * Verify we can computeIfAbsent.
     */
    @Test
    void shouldComputeIfAbsent() {
        final Map<Integer, Integer> map = makeMap();
        final var n = 1;

        assertThat(map.computeIfAbsent(n, k -> k))
            .isEqualTo(n);

        assertThat(map.computeIfAbsent(n, k -> k + 1))
            .isEqualTo(n); // value should not change
    }

    /**
     * Verify iteration conversion.
     */
    @Test
    void shouldConvertUpUponIteration() {
        final Map<Integer, Integer> map = makeMap();

        for (int i = 0; i < 1000; ++i) {
            final Integer n = i;
            map.put(n, n);
        }

        final HashSet<Integer> setKeys = new HashSet<>();
        for (Integer n : map.keySet()) {
            assertTrue(setKeys.add(n));
        }

        // verify that we saw all the keys
        assertThat(setKeys.size())
            .isEqualTo(1000);

        // verify the same with entrySet iteration
        setKeys.clear();
        final HashSet<Integer> setValues = new HashSet<>();
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            assertTrue(setKeys.add(e.getKey()));
            assertTrue(setValues.add(e.getValue()));
            assertThat(e.getKey())
                .isEqualTo(e.getValue());
        }

        // verify that we saw all the keys and values
        assertThat(setKeys.size())
            .isEqualTo(1000);

        assertThat(setValues.size())
            .isEqualTo(1000);
    }

    /**
     * Should remove via key iteration.
     */
    @Test
    void shouldRemoveViaKeyIteration() {
        final Map<Integer, Integer> map = makeMap();

        for (int i = 0; i < 10; ++i) {
            final Integer n = i;
            map.put(n, n);
        }

        for (final Iterator<Integer> iter = map.keySet().iterator(); iter.hasNext(); ) {
            if ((iter.next() & 1) == 1) {
                iter.remove();
            }
        }

        assertThat(map.size()).isEqualTo(5);
        assertFalse(map.containsKey(1));
        assertFalse(map.containsKey(3));
        assertFalse(map.containsKey(5));
        assertFalse(map.containsKey(7));
        assertFalse(map.containsKey(9));
    }

    /**
     * Verify removeAll on keys.
     */
    @Test
    void shouldRemoveAllOnKeys() {
        final Map<Integer, Integer> map = makeMap();

        final Set<Integer> oddKeys = new HashSet<>();
        for (int i = 0; i < 10; ++i) {
            final Integer n = i;
            map.put(n, n);

            if ((i & 1) == 1) {
                oddKeys.add(n);
            }
        }

        map.keySet().removeAll(oddKeys);

        assertThat(map.size()).isEqualTo(5);
        assertFalse(map.containsKey(1));
        assertFalse(map.containsKey(3));
        assertFalse(map.containsKey(5));
        assertFalse(map.containsKey(7));
        assertFalse(map.containsKey(9));
    }

    /**
     * Verify removeAll on entries.
     */
    @Test
    void shouldRemoveAllOnKEntries() {
        final Map<Integer, Integer> map = makeMap();

        final Map<Integer, Integer> odds = new HashMap<>();
        for (int i = 0; i < 10; ++i) {
            final Integer n = i;
            map.put(n, n);

            if ((i & 1) == 1) {
                odds.put(n, n);
            }
        }

        map.entrySet().removeAll(odds.entrySet());

        assertThat(map.size()).isEqualTo(5);
        assertFalse(map.containsKey(1));
        assertFalse(map.containsKey(3));
        assertFalse(map.containsKey(5));
        assertFalse(map.containsKey(7));
        assertFalse(map.containsKey(9));
    }

    /**
     * Verify retainAll on keys.
     */
    @Test
    void shouldRetainAllOnKeys() {
        final Map<Integer, Integer> map = makeMap();

        final Set<Integer> evenKeys = new HashSet<>();
        for (int i = 0; i < 10; ++i) {
            final Integer n = i;
            map.put(n, n);

            if ((i & 1) == 0) {
                evenKeys.add(n);
            }
        }

        map.keySet().retainAll(evenKeys);

        assertThat(map.size()).isEqualTo(5);
        assertFalse(map.containsKey(1));
        assertFalse(map.containsKey(3));
        assertFalse(map.containsKey(5));
        assertFalse(map.containsKey(7));
        assertFalse(map.containsKey(9));
    }

    /**
     * Verify removeAll on entries.
     */
    @Test
    void shouldRetainAllOnKEntries() {
        final Map<Integer, Integer> map = makeMap();

        final Map<Integer, Integer> evens = new HashMap<>();
        for (int i = 0; i < 10; ++i) {
            final Integer n = i;
            map.put(n, n);

            if ((i & 1) == 0) {
                evens.put(n, n);
            }
        }

        map.entrySet().retainAll(evens.entrySet());

        assertThat(map.size()).isEqualTo(5);
        assertFalse(map.containsKey(1));
        assertFalse(map.containsKey(3));
        assertFalse(map.containsKey(5));
        assertFalse(map.containsKey(7));
        assertFalse(map.containsKey(9));
    }

    /**
     * Should remove via entry iteration.
     */
    @Test
    void shouldRemoveViaEntryIteration() {
        final Map<Integer, Integer> map = makeMap();

        for (int i = 0; i < 10; ++i) {
            final Integer n = i;
            map.put(n, n);
        }

        for (final Iterator<Map.Entry<Integer, Integer>> iter = map.entrySet().iterator(); iter.hasNext(); ) {
            if ((iter.next().getKey() & 1) == 1) {
                iter.remove();
            }
        }

        assertThat(map.size()).isEqualTo(5);
        assertFalse(map.containsKey(1));
        assertFalse(map.containsKey(3));
        assertFalse(map.containsKey(5));
        assertFalse(map.containsKey(7));
        assertFalse(map.containsKey(9));
    }

    /**
     * Verify should remove from values
     */
    @Test
    void shouldRemoveFromValues() {
        final Map<Integer, Integer> map = makeMap();

        for (int i = 0; i < 10; ++i) {
            final Integer n = i;
            map.put(n, n);
        }

        map.values().remove(5);
        assertThat(map.size()).isEqualTo(9);
        assertThat(map.containsKey(5)).isFalse();
    }

    /**
     * Should remove from values().
     */
    @Test
    void shouldRemoveViaValues() {
        final Map<Integer, Integer> map = makeMap();

        for (int i = 0; i < 10; ++i) {
            final Integer n = i;
            map.put(n, n);
        }

        for (final Iterator<Integer> iter = map.values().iterator(); iter.hasNext(); ) {
            if ((iter.next() & 1) == 1) {
                iter.remove();
            }
        }

        assertThat(map.size()).isEqualTo(5);
        assertFalse(map.containsKey(1));
        assertFalse(map.containsKey(3));
        assertFalse(map.containsKey(5));
        assertFalse(map.containsKey(7));
        assertFalse(map.containsKey(9));
    }

    /**
     * Verify streaming.
     */
    @Test
    void shouldStream() {
        final Map<Integer, Integer> map = makeMap();

        int sum = 0;
        for (int i = 0; i < 10; ++i) {
            final Integer n = i;
            map.put(n, n);
            sum += i;
        }

        assertThat(map.keySet().stream().mapToInt(Integer::intValue).sum())
            .isEqualTo(sum);
    }
}

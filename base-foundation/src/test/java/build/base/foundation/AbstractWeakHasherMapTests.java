package build.base.foundation;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AbstractWeakHasherMap}s.
 *
 * @author mark.falco
 * @since March-2022
 */
public abstract class AbstractWeakHasherMapTests
    extends AbstractHasherMapTests {

    /**
     * Verify that the map cleans up garbage.
     */
    @Test
    void shouldRemoveDeadRefs() {
        final Map<Integer, Integer> map = makeMap();

        // keep adding entries until we see the size shrink
        for (int i = 0; map.size() == i; ++i) {
            map.put(i, ThreadLocalRandom.current().nextInt());
        }
    }

    /**
     * Verify that iterators remain stable after cleanup.
     */
    @Test
    void shouldIterateOverDeadEntries() {
        final Map<Integer, Integer> map = makeMap();

        // keep adding entries until we see the size shrink
        Iterator<Map.Entry<Integer, Integer>> iter = null;
        for (int i = 0; map.size() == i; ++i) {
            map.put(i, ThreadLocalRandom.current().nextInt());
            if (iter == null) {
                iter = map.entrySet().iterator();
            }
        }

        while (iter.hasNext()) {
            final Map.Entry<Integer, Integer> e = iter.next();
            assertThat(e.getKey()).isNotNull();
            assertThat(e.getValue()).isNotNull();
        }
    }

    /**
     * Verify that iterators remain stable after cleanup.
     */
    @Test
    void shouldIterateOverDeadKeys() {
        final Map<Integer, Integer> map = makeMap();

        // keep adding entries until we see the size shrink
        Iterator<Integer> iter = null;
        for (int i = 0; map.size() == i; ++i) {
            map.put(i, ThreadLocalRandom.current().nextInt());
            if (iter == null) {
                iter = map.keySet().iterator();
            }
        }

        while (iter.hasNext()) {
            assertThat(iter.next()).isNotNull();
        }
    }

    /**
     * Verify that iterators remain stable after cleanup.
     */
    @Test
    void shouldIterateOverDeadKeysValues() {
        final Map<Integer, Integer> map = makeMap();

        // keep adding entries until we see the size shrink
        Iterator<Integer> iter = null;
        for (int i = 0; map.size() == i; ++i) {
            map.put(i, ThreadLocalRandom.current().nextInt());
            if (iter == null) {
                iter = map.values().iterator();
            }
        }

        while (iter.hasNext()) {
            assertThat(iter.next()).isNotNull();
        }
    }
}

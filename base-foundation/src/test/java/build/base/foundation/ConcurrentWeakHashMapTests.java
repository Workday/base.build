package build.base.foundation;

import java.util.Map;

/**
 * Tests for {@link ConcurrentWeakHashMap}.
 *
 * @author mark.falco
 * @since March-2022
 */
public class ConcurrentWeakHashMapTests
    extends AbstractWeakHasherMapTests {

    @Override
    protected <K, V> Map<K, V> makeMap() {
        return new ConcurrentWeakHashMap<>();
    }
}
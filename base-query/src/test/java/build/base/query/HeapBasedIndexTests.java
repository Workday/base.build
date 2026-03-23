package build.base.query;

/**
 * Tests for {@link HeapBasedIndex}es.
 *
 * @author brian.oliver
 * @since Sep-2025
 */
public class HeapBasedIndexTests implements IndexCompatibilityTests {

    @Override
    public Index createIndex() {
        return new HeapBasedIndex();
    }
}

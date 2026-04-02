package build.base.foundation;

import build.base.foundation.stream.Streams;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to sort by {@link Requires}.
 *
 * @author brian.oliver
 * @since Apr-2023
 */
class RequiresSortTests {

    /**
     * Ensure sorting an empty {@link Stream} by {@link Requires} returns {@link Stream#empty}.
     */
    @Test
    void shouldSortEmptyStream() {
        final var sorted = Streams.sortByRequires(Stream.empty(), Streams.SortOrder.FIRST);
        assertThat(sorted).isEmpty();
    }

    /**
     * Ensure sorting test {@link Class} instances by {@link Requires}.
     */
    @Test
    @SuppressWarnings("rawtypes")
    void shouldSortByRequires() {
        final var unordered = new LinkedHashSet<>();
        unordered.add(new A());
        unordered.add(new D());
        unordered.add(new C());
        unordered.add(new B());

        final var sorted = Streams.sortByRequires(unordered.stream(), Streams.SortOrder.FIRST);

        assertThat(sorted.map(Object::getClass).map(c -> (Class) c))
            .containsExactly(D.class, C.class, B.class, A.class);
    }

    @Requires(B.class)
    static class A {

    }

    @Requires(C.class)
    static class B {

    }

    static class C {

    }

    static class D {

    }
}
